# RAVIA Backend - Deploy en Render

Este backend esta listo para Render usando Docker. El archivo `../render.yaml` define un Web Service llamado `ravia-backend`.

## 1. Antes de subir

No subas archivos secretos:

- `.env`
- `firebase-service-account.json`

Ya estan ignorados por `.gitignore` y `.dockerignore`.

## 2. Crear el servicio

1. Sube el proyecto a GitHub/GitLab/Bitbucket.
2. En Render, crea un **Blueprint** desde el repositorio.
3. Render leera `render.yaml` y creara el servicio `ravia-backend`.
4. Cuando Render pida `FIREBASE_SERVICE_ACCOUNT_JSON`, pega el contenido completo de `ravia-backend/firebase-service-account.json` en una sola variable.

Render recomienda que los web services escuchen en `0.0.0.0` y usen la variable `PORT`; este backend ya lo hace en `src/main.ts`.

## 3. Variables principales

Estas quedan en `render.yaml`:

- `NODE_ENV=production`
- `FIREBASE_PROJECT_ID=ravia-4f174`
- `FIREBASE_STORAGE_BUCKET=ravia-4f174.firebasestorage.app`
- `AI_PROVIDER=internal`
- `THROTTLE_TTL=60000`
- `THROTTLE_LIMIT=100`
- `CORS_ORIGINS=http://localhost:3001`

Estas se configuran como secreto en Render:

- `FIREBASE_SERVICE_ACCOUNT_JSON`
- `JWT_SECRET` se genera automaticamente si usas el Blueprint.

Si quieres usar Groq para el chatbot, agrega manualmente:

- `GROQ_API_KEY`

## 4. Verificar deploy

Cuando Render termine, abre:

```text
https://TU-SERVICIO.onrender.com/api/v1/health
```

Debe responder algo como:

```json
{
  "status": "ok",
  "service": "ravia-backend"
}
```

## 5. Actualizar clientes

Backend base:

```text
https://TU-SERVICIO.onrender.com/api/v1/
```

Android debug:

```properties
ravia.debugBaseUrl=https://TU-SERVICIO.onrender.com/api/v1/
```

Admin web:

```env
NEXT_PUBLIC_API_URL=https://TU-SERVICIO.onrender.com/api/v1
```

Cuando el admin tenga URL publica, actualiza `CORS_ORIGINS` en Render con esa URL.
