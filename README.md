# Midas Core

A Spring Boot service that receives **transactions** from Kafka, validates and records them to an **H2** database, calls an external **Incentive API** to award per‑transaction incentives, and exposes a **REST** endpoint to query user balances.

This repo mirrors the Forage MIDAS exercises, but the codebase is refactored for clearer responsibilities:

- **messaging/TransactionListener** – thin Kafka consumer (receives `Transaction`)  
- **component/TransactionHandler** – orchestrates validate → incentives → persist  
- **component/DatabaseConduit** – DB-only: lookups, validation helper, atomic writes  
- **component/IncentiveClient** – HTTP client for Incentive API (config‑driven URL)  
- **web/BalanceController** – `GET /balance?userId=...` returns JSON `Balance`  
- **entity/** – JPA entities (`UserRecord`, `TransactionRecord`)  
- **repository/** – Spring Data repositories  
- **foundation/** – simple DTOs (`Transaction`, `Balance`, `Incentive`)

---

## Quick Start

### Prerequisites
- **Java 17** (e.g., Amazon Corretto 17)
- **Maven 3.9+**
- Optional: **IntelliJ IDEA** (Community is fine)

### Start the Incentive API (required for Tasks 4–5)
```bash
cd services
java -jar transaction-incentive-api.jar
```
The API listens on **http://localhost:8080/incentive** by default.

### Application configuration
Use **one** of YAML or properties (YAML shown):

`src/main/resources/application.yml`
```yaml
server:
  port: 33400

general:
  kafka-topic: dummy
  incentive-api-url: http://localhost:8080/incentive

spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: com.jpmc.midascore.foundation
        spring.json.value.default.type: com.jpmc.midascore.foundation.Transaction
        spring.json.use.type.headers: false
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

  jpa:
    hibernate:
      ddl-auto: create-drop
```

> Tests use **embedded Kafka**; no local Kafka broker is required for the exercises.

### Run tests
From the project root:
```bash
mvn -q -Dtest=TaskOneTests test
mvn -q -Dtest=TaskTwoTests test
mvn -q -Dtest=TaskThreeTests test
mvn -q -Dtest=TaskFourTests test
mvn -q -Dtest=TaskFiveTests test
```

**IntelliJ Tip:** If console truncates, increase buffer in the run configuration VM options:  
`-Didea.test.cyclic.buffer.size=10485760`  
Or enable **Logs → Save console output to file**.

---

## REST API

### GET `/balance`
Returns the current balance for a user.

**Query parameters**
- `userId` (long, required) – the user’s ID

**Response**
- JSON `Balance` object: `{ "amount": <number> }`  
- If the user does not exist, returns `{ "amount": 0 }`

**Examples**
```
GET http://localhost:33400/balance?userId=5
→ { "amount": 456.58 }
```

> Do **not** modify `Balance.toString()`; some tests verify its format.

---

## Data Flow (runtime)
1. **Kafka** publishes `Transaction` messages.  
2. `messaging/TransactionListener` receives the message.
3. `component/TransactionHandler` validates (via DB), calls **Incentive API**, then persists.
4. `component/DatabaseConduit.saveTransaction(...)` debits sender, credits recipient with **amount + incentive**, and saves a `TransactionRecord`.
5. `web/BalanceController` exposes `GET /balance` for queries.

### Validation rules
A transaction is **valid** if:
- `senderId` exists
- `recipientId` exists
- Sender’s balance ≥ transaction amount

If invalid → discarded (no DB changes).

---

## Persistence Model
- **UserRecord**: `id`, `name`, `balance`
- **TransactionRecord**: `id`, `sender(@ManyToOne)`, `recipient(@ManyToOne)`, `amount`, `incentive`

> For the exercises, balances are simple `float`s. In production, prefer `BigDecimal`.

---

## Project Layout
```
src/
  main/
    java/com/jpmc/midascore/
      MidasCoreApplication.java
      messaging/
        TransactionListener.java
      component/
        TransactionHandler.java
        DatabaseConduit.java
        IncentiveClient.java
      web/
        BalanceController.java
      entity/
        UserRecord.java
        TransactionRecord.java
      repository/
        UserRepository.java
        TransactionRecordRepository.java
      foundation/
        Transaction.java
        Balance.java
        Incentive.java
    resources/
      application.yml
  test/
    java/com/jpmc/midascore/
      TaskOneTests.java
      TaskTwoTests.java
      TaskThreeTests.java
      TaskFourTests.java
      TaskFiveTests.java
    resources/test_data/
      lkjhgfdsa.hjkl       # seed users
      alskdjfh.fhdjsk      # seed transactions
services/
  transaction-incentive-api.jar
```

---

## Troubleshooting

**Could not resolve placeholder `general.kafka-topic`**  
Add it to your active config (`application.yml` or `.properties`).

**Incentive API connection refused / timeout**  
Start the JAR:
```bash
cd services && java -jar transaction-incentive-api.jar
```
Or update `general.incentive-api-url` to your environment.

**Listener not receiving / deserialization errors**  
Ensure the consumer JSON props (trusted packages, default type) match the `Transaction` DTO package: `com.jpmc.midascore.foundation`.

**Port already in use (33400)**  
Stop the process using that port or pick another `server.port` for local runs. Tests expect `33400`.

**Console lost task output**  
Increase buffer or save to file as noted in *Run tests*.

---

## Notes & Future Work
- **@Transactional** is used on the write path to keep debits/credits + record saves atomic.
- Consider **idempotency** (transaction GUID) for real Kafka at-least-once semantics.
- Swap `float` → `BigDecimal` for monetary precision in production.
- Add retry/backoff around Incentive API calls (e.g., Spring Retry) if needed.

---

## License
Educational use for the Forage MIDAS program. Adapt as needed for internal practice projects.

