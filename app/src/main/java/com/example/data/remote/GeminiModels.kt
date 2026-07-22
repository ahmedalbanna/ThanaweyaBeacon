package com.example.data.remote

import com.squareup.moshi.JsonClass

// --- Gemini REST API Payloads ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = 0.4f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

// --- Domain Models Parsed from AI JSON Outputs ---

@JsonClass(generateAdapter = true)
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@JsonClass(generateAdapter = true)
data class HighSchoolQuiz(
    val subject: String,
    val topic: String,
    val questions: List<QuizQuestion>
)

@JsonClass(generateAdapter = true)
data class AnalysisSection(
    val title: String,
    val details: String
)

@JsonClass(generateAdapter = true)
data class TextAnalysisResult(
    val title: String,
    val subjectType: String, // نصوص أدبية، إعراب ونحو، قوانين علمية
    val overview: String,
    val sections: List<AnalysisSection>,
    val keyTakeaways: List<String>,
    val verificationNote: String? = "تنبيه الدقة العلمية: قم بالتحقق من القوانين والإعراب مع المعلم أو الكتاب المدرسي المعتمد."
)

@JsonClass(generateAdapter = true)
data class MindMapNode(
    val id: String,
    val title: String,
    val details: String? = null,
    val children: List<MindMapNode> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MindMapResult(
    val topic: String,
    val subject: String,
    val rootNodes: List<MindMapNode>,
    val summary: String
)

@JsonClass(generateAdapter = true)
data class ResearchReference(
    val title: String,
    val description: String,
    val sourceName: String
)

@JsonClass(generateAdapter = true)
data class ResearchResult(
    val topic: String,
    val subject: String,
    val executiveSummary: String,
    val researchOutline: List<String>,
    val keyReferences: List<ResearchReference>,
    val searchKeywords: List<String>,
    val verificationGuidance: String
)

@JsonClass(generateAdapter = true)
data class DebateTurnFeedback(
    val argumentStrengthScore: Int = 8,
    val sourceUsageScore: Int = 7,
    val reasoningClarityScore: Int = 8,
    val strengthAnalysis: String = "",
    val improvementAdvice: String = "",
    val overallEvaluation: String = ""
)

@JsonClass(generateAdapter = true)
data class DebateTurnResponse(
    val counterArgumentText: String,
    val feedback: DebateTurnFeedback
)

data class DebateMessage(
    val id: String,
    val isUser: Boolean,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val feedback: DebateTurnFeedback? = null
)

data class DebateTopic(
    val id: String,
    val title: String,
    val category: String,
    val initialPerspective: String,
    val aiOpponentRole: String,
    val description: String
)
