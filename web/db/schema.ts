import { index, integer, jsonb, pgTable, real, text, timestamp } from "drizzle-orm/pg-core";
import type { AssessmentReport } from "../shared/models.js";

export const essays = pgTable(
  "essays",
  {
    id: text().primaryKey(),
    deviceId: text("device_id").notNull(),
    topic: text().notNull(),
    wordCount: integer("word_count").notNull(),
    overallBand: real("overall_band").notNull(),
    taskResponseBand: real("task_response_band").notNull(),
    coherenceBand: real("coherence_band").notNull(),
    lexicalBand: real("lexical_band").notNull(),
    grammarBand: real("grammar_band").notNull(),
    report: jsonb().$type<AssessmentReport>().notNull(),
    createdAt: timestamp("created_at", { withTimezone: true }).defaultNow().notNull(),
  },
  (t) => [index("essays_device_created_idx").on(t.deviceId, t.createdAt)],
);
