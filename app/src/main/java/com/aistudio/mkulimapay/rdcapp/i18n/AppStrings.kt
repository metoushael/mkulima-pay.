package com.aistudio.mkulimapay.rdcapp.i18n

import com.aistudio.mkulimapay.rdcapp.model.AgriculturalSector
import com.aistudio.mkulimapay.rdcapp.model.AppLanguage
import com.aistudio.mkulimapay.rdcapp.model.CreditPurpose
import com.aistudio.mkulimapay.rdcapp.model.CreditStatus
import com.aistudio.mkulimapay.rdcapp.model.CropType
import com.aistudio.mkulimapay.rdcapp.model.IdDocumentType
import com.aistudio.mkulimapay.rdcapp.model.IdentityStatus
import com.aistudio.mkulimapay.rdcapp.model.RiskLevel
import com.aistudio.mkulimapay.rdcapp.model.TransactionStatus
import com.aistudio.mkulimapay.rdcapp.model.TransactionType

object AppStrings {

    val drcProvinces = listOf(
        "Kinshasa",
        "Kongo-Central",
        "Nord-Kivu (Goma / Beni / Butembo)",
        "Sud-Kivu (Bukavu / Uvira)",
        "Haut-Katanga (Lubumbashi / Likasi)",
        "Lualaba (Kolwezi)",
        "Ituri (Bunia)",
        "Tshopo (Kisangani)",
        "Kasaï (Tshikapa)",
        "Kasaï-Central (Kananga)",
        "Kasaï-Oriental (Mbuji-Mayi)",
        "Équateur (Mbandaka)",
        "Tanganyika (Kalemie)",
        "Maniema (Kindu)",
        "Kwango / Kwilu",
        "Autre territoire RDC"
    )

    val paymentChannels = listOf(
        "M-Pesa",
        "Airtel Money",
        "Orange Money",
        "Afrimoney"
    )

    fun get(lang: AppLanguage): LocalizedStrings = when (lang) {
        AppLanguage.FR -> FrenchStrings
        AppLanguage.SW -> SwahiliStrings
    }

    fun cropLabel(crop: CropType, lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> when (crop) {
            CropType.MAIS -> "Maïs"
            CropType.MANIOC -> "Manioc"
            CropType.HARICOT -> "Haricot"
            CropType.RIZ -> "Riz"
            CropType.CAFE -> "Café"
            CropType.BANANE -> "Banane"
            CropType.AUTRE -> "Autre culture"
        }
        AppLanguage.SW -> when (crop) {
            CropType.MAIS -> "Mahindi"
            CropType.MANIOC -> "Muhogo"
            CropType.HARICOT -> "Maharagwe"
            CropType.RIZ -> "Mchele"
            CropType.CAFE -> "Kahawa"
            CropType.BANANE -> "Ndizi"
            CropType.AUTRE -> "Zao lingine"
        }
    }

    fun purposeLabel(purpose: CreditPurpose, lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> when (purpose) {
            CreditPurpose.SEMENCES -> "Semences améliorées"
            CreditPurpose.OUTILS -> "Outils agricoles"
            CreditPurpose.ENGRAIS -> "Engrais & intrants"
            CreditPurpose.STOCKAGE -> "Stockage & sacs"
            CreditPurpose.MAIN_OEUVRE -> "Main-d'œuvre"
        }
        AppLanguage.SW -> when (purpose) {
            CreditPurpose.SEMENCES -> "Mbegu bora"
            CreditPurpose.OUTILS -> "Zana za kilimo"
            CreditPurpose.ENGRAIS -> "Mbolea na pembejeo"
            CreditPurpose.STOCKAGE -> "Uhifadhi wa mavuno"
            CreditPurpose.MAIN_OEUVRE -> "Wafanyakazi"
        }
    }

    fun idTypeLabel(type: IdDocumentType, lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> when (type) {
            IdDocumentType.VOTER_CARD -> "Carte d'électeur"
            IdDocumentType.PASSPORT -> "Passeport"
            IdDocumentType.DRIVER_LICENSE -> "Permis de conduire"
            IdDocumentType.ATTESTATION -> "Attestation d'identité"
        }
        AppLanguage.SW -> when (type) {
            IdDocumentType.VOTER_CARD -> "Kadi ya mpiga kura"
            IdDocumentType.PASSPORT -> "Pasipoti"
            IdDocumentType.DRIVER_LICENSE -> "Leseni ya udereva"
            IdDocumentType.ATTESTATION -> "Cheti cha utambulisho"
        }
    }

    fun sectorLabel(sector: AgriculturalSector, lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> when (sector) {
            AgriculturalSector.VIVRIER -> "Cultures vivrières"
            AgriculturalSector.MARAICHAGE -> "Maraîchage"
            AgriculturalSector.CAFE_CACAO -> "Café & Cacao"
            AgriculturalSector.ELEVAGE -> "Élevage & Pisciculture"
            AgriculturalSector.AGROFORESTERIE -> "Agroforesterie"
            AgriculturalSector.AUTRE -> "Autre secteur"
        }
        AppLanguage.SW -> when (sector) {
            AgriculturalSector.VIVRIER -> "Mazao ya chakula"
            AgriculturalSector.MARAICHAGE -> "Kilimo cha mboga"
            AgriculturalSector.CAFE_CACAO -> "Kahawa na Kakao"
            AgriculturalSector.ELEVAGE -> "Ufugaji na Samaki"
            AgriculturalSector.AGROFORESTERIE -> "Kilimo mseto"
            AgriculturalSector.AUTRE -> "Sekta nyingine"
        }
    }

