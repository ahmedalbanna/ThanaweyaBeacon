package com.example.data.remote

import com.squareup.moshi.JsonClass

enum class TopicStatus(val label: String) {
    LOCKED("مغلق (يتطلب إتقان السابق)"),
    IN_PROGRESS("قيد التعلم والتدريب"),
    NEEDS_REINFORCEMENT("يحتاج دعم وتمارين إضافية"),
    MASTERED("مُتقَن (تسريع التقدم)")
}

data class MathExample(
    val title: String,
    val problemText: String,
    val stepByStepSolution: List<String>,
    val keyFormula: String
)

data class CalculusQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: String = "متوسط", // "سهل", "متوسط", "صعب", "تحدي"
    val hint: String = ""
)

data class CalculusTopicNode(
    val id: String,
    val unitTitle: String,
    val title: String,
    val gradeLevel: String = "الصف الثاني الثانوي",
    val subject: String = "الرياضيات - التفاضل والتكامل",
    val prerequisiteId: String? = null,
    val summary: String,
    val keyFormulas: List<String>,
    val interactiveExamples: List<MathExample>,
    val standardQuestions: List<CalculusQuestion>,
    val remedialQuestions: List<CalculusQuestion>,
    val challengeQuestions: List<CalculusQuestion>
)

data class TopicMasteryState(
    val topicId: String,
    var status: TopicStatus = TopicStatus.LOCKED,
    var scorePercentage: Int = 0,
    var quizAttempts: Int = 0,
    var extraPracticeDone: Boolean = false,
    var recommendationNote: String = "ابدأ بدارسة مفاهيم الدرس ثم أجرِ الاختبار التشخيصي."
)

@JsonClass(generateAdapter = true)
data class AdaptiveMathResponse(
    val adaptiveNote: String,
    val recommendedDifficulty: String,
    val additionalExplanation: String,
    val newQuestions: List<CalculusQuestionJson>? = null
)

@JsonClass(generateAdapter = true)
data class CalculusQuestionJson(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: String
)
