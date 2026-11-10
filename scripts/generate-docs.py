from pathlib import Path
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.lib.utils import ImageReader
from reportlab.platypus import (
    Image as RLImage,
    KeepTogether,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
    XPreformatted,
)

ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "docs" / "MEDs_Documentacion_Equipo_v1.1.pdf"

TEAL = colors.HexColor("#0F6B63")
DARK = colors.HexColor("#1F2933")
MUTED = colors.HexColor("#5B6770")
LIGHT = colors.HexColor("#F2F7F5")
BORDER = colors.HexColor("#D7E3DF")

PAGE_WIDTH, PAGE_HEIGHT = A4
LEFT = 46
RIGHT = 46
TOP = 58
BOTTOM = 54
CONTENT_WIDTH = PAGE_WIDTH - LEFT - RIGHT

TEAM = "Ian Oscani Diaz Vazquez, Carlos Adrian Alarcon Valencia"
UNIVERSITY = "Universidad Politécnica de Chiapas"
DATE_TEXT = "9 de noviembre de 2026"

styles = getSampleStyleSheet()

cover_title = ParagraphStyle(
    "CoverTitle",
    parent=styles["Title"],
    fontName="Helvetica-Bold",
    fontSize=38,
    leading=44,
    textColor=colors.white,
    alignment=0,
)
cover_heading = ParagraphStyle(
    "CoverHeading",
    parent=styles["Heading1"],
    fontName="Helvetica-Bold",
    fontSize=24,
    leading=30,
    textColor=DARK,
    spaceAfter=8,
)
cover_sub = ParagraphStyle(
    "CoverSub",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=12,
    leading=18,
    textColor=MUTED,
)
cover_university = ParagraphStyle(
    "CoverUniversity",
    parent=styles["BodyText"],
    fontName="Helvetica-Bold",
    fontSize=12,
    leading=16,
    textColor=TEAL,
)
cover_team_label = ParagraphStyle(
    "CoverTeamLabel",
    parent=styles["BodyText"],
    fontName="Helvetica-Bold",
    fontSize=8,
    leading=12,
    textColor=MUTED,
)
cover_team = ParagraphStyle(
    "CoverTeam",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=10.5,
    leading=15,
    textColor=DARK,
)
h1 = ParagraphStyle(
    "H1Custom",
    parent=styles["Heading1"],
    fontName="Helvetica-Bold",
    fontSize=18,
    leading=23,
    textColor=TEAL,
    spaceBefore=8,
    spaceAfter=8,
)
h2 = ParagraphStyle(
    "H2Custom",
    parent=styles["Heading2"],
    fontName="Helvetica-Bold",
    fontSize=12.5,
    leading=17,
    textColor=DARK,
    spaceBefore=10,
    spaceAfter=5,
)
body = ParagraphStyle(
    "BodyCustom",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=9.7,
    leading=14,
    textColor=DARK,
    spaceAfter=5,
)
small = ParagraphStyle(
    "SmallCustom",
    parent=body,
    fontSize=8.5,
    leading=12,
    textColor=MUTED,
)
bullet = ParagraphStyle(
    "BulletCustom",
    parent=body,
    leftIndent=14,
    bulletIndent=4,
    spaceAfter=3,
)
caption = ParagraphStyle(
    "CaptionCustom",
    parent=body,
    fontName="Helvetica-Oblique",
    fontSize=8,
    leading=11,
    textColor=MUTED,
    alignment=1,
    spaceBefore=3,
)
code = ParagraphStyle(
    "CodeCustom",
    parent=styles["Code"],
    fontName="Courier",
    fontSize=7.6,
    leading=10,
    textColor=DARK,
    backColor=LIGHT,
    borderColor=BORDER,
    borderWidth=0.5,
    borderPadding=6,
    spaceBefore=4,
    spaceAfter=8,
)
cell = ParagraphStyle(
    "CellCustom",
    parent=body,
    fontSize=8.4,
    leading=11.5,
    spaceAfter=0,
)
cell_header = ParagraphStyle(
    "CellHeader",
    parent=cell,
    fontName="Helvetica-Bold",
    textColor=colors.white,
)
cell_small = ParagraphStyle(
    "CellSmall",
    parent=cell,
    fontSize=7.8,
    leading=10.5,
)


def P(text, style=body):
    return Paragraph(escape(text), style)


def H1(text):
    return Paragraph(escape(text), h1)


def H2(text):
    return Paragraph(escape(text), h2)


def BULLETS(items):
    return [Paragraph(escape(item), bullet, bulletText="•") for item in items]


def CODE(text):
    return XPreformatted(text.strip("\n"), code)


