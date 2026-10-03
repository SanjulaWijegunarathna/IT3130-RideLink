# RideLink — Account Service

Independent Spring Boot microservice responsible for authentication, user registration, role-based authorization, profile management, and account lifecycle for the RideLink platform.

---

## 1. Service Purpose & Scope

The **Account Service** is an individual microservice module of the RideLink platform (IT3130). It owns all user accounts and credentials, issuing cryptographic JSON Web Tokens (JWT) for stateless authentication across the platform while enforcing role-based access control.

### Core Capabilities
- **Passenger Registration**: Self-registration for passengers with email validation and BCrypt password encryption.
- **Driver Registration**: Self-registration for drivers to establish login credentials.
- **Authentication & JWT Issuance**: Secure login verifying BCrypt credentials and issuing signed HMAC-SHA256 JWT tokens containing `userId`, `email`, and `role`.
- **Role-Based Authorization**: Distinct permissions for `PASSENGER`, `DRIVER`, and `ADMIN`. Admin self-registration is strictly disallowed.
- **Profile Viewing**: Current authenticated user retrieval (`/api/accounts/me`) and ID-based account lookup (`/api/accounts/{id}`) with ownership checks.
- **Profile Updating**: Updating user information (`fullName`, `phone`) with self-authorization.
- **Account Status Lifecycle**: Managing account statuses (`ACTIVE`, `SUSPENDED`, `DEACTIVATED`). Inactive accounts are blocked from login and API access. Status modifications are restricted to `ADMIN`.
- **Stateless Security**: Spring Security filter chain with Bearer token validation and Method Security (`@PreAuthorize`).
- **Input Validation & Exception Handling**: Centralized exception handling with RFC-compliant JSON error responses.
- **OpenAPI / Swagger UI**: Built-in interactive documentation and testing interface.

---

## 2. Technologies Used

- **Java 21** (LTS)
- **Spring Boot 4.1.1**
- **Spring Security** (Stateless Bearer JWT Authentication)
- **Spring Data JPA / Hibernate ORM**
- **H2 In-Memory Database** (Isolated data persistence)
- **JJWT (io.jsonwebtoken) 0.12.6** (HMAC-SHA256 token signing and verification)
- **Jakarta Validation API** (Bean validation constraints)
- **SpringDoc OpenAPI 3.1.0** (Swagger UI & OpenAPI 3.0 specification)
- **Maven Wrapper** (`mvnw` / `mvnw.cmd`)
- **JUnit 5 & Mockito** (Unit and integration testing)

---

## 3. Project Structure

