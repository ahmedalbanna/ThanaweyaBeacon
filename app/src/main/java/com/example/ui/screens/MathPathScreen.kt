package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.remote.AdaptiveMathResponse
import com.example.data.remote.CalculusQuestion
import com.example.data.remote.CalculusTopicNode
import com.example.data.remote.MathExample
import com.example.data.remote.TopicMasteryState
import com.example.data.remote.TopicStatus
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MathPathScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Curriculum Roadmap & Lessons, 1: Adaptive Practice & Quiz

    val nodes = viewModel.mathCalculusNodes
    val selectedNode by viewModel.selectedCalculusNode.collectAsState()
    val masteryMap by viewModel.mathMasteryStateMap.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("math_path_screen")
    ) {
        TabRow(selectedTabIndex = activeSubTab) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("المسار والدروس التفاعلية") },
                icon = { Icon(Icons.Default.Functions, contentDescription = null) },
                modifier = Modifier.testTag("math_tab_roadmap")
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("الاختبار والتمارين التكيفية") },
                icon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                modifier = Modifier.testTag("math_tab_quiz")
            )
        }

        when (activeSubTab) {
            0 -> MathRoadmapAndLessonsContent(
                viewModel = viewModel,
                nodes = nodes,
                selectedNode = selectedNode,
                masteryMap = masteryMap,
                onStartQuizClick = { node, setType ->
                    viewModel.startMathTopicQuiz(node, setType)
                    activeSubTab = 1
                }
            )
            1 -> MathAdaptiveQuizContent(
                viewModel = viewModel,
                selectedNode = selectedNode,
                onBackToLessons = { activeSubTab = 0 }
            )
        }
    }
}

