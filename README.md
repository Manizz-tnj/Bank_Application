# Apex National Bank - Enterprise Security & Account Management System

An institutional-grade, modern **Java Full-Stack Banking Application** engineered with enterprise data integrity, double-entry ledger mechanics, stateless JWT authentication, automated fraud surveillance, and real-time digital KYC onboarding.

- **Backend**: Spring Boot 3.3.4, Java 21, Spring Security (Stateless JWT), Spring Data JPA with Auditing, Hibernate ORM, Bean Validation, Actuator Observability, and MySQL / H2.
- **Frontend**: Responsive multi-portal web dashboards developed using pure HTML5, CSS3, JavaScript (ES6+ Fetch API with JWT & Idempotency Key handling), and Bootstrap 5.3.

---

## 1. System Architecture

```text
bank_application/
├── .gitignore                                      <-- Clean repository ignore configuration
├── README.md                                       <-- Comprehensive system documentation
│
├── backend/                                        <-- Enterprise Spring Boot Core
│   ├── pom.xml                                     <-- Maven Build File (Java 21, Actuator, JJWT)
│   └── src/
│       ├── main/
│       │   ├── java/com/bank/
│       │   │   ├── BankApplication.java           <-- Main Application Entry Point
│       │   │   ├── config/                        <-- SecurityConfig, JpaAuditingConfig, DataInitializer
│       │   │   ├── controller/                    <-- REST Controllers (Auth, Customer, Account, Txn, Admin, Alert)
│       │   │   ├── dto/                           <-- Request/Response DTOs with Bean Validation
│       │   │   ├── entity/                        <-- JPA Entities (Customer, Account, LedgerEntry, IdempotencyRecord, etc.)
│       │   │   ├── enums/                         <-- UserRole, AccountType, LedgerEntryType, AlertSeverity
│       │   │   ├── exception/                     <-- GlobalExceptionHandler & Custom Exceptions
│       │   │   ├── mapper/                        <-- EntityDtoMapper
│       │   │   ├── repository/                    <-- Spring Data JPA Repositories (Locking & Reconciled queries)
│       │   │   ├── security/                      <-- JwtUtil, JwtAuthenticationFilter, CorrelationIdFilter
│       │   │   ├── service/                       <-- Service Interfaces (Customer, Account, Transaction, Fraud)
│       │   │   │   └── impl/                      <-- Transactional Business Service Implementations
│       │   │   └── util/                          <-- IdGenerator, PasswordValidator
│       │   └── resources/
│       │       ├── application.properties          <-- Default properties (H2 MySQL-mode, Actuator, MDC Pattern)
│       │       ├── application-mysql.properties    <-- Production MySQL configuration profile
│       │       └── application-test.properties     <-- Isolated in-memory test configuration
│       └── test/
│           └── java/com/bank/
│               └── BankApplicationTests.java      <-- Automated Integration Test Suite (5/5 passing)
│
└── frontend/                                       <-- Decoupled Modern Web Client
    ├── index.html                                  <-- Minimalist Authentication & KYC Onboarding Portal
    ├── customer-dashboard.html                     <-- Portfolios, Statements, CSV Export, & Profile
    ├── employee-dashboard.html                     <-- Staff Supervision & Account Management
    ├── admin-dashboard.html                        <-- Executive Dashboard & Personnel Operations
    ├── css/
    │   └── style.css                              <-- Apex Fintech Theme Stylesheet
    └── js/
        ├── api.js                                 <-- Centralized REST Client (JWT, Correlation ID, Idempotency)
        ├── auth.js                                <-- Session State, Route Guards, Toast Notifications
        ├── customer.js                            <-- Customer Portfolio, Deposit, Withdraw, & Transfer Logic
        ├── employee.js                            <-- Staff Customer/Account Supervision Logic
        └── admin.js                               <-- System Analytics & Staff Creation Logic
```

---

## 2. Core Enterprise Architecture Features

### 1. Data Integrity & Double-Entry Ledger Mechanics
- **Immutable Double-Entry Ledger (`LedgerEntry`)**:
  - Every monetary movement writes immutable credit and debit rows alongside balance updates:
    - **Deposit**: Writes a `CREDIT` row with post-transaction balance.
    - **Withdrawal**: Writes a `DEBIT` row with post-transaction balance.
    - **Transfer**: Atomically writes both a `DEBIT` row for the source account and a `CREDIT` row for the destination account within a single `@Transactional` boundary.
  - Balances can be reconciled and audited directly from the ledger using `ledgerEntryRepository.calculateReconciledBalance(accountId)`.
