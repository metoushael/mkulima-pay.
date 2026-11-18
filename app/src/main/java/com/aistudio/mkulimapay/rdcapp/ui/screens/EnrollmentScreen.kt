package com.aistudio.mkulimapay.rdcapp.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Agriculture
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.NoPhotography
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.i18n.LocalizedStrings
import com.aistudio.mkulimapay.rdcapp.model.AgriculturalSector
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.IdDocumentType
import com.aistudio.mkulimapay.rdcapp.ui.EnrollmentUiState
import com.aistudio.mkulimapay.rdcapp.ui.components.BiometricOvalCameraCapture
import com.aistudio.mkulimapay.rdcapp.ui.theme.LocalMkulimaColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentScreen(
    strings: LocalizedStrings,
    language: AppLanguage,
    state: EnrollmentUiState,
    onUpdateState: ((EnrollmentUiState) -> EnrollmentUiState) -> Unit,
    isStep1Valid: Boolean,
    isStep2Valid: Boolean,
    onSubmit: () -> Unit,
    onGoToFaceVerify: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var attemptedStepAdvance by remember { mutableStateOf(false) }

    BackHandler {
        if (state.isSuccess || state.step == 0) {
            onBack()
        } else if (!state.isSubmitting) {
            onUpdateState { it.copy(step = it.step - 1) }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = strings.enrollTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!state.isSuccess) {
                            Text(
                                text = "${strings.stepLabel} ${state.step + 1} / 4",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.isSuccess || state.step == 0) onBack()
                            else if (!state.isSubmitting) onUpdateState { it.copy(step = it.step - 1) }
                        },
                        modifier = Modifier.testTag("enroll_back_button")
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
        ) {
            if (!state.isSuccess) {
                LinearProgressIndicator(
                    progress = { (state.step + 1) / 4f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }

            if (state.isSubmitting) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(56.dp),
                            strokeWidth = 5.dp
                        )
                        Text(
                            text = strings.enrollLoading,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (state.isSuccess) {
                EnrollmentSuccessView(
                    strings = strings,
                    onGoToFaceVerify = onGoToFaceVerify,
                    onDoneHome = onBack
                )
            } else {
                AnimatedContent(
                    targetState = state.step,
                    label = "enrollmentStep",
                    modifier = Modifier.weight(1f)
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (step) {
                            0 -> Step1PersonalInfo(
                                strings = strings,
                                language = language,
                                state = state,
                                showErrors = attemptedStepAdvance,
                                onUpdateState = onUpdateState
                            )
                            1 -> Step2IdDocument(
                                strings = strings,
                                language = language,
                                state = state,
                                showErrors = attemptedStepAdvance,
                                onUpdateState = onUpdateState
                            )
                            2 -> Step3BiometricSelfie(
                                strings = strings,
                                state = state,
                                onUpdateState = onUpdateState
                            )
                            3 -> Step4Summary(
                                strings = strings,
                                language = language,
                                state = state
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Barre de navigation du stepper en bas
                Surface(
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.step > 0) {
                            OutlinedButton(
                                onClick = {
                                    attemptedStepAdvance = false
                                    onUpdateState { it.copy(step = it.step - 1) }
                                },
                                shape = CircleShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("enroll_prev_button")
                            ) {
                                Text(strings.back)
                            }
                        }

                        Button(
                            onClick = {
                                when (state.step) {
                                    0 -> {
                                        attemptedStepAdvance = true
                                        if (isStep1Valid) {
                                            attemptedStepAdvance = false
                                            onUpdateState { it.copy(step = 1) }
                                        }
                                    }
                                    1 -> {
                                        attemptedStepAdvance = true
                                        if (isStep2Valid) {
                                            attemptedStepAdvance = false
                                            onUpdateState { it.copy(step = 2) }
                                        }
                                    }
                                    2 -> {
                                        if (state.selfieUri.isNotBlank()) {
                                            onUpdateState { it.copy(step = 3) }
                                        } else {
                                            onUpdateState { it.copy(errorMessage = strings.errSelfieRequired) }
                                        }
                                    }
                                    3 -> onSubmit()
                                }
                            },
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1.4f)
                                .height(50.dp)
                                .testTag("enroll_next_button")
                        ) {
                            Text(
                                text = when (state.step) {
                                    2 -> strings.validate
                                    3 -> strings.send
                                    else -> strings.next
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (state.step == 3) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1PersonalInfo(
    strings: LocalizedStrings,
    language: AppLanguage,
    state: EnrollmentUiState,
    showErrors: Boolean,
    onUpdateState: ((EnrollmentUiState) -> EnrollmentUiState) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var provinceExpanded by remember { mutableStateOf(false) }
    var sectorExpanded by remember { mutableStateOf(false) }
    val phoneDigitsClean = state.phoneDigits.filter { it.isDigit() }

    Text(
        text = strings.enrollStep1Title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    OutlinedTextField(
        value = state.nom,
        onValueChange = { v -> onUpdateState { it.copy(nom = v) } },
        label = { Text(strings.fieldNom) },
        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
        isError = showErrors && state.nom.isBlank(),
        supportingText = if (showErrors && state.nom.isBlank()) {
            { Text(strings.errRequiredField) }
        } else null,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("enroll_nom_input")
    )

    OutlinedTextField(
        value = state.postnom,
        onValueChange = { v -> onUpdateState { it.copy(postnom = v) } },
        label = { Text(strings.fieldPostnom) },
        isError = showErrors && state.postnom.isBlank(),
        supportingText = if (showErrors && state.postnom.isBlank()) {
            { Text(strings.errRequiredField) }
        } else null,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("enroll_postnom_input")
    )

    OutlinedTextField(
        value = state.prenom,
        onValueChange = { v -> onUpdateState { it.copy(prenom = v) } },
        label = { Text(strings.fieldPrenom) },
        isError = showErrors && state.prenom.isBlank(),
        supportingText = if (showErrors && state.prenom.isBlank()) {
            { Text(strings.errRequiredField) }
        } else null,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("enroll_prenom_input")
    )

    // Date de naissance (M3 DatePickerDialog)
    OutlinedTextField(
        value = state.dateOfBirth,
        onValueChange = {},
        readOnly = true,
        label = { Text(strings.fieldDob) },
        placeholder = { Text(strings.selectDobHint) },
        trailingIcon = {
            IconButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.testTag("enroll_dob_picker_button")
            ) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = strings.selectDobHint)
            }
        },
        isError = showErrors && state.dateOfBirth.isBlank(),
        supportingText = if (showErrors && state.dateOfBirth.isBlank()) {
            { Text(strings.errRequiredField) }
        } else null,
        modifier = Modifier.fillMaxWidth()
    )

    // Sexe
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = strings.fieldSex,
            style = MaterialTheme.typography.labelLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = state.sex == "M",
                onClick = { onUpdateState { it.copy(sex = "M") } },
                label = { Text(strings.sexMale) },
                modifier = Modifier.testTag("enroll_sex_male")
            )
            FilterChip(
                selected = state.sex == "F",
                onClick = { onUpdateState { it.copy(sex = "F") } },
                label = { Text(strings.sexFemale) },
                modifier = Modifier.testTag("enroll_sex_female")
            )
        }
    }

    // Téléphone +243
    OutlinedTextField(
        value = state.phoneDigits,
        onValueChange = { raw ->
            val filtered = raw.filter { it.isDigit() }.take(9)
            onUpdateState { it.copy(phoneDigits = filtered) }
        },
        label = { Text(strings.fieldPhone) },
        prefix = { Text("+243 ") },
        leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = (state.phoneDigits.isNotEmpty() || showErrors) && phoneDigitsClean.length != 9,
        supportingText = if ((state.phoneDigits.isNotEmpty() || showErrors) && phoneDigitsClean.length != 9) {
            { Text(strings.errInvalidPhone) }
        } else null,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("enroll_phone_input")
    )

    // Province / Territoire RDC
    ExposedDropdownMenuBox(
        expanded = provinceExpanded,
        onExpandedChange = { provinceExpanded = !provinceExpanded }
    ) {
        OutlinedTextField(
            value = state.province,
            onValueChange = {},
            readOnly = true,
            label = { Text(strings.fieldProvince) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinceExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .testTag("enroll_province_dropdown")
        )
        ExposedDropdownMenu(
            expanded = provinceExpanded,
            onDismissRequest = { provinceExpanded = false }
        ) {
            AppStrings.drcProvinces.forEach { province ->
                DropdownMenuItem(
                    text = { Text(province) },
                    onClick = {
                        onUpdateState { it.copy(province = province) }
                        provinceExpanded = false
                    }
                )
            }
        }
    }

    // Localité / Territoire / Village
    OutlinedTextField(
        value = state.location,
        onValueChange = { v -> onUpdateState { it.copy(location = v) } },
        label = { Text(strings.fieldLocation) },
        placeholder = { Text(strings.fieldLocationHint) },
        leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null) },
        isError = showErrors && state.location.trim().length < 2,
        supportingText = if (showErrors && state.location.trim().length < 2) {
            { Text(strings.errRequiredField) }
        } else null,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("enroll_location_input")
    )

    // Secteur agricole
    ExposedDropdownMenuBox(
        expanded = sectorExpanded,
        onExpandedChange = { sectorExpanded = !sectorExpanded }
    ) {
        OutlinedTextField(
            value = AppStrings.sectorLabel(state.agriculturalSector, language),
            onValueChange = {},
            readOnly = true,
            label = { Text(strings.fieldAgriculturalSector) },
            leadingIcon = { Icon(Icons.Rounded.Agriculture, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectorExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .testTag("enroll_sector_dropdown")
        )
        ExposedDropdownMenu(
            expanded = sectorExpanded,
            onDismissRequest = { sectorExpanded = false }
        ) {
            AgriculturalSector.entries.forEach { sector ->
                DropdownMenuItem(
                    text = { Text(AppStrings.sectorLabel(sector, language)) },
                    onClick = {
                        onUpdateState { it.copy(agriculturalSector = sector) }
                        sectorExpanded = false
                    }
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = 788918400000L) // ~1995
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(Date(millis))
                            onUpdateState { it.copy(dateOfBirth = formatted) }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(strings.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step2IdDocument(
    strings: LocalizedStrings,
    language: AppLanguage,
    state: EnrollmentUiState,
    showErrors: Boolean,
    onUpdateState: ((EnrollmentUiState) -> EnrollmentUiState) -> Unit
) {
    var idTypeExpanded by remember { mutableStateOf(false) }

    val idPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUpdateState { it.copy(idPhotoUri = uri.toString()) }
        }
    }

    Text(
        text = strings.enrollStep2Title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    ExposedDropdownMenuBox(
        expanded = idTypeExpanded,
        onExpandedChange = { idTypeExpanded = !idTypeExpanded }
    ) {
        OutlinedTextField(
            value = AppStrings.idTypeLabel(state.idType, language),
            onValueChange = {},
            readOnly = true,
            label = { Text(strings.fieldIdType) },
            leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = idTypeExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .testTag("enroll_id_type_dropdown")
        )
        ExposedDropdownMenu(
            expanded = idTypeExpanded,
            onDismissRequest = { idTypeExpanded = false }
        ) {
            IdDocumentType.entries.forEach { docType ->
                DropdownMenuItem(
                    text = { Text(AppStrings.idTypeLabel(docType, language)) },
                    onClick = {
                        onUpdateState { it.copy(idType = docType) }
                        idTypeExpanded = false
                    }
                )
            }
        }
    }

    OutlinedTextField(
        value = state.idNumber,
        onValueChange = { v -> onUpdateState { it.copy(idNumber = v) } },
        label = { Text(strings.fieldIdNumber) },
        isError = showErrors && state.idNumber.trim().length < 4,
        supportingText = if (showErrors && state.idNumber.trim().length < 4) {
            { Text(strings.errRequiredField) }
        } else null,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("enroll_id_number_input")
    )

    OutlinedButton(
        onClick = {
            idPhotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("enroll_import_id_photo_button")
    ) {
        Icon(Icons.Rounded.UploadFile, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (state.idPhotoUri.isBlank()) strings.importIdPhoto else strings.idPhotoSelected)
    }

    if (state.idPhotoUri.isNotBlank()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            AsyncImage(
                model = Uri.parse(state.idPhotoUri),
                contentDescription = strings.idPhotoSelected,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun Step3BiometricSelfie(
    strings: LocalizedStrings,
    state: EnrollmentUiState,
    onUpdateState: ((EnrollmentUiState) -> EnrollmentUiState) -> Unit
) {
    Text(
        text = strings.enrollStep3Title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GuideChecklistRow(icon = Icons.Rounded.Face, text = strings.selfieGuideFrame)
        GuideChecklistRow(icon = Icons.Rounded.LightMode, text = strings.selfieGuideLight)
        GuideChecklistRow(icon = Icons.Rounded.NoPhotography, text = strings.selfieGuideGlasses)
    }

    Spacer(modifier = Modifier.height(6.dp))

    BiometricOvalCameraCapture(
        strings = strings,
        capturedUri = state.selfieUri,
        isScanning = false,
        pulseOval = false,
        primaryActionLabel = strings.captureSelfie,
        onImageCaptured = { uri ->
            onUpdateState { it.copy(selfieUri = uri) }
        },
        onRetake = {
            onUpdateState { it.copy(selfieUri = "") }
        }
    )
}

@Composable
private fun GuideChecklistRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Step4Summary(
    strings: LocalizedStrings,
    language: AppLanguage,
    state: EnrollmentUiState
) {
    Text(
        text = strings.enrollSummaryTitle,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

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
            SummaryRow(strings.fieldNom, "${state.prenom} ${state.nom} ${state.postnom}")
            HorizontalDivider()
            SummaryRow(strings.fieldDob, state.dateOfBirth)
            HorizontalDivider()
            SummaryRow(strings.fieldPhone, "+243 ${state.phoneDigits}")
            HorizontalDivider()
            SummaryRow(strings.fieldProvince, state.province)
            HorizontalDivider()
            SummaryRow(strings.fieldLocation, state.location)
            HorizontalDivider()
            SummaryRow(
                strings.fieldAgriculturalSector,
                AppStrings.sectorLabel(state.agriculturalSector, language)
            )
            HorizontalDivider()
            SummaryRow(
                strings.fieldIdType,
                "${AppStrings.idTypeLabel(state.idType, language)} • ${state.idNumber}"
            )
            HorizontalDivider()
            SummaryRow(
                strings.enrollStep3Title,
                if (state.selfieUri.isNotBlank()) strings.validate else "-"
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun EnrollmentSuccessView(
    strings: LocalizedStrings,
    onGoToFaceVerify: () -> Unit,
    onDoneHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = strings.enrollSuccessTitle,
            tint = LocalMkulimaColors.current.success,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = strings.enrollSuccessTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = strings.enrollSuccessDesc,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onGoToFaceVerify,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("enroll_success_verify_face_button")
        ) {
            Icon(Icons.Rounded.LockOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.actionFaceLoginTitle)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onDoneHome,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(strings.navHome)
        }
    }
}
