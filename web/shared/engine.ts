import {
  calculateIeltsOverallBand,
  countWords,
  roundToHalfBand,
  splitParagraphs,
  type AssessmentReport,
  type CorrectionItem,
  type CriterionScoreDetail,
  type RubricHighlightLink,
} from "./models.js";
import { criterionTitle, getBandDescriptor, type CriterionCode } from "./rubric.js";

const uid = () => globalThis.crypto.randomUUID();

const escapeRe = (s: string) => s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
const wordRe = (w: string, flags = "i") => new RegExp(`\\b${escapeRe(w)}\\b`, flags);

// Academic vocabulary and collocations indicative of Bands 7–9
const ADVANCED_LEXICON = [
  "substantial", "phenomenon", "detrimental", "inevitable", "consequently",
  "furthermore", "exacerbate", "ameliorate", "fundamental", "unprecedented",
  "predominant", "compelling", "imperative", "sustainable", "correlation",
  "implement", "perspective", "disproportionate", "allocation", "ubiquitous",
  "controversial", "repercussions", "advocate", "paramount", "diminish",
  "crucial", "pragmatic", "incentive", "infrastructure", "proponents",
];

const INFORMAL_WORDS: Record<string, [string, string]> = {
  kids: ["children", "Too informal for academic writing; use 'children' or 'youngsters'."],
  "a lot of": ["numerous", "'A lot of' is informal. Prefer 'a substantial number of' or 'numerous'."],
  gonna: ["going to", "Slang contraction. Always write 'going to' in formal essays."],
  stuff: ["factors", "Vague and colloquial. Specify the actual elements, items, or factors."],
  things: ["aspects", "Vague noun. Use precise terms like 'phenomena', 'factors', or 'aspects'."],
  bad: ["detrimental", "Elementary adjective. Consider 'adverse', 'deleterious', or 'detrimental'."],
  good: ["beneficial", "Basic vocabulary. Upgrade to 'advantageous', 'favourable', or 'beneficial'."],
  huge: ["immense", "Colloquial hyperbole. Prefer 'substantial', 'considerable', or 'immense'."],
};

const THIRD_PERSON: Record<string, string> = { have: "has", do: "does", say: "says", go: "goes", make: "makes" };

const GRAMMAR_CHECKS: [RegExp, (m: RegExpMatchArray) => [string, string]][] = [
  [
    /\b(he|she|it|everybody|everyone)\s+(have|do|say|go|make)\b/gi,
    (m) => [`${m[1]} ${THIRD_PERSON[m[2].toLowerCase()] ?? `${m[2]}s`}`, `Subject-verb agreement error with third-person singular '${m[1]}'.`],
  ],
  [
    /\bmore\s+(easier|better|faster|harder|clearer)\b/gi,
    (m) => [m[1], `Double comparative error. Use '${m[1]}' alone, not 'more ${m[1]}'.`],
  ],
  [
    /\baltought\b|\balthough\s+that\b/gi,
    () => ["Although", "Conjunction error. 'Although' should not be followed by 'that'."],
  ],
  [
    /\bin\s+my\s+point\s+of\s+view\b/gi,
    () => ["From my point of view", "Preposition error in fixed idiom: write 'From my point of view' or 'In my view'."],
  ],
  [
    /\bvery\s+(crucial|vital|essential|paramount)\b/gi,
    (m) => [m[1], `Redundant intensifier. '${m[1]}' is an ungradable adjective and cannot take 'very'.`],
  ],
];

const COHESIVE_MARKERS = ["However", "Furthermore", "In addition", "Consequently", "On the other hand", "Therefore", "Moreover", "Similarly", "Conversely"];
const SUBORDINATORS = ["although", "whereas", "while", "even though", "since", "because", "provided that", "unless", "in order that"];
const POSITION_RE = /(I firmly believe|In my opinion|this essay argues|it is argued that|I agree|I disagree|this essay will discuss)/i;

/** Rule-based corrections for informal vocabulary and common grammar slips. */
export function findRuleCorrections(essay: string): CorrectionItem[] {
  const corrections: CorrectionItem[] = [];
  for (const [informal, [suggested, explanation]] of Object.entries(INFORMAL_WORDS)) {
    for (const m of essay.matchAll(wordRe(informal, "gi"))) {
      corrections.push({
        id: uid(),
        originalText: m[0],
        suggestedText: suggested,
        explanation,
        startIndex: m.index!,
        endIndex: m.index! + m[0].length,
        category: "LEXICAL",
      });
    }
  }
  for (const [pattern, fixer] of GRAMMAR_CHECKS) {
    for (const m of essay.matchAll(pattern)) {
      const [suggested, explanation] = fixer(m);
      corrections.push({
        id: uid(),
        originalText: m[0],
        suggestedText: suggested,
        explanation,
        startIndex: m.index!,
        endIndex: m.index! + m[0].length,
        category: "GRAMMAR",
      });
    }
  }
  return corrections.sort((a, b) => a.startIndex - b.startIndex);
}

