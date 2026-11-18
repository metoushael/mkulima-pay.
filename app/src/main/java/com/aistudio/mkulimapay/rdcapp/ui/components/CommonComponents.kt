package com.aistudio.mkulimapay.rdcapp.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.i18n.LocalizedStrings
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.CreditNotificationItem
import com.aistudio.mkulimapay.rdcapp.model.CreditNotificationType
import com.aistudio.mkulimapay.rdcapp.model.CreditStatus
import com.aistudio.mkulimapay.rdcapp.model.IdentityStatus
import com.aistudio.mkulimapay.rdcapp.ui.theme.LocalMkulimaColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Logo vectoriel Mkulima Pay : feuille agricole + pièce de monnaie dorée.
 */
@Composable
fun MkulimaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val primary = MaterialTheme.colorScheme.primary
    val gold = LocalMkulimaColors.current.goldAccent
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(primary),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val w = this.size.width
            val h = this.size.height

            // Pièce de monnaie dorée en arrière-plan droit
            drawCircle(
                color = gold,
                radius = w * 0.28f,
                center = Offset(w * 0.65f, h * 0.42f)
            )
            drawCircle(
                color = onPrimary.copy(alpha = 0.35f),
                radius = w * 0.19f,
                center = Offset(w * 0.65f, h * 0.42f),
                style = Stroke(width = 2.5f)
            )

            // Feuille stylisée en premier plan gauche
            val leafPath = Path().apply {
                moveTo(w * 0.16f, h * 0.78f)
                cubicTo(w * 0.12f, h * 0.34f, w * 0.46f, h * 0.18f, w * 0.64f, h * 0.22f)
                cubicTo(w * 0.68f, h * 0.56f, w * 0.44f, h * 0.80f, w * 0.16f, h * 0.78f)
                close()
            }
            drawPath(leafPath, color = onPrimary)
            drawLine(
                color = primary,
                start = Offset(w * 0.18f, h * 0.76f),
                end = Offset(w * 0.54f, h * 0.34f),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun LanguageToggleButton(
    language: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("language_toggle_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Translate,
                contentDescription = "Changer de langue",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = language.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun IdentityStatusChip(
    status: IdentityStatus,
    language: AppLanguage
) {
    val semantic = LocalMkulimaColors.current
    val (fg, icon) = when (status) {
        IdentityStatus.VERIFIED -> semantic.success to Icons.Rounded.CheckCircle
        IdentityStatus.PENDING -> semantic.warning to Icons.Rounded.Schedule
        IdentityStatus.UNVERIFIED -> MaterialTheme.colorScheme.error to Icons.Rounded.ErrorOutline
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = AppStrings.identityStatusLabel(status, language),
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CreditStatusChip(
    status: CreditStatus,
    language: AppLanguage
) {
    val semantic = LocalMkulimaColors.current
    val (fg, icon) = when (status) {
        CreditStatus.ACCEPTED -> semantic.success to Icons.Rounded.CheckCircle
        CreditStatus.IN_PROGRESS -> semantic.warning to Icons.Rounded.Schedule
        CreditStatus.REJECTED -> MaterialTheme.colorScheme.error to Icons.Rounded.ErrorOutline
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = AppStrings.creditStatusLabel(status, language),
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SkeletonLoaderCard(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "skeleton")
    val alpha by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(placeholderColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(placeholderColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(placeholderColor)
            )
        }
    }
}

/**
 * Composant biométrique réel déléguant à [CameraView] (caméra frontale par défaut)
 * dans un cadre ovale animé, avec prévisualisation plein écran ([FullScreenCameraDialog])
 * et repli zéro-permission sur le Photo Picker Android.
 */
@Composable
fun BiometricOvalCameraCapture(
    strings: LocalizedStrings,
    capturedUri: String,
    isScanning: Boolean,
    pulseOval: Boolean,
    primaryActionLabel: String,
    onImageCaptured: (String) -> Unit,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFullScreenCamera by remember { mutableStateOf(false) }
    var triggerCaptureRef by remember { mutableStateOf<(() -> Unit)?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageCaptured(uri.toString())
        }
    }

    val infinite = rememberInfiniteTransition(label = "ovalPulse")
    val pulseFraction by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val scanY by infinite.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = LocalMkulimaColors.current.goldAccent

    var cameraUnavailableWarning by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .aspectRatio(0.82f)
                .clip(RoundedCornerShape(140.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            if (capturedUri.isNotBlank()) {
                AsyncImage(
                    model = Uri.parse(capturedUri),
                    contentDescription = "Selfie capturé",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CameraView(
                    modifier = Modifier.fillMaxSize(),
                    initialLensFacing = CameraSelector.LENS_FACING_FRONT,
                    permissionTitle = strings.cameraPermissionTitle,
                    permissionActionLabel = strings.cameraPermissionBtn,
                    onImageCaptured = { uri ->
                        cameraUnavailableWarning = false
                        onImageCaptured(uri.toString())
                    },
                    onCameraUnavailable = {
                        cameraUnavailableWarning = true
                    },
                    onError = {
                        cameraUnavailableWarning = true
                    }
                ) { capturePhoto, _, _ ->
                    LaunchedEffect(capturePhoto) {
                        triggerCaptureRef = capturePhoto
                    }
                }
            }

            // Cadre ovale et ligne de scan pendant l'analyse
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = if (isScanning) 6f + pulseFraction * 4f else 5f
                val borderColor = if (isScanning) goldColor else primaryColor
                drawOval(
                    color = borderColor,
                    topLeft = Offset(6f, 6f),
                    size = Size(size.width - 12f, size.height - 12f),
                    style = Stroke(width = strokeWidth)
                )
                if (isScanning) {
                    val lineY = size.height * scanY
                    drawLine(
                        color = goldColor,
                        start = Offset(size.width * 0.14f, lineY),
                        end = Offset(size.width * 0.86f, lineY),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        if (cameraUnavailableWarning && capturedUri.isBlank()) {
            Text(
                text = strings.cameraUnavailableError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Boutons Capture / Plein écran / Importer / Reprendre
        if (capturedUri.isBlank() || isScanning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val trigger = triggerCaptureRef
                        if (trigger != null && !cameraUnavailableWarning) {
                            trigger.invoke()
                        } else {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    },
                    enabled = !isScanning,
                    shape = CircleShape,
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("capture_selfie_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(primaryActionLabel)
                }

                FilledTonalButton(
                    onClick = { showFullScreenCamera = true },
                    enabled = !isScanning,
                    shape = CircleShape,
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("open_fullscreen_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = strings.openFullScreenCamera,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(strings.openFullScreenCamera)
                }
            }

            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = !isScanning,
                shape = CircleShape,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("import_selfie_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(strings.importPhotoFallback)
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRetake,
                    shape = CircleShape,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("retake_selfie_button")
                ) {
                    Text(strings.retake)
                }
            }
        }
    }

    if (showFullScreenCamera) {
        FullScreenCameraDialog(
            strings = strings,
            instructionText = strings.selfieGuideFrame,
            onDismiss = { showFullScreenCamera = false },
            onImageCaptured = onImageCaptured
        )
    }
}

@Composable
fun OfflineModeBanner(
    strings: LocalizedStrings,
    lastSyncedAtMillis: Long,
    modifier: Modifier = Modifier
) {
    val dateFmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_mode_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = strings.offlineBannerTitle,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(22.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = strings.offlineBannerTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = strings.offlineBannerDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                )
                if (lastSyncedAtMillis > 0L) {
                    Text(
                        text = String.format(
                            strings.lastSyncedLabel,
                            dateFmt.format(Date(lastSyncedAtMillis))
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f)
                    )
                }
            }
        }
    }
}

@Composable
fun CreditNotificationsCard(
    strings: LocalizedStrings,
    language: AppLanguage,
    notifications: List<CreditNotificationItem>,
    isFromCache: Boolean,
    modifier: Modifier = Modifier
) {
    val semantic = LocalMkulimaColors.current
    val dateFmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("credit_notifications_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = strings.creditNotificationsTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isFromCache) {
                    Text(
                        text = strings.cachedLocallyBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (notifications.isEmpty()) {
                Text(
                    text = strings.creditNotificationsEmpty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val shown = notifications.take(4)
                shown.forEachIndexed { index, item ->
                    val (icon, tint) = when (item.type) {
                        CreditNotificationType.CREDIT_ACCEPTED -> Icons.Rounded.CheckCircle to semantic.success
                        CreditNotificationType.CREDIT_IN_PROGRESS -> Icons.Rounded.Schedule to semantic.warning
                        CreditNotificationType.CREDIT_REJECTED -> Icons.Rounded.ErrorOutline to MaterialTheme.colorScheme.error
                        CreditNotificationType.DISBURSEMENT -> Icons.Rounded.ArrowDownward to MaterialTheme.colorScheme.primary
                        CreditNotificationType.REPAYMENT -> Icons.Rounded.ArrowUpward to semantic.success
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("credit_notification_item_${item.id}"),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.localizedTitle(language),
                            tint = tint,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(20.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = item.localizedTitle(language),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = item.localizedMessage(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (item.timestampMillis > 0L) {
                            Text(
                                text = dateFmt.format(Date(item.timestampMillis)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (index < shown.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

