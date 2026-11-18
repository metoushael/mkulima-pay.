package com.aistudio.mkulimapay.rdcapp.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aistudio.mkulimapay.rdcapp.data.UiState
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.i18n.LocalizedStrings
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.CreditNotificationItem
import com.aistudio.mkulimapay.rdcapp.model.CreditRequest
import com.aistudio.mkulimapay.rdcapp.model.CreditStatus
import com.aistudio.mkulimapay.rdcapp.model.FinancialTransaction
import com.aistudio.mkulimapay.rdcapp.model.TransactionType
import com.aistudio.mkulimapay.rdcapp.ui.components.CreditNotificationsCard
import com.aistudio.mkulimapay.rdcapp.ui.components.OfflineModeBanner
import com.aistudio.mkulimapay.rdcapp.ui.components.SkeletonLoaderCard
import kotlinx.coroutines.launch

private enum class HistoryTab {
    REQUESTS,
    TRANSACTIONS
}

private enum class HistoryFilter {
    ALL,
    IN_PROGRESS,
    ACCEPTED,
    REJECTED
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    strings: LocalizedStrings,
    language: AppLanguage,
    requestsState: UiState<List<CreditRequest>>,
    transactionsState: UiState<List<FinancialTransaction>>,
    snackbarHostState: SnackbarHostState,
    isOnline: Boolean = true,
    creditNotifications: List<CreditNotificationItem> = emptyList(),
    lastSyncedAtMillis: Long = 0L,
    onOpenCreditResult: (CreditRequest) -> Unit,
    onRecordRepayment: (
        requestId: String,
        amountUsd: Int,
        channel: String,
        reference: String,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit
) {
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf(HistoryTab.REQUESTS) }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var showRepaymentDialog by remember { mutableStateOf(false) }

    val requests = (requestsState as? UiState.Success)?.data.orEmpty()
    val transactions = (transactionsState as? UiState.Success)?.data.orEmpty()
    val acceptedRequests = remember(requests) {
        requests.filter { it.status == CreditStatus.ACCEPTED }
    }

