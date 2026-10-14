# SIH 2K26

This repository contains the landslide early-warning frontend and Spring Boot
backend.

## Run from the repository root

Install the frontend dependencies once:

```powershell
npm --prefix forntend/landslide_detection install
```

Start the frontend:

```powershell
npm run dev
```

Run the backend:

```powershell
npm run backend:run
```

The frontend is available at `http://localhost:5173` and the backend at
`http://localhost:8080`.

## Validate from the repository root

```powershell
npm run build
npm run backend:test
```

The backend test suite uses H2 so it does not require a local PostgreSQL
server. The running application uses PostgreSQL by default and can be
configured with `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and
`SPRING_DATASOURCE_PASSWORD`.
