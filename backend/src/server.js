const path = require("path");
const express = require("express");
const cors = require("cors");
const OpenAI = require("openai");
const dotenv = require("dotenv");

dotenv.config({ path: path.resolve(__dirname, "../../.env") });

const app = express();
app.use(cors());
app.use(express.json());

const port = process.env.PORT || 8080;
const model = process.env.OPENAI_MODEL || "gpt-4o-mini";
const apiKey = process.env.OPENAI_API_KEY;

const client = apiKey ? new OpenAI({ apiKey }) : null;

app.get("/health", (_req, res) => {
  res.json({
    status: "ok",
    model,
    hasApiKey: Boolean(apiKey)
  });
});

app.post("/chat", async (req, res) => {
  try {
    const username = (req.body?.username || "").toString().trim();
    const message = (req.body?.message || "").toString().trim();

    if (!username || !message) {
      return res.status(400).json({ error: "username and message are required" });
    }

    if (!client) {
      return res.status(500).json({ error: "OPENAI_API_KEY is missing in .env" });
    }

    const response = await client.chat.completions.create({
      model,
      messages: [
        {
          role: "system",
          content: "You are a helpful and concise chatbot in an Android app."
        },
        {
          role: "user",
          content: `Username: ${username}\nMessage: ${message}`
        }
      ],
      temperature: 0.7
    });

    const reply = response.choices?.[0]?.message?.content?.trim();
    if (!reply) {
      return res.status(502).json({ error: "LLM returned an empty response" });
    }

    return res.json({ reply });
  } catch (error) {
    return res.status(500).json({
      error: "Failed to generate reply",
      details: error.message
    });
  }
});

app.listen(port, () => {
  console.log(`AiChat backend listening on http://localhost:${port}`);
});
