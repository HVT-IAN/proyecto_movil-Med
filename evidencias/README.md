# Evidencias de funcionamiento

Capturas tomadas en el emulador Pixel_6_Pro. Las primeras con la variante de Firebase Emulator Suite; las de QR, push, consulta CIMA y biometría con el APK de producción y Firebase real.

| Archivo | Contenido |
| --- | --- |
| `00_companion_previo.png` | Pantalla de vinculación del acompañante. |
| `01_rol.png` | Selección de rol con la flecha de retroceso superior derecha. |
| `02_onboarding_paso1.png` | Onboarding, paso 1, con la flecha para volver a elegir rol. |
| `03_vuelta_rol.png` | Resultado de volver desde onboarding a la selección de rol. |
| `04_onboarding_paso2.png` | Onboarding, paso 2, con la flecha interna y la flecha de regreso al rol. |
| `05_home_paciente.png` | Pantalla Hoy del paciente. |
| `06_editor_medicamento.png` | Editor de medicamento. |
| `07_selector_hora.png` | Selector de hora del recordatorio. |
| `08_editor_completo.png` | Editor con horario configurado y botón Guardar. |
| `09_home_con_medicamento.png` | Hoy con la próxima toma visible. |
| `10_launcher_widgets.png` | Menú del launcher con la opción Widgets. |
| `11_widget_picker.png` | Buscador de widgets mostrando MEDs. |
| `12_widget_preview.png` | Vista previa del widget de próxima toma (3x2). |
| `13_biometria_huella_agregada.png` | Huella enrolada en el emulador para probar la biometría. |
| `14_biometria_bloqueo.png` | Pantalla de bloqueo biométrico al entrar a un espacio ya creado. |
| `15_biometria_desbloqueada.png` | App desbloqueada tras autenticación correcta. |
| `16_auth_login.png` | Inicio de sesión con correo y contraseña. |
| `17_ajustes_cuenta.png` | Ajustes con la cuenta vinculada y cierre de sesión. |
| `18_qr_acompanante.png` | Código QR temporal para vincular a un acompañante. |
| `19_escaner_vertical.png` | Escáner de QR abierto en vertical tras la corrección. |
| `20_escaner_sin_rotar.png` | El escáner permanece vertical aunque el dispositivo se gire a horizontal. |
| `21_qr_codigo_anterior.png` | Primer código QR generado para vincular a un acompañante. |
| `22_qr_codigo_regenerado.png` | Código nuevo generado desde el botón "Generar nuevo QR". |
| `23_qr_aviso_regeneracion.png` | Aviso "Nuevo código generado. El anterior ya no funciona." |
| `24_qr_codigo_vigente.png` | Código vigente después de invalidar el anterior. |
| `25_codigo_anterior_rechazado.png` | Un acompañante intenta usar el código anterior y recibe "Este código ya fue utilizado." |
| `26_vinculacion_exitosa.png` | Vinculación correcta con un código vigente; el panel carga los datos del paciente. |
| `27_recordatorio_enviado.png` | El acompañante envía un recordatorio y la app confirma "Recordatorio enviado." |
| `28_paciente_recibe_recordatorio.png` | El paciente ve el recordatorio pendiente en la pantalla Hoy. |
| `29_paciente_acompanante_vinculado.png` | El paciente ve al acompañante con acceso activo y el historial de recordatorios. |
| `30_canales_notificacion.png` | Canales de notificación de MEDs: vinculación, recordatorios y medicación. |
| `31_canal_sonido_vibracion.png` | Canal de recordatorios con importancia alta, sonido predeterminado, vibración y aviso en pantalla. |
| `32_consulta_medicamento.png` | Pantalla de consulta de medicamentos con la API oficial de CIMA/AEMPS. |
| `33_resultados_cima.png` | Resultados en español; se muestran 20 de 137 y se prioriza el principio activo buscado. |
| `34_detalle_medicamento.png` | Ficha detallada con principios activos, código ATC, presentaciones y documentos oficiales. |
| `35_sin_resultados.png` | Estado sin resultados al buscar un término inexistente. |
| `36_error_conexion.png` | Estado de error sin conexión al consultar CIMA, con opción de reintentar. |
| `37_biometria_tras_login.png` | Bloqueo biométrico al entrar a un espacio ya creado; ya no aparece antes del login. |
| `38_login_sin_biometria.png` | Pantalla de login sin solicitud de huella después de cerrar sesión. |

El diálogo del sistema biométrico no se puede capturar porque Android protege esa ventana; el desbloqueo exitoso se evidencia en `15_biometria_desbloqueada.png`.

Las capturas 21 a 38 se tomaron con el APK de producción y Firebase real. La entrega push con la app completamente cerrada requiere desplegar las Cloud Functions y habilitar el plan Blaze; esa prueba queda pendiente de despliegue.
