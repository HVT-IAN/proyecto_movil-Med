# MEDs

Aplicación Android nativa para recordar medicamentos, pensada para personas con TDAH. Este repositorio usa dos ramas principales:

- `main`: cascarón inicial compilable. Contiene la estructura, el tema visual, la navegación principal y pantallas placeholder.
- `develop`: desarrollo activo de la aplicación.

La interfaz está en español y no se usan emojis en el código, la documentación, las notificaciones ni los mensajes de commit.

## Estado de main

`main` es una base mínima y funcional. Se puede abrir en Android Studio, compilar e instalar. Todavía no incluye la lógica del producto.

Incluye:

- Proyecto Gradle con Kotlin y Jetpack Compose.
- Material 3, tema claro y oscuro, tipografías y componentes base.
- Estructura completa de carpetas.
- Navegación inicial de paciente y acompañante.
- Pantallas placeholder para cada ruta.
- Iconos, fuentes y recursos de lanzamiento.
- Script para comprobar que no se introduzcan emojis.

No incluye todavía:

- Firebase Authentication ni Cloud Firestore.
- Alarmas, notificaciones locales o QR.
- Repositorios, ViewModels o lógica de negocio.
- `google-services.json`.

## Stack

| Área | Tecnología |
| --- | --- |
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose y Material 3 |
| Arquitectura | MVVM con inyección manual de dependencias |
| Navegación | Navigation Compose |
| Build | Gradle Kotlin DSL |
| SDK | minSdk 26, compileSdk 37, targetSdk 37 |

Versiones base:

- Android Gradle Plugin 9.4.1
- Kotlin 2.4.21
- Gradle 9.6.0
- Compose BOM 2026.02.01

## Estructura

```text
app/src/main/java/com/meds/app
  MainActivity.kt
  MedsApp.kt

  data
    local
    model
    repository

  notifications

  ui
    MedsRoot.kt
    components
    screens
      companion
      onboarding
      patient
      role
      shared
    theme

  util

app/src/main/res
  drawable
  font
  mipmap-anydpi-v26
  values
  values-night
  xml

docs
scripts
gradle/wrapper
```

Las carpetas que todavía no tienen código conservan un archivo `.gitkeep` para que Git las versione.

## Rutas iniciales

### Nivel raíz

| Ruta | Pantalla |
| --- | --- |
| `role` | Selección de rol |
| `onboarding/{role}` | Introducción y nombre |
| `patient` | Aplicación del paciente |
| `companion` | Aplicación del acompañante |

### Paciente

| Ruta | Pantalla |
| --- | --- |
| `home` | Hoy |
| `medications` | Medicamentos |
| `garden` | Jardín |
| `history` | Historial |
| `companions` | Acompañantes |
| `medication_editor/new` | Nuevo medicamento |
| `medication_editor/{medicationId}` | Editar medicamento |

La barra inferior muestra `home`, `medications` y `garden`. Las demás rutas se abren desde la pantalla Hoy o desde la lista de medicamentos.

### Acompañante

| Ruta | Pantalla |
| --- | --- |
| `link` | Vinculación por QR o código manual |
| `panel` | Panel del día |

## Flujo de ramas

```text
main
  └── develop
        ├── feature/paciente-home
        ├── feature/medication-editor
        ├── feature/companion-qr
        ├── feature/notifications
        ├── fix/alarm-reschedule
        └── docs/readme
```

Reglas sugeridas:

- `main` recibe solo versiones estables.
- `develop` integra el trabajo diario.
- Cada funcionalidad nace de `develop` y vuelve a `develop` mediante Pull Request.
- Los nombres de rama usan `feature/`, `fix/`, `docs/` o `chore/`.
- Los commits siguen Conventional Commits en español y sin emojis.

Ejemplos:

```text
feat(paciente): agregar editor de medicamentos
feat(recordatorios): programar alarmas exactas
fix(acompanante): corregir vinculacion manual
docs: actualizar guia de Firebase
```

## Compilar

Requisitos:

- Android Studio con JDK 17.
- SDK de Android 37.
- Emulador o dispositivo con Android 8.0 o superior.

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:assembleDebug
```

El APK queda en:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Para instalarlo:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Validación

Comprobar que no se introdujeron emojis:

```powershell
.\scripts\check-no-emoji.ps1
```

## Siguiente etapa en develop

- Agregar Firebase Authentication anónima y Cloud Firestore.
- Implementar repositorios, ViewModels y modelos de datos.
- Completar el onboarding y la sesión.
- Implementar medicamentos, horarios y tomas.
- Conectar alarmas exactas y notificaciones locales.
- Implementar QR, vinculación y panel del acompañante.
- Completar el jardín y el historial.
- Añadir reglas de Firestore y pruebas.

## Convenciones

- Textos de interfaz en español.
- Sin emojis.
- Sin Room, Hilt, NanoHTTPD, FCM ni Cloud Functions.
- Inyección de dependencias manual.
- Las pantallas no acceden directamente a Firebase.
- Los repositorios concentran el acceso a datos, alarmas y preferencias.

## Licencia

Este repositorio no incluye una licencia explícita. Antes de publicarlo o reutilizar el código, agrega un archivo `LICENSE` con la licencia elegida.
