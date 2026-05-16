const OpenAI = require("openai");

let client = null;
let model = "gpt-4o-mini";

function initLlm({ apiKey, selectedModel }) {
    model = selectedModel || "gpt-4o-mini";
    if (!apiKey) {
        console.warn("OPENAI_API_KEY is missing. LLM routes will fail until configured.");
        return;
    }
    client = new OpenAI({ apiKey });
}

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

function getModel() {
    return model;
}

module.exports = {
    initLlm,
    generateJson,
    getModel
};
