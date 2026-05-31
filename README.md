# AppCitas - Aplicación de Citas

Aplicación de citas gratuita para Android, monetizada con banners publicitarios, con verificación facial por IA, chat E2E encriptado, y sistema de comunidades.

## Características Principales

- **Verificación Facial por IA**: Sistema de verificación usando InsightFace con detección de liveness
- **Chat E2E Encriptado**: Mensajería segura con Signal Protocol
- **Chat Comunal**: Chats por región con distancias ocultas por defecto
- **Filtro de Palabras**: IA para moderación de contenido
- **Sistema de Bloqueo y Reporte**: Protección contra acoso
- **Temas Rompe-Hielo**: Conversaciones para iniciar interacciones
- **Panel de Administración**: Gestión completa de la aplicación
- **Monetización**: Banners publicitarios con AdMob

## Stack Tecnológico

### Android
- Kotlin + Jetpack Compose
- Hilt (Dependency Injection)
- Room (Base de datos local)
- Retrofit (Comunicación HTTP)
- Signal Protocol (E2E Encryption)

### Backend
- Node.js + Express
- TypeScript
- Prisma ORM
- PostgreSQL
- Redis

### AI Service
- Python + FastAPI
- InsightFace (Face Detection/Recognition)
- Liveness Detection
- Docker

### CI/CD
- GitHub Actions
- Docker
- Firebase App Distribution

## Estructura del Proyecto

```
appcitas/
├── android/          # App Android (Kotlin)
├── backend/          # API Backend (Node.js)
├── ai-service/       # Servicio de Verificación IA (Python)
├── admin-panel/      # Panel de Administración (React)
├── .github/          # GitHub Actions CI/CD
├── docs/             # Documentación
└── docker-compose.yml
```

## Requisitos Previos

- Android Studio 2024.3.2+
- Node.js 20+
- Python 3.11+
- Docker & Docker Compose
- PostgreSQL 16+
- Redis 7+

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/appcitas.git
cd appcitas
```

### 2. Configurar Backend

```bash
cd backend
cp .env.example .env
# Editar .env con tus configuraciones
npm install
npx prisma migrate dev
npm run dev
```

### 3. Configurar AI Service

```bash
cd ai-service
cp .env.example .env
python -m venv venv
source venv/bin/activate  # Linux/Mac
pip install -r requirements.txt
uvicorn app.main:app --reload
```

### 4. Configurar Android

```bash
cd android
# Abrir en Android Studio
# Sincronizar Gradle
# Ejecutar en emulador o dispositivo
```

### 5. Usar Docker (Opcional)

```bash
docker-compose up -d
```

## API Endpoints

### Auth
- `POST /api/v1/auth/register` - Registrar usuario
- `POST /api/v1/auth/login` - Iniciar sesión
- `POST /api/v1/auth/refresh` - Refrescar token
- `POST /api/v1/auth/logout` - Cerrar sesión

### Verification
- `POST /api/v1/verify` - Verificar usuario
- `GET /api/v1/verification/status` - Estado de verificación

### Chat
- `GET /api/v1/chat` - Obtener chats
- `POST /api/v1/chat/{userId}/messages` - Enviar mensaje
- `GET /api/v1/chat/community/channels` - Canales comunales

## Variables de Entorno

### Backend (.env)

```env
DATABASE_URL=postgresql://user:password@localhost:5432/appcitas
REDIS_URL=redis://localhost:6379
JWT_SECRET=tu-secreto-jwt
JWT_REFRESH_SECRET=tu-secreto-refresh
AI_SERVICE_URL=http://localhost:8000
```

### AI Service (.env)

```env
API_KEY=tu-api-key-ai
MIN_SIMILARITY_SCORE=0.6
MIN_LIVENESS_SCORE=0.7
```

## GitHub Secrets Requeridos

- `FIREBASE_APP_ID` - ID de la app en Firebase
- `FIREBASE_CREDENTIALS` - Credenciales de Firebase
- `DOCKER_USERNAME` - Usuario de Docker Hub
- `DOCKER_PASSWORD` - Contraseña de Docker Hub
- `SERVER_HOST` - IP del servidor de backend
- `SERVER_USER` - Usuario SSH del servidor
- `SERVER_SSH_KEY` - Clave SSH del servidor
- `ORACLE_VPS_HOST` - IP del VPS Oracle
- `ORACLE_VPS_USER` - Usuario SSH del VPS
- `ORACLE_VPS_SSH_KEY` - Clave SSH del VPS

## Testing

### Backend

```bash
cd backend
npm test
```

### Android

```bash
cd android
./gradlew test
```

### AI Service

```bash
cd ai-service
python -m pytest tests/ -v
```

## Despliegue

### Desarrollo

```bash
docker-compose up -d
```

### Producción

1. Push a `main` branch
2. GitHub Actions ejecutará CI/CD automáticamente
3. Docker images se construirán y subirán a Docker Hub
4. Servidores se desplegarán vía SSH

## Licencia

MIT License

## Contacto

- Email: tu-email@ejemplo.com
- GitHub: https://github.com/tu-usuario
