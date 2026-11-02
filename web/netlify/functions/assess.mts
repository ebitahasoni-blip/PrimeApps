import { GoogleGenAI, Type } from "@google/genai";
import type { Config } from "@netlify/functions";
import { db } from "../../db/index.js";
import { essays } from "../../db/schema.js";
import { evaluateEssayWithRules, findRuleCorrections } from "../../shared/engine.js";
import {
  calculateIeltsOverallBand,
  countWords,
  roundToHalfBand,
  splitParagraphs,
  type AssessmentReport,
  type CorrectionCategory,
  type CorrectionItem,
  type CriterionScoreDetail,
  type RubricHighlightLink,
} from "../../shared/models.js";
import { CRITERIA, RUBRIC, getBandDescriptor, type CriterionCode } from "../../shared/rubric.js";

const MODEL = "gemini-flash-latest";
const CATEGORIES: CorrectionCategory[] = ["GRAMMAR", "LEXICAL", "COHERENCE", "TASK_RESPONSE", "PUNCTUATION"];
const CODES: CriterionCode[] = ["TR", "CC", "LR", "GRA"];

const criterionSchema = {
  type: Type.OBJECT,
  properties: {
    band: { type: Type.NUMBER, description: "Band 0-9 in 0.5 steps" },
    justification: { type: Type.STRING },
    strengths: { type: Type.ARRAY, items: { type: Type.STRING } },
    improvements: { type: Type.ARRAY, items: { type: Type.STRING } },
    nextBandActionPlan: { type: Type.STRING },
  },
  required: ["band", "justification", "strengths", "improvements", "nextBandActionPlan"],
};

const responseSchema = {
  type: Type.OBJECT,
  properties: {
    taskResponse: criterionSchema,
    coherenceCohesion: criterionSchema,
    lexicalResource: criterionSchema,
    grammaticalRangeAccuracy: criterionSchema,
    corrections: {
      type: Type.ARRAY,
      items: {
        type: Type.OBJECT,
        properties: {
          originalText: { type: Type.STRING, description: "Exact substring copied verbatim from the essay" },
          suggestedText: { type: Type.STRING },
          explanation: { type: Type.STRING },
          category: { type: Type.STRING, enum: CATEGORIES },
        },
        required: ["originalText", "suggestedText", "explanation", "category"],
      },
    },
    rubricLinks: {
      type: Type.ARRAY,
      items: {
        type: Type.OBJECT,
        properties: {
          criterion: { type: Type.STRING, enum: CODES },
          bandLevel: { type: Type.INTEGER },
          descriptorText: { type: Type.STRING, description: "Descriptor quoted from the official rubric" },
          essaySnippet: { type: Type.STRING, description: "Exact sentence or phrase copied verbatim from the essay" },
          isPositive: { type: Type.BOOLEAN },
          examinerCommentary: { type: Type.STRING },
        },
        required: ["criterion", "bandLevel", "descriptorText", "essaySnippet", "isPositive", "examinerCommentary"],
      },
    },
    generalFeedback: { type: Type.STRING },
  },
  required: ["taskResponse", "coherenceCohesion", "lexicalResource", "grammaticalRangeAccuracy", "corrections", "rubricLinks", "generalFeedback"],
};

const rubricText = CRITERIA.map(
  (c) =>
    `## ${c.title} (${c.code})\n` +
    RUBRIC.filter((d) => d.criterion === c.code)
      .sort((a, b) => b.band - a.band)
      .map((d) => `Band ${d.band}: ${d.descriptors.join("; ")}`)
      .join("\n"),
).join("\n\n");

const SYSTEM_PROMPT = `You are a certified, experienced IELTS Writing examiner. Assess IELTS Academic Writing Task 2 responses strictly against the official 2023 public band descriptors below. Be calibrated and honest: do not inflate scores. Responses under 250 words must be penalised in Task Response; off-topic or memorised content must be penalised heavily.

${rubricText}

Rules:
- Award each criterion a band from 0 to 9 in 0.5 increments.
- Corrections: list up to 15 of the most important errors or weak word choices. "originalText" MUST be copied exactly, character for character, from the essay so it can be located.
- Rubric links: give 4-8 links tying specific essay sentences to specific descriptors, mixing strengths (isPositive true) and weaknesses. "essaySnippet" MUST be copied verbatim from the essay.
- Write feedback in clear, encouraging British English addressed to the student ("you").
- "nextBandActionPlan" should be one or two concrete steps to reach the next half band.`;

type AiCriterion = { band: number; justification: string; strengths: string[]; improvements: string[]; nextBandActionPlan: string };
type AiResult = {
  taskResponse: AiCriterion;
  coherenceCohesion: AiCriterion;
  lexicalResource: AiCriterion;
  grammaticalRangeAccuracy: AiCriterion;
  corrections: { originalText: string; suggestedText: string; explanation: string; category: string }[];
  rubricLinks: { criterion: string; bandLevel: number; descriptorText: string; essaySnippet: string; isPositive: boolean; examinerCommentary: string }[];
  generalFeedback: string;
};

