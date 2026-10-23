# PahadSuraksha Product Requirements Document

**Version:** 1.0  
**Status:** Prototype / development  
**Product:** Landslide Risk Monitoring and Early-Warning Support Platform  
**Last updated:** 23 October 2026

## 1. Product summary

PahadSuraksha is a web platform for monitoring landslide-related conditions, collecting citizen incident reports, reviewing those reports, displaying risk information, and coordinating notifications for field and response teams.

The product supports the flow from observation to review and response. It is a prototype and must not be treated as an official emergency-warning system until its data sources, models, authentication, and operational procedures have been validated.

## 2. Problem statement

Communities and response teams need a single place to:

- understand current landslide risk and monitoring conditions;
- view sensor and road information on a map;
- report possible landslides, rockfalls, road blockages, cracks, and slope movement;
- verify citizen reports before treating them as confirmed incidents;
- notify responsible personnel through SMS or voice calls; and
- maintain a clear status and audit trail for every report.

## 3. Goals and success criteria

### Goals

1. Give users a clear overview of monitored areas and current risk.
2. Allow a citizen to submit a location-based incident report with photo evidence.
3. Use AI as an initial image-screening step, not as the final authority.
4. Require administrative review before an incident is treated as approved.
5. Provide practical notification channels for urgent response.
6. Preserve report, sensor, review, and notification history.

### Success criteria

- A user can open the public landing experience and understand the product purpose.
- A signed-in user can access dashboard, monitoring, map, alerts, profile, and settings pages.
- A report with valid evidence is either accepted into review or rejected with a clear reason.
- Every accepted report has a visible `PENDING`, `APPROVED`, or `REJECTED` status.
- An admin can approve or reject a pending report.
- Approval can trigger an SMS notification when Twilio is configured.
- Node and road data can be retrieved and displayed by the frontend.
- Failures, missing data, stale data, and unavailable integrations are visible to the user.

## 4. Personas and roles

| Role | Responsibilities | Main permissions |
|---|---|---|
| Citizen / resident | Report a possible hazard and view safety information | Submit reports; view public information |
| Field officer | Monitor sensor and road conditions | View dashboards, nodes, maps, and reports |
| Admin / monitoring operator | Verify reports and coordinate response | Review, approve, reject, and notify |
| Response authority | Act on verified incidents | Receive alerts and follow official procedures |

The current frontend uses mock authentication. During development, an email beginning with `admin` is treated as an admin account. Production must replace this with server-side authentication, role-based authorization, and an audit log.

## 5. Product scope

### In scope

- Public landing page and safety information.
- Authentication screens and protected dashboard pages.
- Monitoring dashboard with sensor telemetry and status cards.
- Map view for nodes, roads, risk, and incident locations.
- Citizen incident reporting with camera/photo upload and geolocation.
- AI image screening for a possible landslide.
- Admin incident review and approval workflow.
- Node creation, update, deletion, lookup, and history APIs.
- Road-segment risk scoring and route comparison.
- Twilio voice-call integration.
- Twilio SMS notification integration.
- Settings and notification preferences UI.

### Out of scope for the prototype

- Official government dispatch or emergency-service integration.
- Guaranteed real-time sensor ingestion.
- Fully autonomous evacuation or closure decisions.
- Medical, rescue, traffic-control, or public-safety decisions without human authority.
- Production-grade identity, access control, billing, and compliance.

## 6. Core workflows

### 6.1 Citizen incident-report workflow

1. User opens **Report a landslide**.
2. User captures or selects a photo.
3. Browser attempts to detect the user location.
4. User supplies name, mobile number, hazard type, description, and coordinates.
5. Frontend submits multipart form data to `/landslide-report/makeReport`.
6. Backend forwards the image to the configured AI model.
7. If the AI does not detect a landslide, the report is rejected with a clear message.
8. If confidence is below 60%, the report is rejected as insufficiently confident.
9. If accepted, the image is uploaded to Cloudinary and the report is saved as `PENDING`.
10. The user receives a report ID.

AI screening is an intake filter. It is not human verification and must not be presented as proof that an incident occurred.

### 6.2 Admin review workflow

1. Admin opens the Alerts page.
2. Admin reviews photo, reporter details, location, description, and AI-screened report.
3. Admin selects one action:
   - **Approve:** report becomes `APPROVED` and an SMS notification is attempted.
   - **Reject:** report becomes `REJECTED`; the reason should be recorded.
