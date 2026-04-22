require("dotenv").config();
const express = require("express");
const cors = require("cors");
const OpenAI = require("openai");

const app = express();
const port = Number(process.env.PORT || 8080);
const model = process.env.OPENAI_MODEL || "gpt-4o-mini";

const apiKey = process.env.OPENAI_API_KEY;
if (!apiKey) {
    console.warn("OPENAI_API_KEY is missing. Requests will fail until configured.");
}

const client = apiKey ? new OpenAI({ apiKey }) : null;

app.use(cors());
app.use(express.json());

app.get("/health", (_req, res) => {
    res.json({ ok: true, provider: "openai", model });
});

function extractTextFromResponse(response) {
    if (response && typeof response.output_text === "string" && response.output_text.trim()) {
        return response.output_text.trim();
    }

    const output = Array.isArray(response?.output) ? response.output : [];
    const textParts = [];

    for (const item of output) {
        const content = Array.isArray(item?.content) ? item.content : [];
        for (const part of content) {
            if (part?.type === "output_text" && part?.text) {
                textParts.push(part.text);
            }
        }
    }

    return textParts.join("\n").trim();
}

function parseJsonFromText(text) {
    const cleaned = text
        .replace(/^```json\s*/i, "")
        .replace(/^```\s*/i, "")
        .replace(/\s*```$/i, "")
        .trim();
    return JSON.parse(cleaned);
}

async function generateJson(systemInstruction, userPrompt) {
    if (!client) {
        throw new Error("OPENAI_API_KEY is not configured in backend .env.");
    }

    const response = await client.responses.create({
        model,
        input: [
            { role: "system", content: systemInstruction },
            { role: "user", content: userPrompt }
        ]
    });

    const text = extractTextFromResponse(response);
    if (!text) {
        throw new Error("LLM returned empty response.");
    }

    return parseJsonFromText(text);
}

app.post("/api/learning/generate-task", async (req, res) => {
    try {
        const studentName = req.body?.studentName || "Student";
        const interests = Array.isArray(req.body?.interests) ? req.body.interests : [];
        const safeInterests = interests.length ? interests.join(", ") : "Algorithms, Testing";

        const prompt = `Create 2 multiple-choice revision questions for ${studentName} based on: ${safeInterests}.
Return JSON only:
{
  "prompt": "string",
  "questions": [
    {
      "id": "q1",
      "question": "string",
      "options": ["option1", "option2", "option3", "option4"],
      "correctIndex": 0
    }
  ]
}
Rules:
- Exactly 2 questions
- Exactly 4 options each
- correctIndex must be 0..3
- Suitable for learning review, not chit-chat`;

        const payload = await generateJson(
            "You are an educational assessment designer. Return valid JSON only.",
            prompt
        );

        if (!Array.isArray(payload.questions) || payload.questions.length < 2) {
            throw new Error("Invalid questions shape from LLM.");
        }

        res.json(payload);
    } catch (error) {
        console.error("generate-task error:", error);
        res.status(500).json({
            error: "TASK_GENERATION_FAILED",
            message: error.message || "Failed to generate task."
        });
    }
});

app.post("/api/learning/generate-hint", async (req, res) => {
    try {
        const question = req.body?.question || "";
        const selectedAnswer = req.body?.selectedAnswer || "";
        const prompt = `Question: ${question}
Student selected: ${selectedAnswer || "(not selected)"}
Give a concise learning hint that helps student reason toward the right concept without revealing the final answer.
Return JSON only:
{
  "prompt": "string",
  "response": "string"
}`;

        const payload = await generateJson(
            "You are a teaching assistant. Return valid JSON only.",
            prompt
        );

        res.json(payload);
    } catch (error) {
        console.error("generate-hint error:", error);
        res.status(500).json({
            error: "HINT_GENERATION_FAILED",
            message: error.message || "Failed to generate hint."
        });
    }
});

app.post("/api/learning/explain-answers", async (req, res) => {
    try {
        const answers = Array.isArray(req.body?.answers) ? req.body.answers : [];
        if (!answers.length) {
            return res.status(400).json({
                error: "INVALID_INPUT",
                message: "answers array is required."
            });
        }

        const prompt = `For each answer, explain if it is correct or incorrect and why, in teaching style.
Input answers:
${JSON.stringify(answers, null, 2)}

Return JSON only:
{
  "prompt": "string",
  "explanations": [
    {
      "question": "string",
      "selectedAnswer": "string",
      "explanation": "string",
      "correct": true
    }
  ]
}
Rules:
- Keep explanations short (1-3 sentences each)
- Focus on learning feedback`;

        const payload = await generateJson(
            "You are an educational feedback assistant. Return valid JSON only.",
            prompt
        );

        if (!Array.isArray(payload.explanations)) {
            throw new Error("Invalid explanations shape from LLM.");
        }

        res.json(payload);
    } catch (error) {
        console.error("explain-answers error:", error);
        res.status(500).json({
            error: "EXPLANATION_FAILED",
            message: error.message || "Failed to explain answers."
        });
    }
});

app.listen(port, () => {
    console.log(`AiLearning backend running on http://localhost:${port}`);
    console.log(`LLM provider: OpenAI (${model})`);
});