async function evaluateWithAi(topic: string, essay: string): Promise<AssessmentReport> {
  const ai = new GoogleGenAI({});
  const response = await ai.models.generateContent({
    model: MODEL,
    contents: `TASK 2 PROMPT:\n${topic || "(no prompt supplied — judge relevance from the essay itself)"}\n\nCANDIDATE ESSAY (${countWords(essay)} words):\n${essay}`,
    config: {
      systemInstruction: SYSTEM_PROMPT,
      responseMimeType: "application/json",
      responseSchema,
      temperature: 0.2,
    },
  });
  const result = JSON.parse(response.text ?? "") as AiResult;

  const toDetail = (code: CriterionCode, c: AiCriterion): CriterionScoreDetail => {
    const band = roundToHalfBand(Number(c.band) || 0, 0, 9);
    return {
      criterion: code,
      bandScore: band,
      justification: c.justification,
      officialDescriptorSummary: getBandDescriptor(code, band)?.keySummary ?? "",
      keyStrengths: c.strengths ?? [],
      areasToImprove: c.improvements ?? [],
      nextBandActionPlan: c.nextBandActionPlan,
    };
  };

  const tr = toDetail("TR", result.taskResponse);
  const cc = toDetail("CC", result.coherenceCohesion);
  const lr = toDetail("LR", result.lexicalResource);
  const gra = toDetail("GRA", result.grammaticalRangeAccuracy);

  // Locate AI-quoted snippets in the essay so they can be highlighted; drop any that can't be found.
  const used = new Set<number>();
  const locate = (snippet: string): number => {
    let from = 0;
    while (snippet) {
      const i = essay.indexOf(snippet, from);
      if (i < 0) return -1;
      if (!used.has(i)) return i;
      from = i + 1;
    }
    return -1;
  };

  const corrections: CorrectionItem[] = [];
  for (const c of result.corrections ?? []) {
    const start = locate(c.originalText);
    if (start < 0 || c.originalText === c.suggestedText) continue;
    used.add(start);
    corrections.push({
      id: crypto.randomUUID(),
      originalText: c.originalText,
      suggestedText: c.suggestedText,
      explanation: c.explanation,
      startIndex: start,
      endIndex: start + c.originalText.length,
      category: (CATEGORIES as string[]).includes(c.category) ? (c.category as CorrectionCategory) : "GRAMMAR",
    });
  }
  // Keep deterministic rule-based catches the model may have missed
  for (const rc of findRuleCorrections(essay)) {
    if (!corrections.some((c) => rc.startIndex < c.endIndex && c.startIndex < rc.endIndex)) corrections.push(rc);
  }
  corrections.sort((a, b) => a.startIndex - b.startIndex);

  const rubricLinks: RubricHighlightLink[] = (result.rubricLinks ?? [])
    .filter((l) => (CODES as string[]).includes(l.criterion))
    .map((l) => {
      const start = essay.indexOf(l.essaySnippet);
      return {
        id: crypto.randomUUID(),
        criterion: l.criterion as CriterionCode,
        bandLevel: Math.min(9, Math.max(0, Math.round(l.bandLevel))),
        descriptorText: l.descriptorText,
        essaySnippet: l.essaySnippet,
        startIndex: start,
        endIndex: start < 0 ? -1 : start + l.essaySnippet.length,
        isPositive: !!l.isPositive,
        examinerCommentary: l.examinerCommentary,
      };
    });

  return {
    id: crypto.randomUUID(),
    topic,
    essayText: essay,
    wordCount: countWords(essay),
    paragraphCount: splitParagraphs(essay).length,
    createdAt: new Date().toISOString(),
    overallBandScore: calculateIeltsOverallBand(tr.bandScore, cc.bandScore, lr.bandScore, gra.bandScore),
    taskResponse: tr,
    coherenceCohesion: cc,
    lexicalResource: lr,
    grammaticalRangeAccuracy: gra,
    corrections,
    rubricLinks,
    generalFeedback: result.generalFeedback,
    source: "ai",
  };
}

export default async (req: Request) => {
  if (req.method !== "POST") return new Response("Method not allowed", { status: 405 });

  const deviceId = req.headers.get("x-device-id")?.trim();
  if (!deviceId || deviceId.length > 100) return Response.json({ error: "Missing device id" }, { status: 400 });

  let body: { topic?: unknown; essay?: unknown };
  try {
    body = await req.json();
  } catch {
    return Response.json({ error: "Invalid JSON body" }, { status: 400 });
  }
  const topic = typeof body.topic === "string" ? body.topic.trim().slice(0, 2000) : "";
  const essay = typeof body.essay === "string" ? body.essay.trim() : "";
  const words = countWords(essay);
  if (words < 50) return Response.json({ error: "Please write at least 50 words before requesting an assessment." }, { status: 400 });
  if (words > 1500 || essay.length > 12000) return Response.json({ error: "Essays are limited to 1,500 words." }, { status: 400 });

  let report: AssessmentReport;
  try {
    report = await evaluateWithAi(topic, essay);
  } catch (error) {
    console.error("AI assessment failed, using rule engine", error);
    report = evaluateEssayWithRules(topic, essay);
  }

  await db.insert(essays).values({
    id: report.id,
    deviceId,
    topic: report.topic,
    wordCount: report.wordCount,
    overallBand: report.overallBandScore,
    taskResponseBand: report.taskResponse.bandScore,
    coherenceBand: report.coherenceCohesion.bandScore,
    lexicalBand: report.lexicalResource.bandScore,
    grammarBand: report.grammaticalRangeAccuracy.bandScore,
    report,
    createdAt: new Date(report.createdAt),
  });

  return Response.json(report, { status: 201 });
};

export const config: Config = {
  path: "/api/assess",
};
