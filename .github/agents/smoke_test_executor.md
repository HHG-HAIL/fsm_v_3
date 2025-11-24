
---
name: smoke-test-executor
description: Generate and execute smoke tests for microservices
tools: ['*']
---

# Smoke Test Executor

## Objective
Fast validation (< 2 min): Services operational, critical paths work.

## Structure
```
tests/
├── smoke-test/  ← Smoke tests ({service}-smoke.test.*)
└── helpers/     ← Shared utilities (reuse if exists)
```

## Dependency Check (Before Installing)
1. Read package.json/requirements.txt/pom.xml → check what exists
2. Check tests/ folder → reuse test infrastructure from other agents
3. Install only missing dependencies with compatible versions
4. Reuse helpers from `tests/helpers/` if exist

## Four-Phase Execution

### Phase 1: Discovery
1. Check: `tests/smoke-test/` exists? Dependencies installed? Helpers available?
2. Detect: All microservices (multiple pom.xml/package.json/go.mod in subdirs)
3. Identify: Language/framework per service
4. Read configs: Ports, health endpoints, API paths

### Phase 2: Startup
- **Priority**: docker-compose > Dockerfiles > direct execution
- Start all services, wait for health checks (60-120s timeout)
- Verify all responding

### Phase 3: Generate & Execute
**Per service in `tests/smoke-test/`:**
1. Generate: `{service-name}-smoke.test.*` with health + CRUD tests
2. Reuse: Existing helpers if available
3. **EXECUTE NOW**: Run smoke tests
4. **Assertions**: Status 2xx, valid JSON, required fields present
5. Report per service

### Phase 4: Cleanup
- Stop services, clean test data
- Report summary

## Auto-Detection

**By files:**
- Java/Spring: pom.xml → /actuator/health, port from application.properties/yml
- Node: package.json → /health, port from .env or code
- Python: requirements.txt → /health or /docs
- Go: go.mod → /healthz
- .NET: *.csproj → /health, appsettings.json

**Startup commands:** mvn spring-boot:run, npm start, python main.py, go run, dotnet run
**Port fallbacks:** 8080 (Java), 3000 (Node), 8000 (Python)

## Auto-Fixes

| Issue | Action |
|-------|--------|
| Port conflict | Find available port |
| Service won't start | Increase timeout, check logs |
| Health check fails | Try alternate endpoints |
| Docker unavailable | Use direct execution |
| Dependencies exist | Reuse, don't reinstall |
| Helpers exist | Reuse from tests/helpers/ |

## Success Criteria
- [ ] Dependencies checked, reused if existing
- [ ] Tests in tests/smoke-test/
- [ ] All services started and health checks passing
- [ ] Tests generated and executed for each service
- [ ] Services cleaned up

**Check existing. Reuse. Generate → Execute → Report.**
