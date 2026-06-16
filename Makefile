.PHONY: infra-up infra-down backend-run topics-create docker-build up down logs web-dev

# Infrastructure only (Postgres, Redis, Kafka)
infra-up:
	docker compose up -d postgres redis zookeeper kafka kafka-ui

infra-down:
	docker compose down

# Build backend Docker image
docker-build:
	docker compose build backend

# Full stack: infra + backend (web runs locally via web-dev)
up:
	docker compose up -d postgres redis zookeeper kafka kafka-ui backend

down:
	docker compose down -v

logs:
	docker compose logs -f backend

topics-create:
	docker exec buildagent_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic buildagent.payments.recorded --if-not-exists
	docker exec buildagent_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic buildagent.payments.adjusted --if-not-exists
	docker exec buildagent_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic buildagent.leases.created --if-not-exists
	docker exec buildagent_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic buildagent.leases.expiring --if-not-exists
	docker exec buildagent_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic buildagent.leases.terminated --if-not-exists
	docker exec buildagent_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic buildagent.maintenance.created --if-not-exists
	docker exec buildagent_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic buildagent.maintenance.status-changed --if-not-exists
	docker exec buildagent_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic buildagent.maintenance.sla-breached --if-not-exists
	docker exec buildagent_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic buildagent.notifications.email --if-not-exists
	docker exec buildagent_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic buildagent.audit.events --if-not-exists

# Run the Compose Web frontend locally (dev mode, hot reload)
web-dev:
	./gradlew :composeApp:wasmJsBrowserDevelopmentRun --continuous

backend-run:
	./gradlew :backend:run
