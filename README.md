# Sistema de Control de Asistencia de Trabajadores

Aplicacion de escritorio en Java para un MVP academico de control de asistencia. Permite iniciar sesion, registrar entradas y salidas, administrar usuarios y consultar reportes de atrasos, salidas anticipadas e inasistencias.

## Tecnologias utilizadas

- Java 17
- Java Swing
- Maven
- SQLite
- JDBC
- JUnit 5

## Requisitos

- JDK 17 o superior
- Maven 3.9 o superior
- Un IDE como IntelliJ IDEA, NetBeans o Eclipse

## Ejecucion

Desde la carpeta del proyecto:

```bash
mvn clean compile
mvn exec:java
```

La aplicacion crea automaticamente el archivo `asistencia.db` en la raiz del proyecto si no existe.

## Credenciales de prueba

Estas credenciales son solo para pruebas academicas:

- Correo: `admin@empresa.cl`
- Contrasena: `Admin123`
- Rol: `ADMINISTRADOR`

## Estructura del proyecto

```text
src/
  main/
    java/
      com/
        asistencia/
          model/       Entidades y enums del dominio
          dao/         Acceso a datos con JDBC
          service/     Reglas de negocio
          ui/          Ventanas y paneles Swing
          database/    Conexion e inicializacion SQLite
          util/        Hash de contrasenas, validaciones y fechas
  test/
    java/
      com/
        asistencia/
          service/     Pruebas unitarias de servicios
```

## Funcionalidades implementadas

- Inicio de sesion por correo y contrasena.
- Usuario administrador inicial.
- Hash de contrasenas con PBKDF2.
- Registro de entrada y salida para trabajadores.
- Validacion contra entradas duplicadas, salidas sin entrada y turnos repetidos en el mismo dia.
- Gestion de usuarios desde el panel administrador.
- Eliminacion logica de usuarios para mantener el historial de asistencia.
- Reporte de atrasos: entradas despues de las 09:30.
- Reporte de salidas anticipadas: salidas antes de las 17:30.
- Reporte de inasistencias por fecha.
- Persistencia local con SQLite.

## Pruebas

Ejecutar:

```bash
mvn test
```

Las pruebas usan bases SQLite temporales, por lo que no modifican `asistencia.db`.

## Clases principales

- `Main`: inicia la base de datos, servicios y ventana de login.
- `Usuario`: representa a un usuario del sistema.
- `Asistencia`: representa un registro de entrada o salida.
- `UsuarioDAO`: consulta y modifica usuarios en SQLite.
- `AsistenciaDAO`: consulta y registra asistencias en SQLite.
- `AuthService`: valida credenciales.
- `UsuarioService`: valida y gestiona usuarios.
- `AsistenciaService`: aplica reglas de registro de entrada y salida.
- `ReporteService`: genera reportes administrativos.
- `LoginFrame`: ventana de inicio de sesion.
- `WorkerFrame`: ventana del trabajador.
- `AdminFrame`: panel principal del administrador.

## Alcance MVP

El MVP prioriza una implementacion funcional, clara y presentable: autenticacion, roles, registro de asistencia, administracion basica y reportes esenciales.

## Mejoras futuras

- Recuperacion o cambio de contrasena sin requerir reescribirla al modificar un usuario.
- Exportar reportes a PDF o Excel.
- Filtros por trabajador y rango de fechas.
- Control de feriados y fines de semana para inasistencias.
- Auditoria de cambios administrativos.
