# Backend de asistencia

Este documento describe la capa backend preparada para conectar una persistencia real mas adelante. No define tablas, columnas, migraciones ni motor de base de datos.

## Componentes

- `AttendanceController`: punto de entrada conceptual para solicitudes de asistencia.
- `AttendanceService`: contiene las reglas de negocio de entrada, salida, duplicados, reportes y correcciones.
- `AttendanceRepository`: interfaz que debe implementar la capa de persistencia definitiva.
- `InMemoryAttendanceRepository`: implementacion temporal para pruebas y desarrollo.
- `OfficialTimeProvider`: abstraccion de hora oficial.
- `SystemOfficialTimeProvider`: implementacion temporal basada en un `Clock` configurable.

## Hora oficial

La logica de negocio nunca llama directamente a `LocalDateTime.now()` ni a `System.currentTimeMillis()`.

`AttendanceService` obtiene la fecha y hora desde:

```java
OfficialTimeProvider.now()
```

Cuando exista una fuente confiable del servidor o base de datos, se debe crear otra implementacion de `OfficialTimeProvider` y reemplazar la temporal.

## Datos que debe persistir AttendanceRepository

La persistencia definitiva debera guardar, como minimo:

- ID del registro.
- ID del trabajador.
- Nombre del trabajador.
- Identificador o RUT.
- Fecha del registro.
- Hora de entrada.
- Estado de entrada: `A_TIEMPO` o `ATRASO`.
- Minutos de atraso.
- Hora de salida.
- Estado de salida: `SALIDA_NORMAL` o `SALIDA_ANTICIPADA`.
- Minutos faltantes.
- Auditorias de correccion con registro modificado, valor anterior, valor nuevo, administrador, fecha/hora y motivo.

## Metodos que debe implementar la persistencia real

La interfaz `AttendanceRepository` exige:

- `save`
- `findById`
- `findByWorkerAndDate`
- `hasEntryForDate`
- `hasExitForDate`
- `findLateArrivals`
- `findEarlyDepartures`
- `saveAuditLog`
- `findAuditLogsByRecord`

## Reglas implementadas

- Entrada a las 08:00 o antes: `A_TIEMPO`.
- Entrada despues de las 08:00: `ATRASO`.
- Calculo automatico de minutos de atraso.
- Salida a las 17:24 o despues: `SALIDA_NORMAL`.
- Salida antes de las 17:24: `SALIDA_ANTICIPADA`.
- Calculo automatico de minutos faltantes.
- No se permite mas de una entrada por trabajador en la fecha actual.
- No se permite mas de una salida por trabajador en la fecha actual.
- La salida requiere una entrada previa.
- Solo administradores pueden corregir registros.
- Toda correccion genera una auditoria conceptual.

## Datos enviados desde frontend

Los metodos `registerEntry` y `registerExit` del controlador reciben solamente la referencia del trabajador. No reciben fecha ni hora como parametro confiable.

Si un cliente intenta enviar una hora propia, esa hora no participa en el calculo. La hora usada siempre proviene de `OfficialTimeProvider`.
