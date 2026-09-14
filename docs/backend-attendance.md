# Backend de asistencia

Este documento describe la capa backend preparada para conectar una persistencia real mas adelante. No define tablas, columnas, migraciones ni motor de base de datos.

## Componentes

- `AttendanceController`: punto de entrada conceptual para solicitudes de asistencia.
- `AttendanceService`: contiene las reglas de negocio de entrada, salida, duplicados, reportes y correcciones.
- `AttendanceRepository`: interfaz que debe implementar la capa de persistencia definitiva.
- `InMemoryAttendanceRepository`: implementacion temporal para pruebas y desarrollo.
- `ScheduleRepository`: interfaz para obtener el horario aplicable a un trabajador en una fecha.
- `DefaultScheduleRepository`: implementacion temporal con jornada lunes a viernes, 08:00 a 17:24 y 60 minutos de colacion.
- `BackendApiClient`: contrato para llamar a una API intermedia cuando exista.
- `BackendOperationService`: centraliza errores de conexion, timeout, servidor no disponible y respuestas invalidas.
- `OfficialTimeProvider`: abstraccion de hora oficial.
- `SystemOfficialTimeProvider`: implementacion temporal basada en un `Clock` configurable.
- `FirebaseDataConnectConfig`: conserva el proyecto y conector entregados por el encargado de base de datos.

## Firebase Data Connect y Java Desktop

El proyecto es una aplicacion Java Swing de escritorio. Firebase SQL/Data Connect publica SDKs cliente orientados a Kotlin Android, iOS/Swift, Flutter y web. Para esta aplicacion no se debe usar una libreria Android ni conectar directamente a PostgreSQL exponiendo credenciales.

La arquitectura recomendada queda:

```text
Aplicacion Java Desktop
  -> API/backend intermedio
  -> Firebase Data Connect
  -> Base de datos administrada
```

Datos configurados para la futura conexion:

- Proyecto Firebase: `app-asistencia`
- Project ID: `app-asistencia-5e6fe`
- Servicio SQL/Data Connect: `southamerica-west1/app-asistencia-5e6fe-service`
- Conector local: `default`
- Cloud SQL instance: `app-asistencia-5e6fe-instance`
- Database: `app-asistencia-5e6fe-database`

El repositorio ya contiene configuracion local de Data Connect: `firebase.json`, `.firebaserc`, `dataconnect/dataconnect.yaml`, `dataconnect/schema/schema.gql`, `dataconnect/default/connector.yaml`, `dataconnect/default/queries.gql` y `dataconnect/default/mutations.gql`.

`queries.gql` y `mutations.gql` quedan sin operaciones reales hasta que el equipo defina el contrato de lectura/escritura. No se inventaron queries ni mutations.

## Hora oficial

La logica de negocio nunca llama directamente a `LocalDateTime.now()` ni a `System.currentTimeMillis()`.

`AttendanceService` obtiene la fecha y hora desde:

```java
OfficialTimeProvider.now()
```

Cuando exista una fuente confiable del servidor o base de datos, se debe crear otra implementacion de `OfficialTimeProvider` y reemplazar la temporal.

En produccion, el backend intermedio debe fijar la fecha/hora oficial del registro y devolverla a la aplicacion de escritorio. El cliente no debe enviar la hora como dato confiable para registrar entrada o salida.

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
- Horarios por trabajador y dia laboral: entrada, salida, colacion y dias aplicables.

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

La interfaz `ScheduleRepository` exige:

- `findByWorkerAndDate`

La interfaz `UserRepository` prepara:

- `authenticate`
- `findById`
- `findByEmail`
- `create`
- `update`
- `deactivate`
- `findActiveUsers`

## Reglas implementadas

- Entrada a la hora asignada o antes: `A_TIEMPO`.
- Entrada despues de la hora asignada: `ATRASO`.
- Calculo automatico de minutos de atraso.
- Salida a la hora asignada o despues: `SALIDA_NORMAL`.
- Salida antes de la hora asignada: `SALIDA_ANTICIPADA`.
- Calculo automatico de minutos faltantes.
- No se permite mas de una entrada por trabajador en la fecha actual.
- No se permite mas de una salida por trabajador en la fecha actual.
- La salida requiere una entrada previa.
- Solo administradores pueden corregir registros.
- Toda correccion genera una auditoria conceptual.
- Un trabajador sin horario asignado no puede registrar asistencia.
- Las inasistencias se calculan para trabajadores con horario laboral asignado en la fecha consultada y sin registros.

## Comando de inicializacion entregado

El comando de inicializacion interactiva ya no es necesario para crear la configuracion basica local, porque los archivos fueron creados manualmente apuntando al servicio existente. Si el encargado de base de datos pide regenerar la configuracion oficial con Firebase Tools, ejecutar:

```powershell
$env:FIREBASE_PROJECT = 'app-asistencia-5e6fe';
$env:FDC_CONNECTOR = 'southamerica-west1/app-asistencia-5e6fe-service/default';
IEX (New-Object Net.WebClient).DownloadString('https://firebase.tools/init/dataconnect.ps1')
```

Despues de ejecutarlo, se deben revisar los archivos generados antes de modificar codigo Java.

Para validar sin desplegar:

```powershell
firebase dataconnect:compile --project app-asistencia-5e6fe
firebase dataconnect:sql:diff --project app-asistencia-5e6fe
```

No ejecutar `firebase deploy` ni `firebase dataconnect:sql:migrate` hasta revisar el diff con el encargado de base de datos.

## Datos enviados desde frontend

Los metodos `registerEntry` y `registerExit` del controlador reciben solamente la referencia del trabajador. No reciben fecha ni hora como parametro confiable.

Si un cliente intenta enviar una hora propia, esa hora no participa en el calculo. La hora usada siempre proviene de `OfficialTimeProvider`.
