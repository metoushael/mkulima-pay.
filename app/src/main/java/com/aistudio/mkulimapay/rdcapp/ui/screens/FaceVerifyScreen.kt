package com.aistudio.mkulimapay.rdcapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aistudio.mkulimapay.rdcapp.i18n.LocalizedStrings
import com.aistudio.mkulimapay.rdcapp.model.UserProfile
import com.aistudio.mkulimapay.rdcapp.services.FaceFailureReason
import com.aistudio.mkulimapay.rdcapp.ui.FaceVerifyOutcome
import com.aistudio.mkulimapay.rdcapp.ui.FaceVerifyUiState
import com.aistudio.mkulimapay.rdcapp.ui.components.BiometricOvalCameraCapture
import com.aistudio.mkulimapay.rdcapp.ui.theme.LocalMkulimaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceVerifyScreen(
    strings: LocalizedStrings,
    userProfile: UserProfile?,
    state: FaceVerifyUiState,
    onCaptureAndVerify: (String) -> Unit,
    onResetAttempt: () -> Unit,
    onGoToEnroll: () -> Unit,
    onGoToCreditAssistant: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val semantic = LocalMkulimaColors.current

    BackHandler(onBack = onBack)

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.faceVerifyTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("face_verify_back_button")
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (userProfile == null || !userProfile.hasEnrolledProfile) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = strings.faceVerifyNoProfile,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onGoToEnroll,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("face_verify_go_enroll_button")
                        ) {
                            Text(strings.actionEnrollTitle)
                        }
                    }
                }
            } else {
                when (state.outcome) {
                    FaceVerifyOutcome.IDLE -> {
                        Text(
                            text = if (state.isScanning) strings.faceVerifyScanning else strings.faceVerifyPrompt,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        BiometricOvalCameraCapture(
                            strings = strings,
                            capturedUri = if (state.isScanning) state.capturedUri else "",
                            isScanning = state.isScanning,
                            pulseOval = true,
                            primaryActionLabel = strings.simulateBlinkVerify,
                            onImageCaptured = onCaptureAndVerify,
                            onRetake = onResetAttempt
                        )
                    }

                    FaceVerifyOutcome.SUCCESS -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = strings.faceVerifySuccessTitle,
                            tint = semantic.success,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = strings.faceVerifySuccessTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = semantic.successContainer
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = String.format(
                                        strings.faceVerifyMatchScore,
                                        state.lastScore ?: 0
                                    ),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = semantic.onSuccessContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.faceVerifyLivenessOk,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = semantic.onSuccessContainer
                                )
                                Text(
                                    text = strings.faceVerifyDemoNotice,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = semantic.onSuccessContainer.copy(alpha = 0.72f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Button(
                            onClick = onGoToCreditAssistant,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("face_verify_go_credit_button")
                        ) {
                            Icon(Icons.Rounded.Payments, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.actionRequestCreditTitle)
                        }
                        OutlinedButton(
                            onClick = onBack,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(strings.navHome)
                        }
                    }

                    FaceVerifyOutcome.FAILURE, FaceVerifyOutcome.MAX_ATTEMPTS -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = strings.faceVerifyFailTitle,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(52.dp)
                        )
                        Text(
                            text = strings.faceVerifyFailTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        state.lastScore?.let { sc ->
                            Text(
                                text = String.format(strings.faceVerifyMatchScore, sc),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
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
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val failureDetail = if (state.outcome == FaceVerifyOutcome.MAX_ATTEMPTS) {
                                    strings.faceVerifyMaxAttempts
                                } else {
                                    when (state.failureReason) {
                                        FaceFailureReason.NO_FACE -> strings.faceVerifyFailNoFace
                                        FaceFailureReason.MULTIPLE_FACES -> strings.faceVerifyFailMultipleFaces
                                        FaceFailureReason.EYES_CLOSED -> strings.faceVerifyFailEyesClosed
                                        FaceFailureReason.MOCK_FORCED_FAILURE -> strings.faceVerifyFailMockMode
                                        else -> strings.faceVerifyFailTips
                                    }
                                }
                                Text(
                                    text = failureDetail,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (state.outcome == FaceVerifyOutcome.FAILURE) {
                                    Text(
                                        text = String.format(
                                            strings.faceVerifyAttemptsLeft,
                                            state.attemptsRemaining
                                        ),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = strings.faceVerifyDemoNotice,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                                )
                            }
                        }

                        if (state.outcome == FaceVerifyOutcome.FAILURE) {
                            Button(
                                onClick = onResetAttempt,
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("face_verify_retry_button")
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.retry)
                            }
                        } else {
                            Button(
                                onClick = onGoToEnroll,
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text(strings.actionEnrollTitle)
                            }
                        }
                    }
                }
            }
        }
    }
}
