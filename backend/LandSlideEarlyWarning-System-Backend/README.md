# LandSlideEarlyWarning-System-Backend

## Run on Windows

From this directory:

```powershell
.\mvnw.cmd spring-boot:run
```

The application listens on `http://localhost:8080`. PostgreSQL is used by
default. Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and
`SPRING_DATASOURCE_PASSWORD` when using a database other than the defaults.

Run the tests with:

```powershell
.\mvnw.cmd test
```

Tests use an in-memory H2 database and do not require PostgreSQL to be
running.
