import type { CriterionCode, CriterionKey } from "./rubric.js";

export type CorrectionCategory = "GRAMMAR" | "LEXICAL" | "COHERENCE" | "TASK_RESPONSE" | "PUNCTUATION";

export const CORRECTION_LABELS: Record<CorrectionCategory, string> = {
  GRAMMAR: "Grammar & Syntax",
  LEXICAL: "Lexical & Collocation",
  COHERENCE: "Cohesion & Flow",
  TASK_RESPONSE: "Task Response",
  PUNCTUATION: "Punctuation",
};

export interface CorrectionItem {
  id: string;
  originalText: string;
  suggestedText: string;
  explanation: string;
  startIndex: number;
  endIndex: number;
  category: CorrectionCategory;
}

export interface RubricHighlightLink {
  id: string;
  criterion: CriterionCode;
  bandLevel: number;
  descriptorText: string;
  essaySnippet: string;
  startIndex: number;
  endIndex: number;
  isPositive: boolean;
  examinerCommentary: string;
}

export interface CriterionScoreDetail {
  criterion: CriterionCode;
  bandScore: number;
  justification: string;
  officialDescriptorSummary: string;
  keyStrengths: string[];
  areasToImprove: string[];
  nextBandActionPlan: string;
}

export interface AssessmentReport {
  id: string;
  topic: string;
  essayText: string;
  wordCount: number;
  paragraphCount: number;
  createdAt: string;
  overallBandScore: number;
  taskResponse: CriterionScoreDetail;
  coherenceCohesion: CriterionScoreDetail;
  lexicalResource: CriterionScoreDetail;
  grammaticalRangeAccuracy: CriterionScoreDetail;
  corrections: CorrectionItem[];
  rubricLinks: RubricHighlightLink[];
  generalFeedback: string;
  /** "ai" when scored by the Gemini examiner, "rules" when the offline rule engine was used */
  source: "ai" | "rules";
}

export interface EssaySummary {
  id: string;
  topic: string;
  wordCount: number;
  overallBandScore: number;
  taskResponseBand: number;
  coherenceBand: number;
  lexicalBand: number;
  grammarBand: number;
  createdAt: string;
}

export type CriterionScores = Record<CriterionKey, CriterionScoreDetail>;

/**
 * Official IELTS overall band score calculation:
 * Arithmetic mean of the 4 criteria, rounded to nearest half or whole band.
 * .25 rounds up to the next half band, .75 rounds up to the next whole band.
 */
export function calculateIeltsOverallBand(tr: number, cc: number, lr: number, gra: number): number {
  const average = (tr + cc + lr + gra) / 4;
  const whole = Math.floor(average);
  const fractional = average - whole;
  if (fractional < 0.25) return whole;
  if (fractional < 0.75) return whole + 0.5;
  return whole + 1;
}

export function roundToHalfBand(score: number, min = 4, max = 9): number {
  const rounded = Math.round(score * 2) / 2;
  return Math.min(max, Math.max(min, rounded));
}

export function countWords(text: string): number {
  return text.trim().split(/\s+/).filter(Boolean).length;
}

export function splitParagraphs(text: string): string[] {
  return text
    .trim()
    .split(/\n\s*\n/)
    .map((p) => p.trim())
    .filter(Boolean);
}

export function formatBand(band: number): string {
  return band.toFixed(1);
}
