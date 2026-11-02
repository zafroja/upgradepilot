# UpgradePilot

Scans a Java / Spring Boot project and reports what blocks an upgrade to Java 17 and
Spring Boot 3, with AI explanations grounded in migration notes (RAG).

## Architecture

```
React (Vite, :5173)
   │  /api/scans
   ▼
gateway (Spring Cloud Gateway + Spring Security, :8080)
   │
   ▼
scan-service (:8081) ── Spring Boot, JPA/Hibernate, PostgreSQL
   │   unzip → parse poms → run rules → save Scan + Issues
   │   POST /api/rag/explain (internal only)
   ▼
rag-service (:8082) ── LangChain4j, Claude, pgvector
       docs-corpus/*.md → chunk → embed (local MiniLM) → pgvector
```

rag-service is not routed through the gateway, so the public can't run up your LLM bill
or call `/ingest`.

## Run locally

Prerequisites: Java 17, Maven 3.9, Node 20, Docker, an Anthropic API key.

```bash
cp .env.example .env              # add your ANTHROPIC_API_KEY
docker compose up --build         # postgres + 3 services

# load the knowledge base into pgvector (run again whenever you add docs)
curl -X POST http://localhost:8082/api/rag/ingest

cd frontend && npm install && npm run dev   # http://localhost:5173
```

Run a service from your IDE instead: start only Postgres (`docker compose up postgres`),
then run the `*Application` class. Defaults in each `application.yml` point at localhost.

Tests: `mvn test`

## Try it on PiggyMetrics

Download the PiggyMetrics repo as a zip from GitHub and upload it. Expect blockers for the
Java version, Spring Boot 2.x, and Netflix OSS (Hystrix, Zuul, Ribbon), plus javax warnings.
Screenshot that report: it's your case study and landing-page demo.

## Adding a rule

Create a class in `scan-service/.../analysis/rules`, implement `UpgradeRule`, annotate it
with `@Component`. Give the finding a `ragQuestion` if it should get an AI explanation.

Rule ideas: Hibernate 6 HQL changes, `spring.redis.*` properties, Sleuth → Micrometer
Tracing, trailing-slash matching, `@EnableGlobalMethodSecurity`, old Lombok/MapStruct versions.

## Production switches

- Gateway: run with `SPRING_PROFILES_ACTIVE=prod` and `JWT_ISSUER_URI` (Cognito, Auth0 or Keycloak).
- Replace `ddl-auto: update` with Flyway migrations.
- Add `userId` to `Scan` and filter every query by the JWT subject.

## Roadmap to paid product

1. Async scans: publish to Kafka/SQS, return 202, frontend polls `GET /api/scans/{id}`.
2. GitHub App: scan private repos without zip uploads.
3. Billing service with Stripe (Free / Solo $49 / Team $299), plan limits enforced at the gateway.
4. PDF report export with effort estimate.
5. Suggested diffs via OpenRewrite recipes.
6. Self-hosted Docker bundle with a licence key for banks.
7. Deploy: ECS Fargate or EKS, RDS PostgreSQL (enable the `vector` extension), S3 for uploads.