4. The system stores reviewer identity, review time, and optional review note.
5. Approved incidents may be displayed as verified alerts and used for response coordination.

### 6.3 Node monitoring workflow

1. An operator adds a node with a name and coordinates.
2. The node reports latest readings such as soil moisture, tilt, rainfall, sound, vibration, temperature, and humidity.
3. The backend exposes current node data and node history.
4. The frontend displays node condition, map position, telemetry, and risk context.
5. Operators can update or delete nodes.

The default local development setup uses an in-memory H2 database. Nodes and reports are lost when the backend restarts unless PostgreSQL is configured.

### 6.4 Notification workflow

- **SMS:** approval automatically attempts to send an SMS to `ADMIN_PHONE_NUMBER`; an admin can also manually use the Send SMS action.
- **Voice call:** `POST /call/makeCall/{nodeId}` initiates a Twilio voice warning for a node.
- Notifications must record success or failure and must not silently imply delivery when credentials are missing.

## 7. Functional requirements

### FR-1: Authentication

- The system shall provide login, registration, session restoration, profile, and logout experiences.
- Protected application pages shall require a signed-in user.
- Production authorization shall be enforced by the backend, not only by frontend role checks.

### FR-2: Incident reporting

- The form shall collect reporter identity, mobile number, coordinates, description, hazard type, and image evidence.
- The form shall validate required fields and show actionable errors.
- The backend shall reject missing images.
- The backend shall run AI screening before persisting a report.
- The backend shall return a report ID after successful persistence.

### FR-3: Incident review

- New reports shall default to `PENDING`.
- Only authorized admins shall approve or reject reports.
- Review actions shall store status, reviewer, timestamp, and note.
- The UI shall display the current review status.
- Approved and rejected reports shall remain queryable for audit purposes.

### FR-4: Monitoring

- Users shall be able to list all nodes and retrieve a node by name.
- Operators shall be able to add, update, and delete nodes.
- The system shall provide node history.
- The map shall support node and incident locations.

### FR-5: Road risk

- The system shall create, list, and update road segments.
- The system shall calculate segment risk from configured rainfall, terrain, incident, sensor, freshness, distance, and duration inputs.
- Missing or stale inputs shall be represented as data-quality warnings rather than silently treated as zero risk.
- The system shall assess and compare route candidates.

### FR-6: Notifications

- The system shall support Twilio SMS when valid credentials and verified numbers are configured.
- The system shall support Twilio voice calls for node alerts.
- Notification failures shall be returned or logged with a safe, understandable message.
- Secrets shall be supplied through environment variables and never committed to source control.

## 8. Backend API contract

### Incident reports

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/landslide-report/makeReport` | Submit multipart incident report |
| `GET` | `/landslide-report/getAllReports` | List reports and review status |
| `PUT` | `/landslide-report/{id}/approve` | Approve a report |
| `PUT` | `/landslide-report/{id}/reject` | Reject a report |
| `POST` | `/landslide-report/{id}/send-sms` | Manually notify admin by SMS |

### Nodes and history

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/nodes/addNode` | Add a node |
| `GET` | `/nodes/getAllNodes` | List nodes |
| `GET` | `/nodes/getbyName/{nodeName}` | Read a node by name |
| `PUT` | `/nodes/updateNodeLatest` | Update latest node readings |
| `DELETE` | `/nodes/deleteNode/{id}` | Delete a node |
| `POST` | `/nodeHistory/post` | Save node history reading |
| `GET` | `/nodeHistory/getHistory/{nodeId}` | Read node history |

