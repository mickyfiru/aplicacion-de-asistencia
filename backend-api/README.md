# Backend API Asistencia

Este modulo prepara una API confiable para ejecutar en Cloud Run con Java 17 y Spring Boot.

## Flujo de seguridad

- El cliente Swing debe iniciar sesion con Firebase Authentication y enviar solo el Firebase ID Token al backend.
- El cliente Swing no debe recibir ni usar `FIREBASE_ACCESS_TOKEN`, service account JSON ni credenciales administrativas.
- El backend valida el ID Token con Firebase Admin SDK y usa `auth.uid` para encontrar el `User.authUid` correspondiente. Tambien conserva `email` y `claims` del token verificado para validaciones futuras.
- Las operaciones sensibles de Firebase Data Connect quedan con `@auth(level: NO_ACCESS)` y se ejecutan desde el backend con credenciales de servidor.
- `GET /api/health` no requiere autenticacion. Los endpoints `/api/asistencia/**` y `/api/horario/**` requieren `Authorization: Bearer <Firebase ID Token>`.

## Hora oficial

- `Instant.now()` en el PC del trabajador no es confiable para asistencia.
- `Instant.now()` en Cloud Run es aceptable para este sistema porque corre en un entorno servidor controlado.
- La fecha de negocio se calcula con:

```java
backendClock.instant().atZone(ZoneId.of("America/Santiago")).toLocalDate()
```

- `horaEntrada` se registra con `request.time` de Data Connect.
- `horaSalida` se registra con `horaSalida_expr: "request.time"`.
- Los estados y minutos se calculan despues de recuperar el timestamp realmente guardado por Data Connect.

## Pendientes antes de deploy

- Habilitar Firebase Authentication con Email/Password desde Firebase Console si aun no esta activo.
- Configurar una cuenta de servicio dedicada de Cloud Run con permisos minimos para ejecutar Data Connect.
- Desplegar Cloud Run y configurar el cliente Swing para llamar esta API con Firebase ID Token.

## Variables de entorno Cloud Run

Estas variables no son secretas:

```text
FIREBASE_PROJECT_ID=app-asistencia-5e6fe
FIREBASE_DATACONNECT_LOCATION=southamerica-west1
FIREBASE_DATACONNECT_SERVICE=app-asistencia-5e6fe-service
FIREBASE_DATACONNECT_CONNECTOR=default
```

Cloud Run inyecta `PORT`; Spring Boot escucha `server.port=${PORT:8080}`.

## Cuenta de servicio recomendada

Usar una cuenta dedicada, por ejemplo:

```text
asistencia-backend@app-asistencia-5e6fe.iam.gserviceaccount.com
```

Roles de ejecucion recomendados:

- `roles/firebasedataconnect.dataAdmin`, para ejecutar consultas y mutaciones Data Connect desde el backend.
- Sin llaves JSON ni variables con private key. Cloud Run debe usar Application Default Credentials.

Para verificar ID tokens con Firebase Admin SDK en Cloud Run, el backend inicializa el SDK con credenciales por defecto del entorno.
