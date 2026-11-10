# MEDs

Aplicación Android nativa para recordar medicamentos, pensada para personas con TDAH. Incluye un modo acompañante vinculado mediante un código QR temporal y consulta de medicamentos con la API de CIMA/AEMPS. Versión 1.1.

## Ramas

- `main`: cascarón inicial compilable con la estructura, el tema y las pantallas base.
- `develop`: desarrollo activo de la aplicación completa.
- `feature/*`: ramas de trabajo. La consulta CIMA y la biometría se integraron desde `feature/consulta-cima-biometria`.

## Stack

- Kotlin, Jetpack Compose y Material 3.
- MVVM con inyección manual de dependencias.
- Firebase Authentication con correo/contraseña y sesión anónima vinculable.
- Cloud Firestore con caché offline.
- Firebase Cloud Messaging para notificaciones push.
- Cloud Functions (Node 22) para enviar los push desde Firestore.
- Retrofit + Gson contra la API REST de CIMA/AEMPS para consultar medicamentos.
- DataStore para preferencias locales.
- AlarmManager y notificaciones locales.
- ZXing para generar y escanear el QR.
- Autenticación biométrica con androidx.biometric.
- Widget de escritorio con la próxima toma.
- Sin Room, Hilt ni NanoHTTPD.

## Requisitos

- Android Studio con JDK 17.
- SDK de Android 37.
- Un proyecto Firebase con la app `com.meds.app` registrada.
- `app/google-services.json` en su lugar. Por seguridad, este archivo no se versiona; cada integrante debe colocar el suyo antes de compilar.

## Configuración de Firebase

La app no puede iniciar sesión hasta completar estos pasos en la consola de Firebase del proyecto `medsproto`.

1. Abrir Authentication, entrar en Sign-in method y habilitar los proveedores Anónimo y Correo electrónico/Contraseña.
2. Crear una base de datos de Cloud Firestore en modo producción.
3. Publicar las reglas de `firestore.rules`.
4. Habilitar el plan Blaze. Cloud Functions lo exige, aunque el volumen de este proyecto entra en la capa gratuita.

Con Firebase CLI:

```powershell
npm install -g firebase-tools
firebase login
firebase use medsproto
npm install --prefix functions
firebase deploy --only firestore:rules,functions --project medsproto
```

Las reglas también se pueden pegar manualmente en Firestore Database, pestaña Rules. Las Cloud Functions solo se pueden desplegar con el plan Blaze y una cuenta autorizada en Firebase CLI.

## Compilar y ejecutar

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:assembleDebug
```

El APK queda en `app\build\outputs\apk\debug\app-debug.apk`.

Para instalarlo en un emulador o dispositivo conectado:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Release firmado

La versión 1.1 usa `versionCode 2` y `versionName "1.1"`.

```powershell
.\gradlew.bat :app:assembleRelease :app:bundleRelease
```

Artefactos generados:

- `app\build\outputs\apk\release\app-release.apk`
- `app\build\outputs\bundle\release\app-release.aab`
- Copias listas para entregar: `dist\MEDs-release-1.1.apk` y `dist\MEDs-release-1.1.aab`
- Release publicado: `https://github.com/HVT-IAN/proyecto_movil-Med/releases/tag/v1.1`

La firma usa `keystore\meds-release.jks` y `keystore.properties`. Ambos están ignorados por Git y no deben subirse. Hay que respaldarlos porque son necesarios para futuras actualizaciones.

Para verificar la firma del APK:

```powershell
apksigner verify --print-certs dist\MEDs-release-1.1.apk
```

## Pruebas locales con emuladores de Firebase

La app incluye una variante de depuración que apunta a los emuladores locales de Authentication y Firestore. No modifica la variante normal.

```powershell
npx --yes firebase-tools@latest emulators:start --only auth,firestore --project medsproto
```

En otra terminal, con el emulador de Android o un dispositivo conectado por USB:

