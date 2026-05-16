const { MongoClient } = require("mongodb");
const crypto = require("crypto");

let mongoClient = null;
let db = null;

const memoryStore = {
    usersByEmail: new Map(),
    historyByEmail: new Map(),
    sharesById: new Map(),
    purchasesByEmail: new Map()
};

function normalizeEmail(email) {
    return String(email || "").trim().toLowerCase();
}

async function initStorage({ mongodbUri, mongoDbName }) {
    if (!mongodbUri) {
        console.warn("MONGODB_URI is missing. Falling back to in-memory storage.");
        return;
    }

    mongoClient = new MongoClient(mongodbUri);
    await mongoClient.connect();
    db = mongoClient.db(mongoDbName || "ai_learning_app");

    await Promise.all([
        db.collection("users").createIndex({ email: 1 }, { unique: true }),
        db.collection("histories").createIndex({ email: 1, timestamp: -1 }),
        db.collection("shares").createIndex({ shareId: 1 }, { unique: true }),
        db.collection("purchase").createIndex({ email: 1, timestamp: -1 })
    ]);
}

function getStorageMode() {
    return db ? "mongodb" : "memory";
}

async function upsertUser(userData) {
    const email = normalizeEmail(userData.email);
    if (!email) {
        return null;
    }

    const existing = await getUser(email);
    const merged = {
        email,
        username: userData.username || existing?.username || "Student",
        phoneNumber: userData.phoneNumber || existing?.phoneNumber || "",
        accountTier: userData.accountTier || existing?.accountTier || "Free",
        interests: Array.isArray(userData.interests) && userData.interests.length
            ? userData.interests
            : (existing?.interests?.length ? existing.interests : ["Algorithms", "Testing"]),
        updatedAt: new Date().toISOString(),
        createdAt: existing?.createdAt || new Date().toISOString()
    };

    if (!db) {
        memoryStore.usersByEmail.set(email, merged);
        return merged;
    }

    await db.collection("users").updateOne(
        { email },
        {
            $set: {
                email: merged.email,
                username: merged.username,
                phoneNumber: merged.phoneNumber,
                accountTier: merged.accountTier,
                interests: merged.interests,
                updatedAt: merged.updatedAt
            },
            $setOnInsert: { createdAt: merged.createdAt }
        },
        { upsert: true }
    );
    return db.collection("users").findOne({ email }, { projection: { _id: 0 } });
}

async function getUser(email) {
    const normalized = normalizeEmail(email);
    if (!normalized) {
        return null;
    }

    if (!db) {
        return memoryStore.usersByEmail.get(normalized) || null;
    }
    return db.collection("users").findOne({ email: normalized }, { projection: { _id: 0 } });
}

async function appendHistory(email, entry) {
    const normalized = normalizeEmail(email);
    if (!normalized) {
        return;
    }

    const history = {
        email: normalized,
        timestamp: entry.timestamp || new Date().toISOString(),
        taskPrompt: entry.taskPrompt || "",
        totalQuestions: Number(entry.totalQuestions || 0),
        correctCount: Number(entry.correctCount || 0),
        topicSummary: entry.topicSummary || ""
    };

    if (!db) {
        const current = memoryStore.historyByEmail.get(normalized) || [];
        current.unshift(history);
        memoryStore.historyByEmail.set(normalized, current.slice(0, 30));
        return;
    }

    await db.collection("histories").insertOne(history);
}

async function getHistory(email, limit = 30) {
    const normalized = normalizeEmail(email);
    if (!normalized) {
        return [];
    }

    if (!db) {
        return (memoryStore.historyByEmail.get(normalized) || []).slice(0, limit);
    }

    return db.collection("histories")
        .find({ email: normalized }, { projection: { _id: 0, email: 0 } })
        .sort({ timestamp: -1 })
        .limit(limit)
        .toArray();
}

async function createShare(email) {
    const normalized = normalizeEmail(email);
    const shareId = crypto.randomUUID().replace(/-/g, "").slice(0, 16);
    const shareDoc = {
        shareId,
        email: normalized,
        createdAt: new Date().toISOString()
    };

    if (!db) {
        memoryStore.sharesById.set(shareId, shareDoc);
    } else {
        await db.collection("shares").insertOne(shareDoc);
    }

    return shareDoc;
}

async function getShare(shareId) {
    if (!db) {
        return memoryStore.sharesById.get(shareId) || null;
    }
    return db.collection("shares").findOne({ shareId }, { projection: { _id: 0 } });
}

async function recordPurchase(email, fromPlan, toPlan) {
    const normalized = normalizeEmail(email);
    if (!normalized) {
        return;
    }

    const event = {
        email: normalized,
        fromPlan: fromPlan || "Free",
        toPlan: toPlan || "Free",
        timestamp: new Date().toISOString()
    };

    if (!db) {
        const events = memoryStore.purchasesByEmail.get(normalized) || [];
        events.unshift(event);
        memoryStore.purchasesByEmail.set(normalized, events);
        return;
    }

    await db.collection("purchase").insertOne(event);
}

module.exports = {
    initStorage,
    getStorageMode,
    normalizeEmail,
    upsertUser,
    getUser,
    appendHistory,
    getHistory,
    createShare,
    getShare,
    recordPurchase
};
