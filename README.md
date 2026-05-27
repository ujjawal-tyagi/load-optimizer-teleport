# SmartLoad Optimization API

## How to run
```bash
git clone <your-repo>
cd <folder>
docker compose up --build
```

## Health check
```bash
curl http://localhost:8080/actuator/health
```

## Example request
```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
-H "Content-Type: application/json" \
-d '<your-request-body>'
```