### Notifications and road risk

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/call/makeCall/{nodeId}` | Start a Twilio voice alert |
| `POST` | `/road-segments` | Create a road segment |
| `GET` | `/road-segments` | List road segments |
| `PUT` | `/road-segments/{id}` | Update a road segment |
| `GET` | `/road-segments/{id}/risk` | Calculate segment risk |
| `POST` | `/road-segments/risk/assess` | Assess a route |
| `POST` | `/road-segments/risk/compare` | Compare route candidates |

## 9. Data requirements

### Incident report

- ID
- Reporter name and mobile number
- Description
- Latitude and longitude
- Image URL
- Approval status
- Reviewer identity
- Review timestamp
- Review note

### Node

- ID and node name
- Latitude and longitude
- Active state
- Soil moisture
- Tilt angle
- Rainfall / rain drops
- Sound
- Vibrations
- Temperature
- Humidity
- Latest reading timestamp

### Data quality

- Every operational reading should include a timestamp.
- The UI should distinguish live, stale, simulated, unavailable, and manually entered data.
- Missing data must not be presented as a safe condition.

## 10. Non-functional requirements

- **Reliability:** API errors must return useful status codes and messages.
- **Security:** credentials, phone numbers, images, and identity data must be protected.
- **Authorization:** admin review and notification actions must be server-side protected in production.
- **Privacy:** retain only necessary personal data and define retention rules.
- **Accessibility:** keyboard navigation, labels, readable contrast, and screen-reader-friendly status messages.
- **Performance:** dashboard and map should load progressively and avoid blocking on optional integrations.
- **Observability:** log AI, database, Cloudinary, Twilio, and route-scoring failures with correlation IDs where possible.
- **Localization:** user-facing alerts should support English and relevant regional languages.
- **Safety:** every warning must state its source, freshness, uncertainty, and operational limitations.

## 11. Configuration and integrations

### Backend

- Java 21 and Spring Boot.
- PostgreSQL for persistent deployment.
- H2 for local development and tests.
- Cloudinary for report image storage.
- External AI model for image screening.
- Twilio for SMS and voice calls.

### Required notification variables

```text
TWILIO_ACCOUNT_SID
TWILIO_AUTH_TOKEN
TWILIO_PHONE_NUMBER
ADMIN_PHONE_NUMBER
```

### Recommended production variables

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_JPA_DDL_AUTO=validate
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
```

## 12. Acceptance criteria

- [ ] Frontend starts successfully and can reach the backend through the Vite proxy.
- [ ] Backend starts successfully on port 8080.
- [ ] Node list returns persisted nodes from PostgreSQL in production.
- [ ] Citizen report validation catches missing or invalid fields.
- [ ] AI rejection explains whether detection or confidence caused rejection.
- [ ] Accepted reports are saved as `PENDING`.
- [ ] Admin can approve and reject reports.
- [ ] Approval metadata is stored and displayed.
- [ ] Approval attempts SMS delivery when Twilio is configured.
- [ ] SMS configuration failures are visible and do not falsely claim delivery.
- [ ] Voice-call endpoint handles configured and unconfigured Twilio credentials safely.
- [ ] Tests cover report creation, status transitions, authorization, notification failure, and node APIs.
- [ ] No production secrets are committed to the repository.

## 13. Risks and mitigations

| Risk | Mitigation |
|---|---|
| AI false negatives or false positives | Keep admin review mandatory; show model confidence and source |
| In-memory database loses data | Use PostgreSQL for any shared or deployed environment |
| Unauthorized approval or SMS | Add real backend JWT validation and admin authorization |
| Notification delivery failure | Store notification attempts and show delivery status |
| Stale or simulated sensor data | Show freshness and data source on every monitoring view |
| Exposure of citizen phone numbers | Restrict report access and apply retention/redaction policies |
| External AI, Cloudinary, or Twilio outage | Degrade gracefully and provide retry/manual review paths |

## 14. Future roadmap

1. Replace mock authentication with a real identity service and backend JWT authorization.
2. Add a dedicated admin review queue with filters for pending, approved, and rejected reports.
3. Add review notes, rejection reasons, and a complete audit history.
4. Add separate public-approved-incidents and internal-all-reports endpoints.
5. Add notification subscriptions for multiple users and regions.
6. Add delivery receipts and retry policies for SMS and voice calls.
7. Connect trusted live weather, GIS, and sensor providers.
8. Add model versioning, explainability, evaluation metrics, and human feedback.
9. Add automated tests and deployment monitoring.
10. Conduct field validation with disaster-management experts before operational use.

## 15. Responsible-use disclaimer

PahadSuraksha is a prototype. Its AI output, sensor values, map layers, and alerts may be simulated, incomplete, stale, or incorrect. Users must follow official government advisories and emergency instructions. The product must not be used as the sole basis for evacuation, rescue, medical, traffic, or other safety-critical decisions.
