package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository {

    private val apiService = RetrofitClient.service
    private val moshi = RetrofitClient.moshiParser

    private fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key.isNull_or_empty() || key == "MY_GEMINI_API_KEY") {
            ""
        } else {
            key
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

    suspend fun generatePracticeQuiz(
        subject: String,
        topic: String,
        gradeLevel: String,
        questionCount: Int = 4,
        difficulty: String = "متوسط"
    ): Result<HighSchoolQuiz> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.success(getFallbackQuiz(subject, topic))
        }

        val prompt = """
            قم بتوليد اختبار تدريبي ذكي للمرحلة الثانوية ($gradeLevel).
            المادة: $subject
            الموضوع: $topic
            مستوى الصعوبة: $difficulty
            عدد الأسئلة: $questionCount

            يجب أن ترجع استجابة JSON فقط دون أي نصوص إضافية خارج الـ JSON بالشكل التالي:
            {
              "subject": "$subject",
              "topic": "$topic",
              "questions": [
                {
                  "question": "نص السؤال بأسلوب أسئلة الامتحانات الثانوية",
                  "options": ["خيار 1", "خيار 2", "خيار 3", "خيار 4"],
                  "correctIndex": 0,
                  "explanation": "شرح تفصيلي علمي أو لغوي للإجابة الصحيحة وكيفية استنتاجها"
                }
              ]
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.3f,
                responseMimeType = "application/json"
            ),
            systemInstruction = Content(
                parts = listOf(
                    Part(text = "أنت خبير إعداد الامتحانات والمناهج لطلاب المرحلة الثانوية العامة. أجب بأسلوب امتحاني دقيق وبصيغة JSON صريحة.")
                )
            )
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrEmpty()) {
                val adapter = moshi.adapter(HighSchoolQuiz::class.java)
                val quiz = adapter.fromJson(jsonText)
                if (quiz != null && quiz.questions.isNotEmpty()) {
                    return@withContext Result.success(quiz)
                }
            }
            Result.success(getFallbackQuiz(subject, topic))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(getFallbackQuiz(subject, topic))
        }
    }

    suspend fun analyzeTextOrFormula(
        subjectType: String, // نصوص أدبية، إعراب ونحو، قوانين علمية
        inputText: String
    ): Result<TextAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.success(getFallbackAnalysis(subjectType, inputText))
        }

        val prompt = """
            قم بتحليل وتفكيك المحتوى التالي لطلاب الثانوية العامة:
            نوع التحليل: $subjectType
            المحتوى المراد تحليله: "$inputText"

            أرجع النتيجة كـ JSON بدقة متناهية بالصيغة التالية:
            {
              "title": "عنوان التحليل الشامل",
              "subjectType": "$subjectType",
              "overview": "ملخص شامل ومبسط للفكره الأساسية",
              "sections": [
                {
                  "title": "عنوان الجزء أو الشاهد البلاغي/النحوي/القانون العلمي",
                  "details": "الشرح والشواهد أو الخطوات التفصيلية"
                }
              ],
              "keyTakeaways": ["نقطة مستفادة أو قاعدة هامة 1", "قاعدة هامة 2"],
              "verificationNote": "تنبيه الدقة العلمية: قم بالتحقق من المصادر والمراجع الرسمية مع المعلم."
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.3f,
                responseMimeType = "application/json"
            ),
            systemInstruction = Content(
                parts = listOf(
                    Part(text = "أنت معلم ومحلل تعليمي معتمد لطلاب الثانوية. أجب بصيغة JSON مفصلة وتفاعلية للتعلم العميق.")
                )
            )
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrEmpty()) {
                val adapter = moshi.adapter(TextAnalysisResult::class.java)
                val result = adapter.fromJson(jsonText)
                if (result != null) {
                    return@withContext Result.success(result)
                }
            }
            Result.success(getFallbackAnalysis(subjectType, inputText))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(getFallbackAnalysis(subjectType, inputText))
        }
    }

    suspend fun generateMindMap(
        topic: String,
        subject: String
    ): Result<MindMapResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.success(getFallbackMindMap(topic, subject))
        }

        val prompt = """
            قم بإنشاء خريطة ذهنية هيكلية مفصلة لدرس الثانوية التالي:
            المادة: $subject
            الموضوع: $topic

            أرجع النتيجة كـ JSON بالهيكل التالي:
            {
              "topic": "$topic",
              "subject": "$subject",
              "summary": "ملخص موجز لربط مفاهيم الخريطة الذهنية",
              "rootNodes": [
                {
                  "id": "1",
                  "title": "المفهوم الرئيسي الأول",
                  "details": "شرح مختصر للمفهوم",
                  "children": [
                    {
                      "id": "1-1",
                      "title": "فرع فرعي 1",
                      "details": "تفاصيل وتعريفات هامة",
                      "children": []
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.3f,
                responseMimeType = "application/json"
            ),
            systemInstruction = Content(
                parts = listOf(
                    Part(text = "أنت مهندس المناهج وخبير تنظيم المعلومات والخرائط الذهنية لطلاب الثانوية.")
                )
            )
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrEmpty()) {
                val adapter = moshi.adapter(MindMapResult::class.java)
                val result = adapter.fromJson(jsonText)
                if (result != null && result.rootNodes.isNotEmpty()) {
                    return@withContext Result.success(result)
                }
            }
            Result.success(getFallbackMindMap(topic, subject))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(getFallbackMindMap(topic, subject))
        }
    }

    suspend fun generateResearchResources(
        topic: String,
        subject: String
    ): Result<ResearchResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.success(getFallbackResearch(topic, subject))
        }

        val prompt = """
            اقترح مصادر ومراجع وأفكار بحثية وطريقة التحقق لطلاب الثانوية العامة:
            الموضوع: $topic
            المادة: $subject

            أرجع النتيجة كـ JSON فقط بالصيغة التالية:
            {
              "topic": "$topic",
              "subject": "$subject",
              "executiveSummary": "نظرة عامة على أهمية هذا الموضوع البحثي للمرحلة الثانوية",
              "researchOutline": ["المقدمة وتحديد الإشكالية", "العنصر الأول والتحليل", "العنصر الثاني والتطبيقات", "الخاتمة والتوصيات"],
              "keyReferences": [
                {
                  "title": "عنوان كتاب مرجعي أو موسوعة علمية",
                  "description": "شرح ما يتناوله هذا المرجع وكيف يفيد الطالب",
                  "sourceName": "اسم المصدر أو المرجع المعتمد"
                }
              ],
              "searchKeywords": ["كلمة بحث 1", "كلمة بحث 2", "مصطلح انجليزي للمقارنة"],
              "verificationGuidance": "إرشادات التحقق من المعلومة: تأكد من مراجعة الكتب الوزارية والمنصات التعليمية الرسمية."
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.3f,
                responseMimeType = "application/json"
            ),
            systemInstruction = Content(
                parts = listOf(
                    Part(text = "أنت مستشار المهارات البحثية والأكاديمية لطلاب الثانوية العامة.")
                )
            )
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrEmpty()) {
                val adapter = moshi.adapter(ResearchResult::class.java)
                val result = adapter.fromJson(jsonText)
                if (result != null) {
                    return@withContext Result.success(result)
                }
            }
            Result.success(getFallbackResearch(topic, subject))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(getFallbackResearch(topic, subject))
        }
    }

    // --- Fallbacks for instant, reliable off-line or placeholder demonstration ---

    private fun getFallbackQuiz(subject: String, topic: String): HighSchoolQuiz {
        return when {
            subject.contains("فيزياء") || subject.contains("Physics") -> HighSchoolQuiz(
                subject = "الفيزياء",
                topic = topic.ifEmpty { "التيار الكهربي وقانون أوم" },
                questions = listOf(
                    QuizQuestion(
                        question = "ما هو تأثير زيادة طول موصل متجانس إلى ضعف طوله مع ثبات مساحة مقطعه على مقاومتة الكهربية؟",
                        options = listOf("تزداد المقاومة إلى الضعف", "تقل المقاومة إلى النصف", "تظل ثابتة لا تتغير", "تزداد المقاومة بأربعة أمثال"),
                        correctIndex = 0,
                        explanation = "طبقاً للقانون R = ρₑ · (L / A)، فإن المقاومة الكهربية تتناسب طردياً مع طول الموصل عند ثبات مساحة المقطع ونوع المادة."
                    ),
                    QuizQuestion(
                        question = "في دائرة مغلقة تحتوي على مصدر كهرومغناطيسي ومقاومة خارجية، ما القوة الدافعية الكهربية (VB)؟",
                        options = listOf("الشغل الكلي المبذول لنقل شحنة 1 كولوم في الدائرة كلها خارج وداخل المصدر", "فرق الجهد بين طرفي المقاومة فقط", "شدة التيار المار مقسوماً على الزمن", "كمية الشحنة الكهربية في الثقوب"),
                        correctIndex = 0,
                        explanation = "تعرف القوة الدافعة الكهربية للمصدر بأنها الشغل الكلي المبذول لنقل شحنة كهربية مقدارها 1 كولوم في الدائرة الكهربية كلها خارج وداخل المصدر."
                    ),
                    QuizQuestion(
                        question = "ما هي وحدة قياس معامل الحث الذاتي لملف؟",
                        options = listOf("الهنري (Henry)", "التسلا (Tesla)", "الفولت (Volt)", "أوم · ثانية (Ohm/s)"),
                        correctIndex = 0,
                        explanation = "يقاس الحث الذاتي بوحدة الهنري (H) وهي تكافئ (فولت · ثانية / أمبير)."
                    ),
                    QuizQuestion(
                        question = "أي من الحالات التالية تصف سلوك الفوتون عند التفاعل مع إلكترون حر (ظاهرة كومتون)؟",
                        options = listOf("يقل تردد الفوتون المشتت وتزداد طاقته الحركية للالكترون", "يزداد تردد الفوتون المشتت", "تزداد كتلة الفوتون", "ينعدم الشغل تماماً"),
                        correctIndex = 0,
                        explanation = "في ظاهرة كومتون يصدم فوتون عالية الطاقة إلكتروناً حراً، فيقل تردد وطاقة الفوتون بينما يكتسب الإلكترون طاقة حركة وتزداد سرعته."
                    )
                )
            )
            subject.contains("عربية") || subject.contains("Arabic") || topic.contains("نحو") -> HighSchoolQuiz(
                subject = "اللغة العربية",
                topic = topic.ifEmpty { "النحو والنصوص - الثانوية العامة" },
                questions = listOf(
                    QuizQuestion(
                        question = "اعراب كلمة (اجتهاداً) في الجملة: 'ذاكر الطالب اجتهاداً رغبةً في النجاح'؟",
                        options = listOf("مفعول مطلق منصوب وعلامة نصبه الفتحة", "مفعول لأجله منصوب", "حال منصوبة", "تمييز منصوب"),
                        correctIndex = 0,
                        explanation = "(اجتهاداً) مصدر مشتق من لفظ الفعل (ذاكرَ/اجتهدَ) ليؤكد المعنى أو يبين نوعه، فإعرابه مفعول مطلق."
                    ),
                    QuizQuestion(
                        question = "ما نوع الصورة البيانية في قول الشاعر: 'وسلا مصرَ: هل سلا القلبُ عنها'؟",
                        options = listOf("استعارة مكنية (تشخيص للبحر/مصر)", "جناس تام وشبه جملة", "تشبيه بليغ", "كناية عن النسبة"),
                        correctIndex = 0,
                        explanation = "صور مصر بإنسان يخاطب ويسأل (حذف المشبه به وأتى بصفة من صفاته 'السؤال') وهي استعارة مكنية للتشخيص."
                    ),
                    QuizQuestion(
                        question = "عند إسناد الفعل (قال) إلى نون النسوة يكتب:",
                        options = listOf("قُلْنَ (بحذف الألف لمنع التقاء الساكنين)", "قَالْنَ", "قُولْنَ", "يَقُولُونَ"),
                        correctIndex = 0,
                        explanation = "الفعل الماضي الأجوف عند إسناده لضمائر الرفع المتحركة يَبنى على السكون، وتحذف الألف منعاً لالتقاء الساكنين."
                    )
                )
            )
            else -> HighSchoolQuiz(
                subject = subject.ifEmpty { "الكيمياء العامة" },
                topic = topic.ifEmpty { "الاتزان الكيميائي والعوامل المؤثرة" },
                questions = listOf(
                    QuizQuestion(
                        question = "ما تأثير زيادة الضغط على تفاعل متزن متجانس يتساوى فيه عدد مولات المتفاعلات والنواتج الغازية؟",
                        options = listOf("لا يؤثر الضغط على موضع الاتزان", "ينشط التفاعل في الاتجاه الطردي", "ينشط التفاعل في الاتجاه العكسي", "يتوقف التفاعل تماماً"),
                        correctIndex = 0,
                        explanation = "طبقاً لقاعدة لوشاتلييه، إذا كان مجموع حجوم/مولات الغازات المتفاعلة يساوي حجوم الغازات الناتجة فإن تغير الضغط لا يؤثر على موضع الاتزان."
                    ),
                    QuizQuestion(
                        question = "ما هي الصيغة الكيميائية العامة للألكانات؟",
                        options = listOf("CₙH₂ₙ₊₂", "CₙH₂ₙ", "CₙH₂ₙ₋₂", "CₙHₙ"),
                        correctIndex = 0,
                        explanation = "الألكانات هيدروكربونات أليفاتية مشبعة ذات سلاسل مفتوحة وصيغتها العامة CₙH₂ₙ₊₂."
                    )
                )
            )
        }
    }

    private fun getFallbackAnalysis(subjectType: String, inputText: String): TextAnalysisResult {
        return TextAnalysisResult(
            title = "تحليل شمل ومبسط: $inputText",
            subjectType = subjectType,
            overview = "يقدم هذا التحليل تفكيكاً منهجياً دقيقاً للنص أو القانون العلمي لم مساعدة الطالب في الاستيعاب وحل امتحانات الثانوية.",
            sections = listOf(
                AnalysisSection(
                    title = "1. القواعد والأسس الجوهرية",
                    details = "استخراج المفاهيم والروابط الرئيسية، وتحديد علاقة السبب بالنتيجة والدلالات البلاغية أو الرياضية."
                ),
                AnalysisSection(
                    title = "2. خطوات التطبيق والحل العلمي",
                    details = "تحديد المعطيات والمطلوب، وتطبيق القاعدة المعتمدة مع بيان الشواهد والدلائل."
                )
            ),
            keyTakeaways = listOf(
                "التركيز على فهم المعنى الكلي قبل البدء في التفاصيل.",
                "ربط الفكرة بالقواعد المعتمدة في منهج الثانوية العامة."
            ),
            verificationNote = "تنبيه الدقة العلمية: احرص دائماً على مراجعة الشرح مع الكتاب المدرسي والمعلم المعتمد."
        )
    }

    private fun getFallbackMindMap(topic: String, subject: String): MindMapResult {
        return MindMapResult(
            topic = topic.ifEmpty { "الوحدة الدراسية الشاملة" },
            subject = subject.ifEmpty { "المرحلة الثانوية" },
            summary = "خريطة تفاعلية لترتيب المفاهيم الأساسية والتفريعات الهامة لمراجعة ليلة الامتحان.",
            rootNodes = listOf(
                MindMapNode(
                    id = "1",
                    title = "المفاهيم الأساسية والتعريفات",
                    details = "المصطلحات والمحاور الأولية للدرس",
                    children = listOf(
                        MindMapNode(id = "1-1", title = "التعريف العلمي", details = "الصياغة الدقيقة حسب المنهج"),
                        MindMapNode(id = "1-2", title = "الوحدات والشواهد", details = "الأدلة والقوانين المرتبطة")
                    )
                ),
                MindMapNode(
                    id = "2",
                    title = "القوانين والتطبيقات الأهم",
                    details = "طرق حل المسائل البلاغية أو العلمية",
                    children = listOf(
                        MindMapNode(id = "2-1", title = "أمثلة امتحانات نموذجية", details = "كيفية صياغة السؤال بالإجابة النموذحية")
                    )
                )
            )
        )
    }

    private fun getFallbackResearch(topic: String, subject: String): ResearchResult {
        return ResearchResult(
            topic = topic.ifEmpty { "موضوع بحثي ثانوية" },
            subject = subject.ifEmpty { "العلوم والمعارف" },
            executiveSummary = "يقدم هذا الدليل المنهجي محاور ومراجع موثوقة لم مساعدة طالب الثانوية في إجراء بحث علمي أو أدبي متكامل.",
            researchOutline = listOf(
                "المقدمة: صياغة المشكلة البحثية وأهميتها",
                "المحور الأول: المفاهيم النظرية والدراسات المرجعية",
                "المحور الثاني: التحليل والمقارنة والأمثلة",
                "الخاتمة: الاستنتاجات والتوصيات والمراجع"
            ),
            keyReferences = listOf(
                ResearchReference(
                    title = "الموسوعة العلمية والشرح المنهجي - المناهج الوزارية",
                    description = "مرجع الأساس لاختبار وتوثيق المفاهيم العلمية والأدبية بطريقة رسمية.",
                    sourceName = "بنك المعرفة والكتب المدرسية المعتمدة"
                ),
                ResearchReference(
                    title = "الأبحاث والدراسات المساندة للمرحلة الثانوية",
                    description = "تحليلات إضافية ومقالات مراجعة لأبرز العلماء والأدباء.",
                    sourceName = "المكتبة الرقمية والمقالات الأكاديمية"
                )
            ),
            searchKeywords = listOf("المفاهيم الأساسية", "المرحلة الثانوية", "تحليل وتطبيق", "الثانوية العامة"),
            verificationGuidance = "توصية هامة: تأكد من تقاطعات المعلومات مع المصادر الرسمية الموثوقة وكتاب الوزارة المعتمد."
        )
    }
}
