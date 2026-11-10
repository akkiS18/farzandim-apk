package uz.farzandim.parent.ui.screens.ai

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uz.farzandim.parent.data.model.AiWeeklyReportDto

/** One parsed section of the AI weekly report (maps to one story slide). */
private data class StorySection(val title: String, val lines: List<String>)

private val EmojiRegex = Regex("[\\u2600-\\u27BF]|[\\uD83C-\\uD83E][\\uDC00-\\uDFFF]")
private val LegacySplit = Regex("(?=\\*\\*(?:Haftalik|O'sish|Fanlar|Ota-onaga)[^*]+\\*\\*)")

private fun parseSections(raw: String): List<StorySection> {
    val text = raw.replace(EmojiRegex, "")
    if (text.isBlank()) return emptyList()

    val blocks: List<String> =
        if (text.contains("---SECTION:")) {
            text.split(Regex("---SECTION:\\s*")).filter { it.isNotBlank() }
        } else {
            text.split(LegacySplit).filter { it.isNotBlank() }
        }

    val sections = blocks.mapIndexedNotNull { idx, block ->
        val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return@mapIndexedNotNull null
        val title = lines.first()
            .replace("---", "")
            .replace("**", "")
            .replace(Regex("^#+\\s*"), "")
            .replace(Regex("^[0-9]\\.\\s*"), "")
            .trim()
            .ifBlank { "Bo'lim ${idx + 1}" }
        StorySection(title, lines.drop(1).map { it.replace("**", "").trim() })
    }
    return sections.ifEmpty {
        listOf(
            StorySection(
                "Haftalik xulosa",
                text.lines().map { it.replace("**", "").trim() }.filter { it.isNotEmpty() }
            )
        )
    }
}

private data class SlideTheme(val colors: List<Color>, val tag: String)

private val SlideThemes = listOf(
    SlideTheme(listOf(Color(0xFF0F172A), Color(0xFF0C4A6E), Color(0xFF075985)), "HAFTALIK HISOBOT"),
    SlideTheme(listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF065F46)), "01 / HAFTALIK XULOSA"),
    SlideTheme(listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF1E40AF)), "02 / DINAMIKA TAHLILI"),
    SlideTheme(listOf(Color(0xFF4C1D95), Color(0xFF6D28D9), Color(0xFF5B21B6)), "03 / FANLAR VA KITOBXONLIK"),
    SlideTheme(listOf(Color(0xFF7C2D12), Color(0xFFC2410C), Color(0xFF9A3412)), "04 / OTA-ONAGA TAVSIYALAR")
)

private fun dateOnly(s: String?): String = s?.substringBefore('T')?.take(10).orEmpty()

/**
 * Full-screen vertical "story" slider for the weekly AI report, mirroring the web
 * (school-frontend AIReportSection): a cover slide with key metrics followed by one
 * slide per report section, with segmented progress bars and snap paging.
 */
@Composable
fun AiStoryPager(
    report: AiWeeklyReportDto,
    childName: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = remember(report.reportText) { parseSections(report.reportText.orEmpty()) }
    val totalSlides = 1 + sections.size
    val pagerState = rememberPagerState(pageCount = { totalSlides })
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0B0C10))) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val theme = SlideThemes[minOf(page, SlideThemes.lastIndex)]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(theme.colors))
            ) {
                if (page == 0) {
                    CoverSlide(report, childName, theme.tag)
                } else {
                    val idx = page - 1
                    SectionSlide(
                        section = sections[idx],
                        tag = theme.tag,
                        position = idx + 1,
                        count = sections.size,
                        isLast = idx == sections.lastIndex,
                        onNext = { scope.launch { pagerState.animateScrollToPage(page + 1) } },
                        onFinish = onClose
                    )
                }
            }
        }

        // Segmented progress bars
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(totalSlides) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f))
                        .clickable { scope.launch { pagerState.animateScrollToPage(i) } }
                ) {
                    val fill = when {
                        i == pagerState.currentPage -> 1f
                        i < pagerState.currentPage -> 0.6f
                        else -> 0f
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fill)
                            .background(Color.White)
                    )
                }
            }
        }

        // Close button
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onClose)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Close, contentDescription = "Yopish", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.4.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = modifier
    ) {
        Column(content = content)
    }
}

@Composable
private fun CoverSlide(report: AiWeeklyReportDto, childName: String, tag: String) {
    val summary = report.summaryJson
    val avg = (summary?.get("average_grade") as? Number)?.toDouble()?.takeIf { it > 0 }
    val trend = (summary?.get("grade_trend") as? String)?.uppercase()
    val books = (summary?.get("books_read_count") as? Number)?.toInt()?.takeIf { it > 0 }
    val name = childName.ifBlank { "O'quvchi" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 72.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TagChip(tag)
        Spacer(Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
        }
        Text(name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 30.sp, lineHeight = 36.sp)

        val period = when {
            dateOnly(report.startDate).isNotEmpty() && dateOnly(report.endDate).isNotEmpty() ->
                "${dateOnly(report.startDate)} — ${dateOnly(report.endDate)}"
            report.year != null && report.weekNumber != null -> "${report.year}-yil / ${report.weekNumber}-hafta"
            else -> "Haftalik tahlil"
        }
        Text(period, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("O'RTACHA", avg?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "Mavjud emas", Color.White, Modifier.weight(1f))
            MetricCard(
                "DINAMIKA",
                when (trend) {
                    "UP" -> "O'sdi"
                    "DOWN" -> "Pasaydi"
                    "STABLE" -> "Barqaror"
                    else -> "Aniqlanmadi"
                },
                Color(0xFF6EE7B7),
                Modifier.weight(1f)
            )
            MetricCard("KITOBLAR", books?.let { "$it ta" } ?: "Yo'q", Color(0xFFFCD34D), Modifier.weight(1f))
        }

        Spacer(Modifier.weight(1f))
        ScrollHint(Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun MetricCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp).padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ScrollHint(modifier: Modifier = Modifier) {
    val bounce by rememberInfiniteTransition(label = "hint").animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "hintOffset"
    )
    Column(
        modifier = modifier.offset(y = bounce.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PASTGA SURING", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun SectionSlide(
    section: StorySection,
    tag: String,
    position: Int,
    count: Int,
    isLast: Boolean,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 72.dp, bottom = 20.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TagChip(tag)
            Text(section.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, lineHeight = 30.sp)
            section.lines.forEach { line ->
                val isBullet = line.startsWith("-") || line.startsWith("*")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isBullet) line.replace(Regex("^[-*]\\s*"), "") else line,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        fontWeight = if (isBullet) FontWeight.SemiBold else FontWeight.Medium,
                        modifier = Modifier.padding(if (isBullet) 14.dp else 16.dp)
                    )
                }
            }
        }

        HorizontalRule()
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$position / $count bo'lim", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = if (isLast) onFinish else onNext)
            ) {
                Text(
                    text = if (isLast) "Tugatish" else "Keyingisi",
                    color = Color(0xFF0F172A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun HorizontalRule() {
    Box(Modifier.fillMaxWidth().height(1.dp).alpha(0.15f).background(Color.White))
}
