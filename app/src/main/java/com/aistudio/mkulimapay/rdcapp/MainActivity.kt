package com.aistudio.mkulimapay.rdcapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.SystemBarStyle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aistudio.mkulimapay.rdcapp.data.AppRepository
import com.aistudio.mkulimapay.rdcapp.data.LocalBiometricStorage
import com.aistudio.mkulimapay.rdcapp.data.NetworkMonitor
import com.aistudio.mkulimapay.rdcapp.data.OfflineCacheRepository
import com.aistudio.mkulimapay.rdcapp.data.SettingsRepository
import com.aistudio.mkulimapay.rdcapp.data.UiState
import com.aistudio.mkulimapay.rdcapp.i18n.AppStrings
import com.aistudio.mkulimapay.rdcapp.i18n.LocalizedStrings
import com.aistudio.mkulimapay.rdcapp.model.AppSettings
import com.aistudio.mkulimapay.rdcapp.model.ThemePreference
import com.aistudio.mkulimapay.rdcapp.ui.MkulimaViewModel
import com.aistudio.mkulimapay.rdcapp.ui.SettingsViewModel
import com.aistudio.mkulimapay.rdcapp.ui.screens.AuthScreen
import com.aistudio.mkulimapay.rdcapp.ui.screens.CreditAssistantScreen
import com.aistudio.mkulimapay.rdcapp.ui.screens.CreditFinalConfirmedScreen
import com.aistudio.mkulimapay.rdcapp.ui.screens.CreditResultScreen
import com.aistudio.mkulimapay.rdcapp.ui.screens.EnrollmentScreen
import com.aistudio.mkulimapay.rdcapp.ui.screens.FaceVerifyScreen
import com.aistudio.mkulimapay.rdcapp.ui.screens.HistoryScreen
import com.aistudio.mkulimapay.rdcapp.ui.screens.HomeScreen
import com.aistudio.mkulimapay.rdcapp.ui.screens.OnboardingScreen
import com.aistudio.mkulimapay.rdcapp.ui.screens.ProfileScreen
import com.aistudio.mkulimapay.rdcapp.ui.screens.SplashScreen
import com.aistudio.mkulimapay.rdcapp.ui.theme.MkulimaPayTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val ROUTE_SPLASH = "splash"
const val ROUTE_ONBOARDING = "onboarding"
const val ROUTE_MAIN = "main"
const val ROUTE_ENROLLMENT = "enrollment"
const val ROUTE_FACE_VERIFY = "face_verify"
const val ROUTE_CREDIT_RESULT = "credit_result"
const val ROUTE_CREDIT_CONFIRMED = "credit_confirmed"

enum class MainTab {
    HOME,
    CREDIT,
    HISTORY,
    PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuration App Check avec l'Intent de lancement avant tout UI ou ViewModel
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val appContext = applicationContext
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        SettingsViewModel(SettingsRepository(appContext))
                    }
                }
            )
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

            // Les icônes de la barre d'état / de navigation suivent le thème choisi dans l'app
            // (et non celui du téléphone), pour rester lisibles en mode sombre comme en mode clair.
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (settings.themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.AUTO -> systemDark
            }
            DisposableEffect(isDarkTheme) {
                val barStyle = if (isDarkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                onDispose { }
            }

            MkulimaPayTheme(
                themePreference = settings.themePreference,
                largeText = settings.largeText
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MkulimaPayAppRoot(
                        settingsViewModel = settingsViewModel,
                        settings = settings
                    )
                }
            }
        }
    }
}