/** Quick structural checks shown live while the student types. */
export function quickChecks(essay: string) {
  const text = essay.trim();
  return {
    wordCount: countWords(text),
    paragraphCount: splitParagraphs(text).length,
    hasTransitions: COHESIVE_MARKERS.some((m) => wordRe(m).test(text)),
    hasAcademicVocab: ADVANCED_LEXICON.some((w) => wordRe(w).test(text)),
    hasComplexSentences: SUBORDINATORS.some((w) => wordRe(w).test(text)),
    hasClearPosition: POSITION_RE.test(splitParagraphs(text)[0] ?? ""),
  };
}

function justification(code: CriterionCode, band: number, strengths: string[], improvements: string[]): string {
  let s = `Band ${band.toFixed(1)} awarded according to the 2023 IELTS ${criterionTitle(code)} descriptors. `;
  if (strengths.length) s += `Key observed strength: ${strengths[0]} `;
  s += improvements.length ? `Limiting factor: ${improvements[0]}` : "No critical limiting factors observed.";
  return s;
}

/** Offline rule-based evaluator, ported from the Android app's assessment engine. */
export function evaluateEssayWithRules(topic: string, essayText: string): AssessmentReport {
  const essay = essayText.trim();
  const wordCount = countWords(essay);
  const paragraphs = splitParagraphs(essay);
  const paragraphCount = paragraphs.length;
  const rubricLinks: RubricHighlightLink[] = [];
  const corrections = findRuleCorrections(essay);

  // 1. Task Response
  let tr = 6.5;
  const trS: string[] = [];
  const trI: string[] = [];
  if (wordCount < 250) {
    tr -= Math.min(1.5, (250 - wordCount) / 40);
    trI.push(`Word count is under the mandatory 250 words minimum (${wordCount} words). Official IELTS examiners apply an automatic penalty.`);
  } else {
    trS.push(`Exceeds the 250-word minimum comfortably (${wordCount} words), satisfying length requirements.`);
  }
  if (paragraphCount >= 4 && paragraphCount <= 5) {
    trS.push(`Effective essay structure with ${paragraphCount} well-separated paragraphs (Introduction, Body, Conclusion).`);
    tr += 0.5;
  } else if (paragraphCount < 3) {
    trI.push("Insufficient paragraphing. Academic essays require a 4 or 5-paragraph structure.");
    tr -= 0.5;
  }
  const firstPara = paragraphs[0] ?? "";
  if (POSITION_RE.test(firstPara)) {
    trS.push("Clear position presented directly in the introductory paragraph.");
    tr += 0.5;
    rubricLinks.push({
      id: uid(),
      criterion: "TR",
      bandLevel: 7,
      descriptorText: "Presents a clear position throughout the response",
      essaySnippet: firstPara.slice(0, 180) + (firstPara.length > 180 ? "..." : ""),
      startIndex: 0,
      endIndex: Math.min(firstPara.length, 180),
      isPositive: true,
      examinerCommentary: "Your introduction effectively sets an explicit stance, satisfying Band 7+ Task Response criteria.",
    });
  } else {
    trI.push("Position in the introduction is either absent or implicit. IELTS examiners expect a clear thesis statement.");
  }
  tr = Math.min(8.5, Math.max(5, tr));

  // 2. Coherence & Cohesion
  let cc = 6.5;
  const ccS: string[] = [];
  const ccI: string[] = [];
  const foundMarkers = COHESIVE_MARKERS.filter((m) => wordRe(m).test(essay));
  if (foundMarkers.length >= 4) {
    ccS.push(`Uses a varied range of cohesive devices (${foundMarkers.slice(0, 4).join(", ")}).`);
    cc += 0.5;
  } else {
    ccI.push("Limited range of cohesive devices detected. Integrate contrasting and consequential linking.");
  }
  const secondPara = paragraphs[1] ?? "";
  if (secondPara) {
    const start = essay.indexOf(secondPara);
    rubricLinks.push({
      id: uid(),
      criterion: "CC",
      bandLevel: 7,
      descriptorText: "Logically organises information and ideas; there is clear progression throughout",
      essaySnippet: secondPara.slice(0, 160) + (secondPara.length > 160 ? "..." : ""),
      startIndex: start,
      endIndex: start + Math.min(secondPara.length, 160),
      isPositive: true,
      examinerCommentary: "This body paragraph begins with a clear topic sentence, establishing paragraph-level unity.",
    });
  }
  const moreover = [...essay.matchAll(/\bmoreover\b/gi)].length;
  const furthermore = [...essay.matchAll(/\bfurthermore\b/gi)].length;
  if (moreover + furthermore > 3) {
    ccI.push("Overuse of mechanical transitions ('Moreover' / 'Furthermore'). Rely more on thematic referencing.");
    cc -= 0.5;
    rubricLinks.push({
      id: uid(),
      criterion: "CC",
      bandLevel: 6,
      descriptorText: "Cohesion within and/or between sentences may be faulty or mechanical",
      essaySnippet: `Frequent transitions: Moreover (${moreover}), Furthermore (${furthermore})`,
      startIndex: -1,
      endIndex: -1,
      isPositive: false,
      examinerCommentary: "Transition words are repeated mechanically at the start of multiple sentences.",
    });
  }
  cc = Math.min(8.5, Math.max(5, cc));

  // 3. Lexical Resource
  let lr = 6;
  const lrS: string[] = [];
  const lrI: string[] = [];
  const advanced = ADVANCED_LEXICON.filter((w) => wordRe(w).test(essay));
  if (advanced.length >= 5) {
    lrS.push(`Strong academic vocabulary showcasing lexical sophistication (${advanced.slice(0, 5).join(", ")}).`);
    lr += 1;
    rubricLinks.push({
      id: uid(),
      criterion: "LR",
      bandLevel: 7,
      descriptorText: "Uses less common lexical items with some awareness of style and collocation",
      essaySnippet: `Identified lexical items: ${advanced.join(", ")}`,
      startIndex: -1,
      endIndex: -1,
      isPositive: true,
      examinerCommentary: "Candidate demonstrates commendable control of higher-tier Academic Word List items.",
    });
  } else if (advanced.length) {
    lrS.push(`Some academic vocabulary present (${advanced.join(", ")}).`);
    lr += 0.5;
  } else {
    lrI.push("Vocabulary is predominantly general. Needs Academic Word List (AWL) items to reach Band 7+.");
  }
  const lexicalCount = corrections.filter((c) => c.category === "LEXICAL").length;
  if (lexicalCount) lrI.push(`Found ${lexicalCount} informal or imprecise word choice(s) to upgrade.`);
  lr = Math.min(8.5, Math.max(5, lr - lexicalCount * 0.2));

  // 4. Grammatical Range & Accuracy
  let gra = 6.5;
  const graS: string[] = [];
  const graI: string[] = [];
  const subs = SUBORDINATORS.filter((w) => wordRe(w).test(essay));
  if (subs.length >= 3) {
    graS.push(`Demonstrates flexible complex sentence structures using subordinators (${subs.slice(0, 3).join(", ")}).`);
    gra += 0.5;
    rubricLinks.push({
      id: uid(),
      criterion: "GRA",
      bandLevel: 7,
      descriptorText: "Uses a variety of complex structures with good control",
      essaySnippet: `Complex subordinators identified: ${subs.join(", ")}`,
      startIndex: -1,
      endIndex: -1,
      isPositive: true,
      examinerCommentary: "Effective sentence variation balancing compound and complex sentence structures.",
    });
  }
  const grammarCount = corrections.filter((c) => c.category === "GRAMMAR").length;
  if (grammarCount === 0) {
    graS.push("High degree of grammatical accuracy with zero systematic syntax flaws detected.");
    gra += 0.5;
  } else {
    graI.push(`Found ${grammarCount} grammatical inaccuracy/inaccuracies requiring correction.`);
    gra -= grammarCount * 0.3;
  }
  gra = Math.min(8.5, Math.max(5, gra));

  const fTr = roundToHalfBand(tr);
  const fCc = roundToHalfBand(cc);
  const fLr = roundToHalfBand(lr);
  const fGra = roundToHalfBand(gra);
  const overall = calculateIeltsOverallBand(fTr, fCc, fLr, fGra);

  const detail = (
    code: CriterionCode,
    band: number,
    strengths: string[],
    improvements: string[],
    plan: string,
  ): CriterionScoreDetail => ({
    criterion: code,
    bandScore: band,
    justification: justification(code, band, strengths, improvements),
    officialDescriptorSummary: getBandDescriptor(code, band)?.keySummary ?? "",
    keyStrengths: strengths,
    areasToImprove: improvements,
    nextBandActionPlan: `To reach Band ${Math.min(9, band + 0.5).toFixed(1)}: ${plan}`,
  });

  return {
    id: uid(),
    topic,
    essayText: essay,
    wordCount,
    paragraphCount,
    createdAt: new Date().toISOString(),
    overallBandScore: overall,
    taskResponse: detail("TR", fTr, trS, trI, "Expand the second body paragraph with a concrete real-world example and ensure every prompt element is addressed in depth."),
    coherenceCohesion: detail("CC", fCc, ccS, ccI, "Vary transitions beyond 'Moreover' and use grammatical referencing (this trend, such policies) for seamless paragraph progression."),
    lexicalResource: detail("LR", fLr, lrS, lrI, "Substitute general verbs and adjectives with academic collocations (e.g. 'implement stringent measures', 'exert a profound influence')."),
    grammaticalRangeAccuracy: detail("GRA", fGra, graS, graI, "Practise inversion ('Not only... but also...'), mixed conditionals, and the passive voice to demonstrate structural dexterity."),
    corrections,
    rubricLinks,
    generalFeedback:
      `Overall, this is a Band ${overall.toFixed(1)} performance on IELTS Writing Task 2. ` +
      (overall >= 7
        ? "The response exhibits commendable maturity, academic tone, and well-structured argument development. "
        : "The essay communicates the core ideas adequately, but requires closer attention to lexical variety, grammar precision, and cohesive flow to unlock the 7.0+ threshold. ") +
      "Review the individual criterion tabs and the rubric links to inspect specific sentences that influenced your score.",
    source: "rules",
  };
}
