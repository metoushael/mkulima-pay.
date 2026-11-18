package com.aistudio.mkulimapay.rdcapp.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Agriculture
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.i18n.LocalizedStrings
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.ChatMessage
import com.aistudio.mkulimapay.rdcapp.model.ChatWidgetType
import com.aistudio.mkulimapay.rdcapp.model.CreditPurpose
import com.aistudio.mkulimapay.rdcapp.model.CropType
import com.aistudio.mkulimapay.rdcapp.model.IdentityStatus
import com.aistudio.mkulimapay.rdcapp.model.UserProfile
import com.aistudio.mkulimapay.rdcapp.ui.CreditAssistantUiState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CreditAssistantScreen(
    strings: LocalizedStrings,
    language: AppLanguage,
    userProfile: UserProfile?,
    state: CreditAssistantUiState,
    snackbarHostState: SnackbarHostState,
    onStartSession: () -> Unit,
    onSelectCrop: (CropType) -> Unit,
    onSubmitArea: (Float) -> Unit,
    onSelectHarvestMonth: (Int) -> Unit,
    onSubmitExperience: (Int) -> Unit,
    onSubmitIncome: (Int) -> Unit,
    onSubmitAmount: (Int) -> Unit,
    onSubmitDuration: (Int) -> Unit,
    onSelectPurpose: (CreditPurpose) -> Unit,
    onSendText: (String) -> Unit,
    onCalculateResult: () -> Unit,
    onNavigateFaceVerify: () -> Unit,
    onNavigateEnroll: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var textInput by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    // Initialisation automatique du chat si vide
    LaunchedEffect(userProfile?.identityStatus, state.messages.isEmpty()) {
        if (userProfile?.identityStatus == IdentityStatus.VERIFIED && state.messages.isEmpty()) {
            onStartSession()
        }
    }

    // Déclenche le calcul final à la question 8
    LaunchedEffect(state.questionIndex, state.isCalculating, state.errorMessage) {
        if (state.questionIndex == 8 && state.isCalculating && state.errorMessage == null) {
            onCalculateResult()
        }
    }

    // Scroll automatique vers le dernier message
    LaunchedEffect(state.messages.size, state.isTyping) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Reconnaissance vocale Android (fr-FR / sw-KE)
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val audioPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && speechRecognizer != null) {
            startVoiceListening(
                speechRecognizer = speechRecognizer,
                language = language,
                onStart = { isListening = true },
                onResult = { spoken ->
                    isListening = false
                    textInput = spoken
                },
                onError = { isListening = false }
            )
        }
    }

    if (userProfile?.identityStatus != IdentityStatus.VERIFIED) {
        val hasEnrolled = userProfile?.hasEnrolledProfile == true
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = strings.assistantNeedsVerificationTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.assistantNeedsVerificationBody,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = if (!hasEnrolled) onNavigateEnroll else onNavigateFaceVerify,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("assistant_verify_gate_button")
            ) {
                Icon(Icons.Rounded.LockOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (!hasEnrolled) strings.actionEnrollTitle else strings.assistantGoVerify
                )
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête de progression
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.assistantTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(
                                strings.assistantQuestionProgress,
                                (state.questionIndex + 1).coerceAtMost(8),
                                8
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onStartSession,
                        modifier = Modifier.testTag("assistant_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Recommencer"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (state.questionIndex.coerceAtMost(8)) / 8f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }

        // Flux de discussion
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(state.messages, key = { _, m -> m.id }) { index, msg ->
                val isLatestInteractive =
                    index == state.messages.lastIndex && msg.isFromAssistant && !state.isTyping
                ChatBubbleItem(
                    message = msg,
                    showWidget = isLatestInteractive,
                    strings = strings,
                    language = language,
                    state = state,
                    onSelectCrop = onSelectCrop,
                    onSubmitArea = onSubmitArea,
                    onSelectHarvestMonth = onSelectHarvestMonth,
                    onSubmitExperience = onSubmitExperience,
                    onSubmitIncome = onSubmitIncome,
                    onSubmitAmount = onSubmitAmount,
                    onSubmitDuration = onSubmitDuration,
                    onSelectPurpose = onSelectPurpose
                )
            }

            if (state.isTyping) {
                item {
                    TypingIndicatorBubble()
                }
            }

            if (state.isCalculating && state.errorMessage == null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.assistantCalculating,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (state.errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onCalculateResult) {
                                Text(strings.retry)
                            }
                        }
                    }
                }
            }
        }

        // Barre de saisie + micro
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (isListening) {
                    Text(
                        text = strings.assistantMicListening,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (speechRecognizer == null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(strings.assistantMicUnavailable)
                                }
                            } else {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    startVoiceListening(
                                        speechRecognizer = speechRecognizer,
                                        language = language,
                                        onStart = { isListening = true },
                                        onResult = { spoken ->
                                            isListening = false
                                            textInput = spoken
                                        },
                                        onError = { isListening = false }
                                    )
                                } else {
                                    audioPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("assistant_mic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = strings.assistantMicHint,
                            tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text(strings.assistantInputHint) },
                        singleLine = true,
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("assistant_text_input")
                    )

                    FilledIconButton(
                        onClick = {
                            val msg = textInput.trim()
                            if (msg.isNotEmpty()) {
                                textInput = ""
                                onSendText(msg)
                            }
                        },
                        enabled = textInput.isNotBlank() && !state.isTyping && !state.isCalculating,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("assistant_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = strings.send
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    showWidget: Boolean,
    strings: LocalizedStrings,
    language: AppLanguage,
    state: CreditAssistantUiState,
    onSelectCrop: (CropType) -> Unit,
    onSubmitArea: (Float) -> Unit,
    onSelectHarvestMonth: (Int) -> Unit,
    onSubmitExperience: (Int) -> Unit,
    onSubmitIncome: (Int) -> Unit,
    onSubmitAmount: (Int) -> Unit,
    onSubmitDuration: (Int) -> Unit,
    onSelectPurpose: (CreditPurpose) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromAssistant) Alignment.Start else Alignment.End
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (message.isFromAssistant) {
                Icon(
                    imageVector = Icons.Rounded.SupportAgent,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(22.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = if (message.isFromAssistant) 6.dp else 16.dp,
                    topEnd = if (message.isFromAssistant) 16.dp else 6.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (message.isFromAssistant) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.widthIn(max = 290.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.isFromAssistant) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (message.isFromAssistant && message.isOfflineFallback) {
                        Text(
                            text = strings.aiOfflineFallbackNotice,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isFromAssistant) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                        },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        if (showWidget && message.widgetType != ChatWidgetType.NONE) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.padding(start = 30.dp)) {
                InlineChatWidget(
                    widgetType = message.widgetType,
                    strings = strings,
                    language = language,
                    state = state,
                    onSelectCrop = onSelectCrop,
                    onSubmitArea = onSubmitArea,
                    onSelectHarvestMonth = onSelectHarvestMonth,
                    onSubmitExperience = onSubmitExperience,
                    onSubmitIncome = onSubmitIncome,
                    onSubmitAmount = onSubmitAmount,
                    onSubmitDuration = onSubmitDuration,
                    onSelectPurpose = onSelectPurpose
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InlineChatWidget(
    widgetType: ChatWidgetType,
    strings: LocalizedStrings,
    language: AppLanguage,
    state: CreditAssistantUiState,
    onSelectCrop: (CropType) -> Unit,
    onSubmitArea: (Float) -> Unit,
    onSelectHarvestMonth: (Int) -> Unit,
    onSubmitExperience: (Int) -> Unit,
    onSubmitIncome: (Int) -> Unit,
    onSubmitAmount: (Int) -> Unit,
    onSubmitDuration: (Int) -> Unit,
    onSelectPurpose: (CreditPurpose) -> Unit
) {
    when (widgetType) {
        ChatWidgetType.CROP_SELECTOR -> {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CropType.entries.forEach { crop ->
                    AssistChip(
                        onClick = { onSelectCrop(crop) },
                        label = { Text(AppStrings.cropLabel(crop, language)) },
                        leadingIcon = {
                            Icon(
                                imageVector = cropIcon(crop),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("crop_chip_${crop.name}")
                    )
                }
            }
        }

        ChatWidgetType.AREA_SLIDER -> {
            var area by remember { mutableFloatStateOf(state.intake.areaHectares) }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "${String.format(Locale.US, "%.2f", area)} ${strings.hectaresUnit}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = area,
                        onValueChange = { area = it },
                        valueRange = 0.25f..10.0f,
                        modifier = Modifier.testTag("area_slider")
                    )
                    Button(
                        onClick = { onSubmitArea(area) },
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("area_confirm_button")
                    ) {
                        Text(strings.validate)
                    }
                }
            }
        }

        ChatWidgetType.HARVEST_MONTH_SELECTOR -> {
            val months = AppStrings.months(language)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                months.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = state.intake.harvestMonthIndex == idx,
                        onClick = { onSelectHarvestMonth(idx) },
                        label = { Text(label.take(4)) },
                        modifier = Modifier.testTag("harvest_month_$idx")
                    )
                }
            }
        }

        ChatWidgetType.EXPERIENCE_SELECTOR -> {
            val presets = listOf(1, 2, 4, 7, 10)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { yrs ->
                    AssistChip(
                        onClick = { onSubmitExperience(yrs) },
                        label = { Text("$yrs ${strings.yearsUnit}") },
                        modifier = Modifier.testTag("experience_chip_$yrs")
                    )
                }
            }
        }

        ChatWidgetType.INCOME_INPUT -> {
            var incomeText by remember { mutableStateOf(state.intake.monthlyIncomeUsd.toString()) }
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
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(60, 120, 200, 350).forEach { v ->
                            AssistChip(
                                onClick = { onSubmitIncome(v) },
                                label = { Text("$v USD") },
                                modifier = Modifier.testTag("income_preset_$v")
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = incomeText,
                            onValueChange = { raw -> incomeText = raw.filter { it.isDigit() }.take(5) },
                            label = { Text(strings.usdPerMonth) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val parsed = incomeText.toIntOrNull() ?: 120
                                onSubmitIncome(parsed)
                            },
                            shape = CircleShape,
                            modifier = Modifier.testTag("income_confirm_button")
                        ) {
                            Text(strings.validate)
                        }
                    }
                }
            }
        }

        ChatWidgetType.AMOUNT_SLIDER -> {
            var amount by remember { mutableIntStateOf(state.intake.requestedAmountUsd) }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "$amount USD",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = amount.toFloat(),
                        onValueChange = { amount = ((it / 25f).roundToInt() * 25).coerceIn(50, 1500) },
                        valueRange = 50f..1500f,
                        modifier = Modifier.testTag("amount_slider")
                    )
                    Button(
                        onClick = { onSubmitAmount(amount) },
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("amount_confirm_button")
                    ) {
                        Text(strings.validate)
                    }
                }
            }
        }

        ChatWidgetType.DURATION_SELECTOR -> {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(3, 6, 9, 12).forEach { months ->
                    AssistChip(
                        onClick = { onSubmitDuration(months) },
                        label = { Text("$months ${strings.monthsUnit}") },
                        modifier = Modifier.testTag("duration_chip_$months")
                    )
                }
            }
        }

        ChatWidgetType.PURPOSE_SELECTOR -> {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreditPurpose.entries.forEach { purpose ->
                    AssistChip(
                        onClick = { onSelectPurpose(purpose) },
                        label = { Text(AppStrings.purposeLabel(purpose, language)) },
                        leadingIcon = {
                            Icon(
                                imageVector = purposeIcon(purpose),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("purpose_chip_${purpose.name}")
                    )
                }
            }
        }

        ChatWidgetType.NONE -> Unit
    }
}

