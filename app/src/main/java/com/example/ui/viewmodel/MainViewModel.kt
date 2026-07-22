package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.QuizResultEntity
import com.example.data.local.StudySessionEntity
import com.example.data.local.StudyTaskEntity
import com.example.data.remote.DebateMessage
import com.example.data.remote.DebateTopic
import com.example.data.remote.DebateTurnFeedback
import com.example.data.remote.GeminiRepository
import com.example.data.remote.HighSchoolQuiz
import com.example.data.remote.MindMapResult
import com.example.data.remote.ResearchResult
import com.example.data.remote.TextAnalysisResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val quizDao = db.quizResultDao()
    private val taskDao = db.studyTaskDao()
    private val sessionDao = db.studySessionDao()
    private val repository = GeminiRepository()

    // --- Debate Preset Topics ---
    val presetDebateTopics = listOf(
        DebateTopic(
            id = "french_rev",
            title = "أسباب ومسببات الثورة الفرنسية (1789)",
            category = "تاريخ عالمي",
            initialPerspective = "تُعتبر الثورة الفرنسية نتاجاً متوقعاً للظلم الاجتماعي والطبقية المفرطة واستبداد نظام الحكم القديم مع الأزمة المالية الخانقة.",
            aiOpponentRole = "مؤرخ معارض يرى أن الثورة الفرنسية كانت نتيجة صراع نفوذ بين الطبقة البورجوازية النامية والأرستقراطية، وأن العوامل الفكرية لنشر أفكار التنوير وتأثرها بالثورة الأمريكية هي المحرك الأساسي وليس مجرد الجوع والفقر.",
            description = "مناظرة تاريخية حول الأسباب الحقيقية لاشتعال الثورة الفرنسية: هل هي الأزمة المالية والفقر، أم التحول الفكري وصراع النفوذ الطبقي؟"
        ),
        DebateTopic(
            id = "french_campaign_egypt",
            title = "أسباب ودوافع الحملة الفرنسية على مصر (1798)",
            category = "تاريخ مصر الحديث",
            initialPerspective = "جاءت الحملة الفرنسية بقيادة نابليون بونابرت بهدف قطع طريق التجارة الإنجليزي إلى الهند وتأسيس إمبراطورية فرنسية في الشرق.",
            aiOpponentRole = "مؤرخ معارض يرى أن الدافع الأكبر للحملة كان مصلحة نابليون الشخصية للهروب من الأزمات السياسية في باريس، والتنافس الاستعماري التجاري للسيطرة على البحر المتوسط وليس الهند فقط.",
            description = "مناظرة حول أهداف نابليون بونابرت والحملة الفرنسية: التنافس الاستعماري وتأمين التجارة أم الأطماع الشخصية والموقع الاستراتيجي؟"
        ),
        DebateTopic(
            id = "urabi_revolt",
            title = "عوامل ونتائج الثورة العرابية (1881)",
            category = "تاريخ مصر الحديث",
            initialPerspective = "قاد أحمد عرابي الثورة للمطالبة بالحقوق الوطنية والحد من التدخل الأجنبي وإصلاح الجيش وتنفيذ الدستور.",
            aiOpponentRole = "مؤرخ معارض يرى أن الثورة العرابية تعجلت في صدامها مع الخديوي وعطلت مسار الإصلاح السياسي وأتاحت الذريعة المباشرة للاحتلال البريطاني لمصر عام 1882.",
            description = "مناظرة حامية حول الحركة العرابية: هل كانت حركة وطنية حاسمة أم تسببت ثغراتها الميدانية والسياسية في الاحتلال البريطاني؟"
        ),
        DebateTopic(
            id = "revolution_1919",
            title = "دوافع ومكتسبات ثورة 1919 الوطنية",
            category = "تاريخ مصر الحديث",
            initialPerspective = "تُعد ثورة 1919 النموذج الأول للثورة الشعبية الوطنية الشاملة المطالبة بالاستقلال التام وإلغاء الحماية البريطانية بقيادة سعد زغلول.",
            aiOpponentRole = "مؤرخ معارض يرى أن الحلفاء والظروف الدولية عقب الحرب العالمية الأولى وإعلان مبادئ ويلسون كانت الفاعل الحقيقي في نيل التصريح، وأن الثورة لم تحقق الاستقلال الكامل بسبب التحفظات الأربعة.",
            description = "تقييم ثورة 1919: مدى تحقيق الاستقلال الفعلي مقابل مكاسب الدستور ووعي الأمة."
        ),
        DebateTopic(
            id = "ww1_causes",
            title = "الأسباب المباشرة والعميقة للحرب العالمية الأولى (1914)",
            category = "تاريخ عالمي معاصر",
            initialPerspective = "حادثة اغتيال ولي عهد النمسا فريديناند في سراييفو كانت السبب المباشر لاشتعال الحرب نتيجة نظام التحالفات العسكرية المسبقة.",
            aiOpponentRole = "مؤرخ معارض يصر على أن السباق الاستعماري الشرس على المستعمرات والتنافس الاقتصادي البحري بين ألمانيا وبريطانيا هو السبب الجذري والوحيد الحاسم لاندلاع الحرب.",
            description = "مناظرة أسباب الحرب العالمية الأولى: الاغتيال والتحالفات أم الصراع الاستعماري والاقتصادي؟"
        )
    )

    // --- State: Debate Simulator ---
    val selectedDebateTopic = MutableStateFlow(presetDebateTopics.first())
    val debateMessages = MutableStateFlow<List<DebateMessage>>(emptyList())
    val isDebateLoading = MutableStateFlow(false)
    val latestTurnFeedback = MutableStateFlow<DebateTurnFeedback?>(null)


    // --- State: General App Config ---
    val selectedGrade = MutableStateFlow("الصف الثالث الثانوي - الثانوية العامة")
    val selectedSubject = MutableStateFlow("الفيزياء")

    // --- State: Quiz Generator ---
    val isQuizLoading = MutableStateFlow(false)
    val currentQuiz = MutableStateFlow<HighSchoolQuiz?>(null)
    val currentQuestionIndex = MutableStateFlow(0)
    val userAnswers = MutableStateFlow<MutableMap<Int, Int>>(mutableMapOf())
    val quizSubmitted = MutableStateFlow(false)
    val quizTimerSeconds = MutableStateFlow(0)
    private var quizTimerJob: Job? = null

    // Room DB Flows
    val quizHistory: StateFlow<List<QuizResultEntity>> = quizDao.getAllResults().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val averageScorePercentage: StateFlow<Float?> = quizDao.getAveragePercentage().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    // --- State: Analyzer ---
    val isAnalysisLoading = MutableStateFlow(false)
    val analysisResult = MutableStateFlow<TextAnalysisResult?>(null)

    // --- State: Mind Map ---
    val isMindMapLoading = MutableStateFlow(false)
    val mindMapResult = MutableStateFlow<MindMapResult?>(null)

    // --- State: Research ---
    val isResearchLoading = MutableStateFlow(false)
    val researchResult = MutableStateFlow<ResearchResult?>(null)

    // --- State: Tasks & Pomodoro ---
    val studyTasks: StateFlow<List<StudyTaskEntity>> = taskDao.getAllTasks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalStudyMinutes: StateFlow<Int?> = sessionDao.getTotalStudyMinutes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Pomodoro Timer State
    val timerTargetMinutes = MutableStateFlow(25)
    val timerSecondsRemaining = MutableStateFlow(25 * 60)
    val isTimerRunning = MutableStateFlow(false)
    val timerSubject = MutableStateFlow("الفيزياء")
    private var timerJob: Job? = null

    init {
        // Initialize with default demo quiz & analysis
        loadDefaultQuiz()
        startDebateSession(presetDebateTopics.first())
    }

    fun setGrade(grade: String) {
        selectedGrade.value = grade
    }

    fun setSubject(subject: String) {
        selectedSubject.value = subject
    }

    // --- Quiz Actions ---
    fun loadDefaultQuiz() {
        viewModelScope.launch {
            isQuizLoading.value = true
            val result = repository.generatePracticeQuiz(
                subject = selectedSubject.value,
                topic = "شامل الاختبارات الوزارية",
                gradeLevel = selectedGrade.value
            )
            result.onSuccess {
                currentQuiz.value = it
                resetQuizSession()
            }
            isQuizLoading.value = false
        }
    }

    fun generateNewQuiz(topic: String, count: Int, difficulty: String) {
        viewModelScope.launch {
            isQuizLoading.value = true
            val result = repository.generatePracticeQuiz(
                subject = selectedSubject.value,
                topic = topic,
                gradeLevel = selectedGrade.value,
                questionCount = count,
                difficulty = difficulty
            )
            result.onSuccess {
                currentQuiz.value = it
                resetQuizSession()
                startQuizTimer()
            }
            isQuizLoading.value = false
        }
    }

    private fun resetQuizSession() {
        currentQuestionIndex.value = 0
        userAnswers.value = mutableMapOf()
        quizSubmitted.value = false
        quizTimerSeconds.value = 0
    }

    fun startQuizTimer() {
        quizTimerJob?.cancel()
        quizTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                quizTimerSeconds.value += 1
            }
        }
    }

    fun answerQuestion(questionIndex: Int, optionIndex: Int) {
        val updated = userAnswers.value.toMutableMap()
        updated[questionIndex] = optionIndex
        userAnswers.value = updated
    }

    fun submitQuiz() {
        quizTimerJob?.cancel()
        quizSubmitted.value = true
        val quiz = currentQuiz.value ?: return
        var correctCount = 0
        quiz.questions.forEachIndexed { index, q ->
            if (userAnswers.value[index] == q.correctIndex) {
                correctCount++
            }
        }
        val pct = (correctCount.toFloat() / quiz.questions.size.coerceAtLeast(1)) * 100f
        viewModelScope.launch {
            quizDao.insertResult(
                QuizResultEntity(
                    subject = quiz.subject,
                    topic = quiz.topic,
                    score = correctCount,
                    totalQuestions = quiz.questions.size,
                    percentage = pct
                )
            )
        }
    }

    // --- Analyzer Actions ---
    fun runAnalysis(subjectType: String, textToAnalyze: String) {
        if (textToAnalyze.isBlank()) return
        viewModelScope.launch {
            isAnalysisLoading.value = true
            val res = repository.analyzeTextOrFormula(subjectType, textToAnalyze)
            res.onSuccess { analysisResult.value = it }
            isAnalysisLoading.value = false
        }
    }

    // --- Mind Map Actions ---
    fun generateMindMap(topic: String) {
        if (topic.isBlank()) return
        viewModelScope.launch {
            isMindMapLoading.value = true
            val res = repository.generateMindMap(topic, selectedSubject.value)
            res.onSuccess { mindMapResult.value = it }
            isMindMapLoading.value = false
        }
    }

    // --- Research Actions ---
    fun generateResearch(topic: String) {
        if (topic.isBlank()) return
        viewModelScope.launch {
            isResearchLoading.value = true
            val res = repository.generateResearchResources(topic, selectedSubject.value)
            res.onSuccess { researchResult.value = it }
            isResearchLoading.value = false
        }
    }

    // --- Study Task Actions ---
    fun addTask(title: String, subject: String, targetMinutes: Int, priority: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insertTask(
                StudyTaskEntity(
                    title = title,
                    subject = subject,
                    targetMinutes = targetMinutes,
                    priority = priority
                )
            )
        }
    }

    fun toggleTaskCompletion(task: StudyTaskEntity) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            taskDao.deleteTask(taskId)
        }
    }

    // --- Pomodoro Timer Actions ---
    fun setTimerMinutes(minutes: Int) {
        if (isTimerRunning.value) return
        timerTargetMinutes.value = minutes
        timerSecondsRemaining.value = minutes * 60
    }

    fun startTimer() {
        if (isTimerRunning.value) return
        isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timerSecondsRemaining.value > 0 && isTimerRunning.value) {
                delay(1000)
                timerSecondsRemaining.value -= 1
            }
            if (timerSecondsRemaining.value == 0 && isTimerRunning.value) {
                // Completed session!
                isTimerRunning.value = false
                val minutesCompleted = timerTargetMinutes.value
                sessionDao.insertSession(
                    StudySessionEntity(
                        subject = timerSubject.value,
                        durationMinutes = minutesCompleted
                    )
                )
            }
        }
    }

    fun pauseTimer() {
        isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        timerSecondsRemaining.value = timerTargetMinutes.value * 60
    }

    // --- Historical Debate Actions ---
    fun startDebateSession(topic: DebateTopic) {
        selectedDebateTopic.value = topic
        latestTurnFeedback.value = null
        val openingMessage = DebateMessage(
            id = "opening_${System.currentTimeMillis()}",
            isUser = false,
            messageText = "أهلاً بك يا طالب الثانوية العامة! أنا أستاذ ومؤرخ معارض في قضية: \"${topic.title}\".\n\nتزعم القراءة التقليدية أن: ${topic.initialPerspective}\n\nلكن وجهة نظري المعارضة ترى أن: ${topic.aiOpponentRole}\n\nكيف تُصيغ حجتك التاريخية وتدعمها بالأدلة والمصادر لتفنيد طروحاتي وتثبيت رأيك؟"
        )
        debateMessages.value = listOf(openingMessage)
    }

    fun sendStudentArgument(argumentText: String) {
        val trimmed = argumentText.trim()
        if (trimmed.isEmpty() || isDebateLoading.value) return

        val userMessage = DebateMessage(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            messageText = trimmed
        )

        val updatedList = debateMessages.value + userMessage
        debateMessages.value = updatedList
        isDebateLoading.value = true

        viewModelScope.launch {
            val topic = selectedDebateTopic.value
            val res = repository.conductDebateTurn(
                topicTitle = topic.title,
                aiRole = topic.aiOpponentRole,
                conversationHistory = updatedList,
                userArgument = trimmed
            )

            res.onSuccess { turnResponse ->
                val aiMessage = DebateMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    isUser = false,
                    messageText = turnResponse.counterArgumentText,
                    feedback = turnResponse.feedback
                )
                debateMessages.value = debateMessages.value + aiMessage
                latestTurnFeedback.value = turnResponse.feedback
            }
            isDebateLoading.value = false
        }
    }

    fun resetDebate() {
        startDebateSession(selectedDebateTopic.value)
    }
}