```powershell
adb reverse tcp:9099 tcp:9099
adb reverse tcp:8080 tcp:8080
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:assembleDebug "-Pmeds.useFirebaseEmulator=true"
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

La variante de emulador usa `127.0.0.1` y redirige los puertos con `adb reverse`, por lo que funciona tanto en el emulador como en un dispositivo físico conectado por USB. Si se reinicia adb o el dispositivo, hay que repetir los dos comandos `adb reverse`. Para volver a la configuración real, compilar de nuevo sin la propiedad `meds.useFirebaseEmulator`.

La variante de emulador no registra tokens FCM ni permite probar push; para eso hay que usar la variante normal con Firebase real y las Functions desplegadas.

## Flujo principal

1. Abrir la app. Si no hay sesión, iniciar sesión con correo y contraseña, crear una cuenta o continuar sin cuenta. El modo anónimo muestra una advertencia y puede vincularse a un correo más adelante desde Ajustes.
2. Elegir el rol de paciente o acompañante. La flecha superior derecha permite volver.
3. Escribir el nombre y completar la introducción.
4. Al entrar al espacio, si el dispositivo tiene biometría inscrita, desbloquear con huella o rostro. La biometría no se pide antes del login.
5. Como paciente, agregar un medicamento con una o varias horas.
6. En el editor, usar `Consultar medicamento` para buscar información oficial en CIMA/AEMPS.
7. Confirmar o posponer las tomas del día desde Hoy.
8. Abrir el icono de QR en Hoy y generar un código de acompañante. El botón `Generar nuevo QR` invalida el código anterior y crea uno vigente.
9. En otro dispositivo o emulador, elegir el rol de acompañante, escanear el QR o escribir el código.
10. El panel del acompañante muestra en tiempo real las tomas del día y permite enviar un recordatorio.
11. El paciente recibe el recordatorio dentro de la app y como notificación push, con sonido y vibración. La vinculación también genera un push de bienvenida.

## Cuentas y datos

- La cuenta anónima permite usar la app sin registro; sus datos quedan ligados a la sesión del dispositivo.
- Desde Ajustes > Cuenta se puede vincular un correo al usuario anónimo actual. La vinculación conserva el mismo `uid`, por lo que medicamentos, tomas e historial se mantienen.
- Al iniciar sesión en otro dispositivo, el rol y el nombre se recuperan desde Firestore.
- Cerrar sesión borra las preferencias locales. Si la cuenta es anónima y no se vinculó un correo, no se puede volver a acceder a esos datos.

## Biometría

- La biometría protege el espacio ya creado, no la pantalla de autenticación.
- Se pide al entrar al espacio y se vuelve a pedir después de 5 minutos en segundo plano.
- Los cambios rápidos entre apps no vuelven a pedir huella.
- Si el dispositivo no tiene huella o rostro inscrito, la app no bloquea.

## Widget de próxima toma

El widget se agrega desde el selector de widgets del launcher. Muestra el nombre del medicamento, la dosis y la hora de la siguiente toma pendiente. Se actualiza cuando cambian las tomas en la app y cada 30 minutos; si cambia el día, invita a abrir MEDs para refrescar los datos.

## Vinculación de acompañantes

El código de vinculación dura 10 minutos y es de un solo uso. El paciente puede retirar el acceso desde la misma pantalla o generar uno nuevo con `Generar nuevo QR`; al hacerlo, el código anterior queda invalidado de inmediato y cualquier intento de usarlo recibe el mensaje `Este código ya fue utilizado`.

## Consulta de medicamentos

- La pantalla `Consultar medicamento` usa Retrofit contra la API REST pública de CIMA/AEMPS.
- Busca por nombre comercial o principio activo, por ejemplo `ibuprofeno`, `paracetamol` u `omeprazol`.
- Muestra hasta 20 resultados, priorizando el principio activo exacto y los medicamentos comercializados.
- La ficha incluye principios activos, código ATC, presentaciones, excipientes y enlaces a la ficha técnica y al prospecto oficial.
- Se puede usar el nombre comercial o el principio activo para completar el editor.
- No requiere API key. Los estados de carga, sin resultados, sin conexión y error de formato se muestran en la interfaz.
- La información es de referencia y no sustituye el consejo de un profesional sanitario.

## Recordatorios

- La app programa alarmas exactas cuando el sistema lo permite.
- Si no se concede el acceso especial de alarmas exactas, usa alarmas inexactas.
- Las notificaciones incluyen las acciones `Ya la tomé` y `Posponer 10 min`.
- Las alarmas se reprograman al reiniciar el dispositivo.
- En Android 12 o superior conviene habilitar `Alarmas y recordatorios` para la app desde los ajustes del sistema.

### Notificaciones push

- Los recordatorios del acompañante se envían con Firebase Cloud Messaging y funcionan con la app cerrada o en segundo plano.
- La vinculación de un acompañante también genera un push.
- Canales de alta importancia: `meds_nudges_v2` para recordatorios y `meds_companions` para vinculaciones. Ambos usan sonido, vibración y aviso en pantalla.
- La app registra el token FCM del paciente en `users/{uid}/devices/{token}` y lo elimina al cerrar sesión.
- Las Cloud Functions `nudgeCreated` y `companionLinked` escuchan los documentos de Firestore y envían el push a todos los dispositivos del paciente. Si FCM informa un token inválido, la función lo elimina.
- Requiere el plan Blaze y las Functions desplegadas. Sin ese despliegue, la app sigue mostrando los recordatorios dentro de la interfaz, pero no llegan como push.
- En Android 13 o superior hay que conceder el permiso de notificaciones. Si el usuario usa `Forzar detención` desde Ajustes, Android bloquea la entrega de push hasta que vuelva a abrir la app; cerrarla desde recientes no tiene ese efecto.

Para verificar el push de punta a punta después del despliegue:

1. Iniciar sesión como paciente y confirmar que aparece un documento en `users/{uid}/devices` con el token FCM.
2. Cerrar la app desde la pantalla de recientes, sin usar `Forzar detención`.
3. Desde la cuenta del acompañante, enviar un recordatorio o vincularse con un código vigente.
4. Confirmar que la notificación llega con sonido, vibración y aviso en pantalla.
5. Tocar la notificación y comprobar que abre la app en la pantalla Hoy.

## Estructura

```text
app/src/main/java/com/meds/app
  MainActivity.kt
  MedsApp.kt

  data/model          Modelos de Firestore y de consulta CIMA
  data/remote         Cliente Retrofit y DTOs de CIMA
  data/repository     Autenticación, paciente, acompañante y consulta CIMA
  data/local          Preferencias con DataStore
  notifications       Alarmas, canales, receptores y servicio FCM
  widget              Widget de próxima toma
  ui/screens/auth      Inicio de sesión, registro y advertencia anónima
  ui/screens/settings  Cuenta, vinculación de correo y cierre de sesión
  ui/screens/patient  Hoy, medicamentos, consulta CIMA, jardín, historial y acompañante
  ui/screens/companion Panel y vinculación del acompañante
  ui/screens/shared   Carga, error y bloqueo biométrico
  ui/theme            Tema Sereno clínico
  util                Fechas, QR y códigos

