const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_resident";
const BOB_UID = "bob_resident";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user: cannot read pins", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("pins").get());
});

test("Authenticated user: can create dangerous animal hazard pin with valid schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("pins").doc("pin_snake_1").set({
      animalType: "SNAKE",
      label: "Cobra Spotted",
      description: "Aggressive cobra near drain alley",
      status: "PENDING",
      userId: ALICE_UID,
      xRatio: 0.5,
      yRatio: 0.5
    })
  );
});

test("Authenticated user: cannot create pin for another userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("pins").doc("pin_fake").set({
      animalType: "AGGRESSIVE_DOG",
      label: "Stray Dog",
      description: "Aggressive dog",
      status: "PENDING",
      userId: BOB_UID
    })
  );
});

test("Authenticated user: can read pins and update status to DONE", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("pins").doc("pin_dog_1").set({
      animalType: "AGGRESSIVE_DOG",
      label: "Rottweiler Mix",
      description: "Aggressive stray near playground",
      status: "PENDING",
      userId: BOB_UID,
      xRatio: 0.3,
      yRatio: 0.7
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("pins").doc("pin_dog_1").get());

  await assertSucceeds(
    aliceDb.collection("pins").doc("pin_dog_1").update({
      status: "DONE"
    })
  );
});