    fun identityStatusLabel(status: IdentityStatus, lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> when (status) {
            IdentityStatus.UNVERIFIED -> "Non inscrit"
            IdentityStatus.PENDING -> "En attente"
            IdentityStatus.VERIFIED -> "Vérifiée"
        }
        AppLanguage.SW -> when (status) {
            IdentityStatus.UNVERIFIED -> "Hajasajiliwa"
            IdentityStatus.PENDING -> "Inasubiri"
            IdentityStatus.VERIFIED -> "Imethibitishwa"
        }
    }

    fun creditStatusLabel(status: CreditStatus, lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> when (status) {
            CreditStatus.IN_PROGRESS -> "En cours"
            CreditStatus.ACCEPTED -> "Acceptée"
            CreditStatus.REJECTED -> "Refusée"
        }
        AppLanguage.SW -> when (status) {
            CreditStatus.IN_PROGRESS -> "Inaendelea"
            CreditStatus.ACCEPTED -> "Imekubaliwa"
            CreditStatus.REJECTED -> "Imekataliwa"
        }
    }

    fun transactionTypeLabel(type: TransactionType, lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> when (type) {
            TransactionType.DISBURSEMENT -> "Décaissement"
            TransactionType.REPAYMENT -> "Remboursement"
        }
        AppLanguage.SW -> when (type) {
            TransactionType.DISBURSEMENT -> "Utoaji wa mkopo"
            TransactionType.REPAYMENT -> "Marejesho"
        }
    }

    fun transactionStatusLabel(status: TransactionStatus, lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> when (status) {
            TransactionStatus.COMPLETED -> "Confirmée"
            TransactionStatus.PENDING -> "En attente"
        }
        AppLanguage.SW -> when (status) {
            TransactionStatus.COMPLETED -> "Imethibitishwa"
            TransactionStatus.PENDING -> "Inasubiri"
        }
    }

    fun riskLevelLabel(level: RiskLevel, lang: AppLanguage): String = when (lang) {
        AppLanguage.FR -> when (level) {
            RiskLevel.FAIBLE -> "Risque faible"
            RiskLevel.MOYEN -> "Risque moyen"
            RiskLevel.ELEVE -> "Risque élevé"
        }
        AppLanguage.SW -> when (level) {
            RiskLevel.FAIBLE -> "Hatari ndogo"
            RiskLevel.MOYEN -> "Hatari ya kati"
            RiskLevel.ELEVE -> "Hatari kubwa"
        }
    }

