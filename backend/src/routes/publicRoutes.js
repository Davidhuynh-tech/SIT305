const express = require("express");
const fs = require("fs");
const path = require("path");

const templatePath = path.join(__dirname, "..", "views", "public-profile.html");
const templateHtml = fs.readFileSync(templatePath, "utf8");

function escapeHtml(value) {
    return String(value || "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function createPublicRouter(storage) {
    const router = express.Router();

    router.get("/public/:shareId", async (req, res) => {
        try {
            const share = await storage.getShare(req.params.shareId);
            if (!share) {
                return res.status(404).send("<h1>Share link not found</h1><p>This public profile link is invalid or expired.</p>");
            }

            const user = await storage.getUser(share.email);
            const history = await storage.getHistory(share.email, 10);
            const username = escapeHtml(user?.username || "Student");
            const accountTier = escapeHtml(user?.accountTier || "Free");
            const interests = Array.isArray(user?.interests) && user.interests.length
                ? user.interests.map(escapeHtml).join(", ")
                : "Algorithms, Testing";

            const rows = history.length
                ? history.map((item) => {
                    const timestamp = escapeHtml(item.timestamp || "");
                    const topicSummary = escapeHtml(item.topicSummary || "");
                    const total = Number(item.totalQuestions || 0);
                    const correct = Number(item.correctCount || 0);
                    const taskPrompt = escapeHtml(item.taskPrompt || "");
                    return `<tr><td>${timestamp}</td><td>${correct}/${total}</td><td>${topicSummary}</td><td>${taskPrompt}</td></tr>`;
                }).join("")
                : `<tr><td colspan="4">No history yet.</td></tr>`;

            const html = templateHtml
                .replaceAll("__USERNAME__", username)
                .replaceAll("__ACCOUNT_TIER__", accountTier)
                .replaceAll("__INTERESTS__", interests)
                .replaceAll("__HISTORY_ROWS__", rows);

            res.setHeader("Content-Type", "text/html; charset=utf-8");
            res.send(html);
        } catch (_error) {
            res.status(500).send("<h1>Server error</h1><p>Could not load shared profile.</p>");
        }
    });

    return router;
}

module.exports = { createPublicRouter };
