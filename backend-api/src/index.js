import "dotenv/config";
import express from "express";
import { MongoClient } from "mongodb";

const app = express();
app.use(express.json({ limit: "1mb" }));

const port = Number(process.env.PORT || 3000);
const mongoUri = process.env.MONGODB_URI || "";
const dbName = process.env.MONGODB_DB_NAME || "scamguard";

if (!mongoUri) {
  console.error("Missing MONGODB_URI in environment.");
  process.exit(1);
}

const mongoClient = new MongoClient(mongoUri);
await mongoClient.connect();
const db = mongoClient.db(dbName);

await ensureCollection(db, "Users");
await ensureCollection(db, "Histories");

const usersCollection = db.collection("Users");
const historyCollection = db.collection("Histories");

await usersCollection.createIndex({ username: 1 }, { unique: true });
await historyCollection.createIndex({ username: 1, createdAt: -1 });

async function ensureCollection(database, collectionName) {
  const existing = await database
    .listCollections({ name: collectionName }, { nameOnly: true })
    .toArray();

  if (existing.length === 0) {
    await database.createCollection(collectionName);
    console.log(`Created collection: ${collectionName}`);
  }
}

app.get("/api/health", async (_req, res) => {
  try {
    await db.command({ ping: 1 });
    res.json({ ok: true, db: "connected" });
  } catch (error) {
    res.status(500).json({ ok: false, error: String(error.message || error) });
  }
});

app.post("/api/sync/user", async (req, res) => {
  try {
    const { username, passwordHash } = req.body ?? {};
    if (!username || !passwordHash) {
      return res.status(400).json({ ok: false, error: "username and passwordHash are required." });
    }

    await usersCollection.updateOne(
      { username },
      {
        $set: { passwordHash, updatedAt: new Date() },
        $setOnInsert: { createdAt: new Date() }
      },
      { upsert: true }
    );

    return res.json({ ok: true });
  } catch (error) {
    return res.status(500).json({ ok: false, error: String(error.message || error) });
  }
});

app.post("/api/sync/history", async (req, res) => {
  try {
    const { username, message, riskLevel, scamScore, reasoning, suggestedAction } = req.body ?? {};
    if (!username || !message) {
      return res.status(400).json({ ok: false, error: "username and message are required." });
    }

    await historyCollection.insertOne({
      username,
      message,
      riskLevel: riskLevel || "Unknown",
      scamScore: Number.isFinite(Number(scamScore)) ? Number(scamScore) : 0,
      reasoning: reasoning || "",
      suggestedAction: suggestedAction || "",
      createdAt: new Date()
    });

    return res.json({ ok: true });
  } catch (error) {
    return res.status(500).json({ ok: false, error: String(error.message || error) });
  }
});

app.post("/api/sync/history/clear", async (req, res) => {
  try {
    const { username } = req.body ?? {};
    if (!username) {
      return res.status(400).json({ ok: false, error: "username is required." });
    }

    const result = await historyCollection.deleteMany({ username });
    return res.json({ ok: true, deletedCount: result.deletedCount });
  } catch (error) {
    return res.status(500).json({ ok: false, error: String(error.message || error) });
  }
});

app.listen(port, () => {
  console.log(`ScamGuard backend API listening on port ${port}`);
});
