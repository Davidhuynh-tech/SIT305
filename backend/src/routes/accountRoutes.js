const express = require("express");

function createAccountRouter(storage, publicBaseUrl) {
    const router = express.Router();

    router.post("/api/users/upsert", async (req, res) => {
        try {
            const user = await storage.upsertUser(req.body || {});
            if (!user) {
                return res.status(400).json({ error: "INVALID_INPUT", message: "email is required." });
            }
            res.json({ user });
        } catch (error) {
            res.status(500).json({ error: "UPSERT_USER_FAILED", message: error.message || "Could not store user." });
        }
    });

    router.get("/api/profile/:email", async (req, res) => {
        try {
            const user = await storage.getUser(req.params.email);
            if (!user) {
                return res.status(404).json({ error: "NOT_FOUND", message: "User not found." });
            }
            const history = await storage.getHistory(req.params.email, 10);
            res.json({ user, history });
        } catch (error) {
            res.status(500).json({ error: "PROFILE_READ_FAILED", message: error.message || "Could not load profile." });
        }
    });

    router.post("/api/history/append", async (req, res) => {
        try {
            const email = req.body?.email;
            const entry = req.body?.entry || {};
            if (!storage.normalizeEmail(email)) {
                return res.status(400).json({ error: "INVALID_INPUT", message: "email is required." });
            }
            await storage.appendHistory(email, entry);
            res.json({ ok: true });
        } catch (error) {
            res.status(500).json({ error: "HISTORY_WRITE_FAILED", message: error.message || "Could not save history." });
        }
    });

    router.get("/api/history/:email", async (req, res) => {
        try {
            const history = await storage.getHistory(req.params.email);
            res.json({ history });
        } catch (error) {
            res.status(500).json({ error: "HISTORY_READ_FAILED", message: error.message || "Could not load history." });
        }
    });

    router.post("/api/purchase/upgrade", async (req, res) => {
        try {
            const email = req.body?.email;
            const toPlan = req.body?.plan || "Pro";
            const existing = await storage.getUser(email);
            const fromPlan = existing?.accountTier || "Free";

            const user = await storage.upsertUser({ email, accountTier: toPlan });
            if (!user) {
                return res.status(400).json({ error: "INVALID_INPUT", message: "email is required." });
            }
            await storage.recordPurchase(email, fromPlan, toPlan);
            res.json({ ok: true, user });
        } catch (error) {
            res.status(500).json({ error: "PURCHASE_FAILED", message: error.message || "Could not complete purchase." });
        }
    });

    router.post("/api/share/create-link", async (req, res) => {
        try {
            const email = req.body?.email;
            if (!storage.normalizeEmail(email)) {
                return res.status(400).json({ error: "INVALID_INPUT", message: "email is required." });
            }

            const existing = await storage.getUser(email);
            if (!existing) {
                await storage.upsertUser({
                    email,
                    username: req.body?.username || "Student",
                    interests: req.body?.interests
                });
            }

            const share = await storage.createShare(email);
            res.json({
                shareId: share.shareId,
                shareUrl: `${publicBaseUrl}/public/${share.shareId}`
            });
        } catch (error) {
            res.status(500).json({ error: "SHARE_CREATE_FAILED", message: error.message || "Could not create share link." });
        }
    });

    return router;
}

module.exports = { createAccountRouter };
