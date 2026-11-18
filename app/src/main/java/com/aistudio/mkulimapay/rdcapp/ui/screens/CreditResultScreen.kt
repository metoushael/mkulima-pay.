package com.aistudio.mkulimapay.rdcapp.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Agriculture
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.i18n.LocalizedStrings
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.CreditRequest
import com.aistudio.mkulimapay.rdcapp.model.CreditStatus
import com.aistudio.mkulimapay.rdcapp.model.RiskIconType
import com.aistudio.mkulimapay.rdcapp.model.RiskLevel
import com.aistudio.mkulimapay.rdcapp.ui.theme.LocalMkulimaColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditResultScreen(
    strings: LocalizedStrings,
    language: AppLanguage,
    request: CreditRequest,
    onAcceptConfirmed: (onError: (String) -> Unit) -> Unit,
    onModify: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var isAccepting by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.resultTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("result_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Jauge semi-circulaire animée
            RiskGaugeCard(
                strings = strings,
                language = language,
                riskLevel = request.riskLevel,
                riskScore = request.riskScore
            )

            // 2. Pourquoi ce niveau ?
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.whyThisLevel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    request.reasons.forEach { reason ->
                        val icon = when (reason.iconType) {
                            RiskIconType.RATIO -> Icons.AutoMirrored.Rounded.TrendingUp
                            RiskIconType.CROP -> Icons.Rounded.Agriculture
                            RiskIconType.SEASON -> Icons.Rounded.CalendarMonth
                            RiskIconType.EXPERIENCE -> Icons.Rounded.WorkHistory
                        }
                        val tint = if (reason.positive) {
                            LocalMkulimaColors.current.success
                        } else {
                            LocalMkulimaColors.current.warning
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reason.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = reason.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 3. Offre proposée + Calendrier aligné sur la récolte
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = strings.proposedOffer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OfferMetric(
                            label = strings.offerAmount,
                            value = "${request.data.requestedAmountUsd} USD",
                            modifier = Modifier.weight(1f)
                        )
                        OfferMetric(
                            label = strings.offerDuration,
                            value = "${request.data.durationMonths} ${strings.monthsUnit}",
                            modifier = Modifier.weight(1f)
                        )
                        OfferMetric(
                            label = strings.offerMonthly,
                            value = "${request.monthlyInstallmentUsd} USD",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider()

                    Text(
                        text = strings.harvestScheduleTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Frise chronologique des échéances
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        request.schedule.forEachIndexed { idx, inst ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(24.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (inst.harvestPeak) 14.dp else 10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (inst.harvestPeak) LocalMkulimaColors.current.goldAccent
                                                else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                    if (idx < request.schedule.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(22.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = inst.monthLabel,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (inst.harvestPeak) {
                                            Text(
                                                text = strings.harvestPeakBadge,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = inst.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${inst.amountUsd} USD",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (inst.harvestPeak) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Conseils personnalisés
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = strings.personalizedAdvice,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    request.advice.forEach { tip ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(16.dp)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // 5. Actions finales
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (request.status == CreditStatus.IN_PROGRESS) {
                    Button(
                        onClick = { showAcceptDialog = true },
                        enabled = !isAccepting,
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("result_accept_button")
                    ) {
                        Icon(Icons.Rounded.Payments, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.acceptRequest)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (request.status == CreditStatus.IN_PROGRESS) {
                        OutlinedButton(
                            onClick = onModify,
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("result_modify_button")
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.modifyRequest)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val summaryText = buildShareableSummary(strings, language, request)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "${strings.appName} - ${request.requestId}")
                                putExtra(Intent.EXTRA_TEXT, summaryText)
                            }
                            runCatching {
                                context.startActivity(Intent.createChooser(sendIntent, strings.downloadSummary))
                            }
                            scope.launch {
                                snackbarHostState.showSnackbar(strings.summarySharedToast)
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("result_download_button")
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.downloadSummary, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showAcceptDialog) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            title = { Text(strings.acceptDialogTitle) },
            text = {
                Text(
                    String.format(
                        strings.acceptDialogBody,
                        request.data.requestedAmountUsd,
                        request.data.durationMonths
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAcceptDialog = false
                        isAccepting = true
                        onAcceptConfirmed { err ->
                            isAccepting = false
                            scope.launch { snackbarHostState.showSnackbar(err) }
                        }
                    },
                    modifier = Modifier.testTag("result_confirm_accept_dialog_button")
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAcceptDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun RiskGaugeCard(
    strings: LocalizedStrings,
    language: AppLanguage,
    riskLevel: RiskLevel,
    riskScore: Int
) {
    val semantic = LocalMkulimaColors.current
    val gaugeColor = when (riskLevel) {
        RiskLevel.FAIBLE -> semantic.success
        RiskLevel.MOYEN -> semantic.warning
        RiskLevel.ELEVE -> MaterialTheme.colorScheme.error
    }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val sweepAnim = remember { Animatable(0f) }

    LaunchedEffect(riskScore) {
        sweepAnim.animateTo(
            targetValue = (riskScore.coerceIn(0, 100) / 100f) * 180f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(
                    modifier = Modifier
                        .size(width = 230.dp, height = 130.dp)
                ) {
                    val stroke = 24.dp.toPx()
                    val arcSize = Size(size.width - stroke, (size.width - stroke))
                    val topLeft = Offset(stroke / 2f, stroke / 2f)

                    drawArc(
                        color = trackColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = gaugeColor,
                        startAngle = 180f,
                        sweepAngle = sweepAnim.value,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$riskScore",
                        style = MaterialTheme.typography.displayMedium,
                        color = gaugeColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = AppStrings.riskLevelLabel(riskLevel, language),
                        style = MaterialTheme.typography.titleMedium,
                        color = gaugeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format(strings.scoreOutOf100, riskScore),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OfferMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CreditFinalConfirmedScreen(
    strings: LocalizedStrings,
    onGoHistory: () -> Unit,
    onGoHome: () -> Unit
) {
    BackHandler(onBack = onGoHome)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = strings.finalConfirmedTitle,
            tint = LocalMkulimaColors.current.success,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = strings.finalConfirmedTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = strings.finalConfirmedDesc,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onGoHistory,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("confirmed_go_history_button")
        ) {
            Text(strings.navHistory)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onGoHome,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(strings.navHome)
        }
    }
}

private fun buildShareableSummary(
    strings: LocalizedStrings,
    language: AppLanguage,
    request: CreditRequest
): String {
    val crop = AppStrings.cropLabel(request.data.crop, language)
    val risk = AppStrings.riskLevelLabel(request.riskLevel, language)
    val sb = StringBuilder()
    sb.appendLine("${strings.appName} - ${request.requestId}")
    sb.appendLine("${strings.offerAmount}: ${request.data.requestedAmountUsd} USD")
    sb.appendLine("${strings.offerDuration}: ${request.data.durationMonths} ${strings.monthsUnit}")
    sb.appendLine("${strings.offerMonthly}: ${request.monthlyInstallmentUsd} USD")
    sb.appendLine("$crop (${request.data.areaHectares} ${strings.hectaresUnit})")
    sb.appendLine("$risk (${request.riskScore}/100)")
    sb.appendLine()
    sb.appendLine(strings.harvestScheduleTitle)
    request.schedule.forEach { s ->
        sb.appendLine("- ${s.monthLabel}: ${s.amountUsd} USD (${s.note})")
    }
    return sb.toString()
}
