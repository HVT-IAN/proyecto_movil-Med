# MEDs

Aplicación Android nativa para recordar medicamentos, pensada para personas con TDAH. Prioriza una experiencia tranquila, clara y de baja fricción: recordatorios locales, confirmación de tomas en un toque y un modo acompañante vinculado mediante un código QR temporal.

Estado: prototipo funcional. La app compila, funciona con los emuladores locales de Firebase y cubre el flujo principal de paciente y acompañante. La configuración de producción de Firebase requiere completar los pasos de la sección correspondiente.

## Características

### Paciente

- Onboarding con elección de rol y nombre.
- Sesión anónima, sin cuentas ni contraseñas.
- Pantalla Hoy con la próxima toma, confirmación y posposición de 10 minutos.
- Gestión de medicamentos con nombre, dosis, forma, stock y una o varias horas.
- Generación automática de las tomas del día.
- Historial de los últimos 7 días con porcentaje de adherencia.
- Jardín con progreso, racha y escudos.
- Vinculación de acompañantes mediante QR o código manual.
- Revocación del acceso de un acompañante en cualquier momento.
- Recordatorios locales con acciones para confirmar o posponer.

### Acompañante

- Vinculación mediante QR o código de 8 caracteres.
- Panel en tiempo real con las tomas y los medicamentos del día.
- Envío de recordatorios al paciente.
- Acceso de solo lectura a la información del día.
- Posibilidad de desvincularse.

### Recordatorios

- Alarmas exactas cuando el sistema lo permite.
- Alternativa con alarmas inexactas si no se concede el permiso especial.
- Notificaciones locales con las acciones `Ya la tomé` y `Posponer 10 min`.
- Reprogramación al reiniciar el dispositivo o actualizar la app.
- Canales separados para recordatorios de medicación y avisos de acompañantes.
- Sin FCM ni Cloud Functions: los avisos son locales.
