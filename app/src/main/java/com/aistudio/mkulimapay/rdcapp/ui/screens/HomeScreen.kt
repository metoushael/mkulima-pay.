package com.aistudio.mkulimapay.rdcapp.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Agriculture
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aistudio.mkulimapay.rdcapp.data.UiState
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.i18n.LocalizedStrings
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.CreditNotificationItem
import com.aistudio.mkulimapay.rdcapp.model.CreditRequest
import com.aistudio.mkulimapay.rdcapp.model.FinancialTransaction
import com.aistudio.mkulimapay.rdcapp.model.IdentityStatus
import com.aistudio.mkulimapay.rdcapp.model.TransactionType
import com.aistudio.mkulimapay.rdcapp.model.UserProfile
import com.aistudio.mkulimapay.rdcapp.ui.components.CreditNotificationsCard
import com.aistudio.mkulimapay.rdcapp.ui.components.CreditStatusChip
import com.aistudio.mkulimapay.rdcapp.ui.components.IdentityStatusChip
import com.aistudio.mkulimapay.rdcapp.ui.components.LanguageToggleButton
import com.aistudio.mkulimapay.rdcapp.ui.components.MkulimaLogo
import com.aistudio.mkulimapay.rdcapp.ui.components.OfflineModeBanner
import com.aistudio.mkulimapay.rdcapp.ui.components.SkeletonLoaderCard
import com.aistudio.mkulimapay.rdcapp.ui.theme.LocalMkulimaColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    strings: LocalizedStrings,
    language: AppLanguage,
    currentUserEmail: String?,
    profileState: UiState<UserProfile?>,
    creditRequestsState: UiState<List<CreditRequest>>,
    transactionsState: UiState<List<FinancialTransaction>>,
    isOnline: Boolean = true,
    creditNotifications: List<CreditNotificationItem> = emptyList(),
    lastSyncedAtMillis: Long = 0L,
    onToggleLanguage: () -> Unit,
    onNavigateEnroll: () -> Unit,
    onNavigateFaceVerify: () -> Unit,
    onNavigateCreditAssistant: () -> Unit,
    onOpenCreditResult: (CreditRequest) -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val userProfile = (profileState as? UiState.Success)?.data
    val creditRequests = (creditRequestsState as? UiState.Success)?.data.orEmpty()
    val transactions = (transactionsState as? UiState.Success)?.data.orEmpty()

    val identityStatus = userProfile?.identityStatus ?: IdentityStatus.UNVERIFIED
    val isVerified = identityStatus == IdentityStatus.VERIFIED
    val hasProfile = userProfile != null && userProfile.hasEnrolledProfile

    val totalDisbursed = remember(transactions) {
        transactions.filter { it.type == TransactionType.DISBURSEMENT }.sumOf { it.amountUsd }
    }
    val totalRepaid = remember(transactions) {
        transactions.filter { it.type == TransactionType.REPAYMENT }.sumOf { it.amountUsd }
    }
    val remainingBalance = (totalDisbursed - totalRepaid).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top App Bar avec nom réel ou état sans profil
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val selfieUri = userProfile?.localSelfieUri
                    if (!selfieUri.isNullOrBlank()) {
                        Surface(
                            onClick = onNavigateProfile,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("home_avatar_button")
                        ) {
                            AsyncImage(
                                model = Uri.parse(selfieUri),
                                contentDescription = strings.navProfile,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        MkulimaLogo(
                            size = 48.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(onClick = onNavigateProfile)
                                .testTag("home_avatar_button")
                        )
                    }
                    Column {
                        Text(
                            text = if (hasProfile) {
                                "${strings.greetingPrefix} ${userProfile.prenom}"
                            } else {
                                strings.noProfileRegisteredTitle
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (hasProfile) {
                                "${userProfile.formattedLocation} • ${AppStrings.sectorLabel(userProfile.agriculturalSector, language)}"
                            } else {
                                currentUserEmail ?: strings.appName
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                LanguageToggleButton(language = language, onToggle = onToggleLanguage)
            }
        }

        // Bandeau mode hors ligne (DataStore)
        if (!isOnline) {
            item {
                OfflineModeBanner(
                    strings = strings,
                    lastSyncedAtMillis = lastSyncedAtMillis
                )
            }
        }

        // Erreur réseau / Firestore éventuelle (masquée si hors ligne avec cache local disponible)
        val hasOfflineBackup = !isOnline && (userProfile != null || creditNotifications.isNotEmpty())
        if (!hasOfflineBackup &&
            (profileState is UiState.Error || creditRequestsState is UiState.Error || transactionsState is UiState.Error)
        ) {
            val errorMsg = (profileState as? UiState.Error)?.message
                ?: (creditRequestsState as? UiState.Error)?.message
                ?: (transactionsState as? UiState.Error)?.message
                ?: strings.halisiErrorNetwork
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Carte Statut Identité réelle (ou cache local hors ligne)
        item {
            if (profileState is UiState.Loading) {
                SkeletonLoaderCard()
            } else {
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.identityCardTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IdentityStatusChip(
                                status = identityStatus,
                                language = language
                            )
                        }
                        val statusDesc = when (identityStatus) {
                            IdentityStatus.UNVERIFIED -> strings.identityCardDescUnverified
                            IdentityStatus.PENDING -> strings.identityCardDescPending
                            IdentityStatus.VERIFIED -> strings.identityCardDescVerified
                        }
                        Text(
                            text = statusDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Dernières notifications de crédit (live ou cache DataStore hors ligne)
        if (creditNotifications.isNotEmpty() || !isOnline) {
            item {
                CreditNotificationsCard(
                    strings = strings,
                    language = language,
                    notifications = creditNotifications,
                    isFromCache = !isOnline
                )
            }
        }

        // Solde & métriques réelles si transactions existantes
        if (transactions.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LedgerMetric(
                            label = strings.totalDisbursedLabel,
                            value = "$totalDisbursed USD",
                            modifier = Modifier.weight(1f)
                        )
                        LedgerMetric(
                            label = strings.totalRepaidLabel,
                            value = "$totalRepaid USD",
                            modifier = Modifier.weight(1f)
                        )
                        LedgerMetric(
                            label = strings.remainingBalanceLabel,
                            value = "$remainingBalance USD",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3 Grandes cartes d'action
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionCardItem(
                    title = if (hasProfile) strings.enrollTitle else strings.createAccountCta,
                    subtitle = strings.actionEnrollSub,
                    icon = Icons.Rounded.PersonAdd,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    enabled = true,
                    testTag = "home_enroll_card",
                    onClick = onNavigateEnroll
                )
                ActionCardItem(
                    title = strings.actionFaceLoginTitle,
                    subtitle = strings.actionFaceLoginSub,
                    icon = Icons.Rounded.LockOpen,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    enabled = hasProfile,
                    testTag = "home_face_verify_card",
                    onClick = onNavigateFaceVerify
                )
                ActionCardItem(
                    title = strings.actionRequestCreditTitle,
                    subtitle = if (isVerified) strings.actionRequestCreditSub else strings.actionCreditDisabledHint,
                    icon = Icons.Rounded.Payments,
                    containerColor = if (isVerified) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (isVerified) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    enabled = isVerified,
                    testTag = "home_request_credit_card",
                    onClick = onNavigateCreditAssistant
                )
            }
        }

        // Section Mes demandes réelles
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.myRequestsTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${creditRequests.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (creditRequestsState is UiState.Loading) {
            item { SkeletonLoaderCard() }
        } else if (creditRequests.isEmpty()) {
            item {
                EmptyNoticeCard(text = strings.emptyRequestsHome)
            }
        } else {
            items(creditRequests.take(3), key = { it.requestId }) { req ->
                CreditRequestSummaryCard(
                    request = req,
                    language = language,
                    onClick = { onOpenCreditResult(req) }
                )
            }
        }

        // Section Transactions réelles
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.recentTransactionsTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    onClick = onNavigateHistory,
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Text(
                        text = "${transactions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (transactionsState is UiState.Loading) {
            item { SkeletonLoaderCard() }
        } else if (transactions.isEmpty()) {
            item {
                EmptyNoticeCard(text = strings.emptyTransactionsText)
            }
        } else {
            items(transactions.take(3), key = { it.txId }) { tx ->
                TransactionSummaryCard(transaction = tx, language = language)
            }
        }
    }
}

@Composable
private fun LedgerMetric(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyNoticeCard(text: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionCardItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val isSolidPrimary = containerColor == MaterialTheme.colorScheme.primary
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = if (isSolidPrimary) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (enabled && isSolidPrimary) 1.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.72f)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.88f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = contentColor
            )
        }
    }
}

@Composable
fun CreditRequestSummaryCard(
    request: CreditRequest,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val cropLabel = AppStrings.cropLabel(request.data.crop, language)
    val dateText = if (request.createdAtMillis > 0L) {
        dateFmt.format(Date(request.createdAtMillis))
    } else {
        "-"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("credit_request_card_${request.requestId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Agriculture,
                contentDescription = cropLabel,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$cropLabel • ${request.data.requestedAmountUsd} USD",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${request.requestId} • $dateText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CreditStatusChip(status = request.status, language = language)
        }
    }
}

@Composable
fun TransactionSummaryCard(
    transaction: FinancialTransaction,
    language: AppLanguage
) {
    val semantic = LocalMkulimaColors.current
    val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateText = if (transaction.createdAtMillis > 0L) {
        dateFmt.format(Date(transaction.createdAtMillis))
    } else {
        "-"
    }
    val isDisbursement = transaction.type == TransactionType.DISBURSEMENT
    val icon = if (isDisbursement) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward
    val tint = if (isDisbursement) MaterialTheme.colorScheme.primary else semantic.success
    val prefix = if (isDisbursement) "+" else "-"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_card_${transaction.txId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = AppStrings.transactionTypeLabel(transaction.type, language),
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${AppStrings.transactionTypeLabel(transaction.type, language)} • ${transaction.channel}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${transaction.reference} • $dateText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$prefix${transaction.amountUsd} USD",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = tint
                )
                Text(
                    text = AppStrings.transactionStatusLabel(transaction.status, language),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
