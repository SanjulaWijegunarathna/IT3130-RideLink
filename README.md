# RideLink — Backend Microservices (IT3130 Group Assignment)

Four independently runnable Spring Boot services for a fictional ride-sharing platform. Official demo interfaces are **Swagger UI** and the shared **Postman collection**. An optional demo web UI lives in `ridelink-ui` (not required for marks).

| Service | Port | Owner slot | Responsibility |
|---|---|---|---|
| `account-service` | 8081 | Member 1 | Register, login, JWT, roles, profiles, account status |
| `driver-service` | 8082 | Member 2 | Driver/vehicle profile, availability, location, assign/release |
| `ride-service` | 8083 | Member 3 | Ride request, assignment, lifecycle, cancellation |
| `fare-payment-service` | 8084 | Member 4 | Fare estimate/final, simulated payment, async ride-completed queue |

## Prerequisites

- JDK **26** (same as lab stack)
- Maven Wrapper included (`mvnw` / `mvnw.cmd`) — no global Maven required
- Node.js 18+ only if you want to run the optional `ridelink-ui`

## Optional demo UI

After all four backends are running:

```powershell
cd ridelink-ui
npm install
npm run dev
```

Open the URL Vite prints (usually http://localhost:5173).

1. Register / login as **DRIVER** → register vehicle → set available  
2. In another browser profile/window, register / login as **PASSENGER** → estimate fare → request ride  
3. Back on driver session → Accept → Start → Complete → passenger can check payment  

Restart backends after pulling CORS changes so the browser UI can call the APIs.

## Configuration

Shared JWT secret (override in real deployments):

```text
RIDELINK_JWT_SECRET=RideLinkDemoSecretKeyForJwtSigningMustBeLongEnough123
```

Account, driver, and ride services each own an in-memory H2 database. The fare-payment service uses MongoDB. **Do not** share databases or collections across services.

| Service | JDBC URL | H2 console |
|---|---|---|
| Account | `jdbc:h2:mem:accountdb` | http://localhost:8081/h2-console |
| Driver | `jdbc:h2:mem:driverdb` | http://localhost:8082/h2-console |
| Ride | `jdbc:h2:mem:ridedb` | http://localhost:8083/h2-console |
| Fare and payments | `mongodb://localhost:27017/ridelink_fares` | MongoDB Compass at `localhost:27017` |

H2 user for the other services: `sa` / blank password. When the fare-payment service starts, it creates `ridelink_fares` and its `fares` / `payments` collections if needed. Override the Mongo connection with `MONGODB_URI` if needed.

## Start-up order

Open **four terminals** and start in this order:

```powershell
cd account-service; .\mvnw.cmd spring-boot:run
cd driver-service; .\mvnw.cmd spring-boot:run
cd fare-payment-service; .\mvnw.cmd spring-boot:run
cd ride-service; .\mvnw.cmd spring-boot:run
```

Swagger:

- http://localhost:8081/swagger-ui.html
- http://localhost:8082/swagger-ui.html
- http://localhost:8083/swagger-ui.html
- http://localhost:8084/swagger-ui.html

## Demo workflow (happy path)

1. **Register passenger** — `POST http://localhost:8081/api/auth/register`
   ```json
   { "email": "ayesha@ridelink.lk", "password": "pass123", "fullName": "Ayesha Perera", "phone": "0771111111", "role": "PASSENGER" }
   ```
2. **Register driver account** — same endpoint with `"role": "DRIVER"` and email `nimal@ridelink.lk`.
3. **Login** both users — `POST /api/auth/login` — save `accessToken` values.
4. **Register driver profile** (Bearer = driver token) — `POST http://localhost:8082/api/drivers`
   ```json
   {
     "fullName": "Nimal Silva",
     "phone": "0772222222",
     "vehicleNumber": "CAB-1024",
     "vehicleType": "CAR",
     "serviceArea": "Malabe",
     "latitude": 6.9147,
     "longitude": 79.9729,
     "available": true
   }
   ```
5. **Fare estimate** (any token) — `POST http://localhost:8084/api/fares/estimate`
   ```json
   { "pickup": "SLIIT Malabe", "destination": "Colombo Fort" }
   ```
6. **Request ride** (passenger token) — `POST http://localhost:8083/api/rides`
   ```json
   { "pickup": "SLIIT Malabe", "destination": "Colombo Fort" }
   ```
7. **Lifecycle** (driver token):  
   `PATCH /api/rides/{id}/accept` → `/start` → `/complete`
8. **Payment / receipt** — after complete, wait ~2s then  
   `GET http://localhost:8084/api/payments/ride/{rideId}`

### Negative scenarios

- Set driver `available: false`, request ride → **409** `NO_DRIVER_AVAILABLE`
- Invalid status jump (e.g. complete before accept) → **409** `INVALID_RIDE_TRANSITION`
- Call driver-only endpoint with passenger token → **403**
- `POST /api/payments` with `"simulateFailure": true` → failed payment

## Interservice communication

| Interaction | Style | Why |
|---|---|---|
| Ride → Driver `assign-first` / `release` | **Synchronous REST** | Assignment must succeed or fail before the ride is persisted |
| Ride → Fare `estimate` / `final` | **Synchronous REST** | Caller needs the fare amount in the HTTP response |
| Ride → Fare `POST /api/events/ride-completed` | **Async (202 + in-memory queue worker)** | Payment recording can happen after the ride is marked completed |

Stable identifiers (`accountId`, `driverId`, `rideId`) are shared in JSON only. No cross-service DB access.

### Fare rule

```text
baseFare = 150 LKR
perKm    = 45 LKR
distance = Haversine(km) if coordinates provided, else string-length heuristic (min 2 km, cap 50)
fare     = baseFare + distance * perKm
```

Fare estimate and final-fare requests accept `pickup` and `destination`; optional latitude/longitude coordinates use the Haversine distance instead of the text-length estimate. Final fare also requires a positive `rideId`.

```http
POST http://localhost:8084/api/fares/estimate
Content-Type: application/json

{"pickup":"SLIIT Malabe","destination":"Colombo Fort"}
```

```http
POST http://localhost:8084/api/fares/final
Content-Type: application/json

{"rideId":12,"pickup":"SLIIT Malabe","destination":"Colombo Fort"}
```

Create a simulated payment with `POST /api/payments` using `rideId`, `amount`, and optional `passengerAccountId`, `paymentMethod` (`CASH`, `CARD`, or `WALLET`), and `simulateFailure`. Read its receipt from `GET /api/payments/ride/{rideId}`. Ride completion events are accepted asynchronously at `POST /api/events/ride-completed` with the payment fields.

## Tests

```powershell
cd account-service; .\mvnw.cmd -B test
cd ..\driver-service; .\mvnw.cmd -B test
cd ..\ride-service; .\mvnw.cmd -B test
cd ..\fare-payment-service; .\mvnw.cmd -B test
```

CI: `.github/workflows/ci.yml` builds and tests all four services on push/PR.

## Postman

Import:

- `postman/RideLink.postman_collection.json`
- `postman/RideLink.postman_environment.json`
- [Fare/payment Postman guide](fare-payment-service/POSTMAN_GUIDE.md) — request bodies and read/write endpoints for MongoDB.

Set tokens from login responses into environment variables `passengerToken` and `driverToken`.

## Git / contribution notes

- Use feature branches + pull requests (do not push straight to `main`)
- Record primary owners in this README when the group is finalised
- Tag the assessed version, e.g. `git tag v1.0.0-submission`

## Sample credentials (after register)

| Role | Email | Password |
|---|---|---|
| Passenger | ayesha@ridelink.lk | pass123 |
| Driver | nimal@ridelink.lk | pass123 |

These are fictional demo accounts created at runtime — nothing is pre-seeded.
