const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

const db = getFirestore();

const NUDGE_CHANNEL = "meds_nudges_v2";
const COMPANION_CHANNEL = "meds_companions";

async function sendToPatient(patientUid, { title, body, channelId, data }) {
  const devicesSnapshot = await db
    .collection("users")
    .doc(patientUid)
    .collection("devices")
    .get();

  const tokens = devicesSnapshot.docs
    .map((doc) => doc.id)
    .filter((token) => typeof token === "string" && token.length > 0);

  if (tokens.length === 0) {
    logger.info("No hay dispositivos registrados para el paciente", { patientUid });
    return;
  }

  const response = await getMessaging().sendEachForMulticast({
    tokens,
    notification: { title, body },
    data,
    android: {
      priority: "high",
      notification: {
        channelId,
        sound: "default",
        defaultVibrateTimings: true,
        notificationPriority: "PRIORITY_HIGH",
        visibility: "PUBLIC"
      }
    }
  });

  const invalidTokens = [];
  response.responses.forEach((result, index) => {
    if (result.success) return;
    const code = result.error?.code || "";
    if (
      code === "messaging/registration-token-not-registered" ||
      code === "messaging/invalid-registration-token" ||
      code === "messaging/invalid-argument"
    ) {
      invalidTokens.push(tokens[index]);
    } else {
      logger.warn("No se pudo enviar el push", { code, patientUid });
    }
  });

  await Promise.all(
    invalidTokens.map((token) =>
      db
        .collection("users")
        .doc(patientUid)
        .collection("devices")
        .doc(token)
        .delete()
        .catch((error) => {
          logger.warn("No se pudo eliminar el token invalido", { token, error });
        })
    )
  );

  logger.info("Push enviado", {
    patientUid,
    channelId,
    success: response.successCount,
    failure: response.failureCount
  });
}

exports.nudgeCreated = onDocumentCreated(
  "users/{patientUid}/nudges/{nudgeId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;

    const data = snapshot.data() || {};
    const fromName = String(data.fromName || "Tu acompañante").trim() || "Tu acompañante";
    const message =
      String(data.message || "").trim() ||
      "Tu acompañante te envió un recordatorio.";

    await sendToPatient(event.params.patientUid, {
      title: `Recordatorio de ${fromName}`,
      body: message,
      channelId: NUDGE_CHANNEL,
      data: {
        type: "nudge",
        nudgeId: event.params.nudgeId,
        fromName,
        message
      }
    });
  }
);

exports.companionLinked = onDocumentCreated(
  "users/{patientUid}/companions/{companionUid}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;

    const data = snapshot.data() || {};
    const companionName =
      String(data.name || "Tu acompañante").trim() || "Tu acompañante";
    const body = `${companionName} ahora puede ver tus tomas del día.`;

    await sendToPatient(event.params.patientUid, {
      title: "Nuevo acompañante",
      body,
      channelId: COMPANION_CHANNEL,
      data: {
        type: "companion_linked",
        companionUid: event.params.companionUid,
        companionName
      }
    });
  }
);
