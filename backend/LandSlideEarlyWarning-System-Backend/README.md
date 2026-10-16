# LandSlideEarlyWarning-System-Backend

## Run on Windows

From this directory, using the in-memory H2 database (no PostgreSQL installation
required):

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:h2:mem:landslide;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
$env:SPRING_DATASOURCE_USERNAME="sa"
$env:SPRING_DATASOURCE_PASSWORD=""
$env:SPRING_DATASOURCE_DRIVER="org.h2.Driver"
$env:SPRING_JPA_DDL_AUTO="create-drop"

.\mvnw.cmd spring-boot:run
```

The application listens on `http://localhost:8080`. PostgreSQL is used by
default. Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and
`SPRING_DATASOURCE_PASSWORD` when using a database other than the defaults.

Run the tests with:

```powershell
.\mvnw.cmd test
```

Check the server and node endpoint:

```powershell
Invoke-WebRequest http://localhost:8080
Invoke-WebRequest http://localhost:8080/nodes/getAllNodes
```

H2 starts empty each time. Add a sample node before opening the monitoring
screen:

```powershell
$node = @{
  nodeName = "Node-1"
  latitude = 30.382973
  longitude = 78.467093
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri http://localhost:8080/nodes/addNode `
  -Method Post `
  -ContentType "application/json" `
  -Body $node
```

Then verify that the backend returns it:

```powershell
Invoke-RestMethod http://localhost:8080/nodes/getAllNodes
```

With the `local` profile, a sample road segment is also created. Its risk can
be checked with:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
Invoke-RestMethod http://localhost:8080/road-segments
Invoke-RestMethod http://localhost:8080/road-segments/1/risk
```

Create or update a segment by sending `roadName`, start/end coordinates, and
any available risk inputs (`rainfall`, `terrainSusceptibility`,
`recentIncidents`, `sensorRisk`, and `dataUpdatedAt`). Missing inputs are
reported as data-quality warnings and do not count as zero risk.

Tests use an in-memory H2 database and do not require PostgreSQL to be
running.