- **Deadlock-Free Pessimistic Locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`)**:
  - Account balance updates use database row locks (`findByAccountIdWithLock`).
  - Transfers sort account IDs lexicographically before acquiring row locks, completely preventing deadlocks when concurrent transfers happen between the same two accounts in opposite directions.
- **Optimistic Locking**:
  - Configured `@Version` on `Account` to prevent stale-state overwrites.
- **Idempotency Protection (`IdempotencyRecord`)**:
  - Fund transfers support an `Idempotency-Key` HTTP header.
  - The system checks for existing records before processing; replayed requests return the cached response without double-debiting balances.

### 2. High-Grade Security Architecture
- **Stateless JWT Authentication (`io.jsonwebtoken:jjwt:0.12.6`)**:
  - Issues signed HMAC-SHA512 JWT bearer tokens (24-hour expiration).
  - Validated on each request by `JwtAuthenticationFilter` with `SessionCreationPolicy.STATELESS`.
- **Role-Based Access Control (RBAC)**:
  - `ROLE_ADMIN`: Protected access to `/api/admin/**`.
  - `ROLE_EMPLOYEE` / `ROLE_ADMIN`: Protected access to `/api/security/**`.
  - Authenticated sessions required for customer banking operations.
- **Password & PIN Complexity Policies (`PasswordValidator`)**:
  - **Staff Password**: Minimum 8 characters, at least 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special symbol.
  - **Transaction PIN**: 4 to 6 numeric digits, strictly disallows repetitive digits (`0000`, `1111`) and sequential numbers (`1234`, `4321`).
- **Secure Administrator Account**:
  - Primary Administrator (`ADM-101`) initialized with secure password: **`Admin@Apex2026!`** (BCrypt hashed).

### 3. Auditing & Observability
- **Spring Data JPA Auditing**:
  - Automatically captures `@CreatedDate` and `@CreatedBy` using `JpaAuditingConfig`.
- **MDC Correlation ID Tracking**:
  - `CorrelationIdFilter` captures or generates `X-Correlation-Id` for every request.
  - Injected into Logback `MDC` pattern `%X{correlationId}` for distributed tracing.
  - Included in HTTP response headers.
- **Spring Boot Actuator**:
  - Exposes health checks at `GET /actuator/health` monitoring database connectivity and disk status.

### 4. Real-Time Automated Fraud Engine
- **Large Transaction Detection**: Flags single transactions $\ge \$50,000.00$ as high-severity alerts.
- **Brute-Force Lockout Defense**: Locks customer accounts and generates critical alerts after 3 consecutive failed PIN attempts.
- **Rapid Transfer Velocity Check**: Flags accounts initiating $\ge 3$ transfers within a 5-minute window.
- **Capital Drain Detection**: Flags accounts withdrawing $>90\%$ of funds on the same day the account was opened.

---

## 3. Quick Start Guide

### Step 1: Start the Backend Server
The Spring Boot backend runs on port `8080`:

```powershell
mvn spring-boot:run -f backend/pom.xml
```

> **Database Flexibility**:
> - **Default (In-Memory H2)**: Runs out-of-the-box in MySQL compatibility mode with zero configuration needed.
> - **Production MySQL**: Run with the MySQL profile:
>   ```powershell
>   mvn spring-boot:run -f backend/pom.xml -Dspring-boot.run.profiles=mysql
>   ```
>   Or provide environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

### Step 2: Open the Frontend Banking Portal
Open [`frontend/index.html`](frontend/index.html) in any modern web browser:

```powershell
Start-Process "frontend/index.html"
```

Or serve with any static web server:
```powershell
npx serve frontend
# OR
python -m http.server 3000 --directory frontend
```

---

## 4. Default Credentials & Clean-Slate Setup

The bank operates with zero dummy customer accounts:

| Role | Identifier / User | Password / PIN | Description |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `ADM-101` | `Admin@Apex2026!` | Management console, staff onboarding, analytics |
| **Customer** | Self-registered (`CUST-xxxxx`) | Custom 4-6 digit PIN | Register on the **Open Account** tab |

---

## 5. REST API Reference Summary

| Method | Endpoint | Authorization | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Complete digital KYC registration & auto-open primary account |
| `POST` | `/api/auth/login` | Public | Authenticate customer or staff; returns JWT Bearer token |
| `GET` | `/actuator/health` | Public | System and database health status |
| `GET` | `/api/customers/{id}` | Authenticated | Retrieve customer profile and KYC details |
| `PUT` | `/api/customers/{id}` | Authenticated | Update contact information (email, phone, address) |
| `PUT` | `/api/customers/{id}/pin` | Authenticated | Change customer transaction PIN |
| `PUT` | `/api/customers/{id}/lock` | Staff / Admin | Place administrative security hold on customer |
| `PUT` | `/api/customers/{id}/unlock` | Staff / Admin | Release security hold and reset failed attempt count |
| `POST` | `/api/accounts` | Authenticated | Open an additional account (Savings, Current, Fixed Deposit) |
| `GET` | `/api/accounts/{id}` | Authenticated | Retrieve account details and balance |
| `GET` | `/api/accounts/customer/{id}` | Authenticated | List all accounts belonging to a customer |
| `POST` | `/api/accounts/{id}/deposit` | Authenticated | Deposit funds (records double-entry `CREDIT` ledger row) |
| `POST` | `/api/accounts/{id}/withdraw` | Authenticated | Withdraw funds (records double-entry `DEBIT` ledger row) |
| `POST` | `/api/transactions/transfer` | Authenticated | Atomic transfer with `Idempotency-Key` and double-entry ledger |
| `GET` | `/api/transactions/account/{id}` | Authenticated | Transaction history for account |
| `GET` | `/api/security/alerts` | Staff / Admin | List fraud and security incidents |
| `PUT` | `/api/security/alerts/{id}/resolve`| Staff / Admin | Mark security incident as resolved with notes |
| `GET` | `/api/admin/stats` | Admin | Aggregate liquidity and bank metrics |
| `POST` | `/api/admin/employees` | Admin | Onboard new bank employee or administrator |

---

## 6. Running Automated Tests

Run the full integration test suite:

```powershell
mvn test -f backend/pom.xml
```

**Test Coverage Highlights**:
- Context loading and dependency injection.
- Customer onboarding, auto-provisioning, BCrypt hashing, and JWT token issuance.
- Pessimistic locking, atomic double-entry ledger creation, and ledger balance reconciliation.
- Idempotency replay verification (ensuring identical requests do not duplicate deductions).
- Password and PIN security policy validation.
- High-value fraud alert triggering.