def T(data, widths=None, header=True):
    if widths is None:
        widths = [CONTENT_WIDTH / len(data[0])] * len(data[0])
    rows = []
    for row_index, row in enumerate(data):
        style = cell_header if header and row_index == 0 else cell
        rows.append([Paragraph(escape(str(value)), style) for value in row])
    table = Table(rows, colWidths=widths, repeatRows=1 if header else 0)
    commands = [
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("GRID", (0, 0), (-1, -1), 0.4, BORDER),
        ("LEFTPADDING", (0, 0), (-1, -1), 5),
        ("RIGHTPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
    ]
    if header:
        commands.append(("BACKGROUND", (0, 0), (-1, 0), TEAL))
    for index in range(1, len(data)):
        if index % 2 == 0:
            commands.append(("BACKGROUND", (0, index), (-1, index), LIGHT))
    table.setStyle(TableStyle(commands))
    return table


def IMAGE(path, width=230):
    image_path = ROOT / path
    image = ImageReader(str(image_path))
    iw, ih = image.getSize()
    height = width * ih / iw
    return RLImage(str(image_path), width=width, height=height)


def IMAGE_PAIR(path1, caption1, path2, caption2):
    left = KeepTogether([IMAGE(path1), P(caption1, caption)])
    right = KeepTogether([IMAGE(path2), P(caption2, caption)])
    table = Table([[left, right]], colWidths=[CONTENT_WIDTH / 2, CONTENT_WIDTH / 2])
    table.setStyle(
        TableStyle(
            [
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("ALIGN", (0, 0), (-1, -1), "CENTER"),
                ("LEFTPADDING", (0, 0), (-1, -1), 4),
                ("RIGHTPADDING", (0, 0), (-1, -1), 4),
                ("TOPPADDING", (0, 0), (-1, -1), 4),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
            ]
        )
    )
    return KeepTogether([table])


def header_footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(BORDER)
    canvas.setLineWidth(0.5)
    canvas.line(LEFT, PAGE_HEIGHT - 36, PAGE_WIDTH - RIGHT, PAGE_HEIGHT - 36)
    canvas.setFont("Helvetica", 7.5)
    canvas.setFillColor(MUTED)
    canvas.drawString(LEFT, PAGE_HEIGHT - 30, "MEDs - Documentación funcional y técnica")
    canvas.drawRightString(PAGE_WIDTH - RIGHT, PAGE_HEIGHT - 30, "Versión 1.1")
    canvas.line(LEFT, 36, PAGE_WIDTH - RIGHT, 36)
    canvas.drawString(LEFT, 26, f"MEDs 1.1 - {DATE_TEXT}")
    canvas.drawRightString(PAGE_WIDTH - RIGHT, 26, f"Página {doc.page}")
    canvas.restoreState()


def cover_page(canvas, doc):
    canvas.saveState()
    canvas.setFillColor(TEAL)
    canvas.rect(0, PAGE_HEIGHT - 150, PAGE_WIDTH, 150, fill=1, stroke=0)
    canvas.setFillColor(colors.white)
    canvas.setFont("Helvetica-Bold", 40)
    canvas.drawString(LEFT, PAGE_HEIGHT - 92, "MEDs")
    canvas.setFont("Helvetica", 12)
    canvas.drawString(LEFT, PAGE_HEIGHT - 116, "Recordatorios de medicación, sin ruido")
    canvas.setFillColor(MUTED)
    canvas.setFont("Helvetica", 8)
    canvas.drawString(LEFT, 26, f"MEDs 1.1 - {DATE_TEXT}")
    canvas.restoreState()


def build():
    story = []

    # Cover
    story.append(Spacer(1, 100))
    story.append(Paragraph(UNIVERSITY, cover_university))
    story.append(Spacer(1, 10))
    story.append(Paragraph("Documentación funcional y técnica", cover_heading))
    story.append(
        Paragraph(
            "Guía de uso, arquitectura MVVM, API REST de CIMA, biometría, push y cobertura de la rúbrica del proyecto integrador.",
            cover_sub,
        )
    )
    story.append(Spacer(1, 18))
    story.append(
        Table(
            [[""]],
            colWidths=[CONTENT_WIDTH],
            rowHeights=[4],
            style=[("BACKGROUND", (0, 0), (-1, -1), TEAL)],
        )
    )
    story.append(Spacer(1, 60))
    story.append(Paragraph("INTEGRANTES", cover_team_label))
    story.append(Spacer(1, 4))
    story.append(Paragraph(TEAM, cover_team))
    story.append(Spacer(1, 90))
    story.append(Paragraph("Versión 1.1 - versionCode 2", ParagraphStyle(
        "CoverVersion", parent=body, fontName="Helvetica-Bold", fontSize=13, leading=18
    )))
    story.append(Paragraph(DATE_TEXT, body))
    story.append(Paragraph("com.meds.app", small))
    story.append(PageBreak())

    # Contents
    story.append(H1("Contenido"))
    contents = [
        ("1.", "Resumen ejecutivo y cambios de la versión 1.1"),
        ("2.", "Guía de uso: recorrido completo"),
        ("3.", "Arquitectura MVVM y organización del código"),
        ("4.", "Consumo de API y tratamiento de datos"),
        ("5.", "Interfaz con Jetpack Compose y navegación"),
        ("6.", "Gestión de datos y persistencia local"),
        ("7.", "Integraciones del dispositivo, biometría y push"),
        ("8.", "Estabilidad, validaciones y experiencia de uso"),
        ("9.", "Repositorio, documentación y versión entregable"),
        ("10.", "Demostración práctica y defensa técnica"),
        ("11.", "Cobertura de la rúbrica"),
        ("12.", "Evidencias y anexos"),
    ]
    story.append(T([["Sección", "Contenido"]] + [[number, title] for number, title in contents], [40, CONTENT_WIDTH - 40]))
    story.append(Spacer(1, 14))
    story.append(P("Este documento reemplaza a la versión 1.0. La versión 1.1 incorpora la consulta de medicamentos con la API REST de CIMA/AEMPS mediante Retrofit, reubica la biometría después del login y del onboarding, agrega el release firmado y actualiza las evidencias a 38 capturas.", small))
    story.append(PageBreak())

    # 1. Resumen
    story.append(H1("1. Resumen ejecutivo"))
    story.append(P("MEDs es una aplicación Android nativa para recordar medicamentos y sostener la constancia diaria. Está pensada para personas con TDAH y para quienes las acompañan: reduce la carga cognitiva, muestra solo lo necesario y convierte cada toma en una acción de un toque."))
    story.append(P("El proyecto modela dos roles, paciente y acompañante, un jardín que crece con la constancia, recordatorios locales, un widget de próxima toma, vinculación por QR y una consulta de medicamentos contra la API pública de CIMA/AEMPS. La versión 1.1 agrega Retrofit como cliente REST, una pantalla de consulta con resultados y ficha oficial, y un release firmado."))
    story.append(H2("1.1 Problema y propuesta"))
    story += BULLETS([
        "Olvidar una toma no es falta de voluntad: es carga cognitiva. Las apps genéricas añaden pasos, notificaciones ruidosas y pantallas densas.",
        "MEDs reduce la fricción: una toma se confirma con un toque; posponer cuesta otro toque; el estado del día se entiende de un vistazo.",
        "El modo acompañante resuelve el aislamiento: alguien de confianza ve el día y puede enviar un recordatorio, sin acceso al historial completo.",
        "La consulta CIMA permite verificar nombres, principios activos, presentaciones y documentos oficiales antes de guardar un medicamento.",
        "La constancia se refuerza con una planta y una racha, no con castigos ni culpa.",
    ])
    story.append(H2("1.2 Ficha técnica"))
    story.append(T([
        ["Aspecto", "Detalle"],
        ["Nombre y paquete", "MEDs - com.meds.app"],
        ["Propósito", "Recordatorios de medicación, acompañamiento y consulta de medicamentos"],
        ["Plataforma", "Android 8.0 o superior (minSdk 26, targetSdk 37)"],
        ["Lenguaje e interfaz", "Kotlin, Jetpack Compose, Material 3"],
        ["Arquitectura", "MVVM + Repository, StateFlow y corrutinas; inyección manual"],
        ["Backend", "Firebase Authentication + Cloud Firestore (caché offline)"],
        ["API REST", "CIMA/AEMPS con Retrofit 2.11, Gson y OkHttp"],
        ["Push", "Firebase Cloud Messaging + Cloud Functions (Node 22)"],
        ["Persistencia local", "DataStore Preferences, SharedPreferences del widget y AlarmManager"],
        ["Integraciones", "QR con ZXing, biometría con androidx.biometric y widget AppWidgetProvider"],
        ["Versión", "1.1 - versionCode 2"],
        ["Entrega", "APK y AAB firmados en dist/"],
    ], [120, CONTENT_WIDTH - 120]))
    story.append(H2("1.3 Cambios de la versión 1.1"))
    story += BULLETS([
        "Consulta de medicamentos con la API REST pública de CIMA/AEMPS mediante Retrofit, con búsqueda, resultados, ficha, documentos y manejo de errores.",
        "La biometría ya no se pide antes del login: protege el espacio después de autenticarse y completar el onboarding.",
        "Tiempo de gracia de 5 minutos al volver del segundo plano, para no interrumpir cambios rápidos entre apps.",
        "Regeneración de QR que invalida el código anterior, con aviso y rechazo del código usado.",
        "Push de recordatorios y de vinculación con canales de alta importancia, sonido y vibración.",
        "Release 1.1 firmado, PDF actualizado y evidencias 32 a 38.",
    ])
    story.append(PageBreak())

    # 2. Guía de uso
    story.append(H1("2. Guía de uso: recorrido completo"))
    story.append(H2("2.1 Instalar y abrir"))
    story.append(P("La variante de depuración se compila con .\\gradlew.bat :app:assembleDebug y queda en app\\build\\outputs\\apk\\debug\\app-debug.apk. El release firmado se compila con .\\gradlew.bat :app:assembleRelease :app:bundleRelease y se guarda en dist\\MEDs-release-1.1.apk y dist\\MEDs-release-1.1.aab."))
    story.append(H2("2.2 Cuenta y acceso"))
    story += BULLETS([
        "Crear cuenta con correo y contraseña, con mínimo 6 caracteres.",
        "Iniciar sesión si la cuenta ya existe, con recuperación de contraseña por correo.",
        "Continuar sin cuenta: se crea una sesión anónima y se muestra una advertencia con los riesgos de no vincular un correo.",
        "La cuenta anónima se puede vincular a un correo desde Ajustes; la vinculación conserva el mismo uid y los datos.",
        "El registro y el login restauran el rol y el nombre desde Firestore.",
    ])
    story.append(H2("2.3 Biometría"))
    story += BULLETS([
        "La huella o el rostro se piden al entrar a un espacio ya creado, no en la pantalla de login.",
        "Si el dispositivo no tiene biometría inscrita, la app no bloquea.",
        "Si la app vuelve del segundo plano después de 5 minutos, se pide de nuevo.",
        "Los cambios rápidos entre apps no vuelven a pedir huella.",
        "Al cerrar sesión se vuelve al login sin solicitud biométrica.",
    ])
    story.append(H2("2.4 Paciente"))
    story += BULLETS([
        "Hoy: próxima toma, estado del día, botones Ya la tomé y Posponer 10 min, historial y QR de acompañante.",
        "Medicamentos: lista, alta y edición; cada medicamento puede tener una o varias horas.",
        "Consultar medicamento: búsqueda en CIMA desde el editor, con resultados en español y ficha oficial.",
        "Jardín: planta, nivel, XP, racha y total de tomas.",
        "Ajustes: cuenta, vinculación de correo, cierre de sesión y versión.",
    ])
    story.append(H2("2.5 Acompañante"))
    story += BULLETS([
        "Escanear el QR del paciente o escribir el código de 8 caracteres.",
        "Ver en tiempo real las tomas del día, no el historial completo.",
        "Enviar un recordatorio breve y recibir la confirmación en la app.",
        "Desvincularse cuando quiera; el paciente también puede retirar el acceso.",
        "El código dura 10 minutos, es de un solo uso y el botón Generar nuevo QR invalida el anterior.",
    ])
    story.append(H2("2.6 Widget, alarmas y push"))
    story += BULLETS([
        "Widget: muestra la próxima toma pendiente; se actualiza cuando cambian las tomas y cada 30 minutos.",
        "Alarmas: notificaciones locales con acciones Ya la tomé y Posponer 10 min; se reprograman al reiniciar.",
        "Push: recordatorios y vinculación por FCM con canales de alta importancia; requiere desplegar Cloud Functions y el plan Blaze.",
        "El push con la app completamente cerrada se verificó en el diseño y en la carga de Functions; la prueba final depende del despliegue en Blaze.",
    ])
    story.append(PageBreak())

    # 3. Arquitectura
    story.append(H1("3. Arquitectura MVVM y organización del código"))
    story.append(P("La app separa interfaz, estado, acceso a datos y fuentes. Ninguna pantalla habla directamente con Firebase ni con CIMA: las pantallas observan un StateFlow del ViewModel y envían eventos; el ViewModel llama al Repository; el Repository encapsula el SDK, Retrofit y las reglas de negocio."))
    story.append(T([
        ["Capa", "Responsabilidad", "Ejemplos"],
        ["Interfaz (Compose)", "Dibuja el estado y emite eventos; no accede a datos.", "ui/screens, ui/components, MedsRoot"],
        ["Estado (ViewModel)", "Expone StateFlow, lanza corrutinas y traduce errores.", "AppViewModel, PatientViewModel, MedicationInfoViewModel"],
        ["Datos (Repository)", "Operaciones, batches, mapeo, reglas y Result.", "AuthRepository, PatientRepository, CompanionRepository, MedicationInfoRepository"],
        ["Fuentes", "Identidad, base de datos, API REST, preferencias y alarmas.", "FirebaseAuth, FirebaseFirestore, Retrofit, DataStore, AlarmManager"],
    ], [100, 190, CONTENT_WIDTH - 290]))
    story.append(H2("3.1 Paquetes principales"))
    story.append(T([
        ["Paquete", "Contenido"],
        ["data/model", "Modelos inmutables de Firestore y de consulta CIMA."],
        ["data/remote", "CimaApi, CimaClient, DTOs y configuración de Retrofit."],
        ["data/repository", "AuthRepository, UserRepository, PatientRepository, CompanionRepository, MedicationInfoRepository y mapeos."],
        ["data/local", "PreferencesStore con DataStore: rol, nombre y onboarding."],
        ["notifications", "Alarmas, canales, receptores y MedsMessagingService."],
        ["ui/screens/patient", "Hoy, medicamentos, consulta CIMA, jardín, historial y acompañante."],
        ["ui/screens/companion", "Panel y vinculación del acompañante."],
        ["ui/screens/shared", "Carga, error y BiometricGate."],
        ["widget, util", "Widget de próxima toma, fechas, QR y códigos."],
    ], [125, CONTENT_WIDTH - 125]))
    story.append(H2("3.2 Estado, corrutinas y errores"))
    story.append(P("Los ViewModels exponen un único StateFlow. Para combinar fuentes se usa combine; para cambiar de día o de paciente, flatMapLatest; para evitar trabajo innecesario, stateIn(WhileSubscribed(5_000)). Las pantallas recogen el estado con collectAsStateWithLifecycle. Las escrituras son suspend y devuelven Result; los errores se traducen a mensajes comprensibles y se muestran con snackbar o tarjetas de error."))
    story.append(H2("3.3 Recorrido de la consulta CIMA"))
    story.append(P("MedicationEditorScreen -> botón Consultar medicamento -> MedicationInfoScreen -> MedicationInfoViewModel.search -> MedicationInfoRepository.search -> CimaApi.medicamentos -> CIMA/AEMPS. El resultado se mapea a MedicationInfo y se muestra en tarjetas. Al elegir Usar este nombre o Usar principio activo, el nombre viaja por savedStateHandle al editor y se aplica al campo Nombre."))
    story.append(CODE("""
interface CimaApi {
    @GET("medicamentos")
    suspend fun search(
        @Query("nombre") name: String,
        @Query("pagina") page: Int = 1
    ): CimaSearchResponse

    @GET("medicamento")
    suspend fun detail(
        @Query("nregistro") registry: String
    ): CimaDetail
}
"""))
    story.append(CODE("""
class MedicationInfoRepository(
    private val api: CimaApi = CimaClient.api
) {
    suspend fun search(rawQuery: String): Result<MedicationSearchResult> =
        runCatching {
            val query = rawQuery.trim()
            require(query.length >= 2) { "Escribe al menos 2 caracteres." }
            val response = api.search(query, page = 1)
            val sorted = response.resultados
                .map { it.toMedicationInfo() }
                .sortedWith(
                    compareByDescending<MedicationInfo> {
                        it.activeSubstance.equals(query, ignoreCase = true)
                    }.thenByDescending { it.commercialized }
                     .thenBy { it.displayName.lowercase() }
                )
            MedicationSearchResult(sorted.take(20), response.totalFilas)
        }
}
"""))
    story.append(H2("3.4 Inyección de dependencias"))
    story.append(P("La inyección es manual y explícita. Los ViewModels construyen sus repositorios o los reciben con valores por defecto. MedsApp inicializa Firebase y las alarmas. Es una decisión deliberada: la app es pequeña, las dependencias son pocas y visibles, y no se introduce Hilt para no añadir complejidad. Los repositorios aceptan FirebaseFirestore o CimaApi como parámetro, lo que permite sustituirlos en pruebas."))
    story.append(PageBreak())

    # 4. API
    story.append(H1("4. Consumo de API y tratamiento de datos"))
    story.append(P("La app consume dos tipos de servicios externos: Firebase (Authentication, Cloud Firestore, Cloud Messaging y Cloud Functions) y la API REST pública de CIMA/AEMPS para la consulta de medicamentos. No hay datos simulados: las pantallas leen y escriben documentos reales del proyecto medsproto y consultas reales a CIMA."))
    story.append(H2("4.1 API REST de CIMA/AEMPS"))
    story.append(T([
        ["Elemento", "Detalle"],
        ["Base URL", "https://cima.aemps.es/cima/rest/"],
        ["Búsqueda", "GET medicamentos?nombre={nombre}&pagina=1"],
        ["Detalle", "GET medicamento?nregistro={nregistro}"],
        ["Cliente", "Retrofit 2.11 + converter-gson + OkHttp"],
        ["Autenticación", "No requiere API key"],
        ["Resultados", "Se muestran 20, priorizando el principio activo exacto y los comercializados"],
        ["Ficha", "Principios activos, ATC, presentaciones, excipientes y enlaces a ficha técnica y prospecto"],
    ], [110, CONTENT_WIDTH - 110]))
    story.append(P("La búsqueda usa un User-Agent identificable (MEDs-Android/1.1), timeouts de 15 segundos de conexión y 25 de lectura. La respuesta de CIMA puede traer cientos de resultados; el repositorio toma los 20 más relevantes y conserva el total para mostrarlo en la interfaz. El detalle se consulta por nregistro cuando el usuario abre la ficha."))
    story.append(H2("4.2 Tratamiento de errores"))
    story.append(T([
        ["Situación", "Respuesta en la app"],
        ["Sin resultados", "Estado Sin resultados con sugerencia de buscar por principio activo."],
        ["Sin conexión", "Sin conexión. Revisa tu red e intenta de nuevo."],
        ["Timeout", "La consulta tardó demasiado. Revisa tu conexión e intenta de nuevo."],
        ["HTTP 429", "CIMA recibió demasiadas consultas. Espera un momento."],
        ["HTTP 5xx", "El servicio de CIMA no está disponible. Intenta más tarde."],
        ["Respuesta con formato inesperado", "La respuesta de CIMA no se pudo interpretar. Intenta de nuevo."],
    ], [150, CONTENT_WIDTH - 150]))
    story.append(H2("4.3 Operaciones de Firebase"))
    story.append(T([
        ["Operación", "Técnica", "Dónde"],
        ["Registro, login, vínculo y recuperación", "FirebaseAuth (correo/contraseña y anónima)", "AuthRepository"],
        ["Observar medicamentos, horarios, tomas y planta", "Snapshots en tiempo real convertidos a Flow", "PatientRepository"],
        ["Guardar medicamento y horario", "Batch de dos documentos", "PatientRepository"],
        ["Marcar toma", "Batch: doseLog, planta y stock; cancela alarma", "PatientRepository"],
        ["Invitar y vincular acompañante", "Batch + validación de invitación en reglas", "CompanionRepository"],
        ["Enviar y leer nudges", "set / update con permisos por rol", "CompanionRepository"],
        ["Registrar token FCM", "Set(merge) en devices/{token}", "UserRepository"],
        ["Perfil y rol", "set(merge) en users/{uid}", "UserRepository"],
    ], [155, 175, CONTENT_WIDTH - 330]))
    story.append(H2("4.4 Seguridad de los datos"))
    story.append(P("Las reglas de Firestore validan que cada usuario solo escriba sus propios documentos; el acompañante solo lee el día; las invitaciones son de un solo uso y expiran; los nudges solo los crea un acompañante vinculado; los tokens FCM solo los lee y escribe su dueño. El cliente nunca decide los permisos. La consulta CIMA es pública y no envía datos personales del usuario."))
    story.append(H2("4.5 Qué resuelve el criterio 2"))
    story.append(P("La rúbrica pide consumo de API y tratamiento de datos. La versión 1.0 se apoyaba solo en Firebase y dejaba un matiz sobre Retrofit. La versión 1.1 incorpora un cliente Retrofit real contra una API REST pública, con DTOs, mapeo a modelos de dominio, consulta de detalle, estados de carga y error, y documentación del flujo. El riesgo del criterio 2 queda cubierto."))
    story.append(PageBreak())

    # 5. UI
    story.append(H1("5. Interfaz con Jetpack Compose y navegación"))
    story.append(P("Toda la interfaz es Compose + Material 3. El tema Sereno clínico usa Manrope para texto y Fraunces para títulos, con paleta teal, fondos claros y modo oscuro. Los componentes reutilizables (MedsCard, PrimaryActionButton, SecondaryActionButton, TextActionButton, EmptyState) mantienen la consistencia."))
    story.append(H2("5.1 Navegación"))
    story.append(T([
        ["Ruta", "Pantalla", "Propósito"],
        ["home", "HomeScreen", "Próxima toma, tomas del día e historial/QR."],
        ["medications", "MedicationsScreen", "Lista y edición."],
        ["medication_editor/new", "MedicationEditorScreen", "Alta de medicamento."],
        ["medication_editor/{id}", "MedicationEditorScreen", "Edición y consulta CIMA."],
        ["medication_info?query={query}", "MedicationInfoScreen", "Búsqueda y ficha CIMA."],
        ["garden", "GardenScreen", "Planta, nivel, XP y racha."],
        ["settings", "AccountSettingsScreen", "Cuenta, correo y cierre de sesión."],
        ["history", "HistoryScreen", "Últimos 7 días."],
        ["companions", "PatientCompanionScreen", "QR y personas vinculadas."],
    ], [150, 145, CONTENT_WIDTH - 295]))
    story.append(H2("5.2 Retorno del nombre seleccionado"))
    story.append(P("La pantalla de consulta devuelve el nombre mediante savedStateHandle del back stack del editor. El editor observa ese estado, aplica el nombre al campo y consume el valor. Así se evita acoplar la pantalla de consulta al ViewModel del paciente y se conserva el estado de navegación."))
    story.append(H2("5.3 Accesibilidad y adaptación"))
    story += BULLETS([
        "Contenido desplazable y layouts que se adaptan al ancho; probado en 1080x2400 y 1440x3120.",
        "Descripciones de contenido en iconos interactivos: ajustes, volver, borrar búsqueda, visibilidad de contraseña.",
        "Textos de error y ayudas junto a los campos; validación en el momento.",
        "Objetivos táctiles de al menos 48 dp en botones principales.",
        "Modo claro/oscuro según el sistema, con contraste cuidado.",
        "La consulta CIMA incluye aviso de que la información es de referencia y no sustituye el consejo médico.",
    ])
    story.append(PageBreak())

    # 6. Persistencia
    story.append(H1("6. Gestión de datos y persistencia local"))
    story.append(T([
        ["Dato", "Mecanismo", "Supervivencia", "Dónde"],
        ["Rol, nombre y onboarding", "DataStore Preferences", "Reinicio de la app", "PreferencesStore"],
        ["Medicamentos, horarios, tomas, planta y vínculos", "Cloud Firestore + caché offline", "Reinicio y reconexión", "Repositories"],
        ["Próxima toma del widget", "SharedPreferences", "Reinicio y cambios de toma", "NextDoseWidgetProvider"],
        ["Alarmas locales", "AlarmManager + SharedPreferences", "Reinicio del dispositivo", "AlarmScheduler / BootReceiver"],
        ["Token FCM", "Firestore users/{uid}/devices", "Hasta cerrar sesión o invalidarse", "UserRepository"],
        ["Resultados CIMA", "Memoria del ViewModel", "Mientras vive la pantalla", "MedicationInfoViewModel"],
    ], [125, 155, 120, CONTENT_WIDTH - 400]))
    story.append(H2("6.1 Por qué DataStore y no Room"))
    story.append(P("DataStore se usa para preferencias pequeñas y reactivas. Firestore es la fuente de verdad de los datos de dominio y su caché offline cubre la lectura sin red. No se usa Room porque duplicaría la fuente de verdad y añadiría sincronización. La consulta CIMA no se persiste: es información de referencia y se consulta de nuevo cuando el usuario la necesita."))
    story.append(H2("6.2 Biometría y sesión"))
    story.append(P("El estado de desbloqueo vive en la composición de BiometricGate. Se conserva durante la actividad, se pierde al reiniciar el proceso y se vuelve a pedir tras 5 minutos en segundo plano. No se guarda ningún dato biométrico en la app: el sensor y el sistema operativo gestionan la autenticación."))
    story.append(PageBreak())

    # 7. Integraciones
    story.append(H1("7. Integraciones del dispositivo, biometría y push"))
    story.append(P("La rúbrica pide tres funcionalidades entre PDF, QR, cámara/galería, biometría, audio o widget. MEDs implementa QR (generar y escanear), biometría y widget. Además incluye alarmas, notificaciones locales y push con FCM como extras."))
    story.append(H2("7.1 QR para vincular al acompañante"))
    story.append(P("El paciente genera una invitación con QrCodeGenerator (ZXing) y la muestra como QR con el contenido meds://pair?code=XXXXXXXX. El acompañante puede escanearlo con ZXing Embedded o escribir el código de 8 caracteres. La invitación dura 10 minutos, es de un solo uso y las reglas validan que el paciente sea el dueño. Generar nuevo QR invalida el código anterior."))
    story.append(H2("7.2 Biometría después del login"))
    story.append(P("BiometricGate envuelve el contenido autenticado y con onboarding completo, no la pantalla de login. Usa BiometricPrompt con BIOMETRIC_WEAK. Si el dispositivo tiene huella o rostro inscrito, pide desbloqueo al entrar al espacio. Si no hay biometría, no bloquea."))
    story.append(P("Un LifecycleEventObserver registra cuándo la app pasa a segundo plano. Al volver, si pasaron 5 minutos o más, se vuelve a bloquear; si fue un cambio rápido, no interrumpe. Es un equilibrio entre privacidad y la baja fricción que necesita una persona con TDAH."))
    story.append(CODE("""
private const val RELOCK_AFTER_MS = 5 * 60 * 1000L

Lifecycle.Event.ON_STOP -> {
    if (currentUnlocked && currentCanAuthenticate && !currentPrompting) {
        backgroundedAt = SystemClock.elapsedRealtime()
    }
}

Lifecycle.Event.ON_START -> {
    if (currentUnlocked && backgroundedAt > 0L &&
        SystemClock.elapsedRealtime() - backgroundedAt >= RELOCK_AFTER_MS
    ) {
        unlocked = false
    }
    backgroundedAt = 0L
}
"""))
    story.append(H2("7.3 Widget de próxima toma"))
    story.append(P("NextDoseWidgetProvider es un AppWidgetProvider con RemoteViews. Guarda un snapshot de la próxima toma pendiente o pospuesta y lo pinta en el launcher. Se actualiza cuando cambian las tomas y cada 30 minutos (updatePeriodMillis=1800000). Si el día cambió, invita a abrir MEDs para refrescar."))
    story.append(H2("7.4 Alarmas y notificaciones locales"))
    story.append(P("AlarmScheduler programa alarmas exactas cuando el sistema lo permite y usa alarmas inexactas como respaldo. Las notificaciones incluyen las acciones Ya la tomé y Posponer 10 min, que ejecutan DoseActionReceiver. BootReceiver reprograma todo al reiniciar el dispositivo."))
    story.append(H2("7.5 Push con FCM"))
    story.append(P("La app registra el token FCM del paciente en users/{uid}/devices/{token} y lo elimina al cerrar sesión. Las Cloud Functions nudgeCreated y companionLinked escuchan Firestore y envían el push a todos los dispositivos del paciente. Los canales meds_nudges_v2 y meds_companions tienen importancia alta, sonido, vibración y aviso en pantalla."))
    story.append(P("Estado de entrega: la app y las Functions están implementadas y las Functions cargan en el emulador. Para que el push con la app cerrada funcione en producción hay que habilitar Blaze, ejecutar firebase login y desplegar firebase deploy --only functions,firestore:rules. Sin ese despliegue, la app sigue mostrando los recordatorios dentro de la interfaz, pero no como push del sistema. Android tampoco entrega push si el usuario usa Forzar detención."))
    story.append(H2("7.6 Permisos declarados"))
    story.append(T([
        ["Permiso", "Uso"],
        ["INTERNET", "Firebase, CIMA y FCM."],
        ["POST_NOTIFICATIONS", "Recordatorios locales y push (Android 13+)."],
        ["SCHEDULE_EXACT_ALARM", "Alarmas exactas; si no se concede, se usan inexactas."],
        ["RECEIVE_BOOT_COMPLETED", "Reprogramar alarmas tras reiniciar."],
        ["CAMERA", "Escaneo del QR con ZXing."],
        ["VIBRATE / WAKE_LOCK", "Notificaciones y alarmas confiables."],
    ], [160, CONTENT_WIDTH - 160]))
    story.append(PageBreak())

    # 8. Estabilidad
    story.append(H1("8. Estabilidad, validaciones y experiencia de uso"))
    story.append(H2("8.1 Validaciones"))
    story.append(T([
        ["Campo / flujo", "Validación"],
        ["Correo", "Formato con expresión regular; error junto al campo."],
        ["Contraseña", "Mínimo 6 caracteres; confirmación obligatoria en registro y vinculación."],
        ["Medicamento", "Nombre y al menos una hora; dosis y forma con valores por defecto."],
        ["Código de acompañante", "8 caracteres alfanuméricos; existencia, expiración y no uso previo."],
        ["Nudge", "Mensaje obligatorio, máximo 160 caracteres."],
        ["Stock", "Se descuenta solo si hay existencias; nunca queda negativo."],
        ["Búsqueda CIMA", "Mínimo 2 caracteres; se recorta el texto y se limita a 80."],
    ], [150, CONTENT_WIDTH - 150]))
    story.append(H2("8.2 Errores y recuperación"))
    story.append(T([
        ["Situación", "Respuesta"],
        ["Credenciales inválidas", "Mensaje claro; no se cierra la app."],
        ["Correo ya registrado", "Se sugiere iniciar sesión o usar otro correo."],
        ["Contraseña débil", "Se indica el mínimo de 6 caracteres."],
        ["Sin conexión", "Firestore usa caché; CIMA muestra error de red y permite reintentar."],
        ["Permiso retirado al acompañante", "Mensaje específico y opción de desvincularse."],
        ["Perfil lento", "Timeout de 8 s; la app no queda bloqueada."],
        ["Doble toque en una toma", "Se comprueba el estado antes de volver a escribir."],
        ["ID numérico grande de CIMA", "Se corrigió el modelo a Long tras una prueba real."],
        ["Vinculación recién creada", "Reintento breve ante PERMISSION_DENIED transitorio."],
    ], [165, CONTENT_WIDTH - 165]))
    story.append(H2("8.3 Salvaguardas de experiencia"))
    story += BULLETS([
        "Advertencia antes de continuar sin cuenta y confirmación al cerrar sesión.",
        "Estados de carga, vacío y error; snackbars para fallos de escritura.",
        "Cambio de día detectado cada 30 segundos; el widget avisa si quedó obsoleto.",
        "La confirmación de toma es idempotente y cancela la notificación correspondiente.",
        "El modo acompañante muestra solo el día y permite desvincularse.",
        "La biometría no interrumpe cambios rápidos de app; solo protege tras 5 minutos fuera.",
    ])
    story.append(H2("8.4 Pruebas realizadas"))
    story.append(P("Se verificaron los recorridos en emulador Pixel 6 Pro: login, registro, modo anónimo, advertencia, vinculación de correo, cierre de sesión, restauración de perfil, alta de medicamento, toma, historial, QR, biometría, widget, canales de notificación, consulta CIMA con resultados, ficha, sin resultados y error sin conexión."))
    story.append(P("Durante las pruebas se corrigieron defectos reales: la UI no se actualizaba al vincular el correo; la restauración de perfil podía reescribir preferencias del usuario anterior; el panel del acompañante mostraba un error falso justo después de vincular; y el ID vtm de CIMA desbordaba un Int y se cambió a Long. La versión 1.1 se probó en emulador; se recomienda repetir la prueba física en el dispositivo antes de la entrega."))
    story.append(PageBreak())

    # 9. Repositorio y versión
    story.append(H1("9. Repositorio, documentación y versión entregable"))
    story.append(H2("9.1 Estado actual"))
    story.append(T([
        ["Elemento", "Estado"],
        ["Equipo", TEAM],
        ["Universidad", UNIVERSITY],
        ["Fecha de entrega", DATE_TEXT],
        ["Código fuente", "Proyecto Android completo en Kotlin/Compose."],
        ["Repositorio", "https://github.com/HVT-IAN/proyecto_movil-Med"],
        ["Ramas", "main, develop y feature/consulta-cima-biometria integrada a develop."],
        ["README", "Actualizado con stack, API CIMA, biometría, push, release y estructura."],
        ["Documentación", "Este PDF, versión 1.1."],
        ["Evidencias", "38 capturas en evidencias/."],
        ["Versión", "versionName 1.1, versionCode 2."],
        ["Release", "APK y AAB firmados en dist/."],
        ["GitHub Release", "v1.1 publicado con APK y AAB adjuntos."],
        ["Firma", "Keystore local no versionado; contraseña en keystore.properties, ignorado por Git."],
    ], [120, CONTENT_WIDTH - 120]))
    story.append(H2("9.2 Release firmado"))
    story.append(P("El release se compila con assembleRelease y bundleRelease. El APK firmado queda en dist/MEDs-release-1.1.apk y el AAB en dist/MEDs-release-1.1.aab. La firma se verificó con apksigner; el certificado es CN=MEDs, OU=Proyecto integrador, O=MEDs, L=Madrid, ST=Madrid, C=ES. El Release v1.1 está publicado en GitHub con el APK y el AAB adjuntos."))
    story.append(P("El keystore y keystore.properties no se suben al repositorio. Hay que respaldarlos: sin ellos no se pueden publicar actualizaciones con la misma identidad. El APK de depuración también queda en dist/MEDs-debug-production.apk para pruebas internas."))
    story.append(H2("9.3 Checklist antes de entregar"))
    story += BULLETS([
        "Reglas de Firestore publicadas y probadas en producción.",
        "Release 1.1 firmado e instalado en el dispositivo físico.",
        "PDF y README actualizados.",
        "Evidencias 32 a 38 incluidas.",
        "Tag v1.1 y Release de GitHub publicados con APK y AAB adjuntos.",
        "Cuenta de demostración con correo y contraseña disponibles por canal aparte.",
        "Guion de 10 minutos ensayado.",
        "Push: desplegar Functions y Blaze si se va a demostrar con la app cerrada.",
    ])
    story.append(PageBreak())

    # 10. Demo
    story.append(H1("10. Demostración práctica y defensa técnica"))
    story.append(H2("10.1 Guion de 10 minutos"))
    story.append(T([
        ["Minuto", "Qué mostrar"],
        ["0:00 - 1:00", "Problema y propuesta: adherencia, TDAH, dos roles y promesa de bajo ruido."],
        ["1:00 - 2:30", "Login o registro; advertencia anónima; vincular correo en Ajustes y mostrar que los datos se conservan. La biometría aparece después del onboarding, no antes."],
        ["2:30 - 4:00", "Onboarding, alta de medicamento y consulta CIMA: buscar ibuprofeno, abrir ficha y usar el principio activo."],
        ["4:00 - 5:30", "Pantalla Hoy, marcar una toma, posponer otra, historial y jardín."],
        ["5:30 - 7:30", "Modo acompañante en un segundo dispositivo: generar QR, mostrar que el anterior queda inválido, escanear, ver el día y enviar un nudge."],
        ["7:30 - 8:30", "Widget, notificación local con acciones, canales de push y desbloqueo biométrico."],
        ["8:30 - 10:00", "Arquitectura: Repository, ViewModel, UI, Retrofit/CIMA, reglas y persistencia. Cerrar con la rúbrica."],
    ], [85, CONTENT_WIDTH - 85]))
    story.append(H2("10.2 Preguntas probables"))
    story.append(T([
        ["Pregunta", "Respuesta corta"],
        ["¿Dónde está la llamada a la API?", "En MedicationInfoRepository, con Retrofit contra CIMA; las pantallas nunca llaman a la API directamente."],
        ["¿Por qué CIMA y no openFDA?", "CIMA es la API oficial española, devuelve datos en español y encaja con la app; openFDA quedó como alternativa documentada."],
        ["¿Por qué no Room?", "Firestore es la fuente de verdad y ya tiene caché offline; DataStore cubre preferencias. La consulta CIMA es información de referencia y no se persiste."],
        ["¿Cómo funciona sin conexión?", "Firestore conserva la caché; CIMA muestra un error de red claro y permite reintentar."],
        ["¿Cómo se protege la privacidad?", "Reglas de Firestore, invitaciones de un solo uso y biometría después del login."],
        ["¿Por qué la biometría no se pide al abrir?", "Porque antes del login no hay datos que proteger. Se pide al entrar al espacio y tras 5 minutos en segundo plano."],
        ["¿Qué pasa al cerrar sesión?", "Se limpian las preferencias; una cuenta con correo se recupera al iniciar sesión; una anónima sin vincular se pierde y la UI lo advierte."],
        ["¿Qué falta?", "Desplegar Functions/Blaze para el push real y probar el release en el dispositivo físico."],
    ], [135, CONTENT_WIDTH - 135]))
    story.append(H2("10.3 Mapa de código para la defensa"))
    story.append(T([
        ["Tema", "Archivos"],
        ["Entrada", "MainActivity.kt, MedsApp.kt"],
        ["Autenticación", "AuthRepository.kt, AppViewModel.kt, AuthScreen.kt"],
        ["API CIMA", "CimaApi.kt, CimaModels.kt, MedicationInfoRepository.kt, MedicationInfoViewModel.kt"],
        ["Consulta CIMA UI", "MedicationInfoScreen.kt, MedicationEditorScreen.kt, PatientApp.kt"],
        ["Estado del paciente", "PatientViewModel.kt"],
        ["Firestore", "PatientRepository.kt, CompanionRepository.kt, FirestoreMappers.kt"],
        ["Biometría", "BiometricGate.kt, MedsRoot.kt"],
        ["Push", "MedsMessagingService.kt, NotificationHelper.kt, functions/index.js"],
        ["Persistencia", "PreferencesStore.kt, NextDoseWidgetProvider.kt, AlarmScheduler.kt"],
        ["Seguridad", "firestore.rules"],
    ], [130, CONTENT_WIDTH - 130]))
    story.append(PageBreak())

    # 11. Rubric
    story.append(H1("11. Cobertura de la rúbrica"))
    story.append(P("La rúbrica califica sobre 100 puntos y aporta el 60 % de la unidad. La tabla resume dónde se cubre cada criterio en la versión 1.1."))
    story.append(T([
        ["Criterio", "Máx.", "Evidencia en este documento", "Estado"],
        ["1. Arquitectura MVVM y organización", "20", "Secciones 3 y 10.3: capas, StateFlow, repositorios, DI manual y recorridos.", "Cubierto"],
        ["2. Consumo de API y tratamiento de datos", "15", "Sección 4: Firebase + Retrofit contra CIMA, DTOs, mapeo, errores y seguridad.", "Cubierto"],
        ["3. Interfaz Compose y navegación", "15", "Sección 5: NavHost, ruta medication_info, estados, tema y componentes.", "Cubierto"],
        ["4. Gestión de datos y persistencia", "10", "Sección 6: DataStore, caché de Firestore, SharedPreferences y alarmas.", "Cubierto"],
        ["5. Integraciones del dispositivo", "15", "Sección 7: QR, biometría y widget; alarmas y push como extras.", "Cubierto"],
        ["6. Estabilidad, validaciones y UX", "10", "Sección 8: validaciones, errores, estados, pruebas y correcciones.", "Cubierto"],
        ["7. Repositorio, documentación y versión", "5", "Sección 9: repo, ramas, release firmado, PDF y 38 evidencias.", "Cubierto"],
        ["8. Demostración y defensa", "10", "Sección 10: guion de 10 minutos, mapa de código y preguntas.", "Cubierto"],
        ["Total", "100", "Criterios 1 a 8 cubiertos en la versión 1.1.", "100/100"],
    ], [130, 35, CONTENT_WIDTH - 225, 60]))
    story.append(H2("11.1 Riesgos y acciones"))
    story.append(T([
        ["Riesgo", "Nivel", "Acción"],
        ["CIMA no disponible o sin red", "Medio", "La app muestra error y reintento; la consulta no bloquea el resto de la app."],
        ["Push sin desplegar", "Medio", "Habilitar Blaze, firebase login y desplegar Functions/reglas antes de demostrar el push cerrado."],
        ["Prueba física pendiente", "Medio", "Instalar el release 1.1 en el dispositivo y repetir login, biometría, QR, widget y CIMA."],
        ["Keystore no respaldado", "Bajo", "Guardar keystore y keystore.properties en un lugar seguro, fuera de Git."],
        ["Cuenta anónima sin vincular", "Bajo", "Vincular un correo en la demo."],
    ], [150, 55, CONTENT_WIDTH - 205]))
    story.append(H2("11.2 Conclusión"))
    story.append(P("La versión 1.1 cubre los ocho criterios de la rúbrica. El criterio 2 deja de ser un matiz porque incorpora Retrofit y una API REST real; el criterio 7 se completa con repositorio, ramas, release firmado, documentación y evidencias. El push con la app cerrada queda como una funcionalidad extra cuyo despliegue en Blaze debe completarse antes de la demostración."))
    story.append(PageBreak())

    # 12. Evidencias
    story.append(H1("12. Evidencias y anexos"))
    story.append(P("Las capturas están en la carpeta evidencias/. Las imágenes 32 a 38 documentan la consulta CIMA y la ubicación correcta de la biometría. Las capturas 21 a 31 documentan QR, vinculación, recordatorios y canales de push."))
    story.append(T([
        ["Archivo", "Contenido"],
        ["32_consulta_medicamento.png", "Pantalla de consulta de medicamentos con CIMA/AEMPS."],
        ["33_resultados_cima.png", "Resultados en español; 20 de 137, priorizando el principio activo."],
        ["34_detalle_medicamento.png", "Ficha con principios activos, ATC, presentaciones y documentos."],
        ["35_sin_resultados.png", "Estado sin resultados."],
        ["36_error_conexion.png", "Error sin conexión con opción de reintentar."],
        ["37_biometria_tras_login.png", "Bloqueo biométrico después de entrar a un espacio creado."],
        ["38_login_sin_biometria.png", "Login sin solicitud de huella después de cerrar sesión."],
    ], [175, CONTENT_WIDTH - 175]))
    story.append(Spacer(1, 8))
    story.append(IMAGE("evidencias/33_resultados_cima.png", width=250))
    story.append(P("Evidencia 33. Resultados de CIMA en español.", caption))
    story.append(PageBreak())
    story.append(IMAGE("evidencias/34_detalle_medicamento.png", width=250))
    story.append(P("Evidencia 34. Ficha detallada con documentos oficiales.", caption))
    story.append(PageBreak())
    story.append(IMAGE("evidencias/35_sin_resultados.png", width=250))
    story.append(P("Evidencia 35. Estado sin resultados.", caption))
    story.append(PageBreak())
    story.append(IMAGE("evidencias/36_error_conexion.png", width=250))
    story.append(P("Evidencia 36. Error de conexión con reintento.", caption))
    story.append(PageBreak())
    story.append(IMAGE("evidencias/37_biometria_tras_login.png", width=250))
    story.append(P("Evidencia 37. Biometría después del login.", caption))
    story.append(PageBreak())
    story.append(IMAGE("evidencias/38_login_sin_biometria.png", width=250))
    story.append(P("Evidencia 38. Login sin biometría.", caption))
    story.append(PageBreak())

    # Annexes
    story.append(H1("Anexos"))
    story.append(H2("A. Comandos útiles"))
    story.append(CODE("""
# Compilar debug
.\\gradlew.bat :app:assembleDebug

# Compilar release firmado
.\\gradlew.bat :app:assembleRelease :app:bundleRelease

# Instalar el release
adb install -r dist\\MEDs-release-1.1.apk

# Verificar firma
apksigner verify --print-certs dist\\MEDs-release-1.1.apk

# Emuladores de Firebase (opcional)
npx --yes firebase-tools@latest emulators:start --only auth,firestore --project medsproto

# Desplegar Functions y reglas (requiere Blaze y firebase login)
firebase deploy --only functions,firestore:rules --project medsproto
"""))
    story.append(H2("B. Cuentas de prueba"))
    story.append(P("Cuenta de paciente para la demostración: prueba.push.meds@example.com. La contraseña se comparte por un canal aparte del repositorio. También existe una cuenta de acompañante anónima en el emulador, vinculada a la paciente de prueba. Las cuentas de ejemplo deben eliminarse antes de publicar la app."))
    story.append(H2("C. Glosario"))
    story.append(T([
        ["Término", "Significado"],
        ["MVVM", "Model-View-ViewModel: separa datos, estado e interfaz."],
        ["StateFlow", "Flujo de estado observable e inmutable que consume Compose."],
        ["Repository", "Capa que encapsula el acceso a datos y las reglas de negocio."],
        ["Batch", "Escritura atómica de varios documentos en Firestore."],
        ["Retrofit", "Cliente HTTP para consumir la API REST de CIMA."],
        ["CIMA", "Centro de Información online de Medicamentos de la AEMPS."],
        ["Nudge", "Recordatorio breve que el acompañante envía al paciente."],
        ["FCM", "Firebase Cloud Messaging, servicio de push."],
        ["Keystore", "Archivo de firma necesario para publicar actualizaciones."],
    ], [100, CONTENT_WIDTH - 100]))
    story.append(H2("D. Checklist final"))
    story += BULLETS([
        "Release 1.1 firmado e instalado en el dispositivo físico.",
        "Reglas de Firestore publicadas y probadas.",
        "Functions y Blaze desplegados si se demuestra push cerrado.",
        "PDF, README y evidencias 1 a 38 actualizados.",
        "Tag v1.1 y Release de GitHub publicados con APK y AAB adjuntos.",
        "Cuenta de demostración y contraseña disponibles por canal aparte.",
        "Guion de 10 minutos ensayado.",
    ])

    doc = SimpleDocTemplate(
        str(OUTPUT),
        pagesize=A4,
        leftMargin=LEFT,
        rightMargin=RIGHT,
        topMargin=TOP,
        bottomMargin=BOTTOM,
        title="MEDs - Documentación funcional y técnica 1.1",
        author=TEAM,
        subject="Proyecto integrador - aplicación móvil",
    )
    doc.build(story, onFirstPage=cover_page, onLaterPages=header_footer)
    print(f"PDF generado: {OUTPUT}")


if __name__ == "__main__":
    build()
