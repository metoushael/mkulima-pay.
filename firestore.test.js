const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_farmer_01";
const BOB_UID = "bob_farmer_02";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    userId: uid,
    email: "alice@example.com",
    nom: "Kabongo",
    postnom: "Mutombo",
    prenom: "Aline",
    dateOfBirth: "14/05/1992",
    sex: "F",
    phone: "+243812345678",
    province: "Haut-Katanga (Lubumbashi / Likasi)",
    location: "Territoire de Kipushi",
    agriculturalSector: "VIVRIER",
    idType: "VOTER_CARD",
    idNumber: "DRC-19283746",
    hasIdPhoto: false,
    hasSelfie: true,
    identityStatus: "PENDING",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validCreditPayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    userId: uid,
    crop: "MAIS",
    areaHectares: 2.0,
    harvestMonthIndex: 5,
    experienceYears: 5,
    monthlyIncomeUsd: 220,
    requestedAmountUsd: 350,
    durationMonths: 6,
    purpose: "SEMENCES",
    riskLevel: "FAIBLE",
    riskScore: 86,
    monthlyInstallmentUsd: 65,
    status: "ACCEPTED",
    reasons: [
      {
        title: "Ratio montant / revenu",
        detail: "Revenu suffisant pour les mensualites.",
        positive: true,
        iconType: "RATIO",
      },
    ],
    schedule: [
      {
        monthLabel: "Juin",
        amountUsd: 110,
        harvestPeak: true,
        note: "Echeance recolte",
      },
    ],
    advice: ["Epargnez 10% des la recolte."],
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validTxPayload(uid, requestId, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    userId: uid,
    requestId,
    type: "DISBURSEMENT",
    amountUsd: 350,
    channel: "M-Pesa",
    reference: "TX-MPESA-9910",
    description: "Decaissement microcredit Mais",
    status: "COMPLETED",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write profiles, creditRequests, or transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("creditRequests").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get());
});

test("Profile creation enforces PENDING initial state, blocks shadow fields & cross-user writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Cannot create profile directly as VERIFIED (state shortcut attack)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, { identityStatus: "VERIFIED" }))
  );

  // Cannot include ghost fields like isAdmin
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, { isAdmin: true }))
  );

  // Reject invalid agricultural sector enum
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, { agriculturalSector: "CRYPTO_FARM" }))
  );

  // Cannot store selfie or ID photo URIs in Firestore (local-only privacy)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, { localSelfieUri: "file:///selfie.jpg" }))
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, { localIdPhotoUri: "file:///id.jpg" }))
  );

  // Valid creation succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );

  // Alice can update her agricultural sector & location
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      province: "Nord-Kivu (Goma / Beni / Butembo)",
      location: "Territoire de Rutshuru",
      agriculturalSector: "CAFE_CACAO",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Bob cannot read or overwrite Alice's PII profile
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ phone: "+243999999999", updatedAt: new Date(Date.now() - 500) })
  );
});

test("Face verification updates profile to VERIFIED, and creditRequest + transaction batch succeeds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );

  // Unverified user cannot submit a credit request yet
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("creditRequests").doc("REQ_001").set(
      validCreditPayload(ALICE_UID)
    )
  );

  // Cannot verify with low score (< 70)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      identityStatus: "VERIFIED",
      lastVerificationScore: 45,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Valid biometric verification succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      identityStatus: "VERIFIED",
      lastVerificationScore: 92,
      verifiedAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Atomic batch write: create ACCEPTED creditRequest + DISBURSEMENT transaction
  const batch = aliceDb.batch();
  const reqRef = aliceDb.collection("users").doc(ALICE_UID).collection("creditRequests").doc("REQ_001");
  const txRef = aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("TX_001");
  batch.set(reqRef, validCreditPayload(ALICE_UID));
  batch.set(txRef, validTxPayload(ALICE_UID, "REQ_001"));
  await assertSucceeds(batch.commit());

  // Orphaned transaction referencing non-existent creditRequest fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("TX_ORPHAN").set(
      validTxPayload(ALICE_UID, "REQ_DOES_NOT_EXIST")
    )
  );

  // Subsequent repayment transaction referencing REQ_001 succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("TX_002").set(
      validTxPayload(ALICE_UID, "REQ_001", {
        type: "REPAYMENT",
        amountUsd: 65,
        reference: "MPESA-REPAY-01",
        description: "Remboursement echeance 1",
      })
    )
  );

  // Subcollection list queries succeed for owner and fail for another user
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("creditRequests").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("transactions").get());

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("creditRequests").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("transactions").get());
});

test("Rejects tampered future timestamps, @DocumentId id key, and terminal status mutation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Future timestamp rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(
      validProfilePayload(ALICE_UID, { createdAt: new Date(Date.now() + 120000) })
    )
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      identityStatus: "VERIFIED",
      lastVerificationScore: 95,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Including @DocumentId "id" key in payload rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("creditRequests").doc("REQ_BAD").set(
      validCreditPayload(ALICE_UID, { id: "REQ_BAD" })
    )
  );

  // Create IN_PROGRESS request, transition to ACCEPTED, then verify terminal lock
  const reqRef = aliceDb.collection("users").doc(ALICE_UID).collection("creditRequests").doc("REQ_LOCK");
  await assertSucceeds(reqRef.set(validCreditPayload(ALICE_UID, { status: "IN_PROGRESS" })));
  await assertSucceeds(reqRef.update({ status: "ACCEPTED", updatedAt: new Date(Date.now() - 500) }));
  await assertFails(reqRef.update({ status: "REJECTED", updatedAt: new Date(Date.now() - 200) }));
});