    val filteredRequests = remember(requests, selectedFilter) {
        when (selectedFilter) {
            HistoryFilter.ALL -> requests
            HistoryFilter.IN_PROGRESS -> requests.filter { it.status == CreditStatus.IN_PROGRESS }
            HistoryFilter.ACCEPTED -> requests.filter { it.status == CreditStatus.ACCEPTED }
            HistoryFilter.REJECTED -> requests.filter { it.status == CreditStatus.REJECTED }
        }
    }

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = strings.historyTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (!isOnline) {
            item {
                OfflineModeBanner(
                    strings = strings,
                    lastSyncedAtMillis = lastSyncedAtMillis
                )
            }
            if (creditNotifications.isNotEmpty()) {
                item {
                    CreditNotificationsCard(
                        strings = strings,
                        language = language,
                        notifications = creditNotifications,
                        isFromCache = true
                    )
                }
            }
        }

        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = activeTab == HistoryTab.REQUESTS,
                    onClick = { activeTab = HistoryTab.REQUESTS },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("history_tab_requests")
                ) {
                    Text("${strings.tabRequests} (${requests.size})")
                }
                SegmentedButton(
                    selected = activeTab == HistoryTab.TRANSACTIONS,
                    onClick = { activeTab = HistoryTab.TRANSACTIONS },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("history_tab_transactions")
                ) {
                    Text("${strings.tabTransactions} (${transactions.size})")
                }
            }
        }

        if (activeTab == HistoryTab.REQUESTS) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.ALL,
                        onClick = { selectedFilter = HistoryFilter.ALL },
                        label = { Text(strings.filterAll) },
                        modifier = Modifier.testTag("history_filter_all")
                    )
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.IN_PROGRESS,
                        onClick = { selectedFilter = HistoryFilter.IN_PROGRESS },
                        label = { Text(strings.filterInProgress) },
                        modifier = Modifier.testTag("history_filter_progress")
                    )
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.ACCEPTED,
                        onClick = { selectedFilter = HistoryFilter.ACCEPTED },
                        label = { Text(strings.filterAccepted) },
                        modifier = Modifier.testTag("history_filter_accepted")
                    )
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.REJECTED,
                        onClick = { selectedFilter = HistoryFilter.REJECTED },
                        label = { Text(strings.filterRejected) },
                        modifier = Modifier.testTag("history_filter_rejected")
                    )
                }
            }

            if (requestsState is UiState.Loading) {
                items(2) { SkeletonLoaderCard() }
            } else if (filteredRequests.isEmpty()) {
                item {
                    EmptyHistoryBox(
                        title = strings.historyEmptyTitle,
                        description = strings.historyEmptyDesc
                    )
                }
            } else {
                items(filteredRequests, key = { it.requestId }) { req ->
                    CreditRequestSummaryCard(
                        request = req,
                        language = language,
                        onClick = { onOpenCreditResult(req) }
                    )
                }
            }
        } else {
            // Transactions Tab
            item {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(strings.totalDisbursedLabel, style = MaterialTheme.typography.labelSmall)
                            Text("$totalDisbursed USD", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(strings.totalRepaidLabel, style = MaterialTheme.typography.labelSmall)
                            Text("$totalRepaid USD", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(strings.remainingBalanceLabel, style = MaterialTheme.typography.labelSmall)
                            Text(
                                "$remainingBalance USD",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (acceptedRequests.isNotEmpty() && remainingBalance > 0) {
                item {
                    Button(
                        onClick = { showRepaymentDialog = true },
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("history_record_repayment_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.recordRepaymentTitle)
                    }
                }
            }

            if (transactionsState is UiState.Loading) {
                items(2) { SkeletonLoaderCard() }
            } else if (transactions.isEmpty()) {
                item {
                    EmptyHistoryBox(
                        title = strings.transactionsEmptyTitle,
                        description = strings.transactionsEmptyDesc
                    )
                }
            } else {
                items(transactions, key = { it.txId }) { tx ->
                    TransactionSummaryCard(transaction = tx, language = language)
                }
            }
        }
    }

    if (showRepaymentDialog && acceptedRequests.isNotEmpty()) {
        RepaymentDialog(
            strings = strings,
            language = language,
            acceptedRequests = acceptedRequests,
            onDismiss = { showRepaymentDialog = false },
            onSubmit = { reqId, amountUsd, channel, reference ->
                showRepaymentDialog = false
                onRecordRepayment(
                    reqId,
                    amountUsd,
                    channel,
                    reference,
                    {
                        scope.launch {
                            snackbarHostState.showSnackbar(strings.paymentSavedToast)
                        }
                    },
                    { msg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                )
            }
        )
    }
}

@Composable
private fun EmptyHistoryBox(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 42.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepaymentDialog(
    strings: LocalizedStrings,
    language: AppLanguage,
    acceptedRequests: List<CreditRequest>,
    onDismiss: () -> Unit,
    onSubmit: (requestId: String, amountUsd: Int, channel: String, reference: String) -> Unit
) {
    var selectedRequest by remember { mutableStateOf(acceptedRequests.first()) }
    var reqExpanded by remember { mutableStateOf(false) }
    var selectedChannel by remember { mutableStateOf(AppStrings.paymentChannels.first()) }
    var channelExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf(selectedRequest.monthlyInstallmentUsd.toString()) }
    var referenceText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.recordRepaymentTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = reqExpanded,
                    onExpandedChange = { reqExpanded = !reqExpanded }
                ) {
                    OutlinedTextField(
                        value = "${AppStrings.cropLabel(selectedRequest.data.crop, language)} • ${selectedRequest.requestId}",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reqExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = reqExpanded,
                        onDismissRequest = { reqExpanded = false }
                    ) {
                        acceptedRequests.forEach { req ->
                            DropdownMenuItem(
                                text = {
                                    Text("${AppStrings.cropLabel(req.data.crop, language)} • ${req.data.requestedAmountUsd} USD")
                                },
                                onClick = {
                                    selectedRequest = req
                                    reqExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = channelExpanded,
                    onExpandedChange = { channelExpanded = !channelExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedChannel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.paymentChannelLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = channelExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = channelExpanded,
                        onDismissRequest = { channelExpanded = false }
                    ) {
                        AppStrings.paymentChannels.forEach { ch ->
                            DropdownMenuItem(
                                text = { Text(ch) },
                                onClick = {
                                    selectedChannel = ch
                                    channelExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw -> amountText = raw.filter { it.isDigit() }.take(6) },
                    label = { Text(strings.paymentAmountLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { referenceText = it.take(40) },
                    label = { Text(strings.paymentReferenceLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val amountVal = amountText.toIntOrNull() ?: 0
            val valid = amountVal >= 1 && referenceText.trim().length >= 2
            Button(
                onClick = {
                    onSubmit(
                        selectedRequest.requestId,
                        amountVal,
                        selectedChannel,
                        referenceText.trim()
                    )
                },
                enabled = valid
            ) {
                Text(strings.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
