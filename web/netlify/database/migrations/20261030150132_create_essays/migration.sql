CREATE TABLE "essays" (
	"id" text PRIMARY KEY,
	"device_id" text NOT NULL,
	"topic" text NOT NULL,
	"word_count" integer NOT NULL,
	"overall_band" real NOT NULL,
	"task_response_band" real NOT NULL,
	"coherence_band" real NOT NULL,
	"lexical_band" real NOT NULL,
	"grammar_band" real NOT NULL,
	"report" jsonb NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE INDEX "essays_device_created_idx" ON "essays" ("device_id","created_at");