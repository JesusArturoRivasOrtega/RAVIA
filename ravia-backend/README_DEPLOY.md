# RAVIA Backend - Deploy en Cloud Run

Cloud Run evita el problema local de `EADDRINUSE` porque cada revision corre aislada y recibe su propio `PORT`.

## 1. Requisitos

- Google Cloud CLI instalado.
- Proyecto Firebase/GCP: `ravia-4f174`.
- Billing habilitado en Google Cloud.
- Firestore habilitado en el proyecto.

## 2. Login y proyecto

```powershell
gcloud auth login
gcloud config set project ravia-4f174
gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com firestore.googleapis.com firebase.googleapis.com
```

## 3. Deploy desde el codigo fuente

Ejecuta desde esta carpeta:

```powershell
cd C:\Users\ARTU2\AndroidStudioProjects\Ravia\ravia-backend
gcloud run deploy ravia-backend --source . --region us-central1 --allow-unauthenticated --set-env-vars NODE_ENV=production,FIREBASE_PROJECT_ID=ravia-4f174,FIREBASE_STORAGE_BUCKET=ravia-4f174.firebasestorage.app,AI_PROVIDER=internal,THROTTLE_TTL=60000,THROTTLE_LIMIT=100,CORS_ORIGINS=http://localhost:3001
```

Cloud Run inyecta automaticamente la variable `PORT`. No subas `.env` ni `firebase-service-account.json`.

## 4. Firebase Admin en Cloud Run

En local el backend usa `firebase-service-account.json`. En Cloud Run debe usar credenciales del servicio de Google, por eso no se configura `FIREBASE_SERVICE_ACCOUNT_PATH`.

Si Cloud Run no puede leer Firebase/Firestore, asigna permisos a la cuenta de servicio que ejecuta Cloud Run:

```powershell
gcloud projects add-iam-policy-binding ravia-4f174 --member="serviceAccount:PROJECT_NUMBER-compute@developer.gserviceaccount.com" --role="roles/firebase.admin"
gcloud projects add-iam-policy-binding ravia-4f174 --member="serviceAccount:PROJECT_NUMBER-compute@developer.gserviceaccount.com" --role="roles/datastore.user"
```

Cambia `PROJECT_NUMBER` por el numero real del proyecto.

## 5. Actualizar Android

Cuando Cloud Run entregue una URL como:

```text
https://ravia-backend-xxxxx-uc.a.run.app
```

cambia el `BASE_URL` de debug/release a:

```text
https://ravia-backend-xxxxx-uc.a.run.app/api/v1/
```
