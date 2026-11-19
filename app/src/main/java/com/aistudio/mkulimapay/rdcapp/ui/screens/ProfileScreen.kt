package com.aistudio.mkulimapay.rdcapp.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.aistudio.mkulimapay.rdcapp.data.UiState
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.i18n.LocalizedStrings
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.AppSettings
import com.aistudio.mkulimapay.rdcapp.model.IdentityStatus
import com.aistudio.mkulimapay.rdcapp.model.ThemePreference
import com.aistudio.mkulimapay.rdcapp.model.UserProfile
import com.aistudio.mkulimapay.rdcapp.services.MockConfig
import com.aistudio.mkulimapay.rdcapp.services.MockMode
import com.aistudio.mkulimapay.rdcapp.ui.components.FixedHeaderBar
import com.aistudio.mkulimapay.rdcapp.ui.components.IdentityStatusChip
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    strings: LocalizedStrings,
    settings: AppSettings,
    currentUserEmail: String?,
    profileState: UiState<UserProfile?>,
    snackbarHostState: SnackbarHostState,
    isOnline: Boolean = true,
    lastSyncedAtMillis: Long = 0L,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetTheme: (ThemePreference) -> Unit,
    onSetLargeText: (Boolean) -> Unit,
    onSetNotifications: (Boolean) -> Unit,
    onNavigateEnroll: () -> Unit,
    onDeleteAllData: (onDone: (Boolean) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var mockMode by remember { mutableStateOf(MockConfig.mode) }

    val userProfile = (profileState as? UiState.Success)?.data
    val hasProfile = userProfile != null && userProfile.hasEnrolledProfile

    Column(modifier = Modifier.fillMaxSize()) {
        FixedHeaderBar {
            Text(
                text = strings.profileTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Carte Profil réel ou absence de profil
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
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val selfieUri = userProfile?.localSelfieUri
                            if (!selfieUri.isNullOrBlank()) {
                                Surface(
                                    shape = CircleShape,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    AsyncImage(
                                        model = Uri.parse(selfieUri),
                                        contentDescription = userProfile.fullName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (hasProfile) {
                                        userProfile.fullName
                                    } else {
                                        strings.noProfileRegisteredTitle
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!currentUserEmail.isNullOrBlank()) {
                                    Text(
                                        text = "${strings.signedInAccountLabel} : $currentUserEmail",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (hasProfile) {
                                    Text(
                                        text = "${userProfile.phone} • ${userProfile.formattedLocation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${strings.fieldAgriculturalSector} : ${AppStrings.sectorLabel(userProfile.agriculturalSector, settings.language)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                IdentityStatusChip(
                                    status = userProfile?.identityStatus ?: IdentityStatus.UNVERIFIED,
                                    language = settings.language
                                )
                            }
                        }

                        if (!hasProfile) {
                            Button(
                                onClick = onNavigateEnroll,
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("profile_create_account_button")
                            ) {
                                Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.createAccountCta)
                            }
                        }
                    }
                }
            }

            // Langue & Thème
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
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = strings.languageLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppLanguage.entries.forEach { lang ->
                                FilterChip(
                                    selected = settings.language == lang,
                                    onClick = { onSetLanguage(lang) },
                                    label = { Text(lang.displayLabel) },
                                    modifier = Modifier.testTag("profile_lang_${lang.name}")
                                )
                            }
                        }

                        HorizontalDivider()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DarkMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = strings.themeLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = settings.themePreference == ThemePreference.AUTO,
                                onClick = { onSetTheme(ThemePreference.AUTO) },
                                label = { Text(strings.themeAuto) },
                                modifier = Modifier.testTag("profile_theme_auto")
                            )
                            FilterChip(
                                selected = settings.themePreference == ThemePreference.LIGHT,
                                onClick = { onSetTheme(ThemePreference.LIGHT) },
                                label = { Text(strings.themeLight) },
                                modifier = Modifier.testTag("profile_theme_light")
                            )
                            FilterChip(
                                selected = settings.themePreference == ThemePreference.DARK,
                                onClick = { onSetTheme(ThemePreference.DARK) },
                                label = { Text(strings.themeDark) },
                                modifier = Modifier.testTag("profile_theme_dark")
                            )
                        }
                    }
                }
            }

            // Accessibilité & Notifications
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
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsSwitchRow(
                            icon = Icons.Rounded.FormatSize,
                            title = strings.largeTextLabel,
                            subtitle = strings.largeTextSub,
                            checked = settings.largeText,
                            onCheckedChange = onSetLargeText,
                            testTag = "profile_large_text_switch"
                        )
                        HorizontalDivider()
                        SettingsSwitchRow(
                            icon = Icons.Rounded.Notifications,
                            title = strings.notificationsLabel,
                            subtitle = null,
                            checked = settings.notificationsEnabled,
                            onCheckedChange = onSetNotifications,
                            testTag = "profile_notifications_switch"
                        )
                    }
                }
            }

            // Mode démo (développeur)
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
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BugReport,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = strings.devDemoTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = strings.devDemoSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MockMode.entries.forEach { mode ->
                                val label = when (mode) {
                                    MockMode.NORMAL -> strings.devModeNormal
                                    MockMode.ECHEC_VISAGE -> strings.devModeFaceFail
                                    MockMode.ECHEC_RESEAU -> strings.devModeNetworkFail
                                    MockMode.LENT -> strings.devModeSlow
                                }
                                FilterChip(
                                    selected = mockMode == mode,
                                    onClick = {
                                        mockMode = mode
                                        MockConfig.mode = mode
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("profile_dev_mode_${mode.name}")
                                )
                            }
                        }
                    }
                }
            }

            // Statut Cloud Firestore & cache hors ligne DataStore
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOnline) {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                            contentDescription = null,
                            tint = if (isOnline) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOnline) strings.cloudSyncTitle else strings.offlineBannerTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isOnline) strings.cloudSyncSubtitle else strings.offlineBannerDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (lastSyncedAtMillis > 0L) {
                                val formattedSync = remember(lastSyncedAtMillis) {
                                    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                        .format(java.util.Date(lastSyncedAtMillis))
                                }
                                Text(
                                    text = String.format(strings.lastSyncedLabel, formattedSync),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Actions de compte : Déconnexion, À propos, Suppression Firestore
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { showAboutDialog = true },
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_about_button")
                    ) {
                        Icon(Icons.Rounded.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.aboutTitle)
                    }

                    OutlinedButton(
                        onClick = {
                            signOutFromAccount(
                                credentialManager = credentialManager,
                                scope = scope
                            )
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_sign_out_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.signOut)
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("profile_delete_data_button")
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.deleteDataTitle)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteDataTitle) },
            text = { Text(strings.deleteDataConfirmBody) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAllData { ok ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (ok) strings.dataDeletedToast else strings.halisiErrorNetwork
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("profile_confirm_delete_button")
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(strings.aboutTitle) },
            text = { Text(strings.aboutBody) },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(strings.close)
                }
            }
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