functions/             Cloud Functions que envían los push
```

### Capas MVVM

- `ui/screens` y `ui/components`: capa View. Composables que dibujan el estado y emiten eventos; no consultan Firebase ni DataStore.
- `ui/*ViewModel`: capa ViewModel. Exponen `StateFlow`, reciben eventos y lanzan corrutinas en `viewModelScope`.
- `data/repository`: capa Repository. Centraliza el acceso a datos, los batches y las reglas de negocio; convierte las respuestas a modelos.
- `data/local`, Firebase Auth/Firestore y `AlarmScheduler`: fuentes de datos remotas, locales y del dispositivo.
- `ui/theme`, `ui/components` y `util`: elementos transversales de interfaz y utilidades.

Las APIs externas son Firebase (Authentication, Cloud Firestore, Cloud Messaging y Cloud Functions) y la API REST de CIMA/AEMPS mediante Retrofit. El almacenamiento local usa DataStore para preferencias, la caché offline de Firestore para los datos de dominio y SharedPreferences para el widget.

## Datos en Firestore

```text
users/{uid}
  name, role, createdAt
  medications/{medicationId}
  schedules/{scheduleId}
  doseLogs/{logId}
  plant/state
  companions/{companionUid}
  nudges/{nudgeId}
  devices/{token}
users/{companionUid}/linkedPatients/{patientUid}
pairingInvites/{code}
```

## Limitaciones conocidas

- Una cuenta anónima sin correo vinculado pierde el acceso a sus datos si se desinstala la app o se cierra sesión. La app advierte y permite vincular un correo desde Ajustes.
- Los push requieren el plan Blaze y las Cloud Functions desplegadas. Mientras no se desplieguen, los recordatorios solo se ven dentro de la app.
- Android no entrega push si el usuario fuerza la detención de la app desde Ajustes; cerrarla desde recientes sí permite la entrega.
- La consulta CIMA depende de la disponibilidad del servicio de la AEMPS. La información es de referencia y no sustituye una receta ni una consulta médica.
- La biometría depende del sensor y de que haya una huella o rostro inscrito en el dispositivo.
- El modo acompañante requiere que el paciente haya abierto la app al menos una vez para crear su perfil y sus tomas del día.
- El historial del acompañante se limita al día actual.

## Evidencias

Las capturas de la verificación funcional están en `evidencias/`. Incluyen selección de rol, onboarding, editor de medicamentos, widget, biometría después del login, inicio de sesión, ajustes, QR, regeneración con invalidación del código anterior, vinculación de acompañante, recordatorios, canales de notificación con sonido y vibración, y la consulta de medicamentos con CIMA: resultados, ficha, sin resultados y error de conexión.

## Documentación para el equipo

La guía funcional y técnica actualizada está en `docs/MEDs_Documentacion_Equipo_v1.1.pdf`. Incluye el recorrido completo de uso, la arquitectura MVVM, la API REST de CIMA con Retrofit, la biometría después del login, el release firmado, la matriz de cobertura de la rúbrica y un guion de demostración de 10 minutos.

## Validación

Para comprobar que no se introdujeron emojis en el código, la documentación ni los recursos:

```powershell
.\scripts\check-no-emoji.ps1
```
