const express = require("express");

function createHealthRouter({ getStorageMode, getModel }) {
    const router = express.Router();

    router.get("/health", (_req, res) => {
        res.json({
            ok: true,
            provider: "openai",
            model: getModel(),
            storage: getStorageMode()
        });
    });

    return router;
}

module.exports = { createHealthRouter };
