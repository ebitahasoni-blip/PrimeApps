import type { Config, Context } from "@netlify/functions";
import { and, desc, eq } from "drizzle-orm";
import { db } from "../../db/index.js";
import { essays } from "../../db/schema.js";
import type { EssaySummary } from "../../shared/models.js";

export default async (req: Request, context: Context) => {
  const deviceId = req.headers.get("x-device-id")?.trim();
  if (!deviceId) return Response.json({ error: "Missing device id" }, { status: 400 });
  const id = context.params.id;

  if (req.method === "GET" && !id) {
    const rows = await db
      .select({
        id: essays.id,
        topic: essays.topic,
        wordCount: essays.wordCount,
        overallBandScore: essays.overallBand,
        taskResponseBand: essays.taskResponseBand,
        coherenceBand: essays.coherenceBand,
        lexicalBand: essays.lexicalBand,
        grammarBand: essays.grammarBand,
        createdAt: essays.createdAt,
      })
      .from(essays)
      .where(eq(essays.deviceId, deviceId))
      .orderBy(desc(essays.createdAt))
      .limit(200);
    const summaries: EssaySummary[] = rows.map((r) => ({ ...r, createdAt: r.createdAt.toISOString() }));
    return Response.json(summaries);
  }

  if (req.method === "GET" && id) {
    const [row] = await db
      .select({ report: essays.report })
      .from(essays)
      .where(and(eq(essays.id, id), eq(essays.deviceId, deviceId)));
    if (!row) return Response.json({ error: "Not found" }, { status: 404 });
    return Response.json(row.report);
  }

  if (req.method === "DELETE" && id) {
    await db.delete(essays).where(and(eq(essays.id, id), eq(essays.deviceId, deviceId)));
    return new Response(null, { status: 204 });
  }

  return new Response("Method not allowed", { status: 405 });
};

export const config: Config = {
  path: ["/api/essays", "/api/essays/:id"],
};
