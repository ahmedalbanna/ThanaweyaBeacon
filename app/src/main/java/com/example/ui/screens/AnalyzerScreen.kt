package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AnalyzerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedAnalysisTab by remember { mutableIntStateOf(0) }
    var inputContent by remember { mutableStateOf("") }

    val isAnalysisLoading by viewModel.isAnalysisLoading.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()

    val tabTitles = listOf(
        "تحليل النصوص الأدبية",
        "تفسير القواعد والإعراب",
        "القوانين والمسائل العلمية"
    )

    val tabIcons = listOf(
        Icons.Default.MenuBook,
        Icons.Default.Psychology,
        Icons.Default.Functions
    )

    val presetExamples = when (selectedAnalysisTab) {
        0 -> listOf(
            "وسلا مصرَ: هل سلا القلبُ عنها ... أو أسا جرحَه الزمانُ المؤسي",
            "اختلفتْ نهارٌ وليلٌ يُنسِي ... اذكُرا لي الصِّبا وأيّامَ أُنسِي",
            "عصفَتْ كالصِّبا اللَّعوبِ ومَرَّتْ ... سِنَةً حُلْوَةً ولَذَّةَ طَيْفِ"
        )
        1 -> listOf(
            "إن المعلمين المخلصين مقدرون من مجتمعهم تقديرًا عظيمًا",
            "لولا العلمُ ما تقدَّمَت الشُّعُوبُ في شتَّى المَيادينِ",
            "ذاكِر اجتهاداً رغبةً في نَيْلِ المراتِبِ العُليا"
        )
        else -> listOf(
            "قانون أوم والقوة الدافعة الكهربية للمصدر: VB = I(R + r)",
            "قاعدة لوشاتلييه وتأثير الضغط والحرارة على الاتزان الكيميائي",
            "قانون السقوط الحر والحركة في بعدين بحساب عجلة الجاذبية"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("analyzer_screen")
    ) {
        TabRow(selectedTabIndex = selectedAnalysisTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedAnalysisTab == index,
                    onClick = {
                        selectedAnalysisTab = index
                        inputContent = ""
                    },
                    text = { Text(title, style = MaterialTheme.typography.labelSmall) },
                    icon = { Icon(tabIcons[index], contentDescription = null) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Input Header
            Text(
                text = "المُحلل الذكي لـ ${tabTitles[selectedAnalysisTab]}:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = inputContent,
                onValueChange = { inputContent = it },
                label = { Text("أدخل البيت الشعري، أو الجملة النحوية، أو القانون العلمي") },
                placeholder = { Text("اكتب أو اختر نموذجاً من المقترحات أدناه...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("analyzer_text_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Preset Examples Chips
            Text(
                text = "نماذج جاهزة للاستئناس والتحليل السريع:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(presetExamples) { sample ->
                    SuggestionChip(
                        onClick = { inputContent = sample },
                        label = { Text(sample.take(28) + "...", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (inputContent.isNotBlank()) {
                        viewModel.runAnalysis(tabTitles[selectedAnalysisTab], inputContent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analyze_submit_btn"),
                enabled = !isAnalysisLoading && inputContent.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isAnalysisLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("جاري التحليل والتفكيك بالذكاء الاصطناعي...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بدء التحليل الشامل")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Analysis Output Display ---
            val result = analysisResult
            if (result != null) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = result.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Overview Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "الفكرة والرؤية العامة:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = result.overview,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Analysis Sections
                        result.sections.forEach { section ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = section.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = section.details,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Key Takeaways
                        if (result.keyTakeaways.isNotEmpty()) {
                            Text(
                                text = "أهم القواعد والفوائد المستخرجة:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            result.keyTakeaways.forEach { takeaway ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = takeaway,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Verification Note
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = result.verificationNote ?: "قم بمراجعة الشرح والإعراب دائماً مع الكتب المدرسية المعتمدة.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