@Composable
private fun MkulimaPayAppRoot(
    settingsViewModel: SettingsViewModel,
    settings: AppSettings
) {
    val strings = AppStrings.get(settings.language)
    var introDone by rememberSaveable { mutableStateOf(false) }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    DisposableEffect(Unit) {
        val auth = Firebase.auth
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // On attend la fin de l'intro ET le chargement des réglages enregistrés avant de décider
    // d'afficher l'onboarding (sinon la valeur par défaut « non terminé » le relançait à chaque fois).
    if (!introDone || !settings.isLoaded) {
        SplashScreen(
            strings = strings,
            onFinished = { introDone = true }
        )
        return
    }

    if (!settings.onboardingCompleted) {
        OnboardingScreen(
            strings = strings,
            language = settings.language,
            onToggleLanguage = { settingsViewModel.toggleLanguage() },
            onFinishOnboarding = { settingsViewModel.completeOnboarding() }
        )
        return
    }

    val signedInUser = currentUser
    if (signedInUser == null) {
        AuthScreen(
            strings = strings,
            language = settings.language,
            onToggleLanguage = { settingsViewModel.toggleLanguage() },
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        key(signedInUser.uid) {
            AuthenticatedMkulimaFlow(
                currentUserId = signedInUser.uid,
                currentUserEmail = signedInUser.email,
                settingsViewModel = settingsViewModel,
                settings = settings,
                strings = strings
            )
        }
    }
}

@Composable
private fun AuthenticatedMkulimaFlow(
    currentUserId: String,
    currentUserEmail: String?,
    settingsViewModel: SettingsViewModel,
    settings: AppSettings,
    strings: LocalizedStrings
) {
    val context = LocalContext.current
    val appContext = context.applicationContext

    val viewModel: MkulimaViewModel = viewModel(
        key = "mkulima_$currentUserId",
        factory = viewModelFactory {
            initializer {
                val databaseId = appContext.getString(R.string.firestore_database_id)
                val firestore = FirebaseFirestore.getInstance(databaseId)
                val repository = AppRepository(db = firestore)
                val readUriBytes: suspend (String) -> ByteArray = { uriString ->
                    withContext(Dispatchers.IO) {
                        runCatching {
                            val uri = Uri.parse(uriString)
                            appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                ?: ByteArray(0)
                        }.getOrElse { ByteArray(0) }
                    }
                }
                MkulimaViewModel(
                    repository = repository,
                    currentUserId = currentUserId,
                    currentUserEmail = currentUserEmail,
                    readUriBytes = readUriBytes,
                    biometricStorage = LocalBiometricStorage(appContext),
                    offlineCacheRepository = OfflineCacheRepository(appContext),
                    isOnlineFlow = NetworkMonitor.observeIsOnline(appContext)
                )
            }
        }
    )

    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val creditRequestsState by viewModel.creditRequestsState.collectAsStateWithLifecycle()
    val transactionsState by viewModel.transactionsState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val offlineCache by viewModel.offlineCacheState.collectAsStateWithLifecycle()
    val creditNotifications by viewModel.creditNotifications.collectAsStateWithLifecycle()
    val enrollmentState by viewModel.enrollment.collectAsStateWithLifecycle()
    val faceVerifyState by viewModel.faceVerify.collectAsStateWithLifecycle()
    val assistantState by viewModel.assistant.collectAsStateWithLifecycle()
    val activeCreditResult by viewModel.activeCreditResult.collectAsStateWithLifecycle()
    val lastSyncedAtMillis = offlineCache?.lastSyncedAtMillis ?: 0L

    val userProfile = (profileState as? UiState.Success)?.data
    var currentRoute by rememberSaveable(currentUserId) { mutableStateOf(ROUTE_MAIN) }
    var activeTab by rememberSaveable(currentUserId) { mutableStateOf(MainTab.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isWideScreen = maxWidth >= 600.dp

        AnimatedContent(
            targetState = currentRoute,
            transitionSpec = {
                (slideInHorizontally { it / 6 } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 6 } + fadeOut())
            },
            label = "rootNavigation",
            modifier = Modifier.fillMaxSize()
        ) { route ->
            when (route) {
                ROUTE_ENROLLMENT -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 520.dp)
                                .fillMaxSize()
                        ) {
                            EnrollmentScreen(
                                strings = strings,
                                language = settings.language,
                                state = enrollmentState,
                                onUpdateState = { viewModel.updateEnrollment(it) },
                                isStep1Valid = viewModel.isEnrollmentStep1Valid(),
                                isStep2Valid = viewModel.isEnrollmentStep2Valid(),
                                onSubmit = { viewModel.submitEnrollment(settings.language) },
                                onGoToFaceVerify = {
                                    viewModel.resetFaceVerify()
                                    currentRoute = ROUTE_FACE_VERIFY
                                },
                                onBack = { currentRoute = ROUTE_MAIN }
                            )
                        }
                    }
                }

                ROUTE_FACE_VERIFY -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 520.dp)
                                .fillMaxSize()
                        ) {
                            FaceVerifyScreen(
                                strings = strings,
                                userProfile = userProfile,
                                state = faceVerifyState,
                                onCaptureAndVerify = { uri ->
                                    viewModel.verifyFaceFromCapture(uri, settings.language)
                                },
                                onResetAttempt = {
                                    viewModel.resetFaceVerify()
                                },
                                onGoToEnroll = {
                                    viewModel.initEnrollmentFromProfile()
                                    currentRoute = ROUTE_ENROLLMENT
                                },
                                onGoToCreditAssistant = {
                                    viewModel.startCreditSession(settings.language)
                                    activeTab = MainTab.CREDIT
                                    currentRoute = ROUTE_MAIN
                                },
                                onBack = { currentRoute = ROUTE_MAIN }
                            )
                        }
                    }
                }

                ROUTE_CREDIT_RESULT -> {
                    val req = activeCreditResult
                    if (req != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 540.dp)
                                    .fillMaxSize()
                            ) {
                                CreditResultScreen(
                                    strings = strings,
                                    language = settings.language,
                                    request = req,
                                    onAcceptConfirmed = { onError ->
                                        viewModel.acceptActiveCreditRequest(
                                            language = settings.language,
                                            onDone = { currentRoute = ROUTE_CREDIT_CONFIRMED },
                                            onError = onError
                                        )
                                    },
                                    onModify = {
                                        viewModel.startCreditSession(settings.language, req.data)
                                        activeTab = MainTab.CREDIT
                                        currentRoute = ROUTE_MAIN
                                    },
                                    onBack = { currentRoute = ROUTE_MAIN }
                                )
                            }
                        }
                    }
                }

                ROUTE_CREDIT_CONFIRMED -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 520.dp)
                                .fillMaxSize()
                        ) {
                            CreditFinalConfirmedScreen(
                                strings = strings,
                                onGoHistory = {
                                    activeTab = MainTab.HISTORY
                                    currentRoute = ROUTE_MAIN
                                },
                                onGoHome = {
                                    activeTab = MainTab.HOME
                                    currentRoute = ROUTE_MAIN
                                }
                            )
                        }
                    }
                }

                else -> {
                    MainTabsScaffold(
                        isWideScreen = isWideScreen,
                        strings = strings,
                        activeTab = activeTab,
                        onSelectTab = { activeTab = it },
                        snackbarHostState = snackbarHostState
                    ) {
                        when (activeTab) {
                            MainTab.HOME -> {
                                HomeScreen(
                                    strings = strings,
                                    language = settings.language,
                                    currentUserEmail = currentUserEmail,
                                    profileState = profileState,
                                    creditRequestsState = creditRequestsState,
                                    transactionsState = transactionsState,
                                    isOnline = isOnline,
                                    creditNotifications = creditNotifications,
                                    lastSyncedAtMillis = lastSyncedAtMillis,
                                    onToggleLanguage = { settingsViewModel.toggleLanguage() },
                                    onNavigateEnroll = {
                                        viewModel.initEnrollmentFromProfile()
                                        currentRoute = ROUTE_ENROLLMENT
                                    },
                                    onNavigateFaceVerify = {
                                        viewModel.resetFaceVerify()
                                        currentRoute = ROUTE_FACE_VERIFY
                                    },
                                    onNavigateCreditAssistant = {
                                        viewModel.startCreditSession(settings.language)
                                        activeTab = MainTab.CREDIT
                                    },
                                    onOpenCreditResult = { req ->
                                        viewModel.setActiveCreditResult(req)
                                        currentRoute = ROUTE_CREDIT_RESULT
                                    },
                                    onNavigateHistory = { activeTab = MainTab.HISTORY },
                                    onNavigateProfile = { activeTab = MainTab.PROFILE }
                                )
                            }

                            MainTab.CREDIT -> {
                                CreditAssistantScreen(
                                    strings = strings,
                                    language = settings.language,
                                    userProfile = userProfile,
                                    state = assistantState,
                                    snackbarHostState = snackbarHostState,
                                    onStartSession = { viewModel.startCreditSession(settings.language) },
                                    onSelectCrop = { viewModel.submitCropAnswer(it, settings.language) },
                                    onSubmitArea = { viewModel.submitAreaAnswer(it, settings.language) },
                                    onSelectHarvestMonth = { viewModel.submitHarvestMonthAnswer(it, settings.language) },
                                    onSubmitExperience = { viewModel.submitExperienceAnswer(it, settings.language) },
                                    onSubmitIncome = { viewModel.submitIncomeAnswer(it, settings.language) },
                                    onSubmitAmount = { viewModel.submitAmountAnswer(it, settings.language) },
                                    onSubmitDuration = { viewModel.submitDurationAnswer(it, settings.language) },
                                    onSelectPurpose = { viewModel.submitPurposeAnswer(it, settings.language) },
                                    onSendText = { txt -> viewModel.sendFreeTextOrVoice(txt, settings.language) },
                                    onCalculateResult = {
                                        viewModel.evaluateCreditRequest(settings.language) {
                                            currentRoute = ROUTE_CREDIT_RESULT
                                        }
                                    },
                                    onNavigateFaceVerify = {
                                        viewModel.resetFaceVerify()
                                        currentRoute = ROUTE_FACE_VERIFY
                                    },
                                    onNavigateEnroll = {
                                        viewModel.initEnrollmentFromProfile()
                                        currentRoute = ROUTE_ENROLLMENT
                                    }
                                )
                            }

                            MainTab.HISTORY -> {
                                HistoryScreen(
                                    strings = strings,
                                    language = settings.language,
                                    requestsState = creditRequestsState,
                                    transactionsState = transactionsState,
                                    snackbarHostState = snackbarHostState,
                                    isOnline = isOnline,
                                    creditNotifications = creditNotifications,
                                    lastSyncedAtMillis = lastSyncedAtMillis,
                                    onOpenCreditResult = { req ->
                                        viewModel.setActiveCreditResult(req)
                                        currentRoute = ROUTE_CREDIT_RESULT
                                    },
                                    onRecordRepayment = { requestId, amountUsd, channel, reference, onDone, onError ->
                                        viewModel.recordRepayment(
                                            requestId = requestId,
                                            amountUsd = amountUsd,
                                            channel = channel,
                                            reference = reference,
                                            language = settings.language,
                                            onDone = onDone,
                                            onError = onError
                                        )
                                    }
                                )
                            }

                            MainTab.PROFILE -> {
                                ProfileScreen(
                                    strings = strings,
                                    settings = settings,
                                    currentUserEmail = currentUserEmail,
                                    profileState = profileState,
                                    snackbarHostState = snackbarHostState,
                                    isOnline = isOnline,
                                    lastSyncedAtMillis = lastSyncedAtMillis,
                                    onSetLanguage = { settingsViewModel.setLanguage(it) },
                                    onSetTheme = { settingsViewModel.setThemePreference(it) },
                                    onSetLargeText = { settingsViewModel.setLargeText(it) },
                                    onSetNotifications = { settingsViewModel.setNotifications(it) },
                                    onNavigateEnroll = {
                                        viewModel.initEnrollmentFromProfile()
                                        currentRoute = ROUTE_ENROLLMENT
                                    },
                                    onDeleteAllData = { done -> viewModel.deleteAllUserData(done) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class NavItemSpec(
    val tab: MainTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
private fun MainTabsScaffold(
    isWideScreen: Boolean,
    strings: LocalizedStrings,
    activeTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    snackbarHostState: SnackbarHostState,
    content: @Composable () -> Unit
) {
    val navItems = listOf(
        NavItemSpec(MainTab.HOME, strings.navHome, Icons.Rounded.Home, Icons.Outlined.Home, "nav_tab_home"),
        NavItemSpec(MainTab.CREDIT, strings.navCredit, Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_tab_credit"),
        NavItemSpec(MainTab.HISTORY, strings.navHistory, Icons.Rounded.History, Icons.Outlined.History, "nav_tab_history"),
        NavItemSpec(MainTab.PROFILE, strings.navProfile, Icons.Rounded.Person, Icons.Outlined.Person, "nav_tab_profile")
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar {
                    navItems.forEach { item ->
                        val selected = activeTab == item.tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onSelectTab(item.tab) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isWideScreen) {
                NavigationRail(modifier = Modifier.fillMaxHeight()) {
                    navItems.forEach { item ->
                        val selected = activeTab == item.tab
                        NavigationRailItem(
                            selected = selected,
                            onClick = { onSelectTab(item.tab) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxSize()
                ) {
                    content()
                }
            }
        }
    }
}
