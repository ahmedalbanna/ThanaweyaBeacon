package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.QuizResultEntity
import com.example.data.local.StudySessionEntity
import com.example.data.local.StudyTaskEntity
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
}
