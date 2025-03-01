# Makefile for Flink SQL Module

# Color definitions
YELLOW := \033[1;33m
GREEN := \033[1;32m
RED := \033[1;31m
CYAN := \033[1;36m
BLUE := \033[1;34m
PURPLE := \033[1;35m
RESET := \033[0m

# Emoji definitions
ROCKET := 🚀
CHECK := ✅
WARNING := ⚠️
INFO := ℹ️
GEAR := ⚙️
CLOCK := 🕒
DATA := 📊
CLOUD := ☁️
COMPUTER := 💻

# Environment variables
KAFKA_TOPIC ?= flights
ENVIRONMENT ?= local

# Project variables
MAIN_CLASS := io.confluent.developer.sql.FlinkSqlMain
JAR_FILE := build/libs/flink-sql-1.0-SNAPSHOT.jar

.PHONY: help
help: ## Show this help
	@echo "${YELLOW}${ROCKET} Flink SQL Module Makefile Help ${RESET}"
	@echo ""
	@echo "${BLUE}Usage:${RESET}"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  ${CYAN}%-20s${RESET} %s\n", $$1, $$2}'

.PHONY: build
build: ## Build the project
	@echo "${YELLOW}${GEAR} Building Flink SQL module...${RESET}"
	@cd .. && ./gradlew :flink-sql:build
	@echo "${GREEN}${CHECK} Build complete!${RESET}"

.PHONY: clean
clean: ## Clean build artifacts
	@echo "${YELLOW}${GEAR} Cleaning build artifacts...${RESET}"
	@cd .. && ./gradlew :flink-sql:clean
	@echo "${GREEN}${CHECK} Clean complete!${RESET}"

.PHONY: run-status
run-status: build ## Run the Flight Status Dashboard use case
	@echo "${YELLOW}${ROCKET} Running Flight Status Dashboard on ${ENVIRONMENT} mode...${RESET}"
	@echo "${BLUE}${INFO} Topic: ${KAFKA_TOPIC}${RESET}"
	@cd .. && ./gradlew :flink-sql:run --args="status ${ENVIRONMENT} ${KAFKA_TOPIC}"

.PHONY: run-routes
run-routes: build ## Run the Flight Route Analytics use case
	@echo "${YELLOW}${ROCKET} Running Flight Route Analytics on ${ENVIRONMENT} mode...${RESET}"
	@echo "${BLUE}${INFO} Topic: ${KAFKA_TOPIC}${RESET}"
	@cd .. && ./gradlew :flink-sql:run --args="routes ${ENVIRONMENT} ${KAFKA_TOPIC}"

.PHONY: run-delays
run-delays: build ## Run the Airline Delay Performance Analysis use case
	@echo "${YELLOW}${ROCKET} Running Airline Delay Performance Analysis on ${ENVIRONMENT} mode...${RESET}"
	@echo "${BLUE}${INFO} Topic: ${KAFKA_TOPIC}${RESET}"
	@cd .. && ./gradlew :flink-sql:run --args="delays ${ENVIRONMENT} ${KAFKA_TOPIC}"

.PHONY: run-all
run-all: build ## Run all use cases together
	@echo "${YELLOW}${ROCKET} Running all use cases on ${ENVIRONMENT} mode...${RESET}"
	@echo "${BLUE}${INFO} Topic: ${KAFKA_TOPIC}${RESET}"
	@cd .. && ./gradlew :flink-sql:run --args="all ${ENVIRONMENT} ${KAFKA_TOPIC}"

.PHONY: run-cloud
run-cloud: ## Run all use cases on cloud environment
	@echo "${YELLOW}${CLOUD} Running all use cases on cloud environment...${RESET}"
	@$(MAKE) run-all ENVIRONMENT=cloud

.PHONY: setup-local
setup-local: ## Ensure Kafka and Schema Registry are running locally
	@echo "${YELLOW}${GEAR} Setting up local Kafka environment...${RESET}"
	@cd .. && make docker-up
	@echo "${GREEN}${CHECK} Kafka and Schema Registry are now running!${RESET}"
	@echo "${BLUE}${INFO} Use 'make docker-logs' to view container logs${RESET}"

.PHONY: view-docs
view-docs: ## List documentation files for all use cases
	@echo "${YELLOW}${DATA} Available documentation:${RESET}"
	@echo "${CYAN}Flight Status Dashboard:${RESET} src/main/resources/docs/FlightStatusDashboard.adoc"
	@echo "${CYAN}Flight Route Analytics:${RESET} src/main/resources/docs/FlightRouteAnalytics.adoc"
	@echo "${CYAN}Airline Delay Performance Analysis:${RESET} src/main/resources/docs/AirlineDelayAnalytics.adoc"
