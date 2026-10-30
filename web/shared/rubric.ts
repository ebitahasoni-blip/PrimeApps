export type CriterionCode = "TR" | "CC" | "LR" | "GRA";

export const CRITERIA: { code: CriterionCode; title: string; key: CriterionKey }[] = [
  { code: "TR", title: "Task Response", key: "taskResponse" },
  { code: "CC", title: "Coherence & Cohesion", key: "coherenceCohesion" },
  { code: "LR", title: "Lexical Resource", key: "lexicalResource" },
  { code: "GRA", title: "Grammatical Range & Accuracy", key: "grammaticalRangeAccuracy" },
];

export type CriterionKey = "taskResponse" | "coherenceCohesion" | "lexicalResource" | "grammaticalRangeAccuracy";

export interface BandDescriptor {
  band: number;
  criterion: CriterionCode;
  keySummary: string;
  descriptors: string[];
}

// Official 2023 IELTS Writing Task 2 band descriptors (bands 5–9)
export const RUBRIC: BandDescriptor[] = [
  { band: 9, criterion: "TR", keySummary: "Fully addresses all parts of the task with a fully developed position.", descriptors: [
    "Fully addresses all parts of the task",
    "Presents a fully developed position in answer to the question with relevant, fully extended and well-supported ideas",
    "Ideas are nuanced and perceptive",
  ] },
  { band: 8, criterion: "TR", keySummary: "Sufficiently addresses all parts of the task with a well-developed response.", descriptors: [
    "Sufficiently addresses all parts of the task",
    "Presents a well-developed position in answer to the question with relevant, extended and supported ideas",
    "May lack the nuanced development of Band 9",
  ] },
  { band: 7, criterion: "TR", keySummary: "Addresses all parts of the task with a clear position throughout.", descriptors: [
    "Addresses all parts of the task",
    "Presents a clear position throughout the response",
    "Presents, extends and supports main ideas, but there may be a tendency to over-generalise or supporting ideas may lack focus",
  ] },
  { band: 6, criterion: "TR", keySummary: "Addresses all parts of the task, though some parts may be more fully covered than others.", descriptors: [
    "Addresses all parts of the task, though some parts may be more fully covered than others",
    "Presents a relevant position, though the conclusions may become unclear or repetitive",
    "Presents relevant main ideas but some may be inadequately developed or unclear",
  ] },
  { band: 5, criterion: "TR", keySummary: "Addresses the task only partially; the format may be inappropriate in places.", descriptors: [
    "Addresses the task only partially; the format may be inappropriate in places",
    "Expresses a position, but the development is not always clear",
    "Presents some main ideas, but these are limited and not sufficiently developed; there may be irrelevant detail",
  ] },
  { band: 9, criterion: "CC", keySummary: "Cohesion is used in such a way that it attracts no attention.", descriptors: [
    "Uses cohesion in such a way that it attracts no attention",
    "Skilfully manages paragraphing",
    "Full progression of ideas is effortlessly maintained",
  ] },
  { band: 8, criterion: "CC", keySummary: "Sequences information and ideas logically with managed paragraphing.", descriptors: [
    "Sequences information and ideas logically",
    "Manages all aspects of cohesion well",
    "Uses paragraphing sufficiently and appropriately",
  ] },
  { band: 7, criterion: "CC", keySummary: "Logically organizes information and ideas with clear progression throughout.", descriptors: [
    "Logically organises information and ideas; there is clear progression throughout",
    "Uses a range of cohesive devices appropriately although there may be some under-/over-use",
    "Presents a clear central topic within each paragraph",
  ] },
  { band: 6, criterion: "CC", keySummary: "Arranges information and ideas coherently with clear overall progression.", descriptors: [
    "Arranges information and ideas coherently and there is a clear overall progression",
    "Uses cohesive devices effectively, but cohesion within and/or between sentences may be faulty or mechanical",
    "May not always use referencing clearly or appropriately; paragraphing may lack uniformity",
  ] },
  { band: 5, criterion: "CC", keySummary: "Presents information with some organisation but may lack overall progression.", descriptors: [
    "Presents information with some organisation but there may be a lack of overall progression",
    "Makes inadequate, inaccurate or overuse of cohesive devices",
    "May be repetitive because of lack of referencing and substitution; paragraphing may be inadequate",
  ] },
  { band: 9, criterion: "LR", keySummary: "Uses a wide range of vocabulary with very natural and sophisticated control.", descriptors: [
    "Uses a wide range of vocabulary with very natural and sophisticated control of lexical features",
    "Rare minor errors occur only as 'slips'",
    "Full flexibility and precise meaning in all academic contexts",
  ] },
  { band: 8, criterion: "LR", keySummary: "Uses a wide resource fluently and flexibly to convey precise meanings.", descriptors: [
    "Uses a wide resource fluently and flexibly to convey precise meanings",
    "Skilfully uses uncommon lexical items, but there may be occasional inaccuracies in word choice and collocation",
    "Produces rare errors in spelling and/or word formation",
  ] },
  { band: 7, criterion: "LR", keySummary: "Uses a sufficient range of vocabulary to allow some flexibility and precision.", descriptors: [
    "Uses a sufficient range of vocabulary to allow some flexibility and precision",
    "Uses less common lexical items with some awareness of style and collocation",
    "May produce occasional errors in word choice, spelling and/or word formation",
  ] },
  { band: 6, criterion: "LR", keySummary: "Uses an adequate range of vocabulary for the task with some inaccuracies.", descriptors: [
    "Uses an adequate range of vocabulary for the task",
    "Attempts to use less common vocabulary but with some inaccuracy",
    "Makes some errors in spelling and/or word formation, but they do not impede communication",
  ] },
  { band: 5, criterion: "LR", keySummary: "Uses a limited range of vocabulary, but minimally adequate for the task.", descriptors: [
    "Uses a limited range of vocabulary, but this is minimally adequate for the task",
    "May make noticeable errors in spelling and/or word formation that may cause some difficulty for the reader",
    "Rarely attempts uncommon lexical items; frequent repetition",
  ] },
  { band: 9, criterion: "GRA", keySummary: "Uses a wide range of structures with full flexibility and accuracy.", descriptors: [
    "Uses a wide range of structures with full flexibility and accuracy",
    "Rare minor errors occur only as 'slips'",
    "Error-free sentences are the continuous norm",
  ] },
  { band: 8, criterion: "GRA", keySummary: "Uses a wide range of structures; the majority of sentences are error-free.", descriptors: [
    "Uses a wide range of structures",
    "The majority of sentences are error-free",
    "Makes only very occasional errors or non-systematic inaccuracies",
  ] },
  { band: 7, criterion: "GRA", keySummary: "Uses a variety of complex structures with frequent error-free sentences.", descriptors: [
    "Uses a variety of complex structures",
    "Produces frequent error-free sentences",
    "Has good control of grammar and punctuation but may make a few errors",
  ] },
  { band: 6, criterion: "GRA", keySummary: "Uses a mix of simple and complex sentence forms with some errors.", descriptors: [
    "Uses a mix of simple and complex sentence forms",
    "Makes some errors in grammar and punctuation, but they rarely reduce communication",
    "Produces several error-free sentences, though errors persist in complex constructions",
  ] },
  { band: 5, criterion: "GRA", keySummary: "Uses only a limited range of structures with frequent grammatical errors.", descriptors: [
    "Uses only a limited range of structures",
    "Attempts complex sentences but these tend to be less accurate than simple sentences",
    "May make frequent grammatical errors and punctuation may be faulty; errors can cause some difficulty for the reader",
  ] },
];

export function getDescriptorsFor(criterion: CriterionCode): BandDescriptor[] {
  return RUBRIC.filter((d) => d.criterion === criterion).sort((a, b) => b.band - a.band);
}

export function getBandDescriptor(criterion: CriterionCode, band: number): BandDescriptor | undefined {
  const normalized = Math.min(9, Math.max(5, Math.floor(band)));
  return RUBRIC.find((d) => d.criterion === criterion && d.band === normalized);
}

export function criterionTitle(code: CriterionCode): string {
  return CRITERIA.find((c) => c.code === code)?.title ?? code;
}