```
.
├── .github/
│   └── workflows/
│       └── ci.yml               # GitHub Actions CI build & test workflow
├── .mvn/
│   └── wrapper/                 # Maven wrapper binaries
├── postman/
│   └── AccountService.postman_collection.json # Ready-to-import Postman collection
├── src/
│   ├── main/
│   │   ├── java/com/ridelink/account/
│   │   │   ├── AccountServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── AccountController.java     # Profile & account management endpoints
│   │   │   │   └── AuthController.java        # Registration & login endpoints
│   │   │   ├── dto/
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── UpdateProfileRequest.java
│   │   │   │   └── UserResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── ApiException.java
│   │   │   │   └── GlobalExceptionHandler.java # Uniform REST error handling
│   │   │   ├── model/
│   │   │   │   ├── AccountStatus.java         # ACTIVE, SUSPENDED, DEACTIVATED
│   │   │   │   ├── Role.java                  # PASSENGER, DRIVER, ADMIN
│   │   │   │   └── UserAccount.java           # JPA entity mapped to 'user_accounts'
│   │   │   ├── repository/
│   │   │   │   └── UserAccountRepository.java # JPA repository
│   │   │   ├── security/
│   │   │   │   ├── CorsConfig.java            # Cross-origin policy configuration
│   │   │   │   ├── JwtAuthFilter.java         # Request filter for Bearer tokens
│   │   │   │   ├── JwtService.java            # JWT generation and parsing
│   │   │   │   ├── OpenApiConfig.java         # Swagger security scheme config
│   │   │   │   ├── SecurityConfig.java        # Filter chain and public/private routes
│   │   │   │   └── UserPrincipal.java         # Spring UserDetails implementation
│   │   │   └── service/
│   │   │       └── AccountService.java        # Business logic & validations
│   │   └── resources/
│   │       └── application.properties         # Service port, H2 DB, JWT config
│   └── test/
│       └── java/com/ridelink/account/
│           ├── AccountServiceApplicationTests.java # Context loading test
│           └── service/
│               └── AccountServiceTest.java     # Business logic & security unit tests
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## 4. Prerequisites

- **Java Development Kit (JDK) 21** or later installed.
- **Git** for version control.
- No global Maven installation is required (Maven Wrapper `mvnw` / `mvnw.cmd` is included).

---

## 5. Configuration

All configuration is located in `src/main/resources/application.properties`:

| Property | Default Value | Description |
|---|---|---|
| `server.port` | `8081` | HTTP port on which the service listens |
| `spring.application.name` | `account-service` | Service identifier |
| `spring.datasource.url` | `jdbc:h2:mem:accountdb;DB_CLOSE_DELAY=-1` | In-memory H2 database URL |
| `spring.datasource.driver-class-name` | `org.h2.Driver` | H2 JDBC driver |
| `spring.datasource.username` | `sa` | Database username |
| `spring.datasource.password` | *(empty)* | Database password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Automatically creates/updates schema |
| `spring.h2.console.enabled` | `true` | Enables browser-based H2 database console |
| `spring.h2.console.path` | `/h2-console` | H2 console path |
| `ridelink.jwt.secret` | `${RIDELINK_JWT_SECRET:...}` | JWT HMAC-SHA signing secret key |
| `ridelink.jwt.expiration-ms` | `86400000` | Token validity duration (24 hours) |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Swagger UI web interface path |
| `springdoc.api-docs.path` | `/api-docs` | OpenAPI JSON schema specification path |

> [!TIP]
> To override the default demo secret key in production, set the environment variable:
> `export RIDELINK_JWT_SECRET="YourStrongSecretKeyWithAtLeast256BitsLength"` (Linux/macOS)
> `$env:RIDELINK_JWT_SECRET="YourStrongSecretKeyWithAtLeast256BitsLength"` (PowerShell)

---

## 6. How to Run the Service

### Windows (PowerShell / Command Prompt)
```powershell
.\mvnw.cmd spring-boot:run
```

### Linux / macOS
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Once started, the service will be available on:
- **Base URL**: `http://localhost:8081`
- **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **OpenAPI Schema**: [http://localhost:8081/api-docs](http://localhost:8081/api-docs)
- **H2 Database Console**: [http://localhost:8081/h2-console](http://localhost:8081/h2-console)
  - JDBC URL: `jdbc:h2:mem:accountdb`
  - User Name: `sa`
  - Password: *(leave blank)*

---

## 7. How to Run Tests

Execute all unit and integration tests using the Maven wrapper:

### Windows
```powershell
.\mvnw.cmd test
```

### Linux / macOS
```bash
./mvnw test
```

---

## 8. Authentication & Security Architecture

1. **Stateless JWT Tokens**:
   - Upon successful login or registration, the client receives a signed Bearer token.
   - The token contains claims: `sub` (User ID), `email`, and `role`.
   - Protected endpoints require the header:
     ```http
     Authorization: Bearer <accessToken>
     ```
2. **Access Control**:
   - Public paths: `/api/auth/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/api-docs/**`, `/v3/api-docs/**`, `/h2-console/**`, `/actuator/health`.
   - Protected paths: All `/api/accounts/**` endpoints require authentication.
   - Self or Admin lookup: Users can retrieve their own profile (`/api/accounts/me` or `/api/accounts/{id}` matching their token ID). Other accounts are accessible only by `ADMIN`.
   - Status transitions: Only `ADMIN` can update account status (`ACTIVE`, `SUSPENDED`, `DEACTIVATED`).
3. **Password Security**:
   - Raw passwords are never persisted. Passwords are encrypted using BCrypt (`BCryptPasswordEncoder`).

---

## 9. API Endpoints

### Authentication (`/api/auth`)

#### 1. Register Account
- **Method**: `POST`
- **Endpoint**: `/api/auth/register`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "email": "ayesha@ridelink.lk",
    "password": "pass123",
    "fullName": "Ayesha Perera",
    "phone": "0771111111",
    "role": "PASSENGER"
  }
  ```
  *(Supported roles: `PASSENGER`, `DRIVER`. `ADMIN` self-registration is rejected with 400 Bad Request).*
- **Response**: `201 Created`
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "email": "ayesha@ridelink.lk",
      "fullName": "Ayesha Perera",
      "phone": "0771111111",
      "role": "PASSENGER",
      "status": "ACTIVE"
    }
  }
  ```

#### 2. Login
- **Method**: `POST`
- **Endpoint**: `/api/auth/login`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "email": "ayesha@ridelink.lk",
    "password": "pass123"
  }
  ```
- **Response**: `200 OK` (Returns `accessToken` and user details)

---

### Account Management (`/api/accounts`)

#### 3. Get Current User Profile
- **Method**: `GET`
- **Endpoint**: `/api/accounts/me`
- **Access**: Authenticated (Bearer Token required)
- **Response**: `200 OK`
  ```json
  {
    "id": 1,
    "email": "ayesha@ridelink.lk",
    "fullName": "Ayesha Perera",
    "phone": "0771111111",
    "role": "PASSENGER",
    "status": "ACTIVE"
  }
  ```

#### 4. Get Account By ID
- **Method**: `GET`
- **Endpoint**: `/api/accounts/{id}`
- **Access**: Authenticated (`#id == principal.id` OR `hasRole('ADMIN')`)
- **Response**: `200 OK` (Returns user details)

#### 5. Update Profile
- **Method**: `PATCH`
- **Endpoint**: `/api/accounts/{id}`
- **Access**: Authenticated (`#id == principal.id` OR `hasRole('ADMIN')`)
- **Request Body**:
  ```json
  {
    "fullName": "Ayesha P. Silva",
    "phone": "0779999999"
  }
  ```
  *(Status changes e.g. `"status": "SUSPENDED"` require `ADMIN` role).*
- **Response**: `200 OK` (Returns updated user response)

---

## 10. Sample Demo Credentials

Because the database runs in-memory (H2), accounts are registered at runtime during testing. Safe demo credentials for registration/login testing:

| User Type | Email | Password | Role |
|---|---|---|---|
| Passenger | `ayesha@ridelink.lk` | `pass123` | `PASSENGER` |
| Driver | `nimal@ridelink.lk` | `pass123` | `DRIVER` |

---

## 11. Testing with Postman

An individual Postman collection is provided in `postman/AccountService.postman_collection.json`.

1. Import `postman/AccountService.postman_collection.json` into Postman.
2. In the Postman environment or collection variables, set `accountUrl` to `http://localhost:8081`.
3. Run the requests in order:
   - **Register Passenger** (automatically extracts `passengerToken` and `passengerId`)
   - **Login Passenger**
   - **Get Current Profile (Me)**
   - **Update Profile**