@Composable
private fun TypingIndicatorBubble() {
    val infinite = rememberInfiniteTransition(label = "typing")
    val a1 by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "d1"
    )
    val a2 by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450, 150, FastOutSlowInEasing), RepeatMode.Reverse),
        label = "d2"
    )
    val a3 by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450, 300, FastOutSlowInEasing), RepeatMode.Reverse),
        label = "d3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.SupportAgent,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(a1, a2, a3).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(alpha)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

private fun cropIcon(crop: CropType): ImageVector = when (crop) {
    CropType.MAIS -> Icons.Rounded.Grain
    CropType.MANIOC -> Icons.Rounded.Spa
    CropType.HARICOT -> Icons.Rounded.Eco
    CropType.RIZ -> Icons.Rounded.Grain
    CropType.CAFE -> Icons.Rounded.LocalCafe
    CropType.BANANE -> Icons.Rounded.Agriculture
    CropType.AUTRE -> Icons.Rounded.Eco
}

private fun purposeIcon(purpose: CreditPurpose): ImageVector = when (purpose) {
    CreditPurpose.SEMENCES -> Icons.Rounded.Eco
    CreditPurpose.OUTILS -> Icons.Rounded.Build
    CreditPurpose.ENGRAIS -> Icons.Rounded.Science
    CreditPurpose.STOCKAGE -> Icons.Rounded.Inventory2
    CreditPurpose.MAIN_OEUVRE -> Icons.Rounded.Groups
}

private fun startVoiceListening(
    speechRecognizer: SpeechRecognizer,
    language: AppLanguage,
    onStart: () -> Unit,
    onResult: (String) -> Unit,
    onError: () -> Unit
) {
    val localeTag = if (language == AppLanguage.FR) "fr-FR" else "sw-KE"
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
    }
    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = onStart()
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) = onError()
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            onResult(matches?.firstOrNull().orEmpty())
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })
    runCatching {
        speechRecognizer.startListening(intent)
    }.onFailure {
        onError()
    }
}