    fun months(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.FR -> listOf(
            "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        )
        AppLanguage.SW -> listOf(
            "Januari", "Februari", "Machi", "Aprili", "Mei", "Juni",
            "Julai", "Agosti", "Septemba", "Oktoba", "Novemba", "Desemba"
        )
    }
}

data class LocalizedStrings(
    val appName: String,
    val splashSubtitle: String,
    val retry: String,
    val cancel: String,
    val confirm: String,
    val close: String,
    val skip: String,
    val next: String,
    val start: String,
    val back: String,
    val send: String,
    val save: String,
    val continueText: String,

    // Auth / Account Gate
    val authWelcomeBadge: String,
    val authTitle: String,
    val authSubtitle: String,
    val authBulletIdentity: String,
    val authBulletCredit: String,
    val authBulletTransactions: String,
    val signInWithGoogle: String,
    val signingIn: String,
    val signOut: String,
    val signedInAccountLabel: String,
    val noProfileRegisteredTitle: String,
    val noProfileRegisteredDesc: String,
    val createAccountCta: String,

    // Onboarding
    val onboard1Title: String,
    val onboard1Subtitle: String,
    val onboard1Desc: String,
    val onboard2Title: String,
    val onboard2Desc: String,
    val onboard3Title: String,
    val onboard3Desc: String,
    val onboard4Title: String,
    val onboard4Desc: String,
    val onboardConsentLabel: String,
    val onboardViewTerms: String,
    val termsDialogTitle: String,
    val termsDialogBody: String,

    // Bottom Navigation
    val navHome: String,
    val navCredit: String,
    val navHistory: String,
    val navProfile: String,

    // Home
    val greetingPrefix: String,
    val identityCardTitle: String,
    val identityCardDescUnverified: String,
    val identityCardDescPending: String,
    val identityCardDescVerified: String,
    val actionEnrollTitle: String,
    val actionEnrollSub: String,
    val actionFaceLoginTitle: String,
    val actionFaceLoginSub: String,
    val actionRequestCreditTitle: String,
    val actionRequestCreditSub: String,
    val actionCreditDisabledHint: String,
    val myRequestsTitle: String,
    val emptyRequestsHome: String,
    val recentTransactionsTitle: String,
    val emptyTransactionsText: String,
    val totalDisbursedLabel: String,
    val totalRepaidLabel: String,
    val remainingBalanceLabel: String,

    // Enrollment
    val enrollTitle: String,
    val stepLabel: String,
    val enrollStep1Title: String,
    val enrollStep2Title: String,
    val enrollStep3Title: String,
    val enrollStep4Title: String,
    val fieldNom: String,
    val fieldPostnom: String,
    val fieldPrenom: String,
    val fieldDob: String,
    val selectDobHint: String,
    val fieldSex: String,
    val sexMale: String,
    val sexFemale: String,
    val fieldPhone: String,
    val fieldProvince: String,
    val fieldLocation: String,
    val fieldLocationHint: String,
    val fieldAgriculturalSector: String,
    val fieldIdType: String,
    val fieldIdNumber: String,
    val importIdPhoto: String,
    val idPhotoSelected: String,
    val selfieGuideFrame: String,
    val selfieGuideLight: String,
    val selfieGuideGlasses: String,
    val captureSelfie: String,
    val importPhotoFallback: String,
    val retake: String,
    val validate: String,
    val cameraPermissionTitle: String,
    val cameraPermissionBtn: String,
    val cameraUnavailableError: String,
    val openFullScreenCamera: String,
    val switchCamera: String,
    val closeCamera: String,
    val enrollSummaryTitle: String,
    val enrollLoading: String,
    val enrollSuccessTitle: String,
    val enrollSuccessDesc: String,
    val errRequiredField: String,
    val errInvalidPhone: String,
    val errSelfieRequired: String,

    // Face Verification
    val faceVerifyTitle: String,
    val faceVerifyPrompt: String,
    val faceVerifyScanning: String,
    val faceVerifySuccessTitle: String,
    val faceVerifyMatchScore: String,
    val faceVerifyLivenessOk: String,
    val faceVerifyFailTitle: String,
    val faceVerifyFailTips: String,
    val faceVerifyAttemptsLeft: String,
    val faceVerifyMaxAttempts: String,
    val faceVerifyNoProfile: String,
    val simulateBlinkVerify: String,
    val faceVerifyDemoNotice: String,
    val faceVerifyFailNoFace: String,
    val faceVerifyFailMultipleFaces: String,
    val faceVerifyFailEyesClosed: String,
    val faceVerifyFailMockMode: String,

    // Credit Assistant
    val assistantTitle: String,
    val assistantSubtitle: String,
    val assistantQuestionProgress: String,
    val assistantCalculating: String,
    val assistantInputHint: String,
    val assistantMicHint: String,
    val assistantMicListening: String,
    val assistantMicUnavailable: String,
    val assistantNeedsVerificationTitle: String,
    val assistantNeedsVerificationBody: String,
    val assistantGoVerify: String,
    val aiOfflineFallbackNotice: String,
    val q1Crop: String,
    val q2Area: String,
    val q3Harvest: String,
    val q4Experience: String,
    val q5Income: String,
    val q6Amount: String,
    val q7Duration: String,
    val q8Purpose: String,
    val yearsUnit: String,
    val hectaresUnit: String,
    val monthsUnit: String,
    val usdPerMonth: String,

    // Credit Result
    val resultTitle: String,
    val scoreOutOf100: String,
    val whyThisLevel: String,
    val proposedOffer: String,
    val offerAmount: String,
    val offerDuration: String,
    val offerMonthly: String,
    val harvestScheduleTitle: String,
    val harvestPeakBadge: String,
    val personalizedAdvice: String,
    val acceptRequest: String,
    val modifyRequest: String,
    val downloadSummary: String,
    val acceptDialogTitle: String,
    val acceptDialogBody: String,
    val finalConfirmedTitle: String,
    val finalConfirmedDesc: String,
    val summarySharedToast: String,

    // History & Transactions
    val historyTitle: String,
    val tabRequests: String,
    val tabTransactions: String,
    val filterAll: String,
    val filterInProgress: String,
    val filterAccepted: String,
    val filterRejected: String,
    val historyEmptyTitle: String,
    val historyEmptyDesc: String,
    val transactionsEmptyTitle: String,
    val transactionsEmptyDesc: String,
    val recordRepaymentTitle: String,
    val paymentChannelLabel: String,
    val paymentAmountLabel: String,
    val paymentReferenceLabel: String,
    val paymentSavedToast: String,

    // Profile & Settings
    val profileTitle: String,
    val languageLabel: String,
    val themeLabel: String,
    val themeAuto: String,
    val themeLight: String,
    val themeDark: String,
    val largeTextLabel: String,
    val largeTextSub: String,
    val notificationsLabel: String,
    val cloudSyncTitle: String,
    val cloudSyncSubtitle: String,
    val deleteDataTitle: String,
    val deleteDataSub: String,
    val deleteDataConfirmBody: String,
    val dataDeletedToast: String,
    val aboutTitle: String,
    val aboutBody: String,
    val halisiErrorNetwork: String,
    val devDemoTitle: String,
    val devDemoSubtitle: String,
    val devModeNormal: String,
    val devModeFaceFail: String,
    val devModeNetworkFail: String,
    val devModeSlow: String,

    // Offline Cache & Credit Notifications
    val offlineBannerTitle: String,
    val offlineBannerDesc: String,
    val lastSyncedLabel: String,
    val cachedLocallyBadge: String,
    val creditNotificationsTitle: String,
    val creditNotificationsEmpty: String
)

val FrenchStrings = LocalizedStrings(
    appName = "Mkulima Pay",
    splashSubtitle = "Identité numérique & microcrédit agricole",
    retry = "Réessayer",
    cancel = "Annuler",
    confirm = "Confirmer",
    close = "Fermer",
    skip = "Passer",
    next = "Suivant",
    start = "Commencer",
    back = "Retour",
    send = "Envoyer",
    save = "Enregistrer",
    continueText = "Continuer",

    authWelcomeBadge = "Identité numérique • RDC",
    authTitle = "Connexion sécurisée",
    authSubtitle = "Connectez-vous avec votre compte Google pour accéder à votre profil agriculteur, votre secteur agricole et vos transactions réelles.",
    authBulletIdentity = "Aucun profil fictif : vos données sont liées à votre compte réel.",
    authBulletCredit = "Évaluation transparente et calendrier adapté à votre récolte.",
    authBulletTransactions = "Suivi réel des décaissements et remboursements.",
    signInWithGoogle = "Continuer avec Google",
    signingIn = "Connexion en cours…",
    signOut = "Se déconnecter",
    signedInAccountLabel = "Compte connecté",
    noProfileRegisteredTitle = "Aucun compte agriculteur enregistré",
    noProfileRegisteredDesc = "Vous êtes connecté, mais aucun profil d'identité Mkulima Pay n'a encore été créé pour ce compte.",
    createAccountCta = "Créer mon profil agriculteur",

    onboard1Title = "Bienvenue sur Mkulima Pay",
    onboard1Subtitle = "« Mkulima » = agriculteur",
    onboard1Desc = "Votre identité et votre crédit, simples et sécurisés, conçus pour les producteurs de la RDC.",
    onboard2Title = "Identité sécurisée",
    onboard2Desc = "Enrôlez-vous rapidement avec votre pièce d'identité et un selfie biométrique pour protéger votre compte.",
    onboard3Title = "Microcrédit responsable",
    onboard3Desc = "Échangez avec Assistant Mkulima pour estimer votre capacité d'emprunt et préparer votre demande.",
    onboard4Title = "Vos données protégées",
    onboard4Desc = "Vos informations personnelles restent isolées sur votre compte et ne servent qu'à votre identité et vos transactions réelles.",
    onboardConsentLabel = "J'accepte le traitement de mes données",
    onboardViewTerms = "Lire les conditions d'utilisation",
    termsDialogTitle = "Conditions & confidentialité",
    termsDialogBody = "Mkulima Pay collecte uniquement vos informations d'identité, votre photo biométrique et vos données agricoles afin de vérifier votre identité et gérer vos demandes et transactions de microcrédit réelles. Vous pouvez supprimer toutes vos données à tout moment depuis votre Profil.",

    navHome = "Accueil",
    navCredit = "Crédit",
    navHistory = "Historique",
    navProfile = "Profil",

    greetingPrefix = "Mbote,",
    identityCardTitle = "Statut Identité",
    identityCardDescUnverified = "Vous n'avez pas encore de profil agriculteur. Créez votre profil pour démarrer.",
    identityCardDescPending = "Profil enregistré. Confirmez votre identité par vérification faciale.",
    identityCardDescVerified = "Identité confirmée. Vous pouvez demander un microcrédit adapté à votre récolte.",
    actionEnrollTitle = "Créer mon profil",
    actionEnrollSub = "Enrôlement identité & selfie",
    actionFaceLoginTitle = "Vérifier mon visage",
    actionFaceLoginSub = "Vérification biométrique réelle",
    actionRequestCreditTitle = "Demander un crédit",
    actionRequestCreditSub = "Préparer une demande avec Assistant Mkulima",
    actionCreditDisabledHint = "Créez votre profil et vérifiez votre visage pour débloquer le microcrédit.",
    myRequestsTitle = "Mes demandes de crédit",
    emptyRequestsHome = "Aucune demande enregistrée sur votre compte.",
    recentTransactionsTitle = "Transactions réelles",
    emptyTransactionsText = "Aucune transaction enregistrée pour le moment.",
    totalDisbursedLabel = "Décaissé",
    totalRepaidLabel = "Remboursé",
    remainingBalanceLabel = "Solde restant",

    enrollTitle = "Inscription Agriculteur",
    stepLabel = "Étape",
    enrollStep1Title = "Informations personnelles",
    enrollStep2Title = "Pièce d'identité",
    enrollStep3Title = "Selfie biométrique",
    enrollStep4Title = "Récapitulatif",
    fieldNom = "Nom",
    fieldPostnom = "Postnom",
    fieldPrenom = "Prénom",
    fieldDob = "Date de naissance",
    selectDobHint = "Choisir la date",
    fieldSex = "Sexe",
    sexMale = "Homme",
    sexFemale = "Femme",
    fieldPhone = "Téléphone (+243)",
    fieldProvince = "Province / Territoire",
    fieldLocation = "Localité / Village",
    fieldLocationHint = "Ex : Territoire de Kipushi, Secteur Bukanda",
    fieldAgriculturalSector = "Secteur agricole",
    fieldIdType = "Type de pièce",
    fieldIdNumber = "Numéro de la pièce",
    importIdPhoto = "Importer une photo du document (optionnel)",
    idPhotoSelected = "Photo du document ajoutée",
    selfieGuideFrame = "Placez votre visage dans le cadre",
    selfieGuideLight = "Bonne lumière",
    selfieGuideGlasses = "Retirez lunettes et chapeau",
    captureSelfie = "Capturer le selfie",
    importPhotoFallback = "Importer une photo",
    retake = "Reprendre",
    validate = "Valider",
    cameraPermissionTitle = "Accès caméra requis pour la biométrie",
    cameraPermissionBtn = "Autoriser la caméra",
    cameraUnavailableError = "Caméra indisponible ou capture impossible. Utilisez « Importer une photo ».",
    openFullScreenCamera = "Plein écran",
    switchCamera = "Changer de caméra",
    closeCamera = "Fermer la caméra",
    enrollSummaryTitle = "Vérifiez vos informations",
    enrollLoading = "Enregistrement sécurisé de votre compte…",
    enrollSuccessTitle = "Profil enregistré",
    enrollSuccessDesc = "Votre identité numérique est enregistrée. Effectuez la vérification faciale pour activer vos demandes de crédit.",
    errRequiredField = "Ce champ est obligatoire",
    errInvalidPhone = "Numéro RDC invalide (9 chiffres après +243)",
    errSelfieRequired = "Veuillez capturer ou importer une vraie photo selfie",

    faceVerifyTitle = "Vérification faciale",
    faceVerifyPrompt = "Regardez la caméra avec un bon éclairage",
    faceVerifyScanning = "Analyse biométrique de votre photo en cours…",
    faceVerifySuccessTitle = "Identité vérifiée",
    faceVerifyMatchScore = "Score biométrique : %d%%",
    faceVerifyLivenessOk = "Image et éclairage validés",
    faceVerifyFailTitle = "Vérification refusée",
    faceVerifyFailTips = "Placez-vous face à la lumière, sans lunettes, et capturez une image nette.",
    faceVerifyAttemptsLeft = "Tentatives restantes : %d",
    faceVerifyMaxAttempts = "Nombre maximal d'essais (3) atteint. Vérifiez votre éclairage ou mettez à jour votre selfie.",
    faceVerifyNoProfile = "Aucun profil enregistré. Veuillez d'abord créer votre profil avec un selfie de référence.",
    simulateBlinkVerify = "Capturer et vérifier",
    faceVerifyDemoNotice = "Mode démonstration : correspondance simulée",
    faceVerifyFailNoFace = "Aucun visage détecté. Placez bien votre visage au centre du cadre.",
    faceVerifyFailMultipleFaces = "Plusieurs visages détectés. Une seule personne doit être devant la caméra.",
    faceVerifyFailEyesClosed = "Yeux fermés ou regard peu visible. Regardez la caméra les yeux ouverts.",
    faceVerifyFailMockMode = "Échec de correspondance simulé (mode ECHEC_VISAGE actif).",

    assistantTitle = "Assistant Mkulima",
    assistantSubtitle = "Évaluation agricole guidée",
    assistantQuestionProgress = "Question %d sur %d",
    assistantCalculating = "Évaluation de votre demande…",
    assistantInputHint = "Écrire une réponse…",
    assistantMicHint = "Dicter au micro",
    assistantMicListening = "Écoute en cours… parlez maintenant",
    assistantMicUnavailable = "Reconnaissance vocale indisponible sur cet appareil",
    assistantNeedsVerificationTitle = "Identité non vérifiée",
    assistantNeedsVerificationBody = "Pour protéger votre microcrédit, veuillez d'abord créer votre profil et valider la vérification faciale.",
    assistantGoVerify = "Vérifier mon visage",
    aiOfflineFallbackNotice = "Réponse automatique (mode hors ligne)",
    q1Crop = "Quelle est votre culture principale pour cette saison ?",
    q2Area = "Quelle superficie cultivez-vous (en hectares) ?",
    q3Harvest = "Pour quel mois attendez-vous votre prochaine récolte ?",
    q4Experience = "Depuis combien d'années pratiquez-vous l'agriculture ?",
    q5Income = "Quel est votre revenu mensuel estimé (en USD) ?",
    q6Amount = "Quel montant de microcrédit souhaitez-vous emprunter (USD) ?",
    q7Duration = "Sur quelle durée préférez-vous rembourser ?",
    q8Purpose = "Quel est l'objectif principal de ce crédit ?",
    yearsUnit = "ans",
    hectaresUnit = "ha",
    monthsUnit = "mois",
    usdPerMonth = "USD / mois",

    resultTitle = "Analyse du microcrédit",
    scoreOutOf100 = "Score de confiance : %d / 100",
    whyThisLevel = "Pourquoi ce niveau ?",
    proposedOffer = "Offre proposée",
    offerAmount = "Montant",
    offerDuration = "Durée",
    offerMonthly = "Mensualité moyenne",
    harvestScheduleTitle = "Calendrier aligné sur la récolte",
    harvestPeakBadge = "Pic récolte",
    personalizedAdvice = "Conseils personnalisés",
    acceptRequest = "Accepter et décaisser",
    modifyRequest = "Modifier",
    downloadSummary = "Partager le résumé",
    acceptDialogTitle = "Confirmer l'acceptation du crédit",
    acceptDialogBody = "Souhaitez-vous enregistrer cette demande de %d USD sur %d mois et générer la transaction de décaissement ?",
    finalConfirmedTitle = "Crédit et transaction enregistrés",
    finalConfirmedDesc = "Votre demande acceptée et son décaissement sont enregistrés dans votre compte. Suivez vos remboursements dans l'Historique.",
    summarySharedToast = "Résumé prêt pour partage",

    historyTitle = "Historique & Transactions",
    tabRequests = "Demandes",
    tabTransactions = "Transactions",
    filterAll = "Toutes",
    filterInProgress = "En cours",
    filterAccepted = "Acceptées",
    filterRejected = "Refusées",
    historyEmptyTitle = "Aucune demande enregistrée",
    historyEmptyDesc = "Vous n'avez encore soumis aucune demande de microcrédit.",
    transactionsEmptyTitle = "Aucune transaction",
    transactionsEmptyDesc = "Les décaissements et remboursements réels apparaîtront ici dès qu'un crédit est accepté.",
    recordRepaymentTitle = "Enregistrer un remboursement",
    paymentChannelLabel = "Canal Mobile Money",
    paymentAmountLabel = "Montant remboursé (USD)",
    paymentReferenceLabel = "Référence transaction",
    paymentSavedToast = "Remboursement enregistré",

    profileTitle = "Profil & Compte",
    languageLabel = "Langue de l'application",
    themeLabel = "Apparence",
    themeAuto = "Auto",
    themeLight = "Clair",
    themeDark = "Sombre",
    largeTextLabel = "Grand texte",
    largeTextSub = "Augmente la taille de lecture partout",
    notificationsLabel = "Notifications de suivi",
    cloudSyncTitle = "Synchronisation Cloud",
    cloudSyncSubtitle = "Vos données, demandes et transactions réelles sont sauvegardées sur votre compte.",
    deleteDataTitle = "Supprimer mes données",
    deleteDataSub = "Efface votre profil, vos demandes et vos transactions",
    deleteDataConfirmBody = "Cette action supprimera définitivement votre profil agriculteur, vos demandes de crédit et votre historique de transactions de votre compte. Continuer ?",
    dataDeletedToast = "Toutes vos données ont été supprimées",
    aboutTitle = "À propos de Mkulima Pay",
    aboutBody = "Mkulima Pay (v1.0) gère l'identité numérique et le microcrédit des agriculteurs de la RDC.",
    halisiErrorNetwork = "Erreur lors de l'enregistrement ou de la vérification. Veuillez réessayer.",
    devDemoTitle = "Mode démo (développeur)",
    devDemoSubtitle = "Simule les réponses Halisi pour tester réussite et échecs",
    devModeNormal = "Normal",
    devModeFaceFail = "Échec visage",
    devModeNetworkFail = "Échec réseau",
    devModeSlow = "Réseau lent",

    offlineBannerTitle = "Mode hors ligne actif",
    offlineBannerDesc = "Aucune connexion Internet. Votre statut d'identité et vos dernières notifications sont affichés depuis le cache local.",
    lastSyncedLabel = "Dernière synchro : %s",
    cachedLocallyBadge = "Cache local",
    creditNotificationsTitle = "Dernières notifications de crédit",
    creditNotificationsEmpty = "Aucune notification de crédit récente."
)

val SwahiliStrings = LocalizedStrings(
    appName = "Mkulima Pay",
    splashSubtitle = "Utambulisho wa kidijitali na mikopo midogo ya kilimo",
    retry = "Jaribu tena",
    cancel = "Ghairi",
    confirm = "Thibitisha",
    close = "Funga",
    skip = "Ruka",
    next = "Inayofuata",
    start = "Anza",
    back = "Rudi",
    send = "Tuma",
    save = "Hifadhi",
    continueText = "Endelea",

    authWelcomeBadge = "Utambulisho wa kidijitali • DRC",
    authTitle = "Ingia kwa usalama",
    authSubtitle = "Ingia ukitumia akaunti yako ya Google ili kufikia wasifu wako wa mkulima, sekta yako ya kilimo na miamala halisi.",
    authBulletIdentity = "Hakuna data bandia: kila akaunti ni ya mkulima halisi.",
    authBulletCredit = "Tathmini ya uwazi na ratiba kulingana na mavuno yako.",
    authBulletTransactions = "Ufuatiliaji halisi wa utoaji na marejesho ya mkopo.",
    signInWithGoogle = "Endelea na Google",
    signingIn = "Inaingia…",
    signOut = "Ondoka",
    signedInAccountLabel = "Akaunti iliyounganishwa",
    noProfileRegisteredTitle = "Hakuna wasifu wa mkulima bado",
    noProfileRegisteredDesc = "Umeingia, lakini bado hujasajili wasifu wa utambulisho wa Mkulima Pay kwa akaunti hii.",
    createAccountCta = "Unda wasifu wangu wa mkulima",

    onboard1Title = "Karibu Mkulima Pay",
    onboard1Subtitle = "« Mkulima » = mkulima",
    onboard1Desc = "Utambulisho wako na mkopo wako kwa urahisi na usalama, kwa wakulima wa DRC.",
    onboard2Title = "Utambulisho salama",
    onboard2Desc = "Jisajili haraka na kitambulisho chako na picha ya uso ili kulinda akaunti yako.",
    onboard3Title = "Mikopo yenye uwajibikaji",
    onboard3Desc = "Tumia Msaidizi wa Mkulima kutathmini hatari na kupata ushauri wa kilimo.",
    onboard4Title = "Data yako inalindwa",
    onboard4Desc = "Taarifa zako zinalindwa kwenye akaunti yako binafsi na zinatumika tu kwa utambulisho na miamala yako halisi.",
    onboardConsentLabel = "Ninakubali uchakataji wa data yangu",
    onboardViewTerms = "Soma masharti ya matumizi",
    termsDialogTitle = "Masharti na faragha",
    termsDialogBody = "Mkulima Pay hukusanya taarifa zako za utambulisho, picha ya uso na data za kilimo ili kuthibitisha utambulisho wako na kusimamia mikopo na miamala yako halisi. Unaweza kufuta data yako wakati wowote kwenye Wasifu.",

    navHome = "Nyumbani",
    navCredit = "Mkopo",
    navHistory = "Historia",
    navProfile = "Wasifu",

    greetingPrefix = "Jambo,",
    identityCardTitle = "Hali ya Utambulisho",
    identityCardDescUnverified = "Bado huna wasifu wa mkulima. Unda wasifu wako ili kuanza.",
    identityCardDescPending = "Wasifu umehifadhiwa. Thibitisha uso wako ili kuendelea.",
    identityCardDescVerified = "Utambulisho umethibitishwa. Sasa unaweza kuomba mkopo wa mavuno.",
    actionEnrollTitle = "Unda wasifu",
    actionEnrollSub = "Usajili wa kitambulisho na uso",
    actionFaceLoginTitle = "Thibitisha uso",
    actionFaceLoginSub = "Uthibitisho halisi wa uso",
    actionRequestCreditTitle = "Omba mkopo",
    actionRequestCreditSub = "Andaa ombi na Msaidizi wa Mkulima",
    actionCreditDisabledHint = "Unda wasifu na uthibitishe uso wako kwanza ili kufungua mikopo.",
    myRequestsTitle = "Maombi yangu ya mkopo",
    emptyRequestsHome = "Hakuna maombi yaliyosajiliwa kwenye akaunti yako.",
    recentTransactionsTitle = "Miamala halisi",
    emptyTransactionsText = "Hakuna miamala bado.",
    totalDisbursedLabel = "Iliyotolewa",
    totalRepaidLabel = "Iliyorejeshwa",
    remainingBalanceLabel = "Salio lililobaki",

    enrollTitle = "Usajili wa Mkulima",
    stepLabel = "Hatua",
    enrollStep1Title = "Taarifa binafsi",
    enrollStep2Title = "Kitambulisho",
    enrollStep3Title = "Picha ya uso",
    enrollStep4Title = "Muhtasari",
    fieldNom = "Jina la ukoo",
    fieldPostnom = "Jina la kati",
    fieldPrenom = "Jina la kwanza",
    fieldDob = "Tarehe ya kuzaliwa",
    selectDobHint = "Chagua tarehe",
    fieldSex = "Jinsia",
    sexMale = "Mwanaume",
    sexFemale = "Mwanamke",
    fieldPhone = "Simu (+243)",
    fieldProvince = "Mkoa / Wilaya",
    fieldLocation = "Eneo / Kijiji",
    fieldLocationHint = "Mfano: Wilaya ya Rutshuru, Kijiji Kiwanja",
    fieldAgriculturalSector = "Sekta ya kilimo",
    fieldIdType = "Aina ya kitambulisho",
    fieldIdNumber = "Namba ya kitambulisho",
    importIdPhoto = "Pakia picha ya kitambulisho (si lazima)",
    idPhotoSelected = "Picha ya kitambulisho imeongezwa",
    selfieGuideFrame = "Weka uso wako ndani ya duara",
    selfieGuideLight = "Mwanga mzuri",
    selfieGuideGlasses = "Vua miwani na kofia",
    captureSelfie = "Piga picha ya uso",
    importPhotoFallback = "Chagua picha",
    retake = "Piga tena",
    validate = "Thibitisha",
    cameraPermissionTitle = "Ruhusa ya kamera inahitajika",
    cameraPermissionBtn = "Ruhusu kamera",
    cameraUnavailableError = "Kamera haipatikani. Tafadhali tumia « Chagua picha ».",
    openFullScreenCamera = "Skrini nzima",
    switchCamera = "Badilisha kamera",
    closeCamera = "Funga kamera",
    enrollSummaryTitle = "Kagua taarifa zako",
    enrollLoading = "Inahifadhi akaunti yako salama…",
    enrollSuccessTitle = "Wasifu umehifadhiwa",
    enrollSuccessDesc = "Utambulisho wako umehifadhiwa. Fanya uthibitisho wa uso ili kufungua maombi ya mkopo.",
    errRequiredField = "Sehemu hii inahitajika",
    errInvalidPhone = "Namba ya DRC si sahihi (tarakimu 9 baada ya +243)",
    errSelfieRequired = "Tafadhali piga au chagua picha halisi ya uso",

    faceVerifyTitle = "Uthibitisho wa uso",
    faceVerifyPrompt = "Tazama kamera kwenye mwanga mzuri",
    faceVerifyScanning = "Inachambua picha yako…",
    faceVerifySuccessTitle = "Utambulisho umethibitishwa",
    faceVerifyMatchScore = "Alama ya picha: %d%%",
    faceVerifyLivenessOk = "Picha na mwanga vimethibitishwa",
    faceVerifyFailTitle = "Uthibitisho umekataliwa",
    faceVerifyFailTips = "Simama kwenye mwanga mzuri, vua miwani, na piga picha iliyo wazi.",
    faceVerifyAttemptsLeft = "Majaribio yaliyobaki: %d",
    faceVerifyMaxAttempts = "Umefikia majaribio 3. Angalia mwanga au sasisha picha yako kwenye usajili.",
    faceVerifyNoProfile = "Hakuna wasifu uliosajiliwa. Tafadhali jisajili kwanza ili kuhifadhi picha ya uso.",
    simulateBlinkVerify = "Piga picha na thibitisha",
    faceVerifyDemoNotice = "Hali ya maonyesho: ulinganisho wa mfano",
    faceVerifyFailNoFace = "Hakuna uso uliogunduliwa. Weka uso wako katikati ya duara.",
    faceVerifyFailMultipleFaces = "Nyuso nyingi zimegunduliwa. Mtu mmoja tu anapaswa kuonekana.",
    faceVerifyFailEyesClosed = "Macho yamefungwa au hayaonekani. Fungua macho ukitazama kamera.",
    faceVerifyFailMockMode = "Kushindwa kwa majaribio (hali ya ECHEC_VISAGE imewashwa).",

    assistantTitle = "Msaidizi wa Mkulima",
    assistantSubtitle = "Tathmini ya kilimo hatua kwa hatua",
    assistantQuestionProgress = "Swali %d kati ya %d",
    assistantCalculating = "Inakokotoa wasifu wako…",
    assistantInputHint = "Andika jibu…",
    assistantMicHint = "Sema kwenye maikrofoni",
    assistantMicListening = "Inasikiliza… ongea sasa",
    assistantMicUnavailable = "Utambuzi wa sauti haupatikani kwenye kifaa hiki",
    assistantNeedsVerificationTitle = "Utambulisho haujathibitishwa",
    assistantNeedsVerificationBody = "Ili kulinda mkopo wako, tafadhali unda wasifu na uthibitishe uso kwanza.",
    assistantGoVerify = "Thibitisha uso wangu",
    aiOfflineFallbackNotice = "Jibu la kiotomatiki (hali ya nje ya mtandao)",
    q1Crop = "Zao lako kuu kwa msimu huu ni lipi?",
    q2Area = "Unalima eneo la ukubwa gani (hektari)?",
    q3Harvest = "Unatarajia kuvuna katika mwezi gani?",
    q4Experience = "Una uzoefu wa miaka mingapi katika kilimo?",
    q5Income = "Kipato chako cha mwezi kinakadiriwa kuwa kiasi gani (USD)?",
    q6Amount = "Unahitaji mkopo wa kiasi gani (USD)?",
    q7Duration = "Ungependa kulipa kwa muda gani?",
    q8Purpose = "Lengo kuu la mkopo huu ni lipi?",
    yearsUnit = "miaka",
    hectaresUnit = "ha",
    monthsUnit = "miezi",
    usdPerMonth = "USD / mwezi",

    resultTitle = "Matokeo ya mkopo",
    scoreOutOf100 = "Alama ya uaminifu: %d / 100",
    whyThisLevel = "Kwa nini kiwango hiki?",
    proposedOffer = "Pendekezo la mkopo",
    offerAmount = "Kiasi",
    offerDuration = "Muda",
    offerMonthly = "Malipo ya mwezi",
    harvestScheduleTitle = "Ratiba kulingana na mavuno",
    harvestPeakBadge = "Kilele cha mavuno",
    personalizedAdvice = "Ushauri binafsi",
    acceptRequest = "Kubali na pokea",
    modifyRequest = "Rekebisha",
    downloadSummary = "Shiriki muhtasari",
    acceptDialogTitle = "Thibitisha ombi la mkopo",
    acceptDialogBody = "Je, ungependa kukubali ombi hili la %d USD kwa miezi %d na kusajili muamala wa utoaji?",
    finalConfirmedTitle = "Mkopo na muamala vimehifadhiwa",
    finalConfirmedDesc = "Ombi lako lililokubaliwa na utoaji wa fedha vimehifadhiwa kwenye akaunti yako. Fuatilia marejesho kwenye Historia.",
    summarySharedToast = "Muhtasari uko tayari kushirikiwa",

    historyTitle = "Historia na Miamala",
    tabRequests = "Maombi",
    tabTransactions = "Miamala",
    filterAll = "Yote",
    filterInProgress = "Inaendelea",
    filterAccepted = "Imekubaliwa",
    filterRejected = "Imekataliwa",
    historyEmptyTitle = "Hakuna maombi yaliyohifadhiwa",
    historyEmptyDesc = "Bado hujatuma ombi lolote la mkopo.",
    transactionsEmptyTitle = "Hakuna miamala",
    transactionsEmptyDesc = "Miamala halisi ya utoaji na marejesho itaonekana hapa mkopo ukishakubaliwa.",
    recordRepaymentTitle = "Rekodi marejesho",
    paymentChannelLabel = "Njia ya Mobile Money",
    paymentAmountLabel = "Kiasi kilicholipwa (USD)",
    paymentReferenceLabel = "Kumbukumbu ya muamala",
    paymentSavedToast = "Marejesho yamehifadhiwa",

    profileTitle = "Wasifu na Akaunti",
    languageLabel = "Lugha ya programu",
    themeLabel = "Mwonekano",
    themeAuto = "Otomatiki",
    themeLight = "Mwangaza",
    themeDark = "Giza",
    largeTextLabel = "Maandishi makubwa",
    largeTextSub = "Huongeza ukubwa wa herufi kote",
    notificationsLabel = "Arifa za ufuatiliaji",
    cloudSyncTitle = "Usawazishaji wa Wingu",
    cloudSyncSubtitle = "Wasifu wako, maombi na miamala halisi vinahifadhiwa kwenye akaunti yako.",
    deleteDataTitle = "Futa data yangu",
    deleteDataSub = "Hufuta wasifu, maombi na miamala yako",
    deleteDataConfirmBody = "Hatua hii itafuta kabisa wasifu wako, maombi ya mkopo na historia ya miamala kwenye akaunti yako. Endelea?",
    dataDeletedToast = "Data zote zimefutwa",
    aboutTitle = "Kuhusu Mkulima Pay",
    aboutBody = "Mkulima Pay (v1.0) inasimamia utambulisho wa kidijitali na mikopo kwa wakulima wa DRC.",
    halisiErrorNetwork = "Hitilafu wakati wa kuhifadhi au kuthibitisha. Tafadhali jaribu tena.",
    devDemoTitle = "Hali ya maonyesho (msanidi)",
    devDemoSubtitle = "Huiga majibu ya Halisi ili kujaribu mafanikio na hitilafu",
    devModeNormal = "Kawaida",
    devModeFaceFail = "Kushindwa uso",
    devModeNetworkFail = "Hitilafu mtandao",
    devModeSlow = "Mtandao polepole",

    offlineBannerTitle = "Hali ya nje ya mtandao",
    offlineBannerDesc = "Hakuna mtandao wa intaneti. Hali yako ya utambulisho na arifa za hivi karibuni zinaonyeshwa kutoka hifadhi ya ndani.",
    lastSyncedLabel = "Usawazishaji wa mwisho: %s",
    cachedLocallyBadge = "Hifadhi ya ndani",
    creditNotificationsTitle = "Arifa za hivi karibuni za mkopo",
    creditNotificationsEmpty = "Hakuna arifa mpya za mkopo."
)
