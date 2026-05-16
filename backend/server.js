require("dotenv").config();
const express = require("express");
const cors = require("cors");
const path = require("path");

const storage = require("./src/lib/storage");
const llm = require("./src/lib/llm");
const { createHealthRouter } = require("./src/routes/healthRoutes");
const { createLearningRouter } = require("./src/routes/learningRoutes");
const { createAccountRouter } = require("./src/routes/accountRoutes");
const { createPublicRouter } = require("./src/routes/publicRoutes");

const app = express();
const port = Number(process.env.PORT || 8080);
const publicBaseUrl = process.env.PUBLIC_BASE_URL || `http://localhost:${port}`;

app.use(cors());
app.use(express.json());
app.use("/public-assets", express.static(path.join(__dirname, "src", "views")));

llm.initLlm({
    apiKey: process.env.OPENAI_API_KEY,
    selectedModel: process.env.OPENAI_MODEL || "gpt-4o-mini"
});

app.use(createHealthRouter({
    getStorageMode: storage.getStorageMode,
    getModel: llm.getModel
}));
app.use(createLearningRouter({ generateJson: llm.generateJson }));
app.use(createAccountRouter(storage, publicBaseUrl));
app.use(createPublicRouter(storage));

async function startServer() {
    try {
        await storage.initStorage({
            mongodbUri: process.env.MONGODB_URI,
            mongoDbName: process.env.MONGODB_DB || "ai_learning_app"
        });

        app.listen(port, () => {
            console.log(`AiLearning backend running on http://localhost:${port}`);
            console.log(`LLM provider: OpenAI (${llm.getModel()})`);
            console.log(`Data storage: ${storage.getStorageMode() === "mongodb" ? "MongoDB" : "In-memory fallback"}`);
        });
    } catch (error) {
        console.error("Failed to start backend:", error);
        process.exit(1);
    }
}

startServer();
