# Developer Setup

## Prerequisites

- Java 21.
- Node.js 22.
- npm 11 or compatible npm for Node 22.
- Docker and Docker Compose.

## Install Dependencies

Backend dependencies are resolved by the Maven wrapper:

```bash
cd backend
./mvnw test
```

Frontend dependencies are installed with npm:

```bash
cd frontend
npm ci
```

Root documentation tooling is installed separately:

```bash
npm ci
```

## Run Locally Without Docker

Start PostgreSQL first, then run the backend:

```bash
cd backend
./mvnw spring-boot:run
```

Run the frontend in another terminal:

```bash
cd frontend
npm run dev
```

Open the UI at `http://localhost:5173`.

Backend health is available at `http://localhost:8080/actuator/health`.

## First-Run Bootstrap

Check whether the deployment needs an initial administrator:

```bash
curl http://localhost:8080/api/v1/bootstrap/status
```

Create the initial local administrator once:

```bash
curl -X POST http://localhost:8080/api/v1/bootstrap/admin \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.test","displayName":"Admin","password":"change-this-password","tenantSlug":"default","tenantName":"Default Tenant","organizationSlug":"platform","organizationName":"Platform Operations"}'
```

After bootstrap, protected local APIs can be called with HTTP Basic or scoped service account bearer tokens.

## Validation Commands

```bash
cd backend
./mvnw test
```

```bash
cd frontend
npm run lint
npm test
npm run build
```

```bash
npm run docs:lint
```