@Composable
fun MathRoadmapAndLessonsContent(
    viewModel: MainViewModel,
    nodes: List<CalculusTopicNode>,
    selectedNode: CalculusTopicNode,
    masteryMap: Map<String, TopicMasteryState>,
    onStartQuizClick: (CalculusTopicNode, String) -> Unit
) {
    val totalMastered = masteryMap.values.count { it.status == TopicStatus.MASTERED }
    val totalProgress = (totalMastered.toFloat() / nodes.size.coerceAtLeast(1))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مسار التفاضل والتكامل التكيفي",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "الصف الثاني الثانوي • الرياضيات التطبيقية والبحتة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${(totalProgress * 100).toInt()}% إتقان",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "مؤشر التقدم في الوحدات:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { totalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            }
        }

        item {
            Text(
                text = "خريطة المنهج والتسلسل التعليمي:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(nodes) { node ->
            val state = masteryMap[node.id] ?: TopicMasteryState(node.id)
            val isSelected = node.id == selectedNode.id

            MathTopicNodeCard(
                node = node,
                state = state,
                isSelected = isSelected,
                onClick = {
                    if (state.status != TopicStatus.LOCKED) {
                        viewModel.selectCalculusTopicNode(node)
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            val selectedState = masteryMap[selectedNode.id] ?: TopicMasteryState(selectedNode.id)
            InteractiveLessonCard(
                node = selectedNode,
                state = selectedState,
                onStartQuizClick = onStartQuizClick
            )
        }
    }
}

@Composable
fun MathTopicNodeCard(
    node: CalculusTopicNode,
    state: TopicMasteryState,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isLocked = state.status == TopicStatus.LOCKED

    val badgeColor = when (state.status) {
        TopicStatus.MASTERED -> Color(0xFF2E7D32)
        TopicStatus.NEEDS_REINFORCEMENT -> Color(0xFFD32F2F)
        TopicStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        TopicStatus.LOCKED -> MaterialTheme.colorScheme.outline
    }

    val badgeIcon = when (state.status) {
        TopicStatus.MASTERED -> Icons.Default.CheckCircle
        TopicStatus.NEEDS_REINFORCEMENT -> Icons.Default.TrendingUp
        TopicStatus.IN_PROGRESS -> Icons.Default.Psychology
        TopicStatus.LOCKED -> Icons.Default.Lock
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else if (isLocked) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(14.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked) { onClick() }
            .testTag("node_card_${node.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.unitTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state.status.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = node.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isLocked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )

            if (state.scorePercentage > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "آخر نتيجة اختبار: ${state.scorePercentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (state.extraPracticeDone) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "تمت التمارين الإضافية ✓",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveLessonCard(
    node: CalculusTopicNode,
    state: TopicMasteryState,
    onStartQuizClick: (CalculusTopicNode, String) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "الدرس التفاعلي الشارح: ${node.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = node.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "القوانين والنظريات الأساسية:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    node.keyFormulas.forEach { formula ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formula,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "أمثلة تفاعلية خطوة بخطوة:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            node.interactiveExamples.forEach { ex ->
                WorkedExampleItem(example = ex)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "التوجيه التكيفي للذكاء الاصطناعي:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = state.recommendationNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.status == TopicStatus.NEEDS_REINFORCEMENT) {
                Button(
                    onClick = { onStartQuizClick(node, "REMEDIAL") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("remedial_btn")
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تمارين معالجة إضافية (دعم وتقوية الفهم)")
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else if (state.status == TopicStatus.MASTERED) {
                Button(
                    onClick = { onStartQuizClick(node, "CHALLENGE") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("challenge_btn")
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تسريع المسار - أسئلة التحدي والتميّز")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { onStartQuizClick(node, "STANDARD") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_math_quiz_btn")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("بدء الاختبار التكيفي المباشر")
            }
        }
    }
}

@Composable
fun WorkedExampleItem(example: MathExample) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = example.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = example.problemText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            example.stepByStepSolution.forEach { step ->
                Text(
                    text = "• $step",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun MathAdaptiveQuizContent(
    viewModel: MainViewModel,
    selectedNode: CalculusTopicNode,
    onBackToLessons: () -> Unit
) {
    val questions by viewModel.currentMathQuestions.collectAsState()
    val setType by viewModel.mathQuestionSetType.collectAsState()
    val userAnswers by viewModel.mathUserAnswers.collectAsState()
    val isSubmitted by viewModel.mathQuizSubmitted.collectAsState()
    val score by viewModel.mathQuizScore.collectAsState()
    val isEvaluating by viewModel.isMathEvaluating.collectAsState()
    val adaptResponse by viewModel.latestAdaptiveResponse.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "اختبار تفاعلي تكيفي: ${selectedNode.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (setType) {
                            "REMEDIAL" -> "وضع التمارين المعالجة والإضافية لتدعيم الفهم"
                            "CHALLENGE" -> "وضع تسريع المسار والتحدي للمتفوقين"
                            else -> "وضع الاختبار القصير المباشر"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                OutlinedButton(
                    onClick = onBackToLessons,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("العودة للدرس")
                }
            }
        }

        if (isSubmitted) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (score >= 85) Color(0xFFE8F5E9)
                        else if (score >= 60) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else Color(0xFFFFEBEE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (score >= 85) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (score >= 85) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "نتيجة التقييم التكيفي: $score%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (score >= 85) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                            )
                        }

                        if (isEvaluating) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الذكاء الاصطناعي يحلل أدائك ويحدث المسار التكيفي...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            adaptResponse?.let { adapt ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = adapt.adaptiveNote,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = adapt.additionalExplanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        itemsIndexed(questions) { index, q ->
            val selectedOption = userAnswers[index]

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "سؤال ${index + 1} من ${questions.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = q.difficulty,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = q.questionText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    q.options.forEachIndexed { optIndex, optionText ->
                        val isChosen = selectedOption == optIndex
                        val isCorrect = optIndex == q.correctAnswerIndex

                        val optionBg = when {
                            isSubmitted && isCorrect -> Color(0xFFE8F5E9)
                            isSubmitted && isChosen && !isCorrect -> Color(0xFFFFEBEE)
                            isChosen -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        }

                        val optionBorder = when {
                            isSubmitted && isCorrect -> Color(0xFF2E7D32)
                            isSubmitted && isChosen && !isCorrect -> Color(0xFFC62828)
                            isChosen -> MaterialTheme.colorScheme.primary
                            else -> Color.Transparent
                        }

                        Surface(
                            color = optionBg,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .border(1.dp, optionBorder, RoundedCornerShape(10.dp))
                                .clickable(enabled = !isSubmitted) {
                                    viewModel.selectMathAnswer(index, optIndex)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isChosen,
                                    onClick = {
                                        if (!isSubmitted) {
                                            viewModel.selectMathAnswer(index, optIndex)
                                        }
                                    },
                                    enabled = !isSubmitted
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSubmitted) {
                                    if (isCorrect) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else if (isChosen) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isSubmitted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "الشرح العلمي النموذجي:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = q.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            if (!isSubmitted) {
                Button(
                    onClick = { viewModel.submitMathQuiz() },
                    enabled = userAnswers.size == questions.size,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_math_quiz_btn")
                ) {
                    Text("تسليم إجابات الاختبار القصير")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.startMathTopicQuiz(selectedNode, setType) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة الاختبار")
                    }

                    OutlinedButton(
                        onClick = onBackToLessons,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("متابعة المسار التعليمي")
                    }
                }
            }
        }
    }
}
