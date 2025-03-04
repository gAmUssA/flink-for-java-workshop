# 🛠️ Flink for Java Workshop - Setup Makefile 🛠️

# Colors for better readability
BLUE:=$(shell printf "\033[0;34m")
GREEN:=$(shell printf "\033[0;32m")
YELLOW:=$(shell printf "\033[1;33m")
RED:=$(shell printf "\033[0;31m")
BOLD:=$(shell printf "\033[1m")
RESET:=$(shell printf "\033[0m")

# Emojis for better readability
CHECK=✅
WARNING=⚠️
ERROR=❌
INFO=ℹ️
ROCKET=🚀
COFFEE=☕
CLOCK=🕒
CLOUD=☁️
STAR=⭐
TERRAFORM=🌐

# Default target
.PHONY: help
help:
	@echo "${BLUE}${ROCKET} Flink for Java Workshop - Setup Makefile ${RESET}"
	@echo ""
	@echo "${GREEN}${BOLD}Available targets:${RESET}"
	@echo ""
	@echo "${YELLOW}${STAR} Environment Setup:${RESET}"
	@echo "${BLUE}${INFO} 🔧 setup-mac${RESET}          - Install all required dependencies on macOS using Brewfile"
	@echo "${BLUE}${INFO} 🔧 setup-linux${RESET}        - Install all required dependencies on Linux"
	@echo "${BLUE}${INFO} 🔍 check-prereqs${RESET}      - Check if all prerequisites are installed"
	@echo "${BLUE}${INFO} 🔄 update-brew-deps${RESET}   - Update Homebrew dependencies using Brewfile"
	@echo ""
	@echo "${YELLOW}${STAR} Build & Run:${RESET}"
	@echo "${BLUE}${INFO} 🏗️ build${RESET}              - Build the entire project with Gradle"
	@echo "${BLUE}${INFO} 🏗️ build-data-generator${RESET} - Build the Flink Data Generator"
	@echo "${BLUE}${INFO} 🚀 run-streaming${RESET}      - Run the Flink Streaming application"
	@echo "${BLUE}${INFO} 🚀 run-sql${RESET}            - Run the Flink Table API application"
	@echo "${BLUE}${INFO} 🧪 ci-checks${RESET}          - Run CI checks locally"
	@echo ""
	@echo "${YELLOW}${STAR} Data Generator:${RESET}"
	@echo "${BLUE}${INFO} 🚀 run-data-generator-local${RESET} - Run the Flink Data Generator in local environment"
	@echo "${BLUE}${INFO} ☁️ run-data-generator-cloud${RESET} - Run the Flink Data Generator in cloud environment"
	@echo "${BLUE}${INFO} 🚀 run-data-generator-with-props${RESET} - Run with custom properties (PROPS=path/to/properties)"
	@echo ""
	@echo "${YELLOW}${STAR} Flink Table API:${RESET}"
	@echo "${BLUE}${INFO} 🚀 run-sql-status-local${RESET}  - Run Flight Status Dashboard locally"
	@echo "${BLUE}${INFO} 🚀 run-sql-routes-local${RESET}  - Run Flight Route Analytics locally"
	@echo "${BLUE}${INFO} 🚀 run-sql-delays-local${RESET}  - Run Airline Delay Analytics locally"
	@echo "${BLUE}${INFO} 🚀 run-sql-all-local${RESET}     - Run all SQL use cases locally"
	@echo "${BLUE}${INFO} ☁️ run-sql-status-cloud${RESET}  - Run Flight Status Dashboard on cloud"
	@echo "${BLUE}${INFO} ☁️ run-sql-routes-cloud${RESET}  - Run Flight Route Analytics on cloud"
	@echo "${BLUE}${INFO} ☁️ run-sql-delays-cloud${RESET}  - Run Airline Delay Analytics on cloud"
	@echo "${BLUE}${INFO} ☁️ run-sql-all-cloud${RESET}     - Run all SQL use cases on cloud"
	@echo ""
	@echo "${YELLOW}${STAR} Docker Management:${RESET}"
	@echo "${BLUE}${INFO} 🐳 docker-up${RESET}          - Start all containers"
	@echo "${BLUE}${INFO} 🐳 docker-down${RESET}        - Stop and remove all containers"
	@echo "${BLUE}${INFO} 🐳 docker-restart${RESET}     - Restart all containers"
	@echo "${BLUE}${INFO} 🐳 docker-ps${RESET}          - List running containers and their status"
	@echo "${BLUE}${INFO} 🐳 docker-logs${RESET}        - View logs (optionally for a specific service with SERVICE=name)"
	@echo "${BLUE}${INFO} 🐳 docker-build${RESET}       - Build Docker image locally"
	@echo ""
	@echo "${YELLOW}${STAR} Terraform & Confluent Cloud:${RESET}"
	@echo "${BLUE}${INFO} ☁️ setup-terraform${RESET}    - Setup Terraform for Confluent Cloud (using HashiCorp's official tap)"
	@echo "${BLUE}${INFO} ☁️ terraform-init${RESET}     - Initialize Terraform"
	@echo "${BLUE}${INFO} ☁️ terraform-plan${RESET}     - Plan Terraform changes"
	@echo "${BLUE}${INFO} ☁️ terraform-apply${RESET}    - Apply Terraform changes"
	@echo "${BLUE}${INFO} ☁️ terraform-destroy${RESET}  - Destroy Terraform-managed infrastructure"
	@echo "${BLUE}${INFO} ☁️ terraform-output${RESET}   - Generate cloud.properties from Terraform output"
	@echo "${BLUE}${INFO} ☁️ terraform-upgrade${RESET}  - Upgrade Terraform providers"
	@echo "${BLUE}${INFO} ☁️ terraform-org-id${RESET}   - Get Confluent Cloud organization ID"
	@echo "${BLUE}${INFO} ☁️ cc-setup${RESET}           - Complete Confluent Cloud setup (init, plan, apply, output)"
	@echo "${BLUE}${INFO} ☁️ cc-teardown${RESET}        - Teardown Confluent Cloud infrastructure"
	@echo "${BLUE}${INFO} ☁️ tf-init${RESET}            - Shorthand for terraform-init"
	@echo "${BLUE}${INFO} ☁️ tf-plan${RESET}            - Shorthand for terraform-plan"
	@echo "${BLUE}${INFO} ☁️ tf-apply${RESET}           - Shorthand for terraform-apply"
	@echo "${BLUE}${INFO} ☁️ tf-destroy${RESET}         - Shorthand for terraform-destroy"
	@echo "${BLUE}${INFO} ☁️ tf-out${RESET}             - Shorthand for terraform-output"
	@echo "${BLUE}${INFO} ☁️ tf-upgrade${RESET}         - Shorthand for terraform-upgrade"
	@echo "${BLUE}${INFO} ☁️ tf-org-id${RESET}          - Shorthand for terraform-org-id"
	@echo ""
	@echo "${YELLOW}${STAR} Cleanup:${RESET}"
	@echo "${BLUE}${INFO} 🧹 clean${RESET}              - Clean up temporary files"
	@echo ""
	@echo "${BLUE}${INFO} For more information, see README.adoc${RESET}"

# Update Homebrew dependencies
.PHONY: update-brew-deps
update-brew-deps:
	@echo "${BLUE}${ROCKET} Updating Homebrew dependencies...${RESET}"
	@if ! command -v brew >/dev/null 2>&1; then \
		echo "${RED}${ERROR} Homebrew is not installed. Please install it first: https://brew.sh${RESET}"; \
		exit 1; \
	fi
	@echo "${BLUE}${INFO} Updating Homebrew...${RESET}"
	brew update
	@echo "${BLUE}${INFO} Updating dependencies from Brewfile...${RESET}"
	brew bundle || { printf "${YELLOW}${WARNING} Some Brewfile installations failed. Check output for details.${RESET}\n"; }
	@echo "${GREEN}${CHECK} Homebrew dependencies updated!${RESET}"

# Check prerequisites
.PHONY: check-prereqs
check-prereqs:
	@echo "${BLUE}${CLOCK} Checking prerequisites...${RESET}"
	@if command -v java >/dev/null 2>&1; then \
		echo "${GREEN}${CHECK} Java is installed${RESET}"; \
		java -version; \
	else \
		echo "${RED}${ERROR} Java is not installed${RESET}"; \
	fi
	@if command -v docker >/dev/null 2>&1; then \
		echo "${GREEN}${CHECK} Docker is installed${RESET}"; \
		docker --version; \
	else \
		echo "${RED}${ERROR} Docker is not installed${RESET}"; \
	fi
	@if command -v docker-compose >/dev/null 2>&1; then \
		echo "${GREEN}${CHECK} Docker Compose is installed${RESET}"; \
		docker-compose --version; \
	else \
		echo "${RED}${ERROR} Docker Compose is not installed${RESET}"; \
	fi
	@if command -v terraform >/dev/null 2>&1; then \
		echo "${GREEN}${CHECK} Terraform is installed${RESET}"; \
		terraform --version; \
	else \
		echo "${YELLOW}${WARNING} Terraform is not installed (optional)${RESET}"; \
	fi
	@if command -v confluent >/dev/null 2>&1; then \
		echo "${GREEN}${CHECK} Confluent CLI is installed${RESET}"; \
		confluent version; \
	else \
		echo "${YELLOW}${WARNING} Confluent CLI is not installed (optional)${RESET}"; \
		echo "${YELLOW}${INFO} Install with: brew install confluentinc/tap/cli${RESET}"; \
	fi
	@if command -v jq >/dev/null 2>&1; then \
		echo "${GREEN}${CHECK} jq is installed${RESET}"; \
		jq --version; \
	else \
		echo "${YELLOW}${WARNING} jq is not installed (optional)${RESET}"; \
	fi
	@if command -v git >/dev/null 2>&1; then \
		echo "${GREEN}${CHECK} Git is installed${RESET}"; \
		git --version; \
	else \
		echo "${RED}${ERROR} Git is not installed${RESET}"; \
	fi
	@if [ -d "$$HOME/.sdkman" ] || command -v sdk >/dev/null 2>&1; then \
		echo "${GREEN}${CHECK} SDKMAN is installed${RESET}"; \
		if ! command -v sdk >/dev/null 2>&1; then \
			echo "${YELLOW}${INFO} SDKMAN is installed but not in your PATH. To activate it, run: source \"$$HOME/.sdkman/bin/sdkman-init.sh\"${RESET}"; \
		fi \
	else \
		echo "${YELLOW}${WARNING} SDKMAN is not installed (recommended for Java version management)${RESET}"; \
	fi

# macOS setup
.PHONY: setup-mac
setup-mac:
	@echo "${BLUE}${ROCKET} Setting up dependencies on macOS...${RESET}"
	@if ! command -v brew >/dev/null 2>&1; then \
		echo "${RED}${ERROR} Homebrew is not installed. Please install it first: https://brew.sh${RESET}"; \
		exit 1; \
	fi
	@echo "${BLUE}${INFO} Updating Homebrew...${RESET}"
	brew update
	@echo "${BLUE}${INFO} Installing dependencies from Brewfile...${RESET}"
	@brew bundle || { printf "${YELLOW}${WARNING} Some Brewfile installations failed. Check output for details.${RESET}\n"; }
	@echo "${BLUE}${INFO} Checking for SDKMAN installation...${RESET}"
	@if [ -d "$$HOME/.sdkman" ]; then \
		echo "${GREEN}${CHECK} SDKMAN is already installed${RESET}"; \
	else \
		echo "${BLUE}${INFO} Installing SDKMAN for Java version management...${RESET}"; \
		curl -s "https://get.sdkman.io" | bash || echo "${YELLOW}${WARNING} SDKMAN installation failed. Please install manually.${RESET}"; \
	fi
	@echo "${GREEN}${CHECK} macOS setup completed!${RESET}"
	@echo "${YELLOW}${INFO} Remember to start OrbStack application${RESET}"
	@echo "${YELLOW}${INFO} To activate SDKMAN in your current shell, run: source \"$$HOME/.sdkman/bin/sdkman-init.sh\"${RESET}"
	@echo "${YELLOW}${INFO} Run 'make check-prereqs' to verify installations${RESET}"

# Linux setup
.PHONY: setup-linux
setup-linux:
	@echo "${BLUE}${ROCKET} Setting up dependencies on Linux...${RESET}"
	@echo "${YELLOW}${INFO} This will install dependencies using apt-get...${RESET}"
	@if ! command -v apt-get >/dev/null 2>&1; then \
		echo "${RED}${ERROR} apt-get is not available. This script is designed for Debian/Ubuntu.${RESET}"; \
		exit 1; \
	fi
	@echo "${BLUE}${INFO} Updating package lists...${RESET}"
	sudo apt-get update
	# Install Java 21 (Temurin)
	@echo "${BLUE}${INFO} Installing Java 21 (Temurin)...${RESET}"
	sudo apt-get install -y wget apt-transport-https gnupg
	wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
	echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
	sudo apt-get update
	sudo apt-get install -y temurin-21-jdk
	# Install Docker and Docker Compose
	@echo "${BLUE}${INFO} Installing Docker and Docker Compose...${RESET}"
	sudo apt-get install -y docker.io docker-compose
	sudo usermod -aG docker $USER
	# Install Terraform
	@echo "${BLUE}${INFO} Installing Terraform...${RESET}"
	sudo apt-get install -y software-properties-common gnupg
	wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
	echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
	sudo apt-get update
	sudo apt-get install -y terraform
	# Install jq
	@echo "${BLUE}${INFO} Installing jq...${RESET}"
	sudo apt-get install -y jq
	# Install git
	@echo "${BLUE}${INFO} Installing Git...${RESET}"
	sudo apt-get install -y git
	# Install Confluent CLI
	@echo "${BLUE}${INFO} Installing Confluent CLI...${RESET}"
	curl -sL --http1.1 https://cnfl.io/cli | sh -s -- latest
	# Install SDKMAN
	@echo "${BLUE}${INFO} Installing SDKMAN for Java version management...${RESET}"
	curl -s "https://get.sdkman.io" | bash
	@echo "${GREEN}${CHECK} Linux setup completed!${RESET}"
	@echo "${YELLOW}${WARNING} You may need to log out and log back in for Docker group permissions to take effect${RESET}"
	@echo "${YELLOW}${INFO} Run 'make check-prereqs' to verify installations${RESET}"

# Setup Terraform for Confluent Cloud
.PHONY: setup-terraform
setup-terraform:
	@echo "${BLUE}${CLOUD} Setting up Terraform for Confluent Cloud...${RESET}"
	@echo "${YELLOW}${INFO} Please enter your Confluent Cloud credentials and organization ID. This will be used to create a new environment and service account.${RESET}"
	@read -p "Confluent Cloud API Key: " API_KEY; \
	read -p "Confluent Cloud API Secret: " API_SECRET; \
	echo "export CONFLUENT_CLOUD_API_KEY=$$API_KEY" > .env; \
	echo "export CONFLUENT_CLOUD_API_SECRET=$$API_SECRET" >> .env; \
	echo "${GREEN}${CHECK} Credentials saved to .env file${RESET}"; \
	echo "${YELLOW}${INFO} Setting up organization ID...${RESET}"; \
	if command -v confluent >/dev/null 2>&1; then \
		source .env; \
		confluent login --save; \
		ORG_ID=$$(confluent organization list -o json | jq -c -r '.[] | select(.is_current)' | jq -r '.id'); \
		echo "export TF_VAR_org_id=$$ORG_ID" >> .env; \
		echo "${GREEN}${CHECK} Organization ID set to: $$ORG_ID${RESET}"; \
	else \
		echo "${YELLOW}${WARNING} Confluent CLI not found. Please set TF_VAR_org_id manually.${RESET}"; \
		read -p "Confluent Cloud Organization ID: " ORG_ID; \
		echo "export TF_VAR_org_id=$$ORG_ID" >> .env; \
	fi; \
	echo "${GREEN}${CHECK} Terraform setup completed!${RESET}"; \
	echo "${YELLOW}${INFO} Run 'source .env' to load the environment variables${RESET}"

# Terraform commands
.PHONY: terraform-init
terraform-init:
	@echo "${BLUE}${ROCKET} Initializing Terraform...${RESET}"
	cd terraform && terraform init

.PHONY: terraform-upgrade
terraform-upgrade:
	@echo "${BLUE}${ROCKET} Updating Terraform...${RESET}"
	cd terraform && terraform init -upgrade

.PHONY: terraform-plan
terraform-plan:
	@echo "${BLUE}${CLOCK} Planning Terraform changes...${RESET}"
	cd terraform && terraform plan -out "tfplan"

.PHONY: terraform-apply
terraform-apply:
	@echo "${BLUE}${ROCKET} Applying Terraform changes...${RESET}"
	cd terraform && terraform apply "tfplan"

.PHONY: terraform-destroy
terraform-destroy:
	@echo "${BLUE}${WARNING} Destroying Terraform-managed infrastructure...${RESET}"
	@echo "${YELLOW}${WARNING} This will destroy all resources created by Terraform!${RESET}"
	@read -p "Are you sure you want to continue? (y/N) " confirm; \
	if [ "$$confirm" = "y" ] || [ "$$confirm" = "Y" ]; then \
		cd terraform && terraform destroy --auto-approve; \
		echo "${GREEN}${CHECK} Infrastructure destroyed!${RESET}"; \
	else \
		echo "${YELLOW}${INFO} Operation cancelled.${RESET}"; \
	fi

# Get Confluent Cloud organization ID
.PHONY: terraform-org-id
terraform-org-id:
	@echo "${BLUE}${CLOUD} Getting Confluent Cloud organization ID...${RESET}"
	@if ! command -v confluent >/dev/null 2>&1; then \
		echo "${RED}${ERROR} Confluent CLI is not installed. Please install it first.${RESET}"; \
		exit 1; \
	fi
	@if ! command -v jq >/dev/null 2>&1; then \
		echo "${RED}${ERROR} jq is not installed. Please install it first.${RESET}"; \
		exit 1; \
	fi
	@echo "${BLUE}${INFO} Exporting organization ID to TF_VAR_org_id...${RESET}"
	@export TF_VAR_org_id=$$(confluent organization list -o json | jq -c -r '.[] | select(.is_current)' | jq '.id'); \
	echo "TF_VAR_org_id=$$TF_VAR_org_id"; \
	echo "export TF_VAR_org_id=$$TF_VAR_org_id" >> .env; \
	echo "${GREEN}${CHECK} Organization ID exported to TF_VAR_org_id and saved to .env file!${RESET}"

# Generate cloud.properties from Terraform output
.PHONY: terraform-output
terraform-output:
	@echo "${BLUE}${CLOUD} Generating cloud.properties from Terraform output...${RESET}"
	@mkdir -p ../common/utils/src/main/resources
	cd terraform && terraform output -json | jq -r 'to_entries | map( {key: .key|tostring|split("_")|join("."), value: .value} ) | map("\(.key)=\(.value.value)") | .[]' | while read -r line ; do echo "$$line"; done > ../common/utils/src/main/resources/cloud.properties
	@echo "${GREEN}${CHECK} cloud.properties generated in ../common/utils/src/main/resources/cloud.properties!${RESET}"

# Shorthand commands for Terraform operations
.PHONY: tf-init
tf-init: terraform-init

.PHONY: tf-plan
tf-plan: terraform-plan

.PHONY: tf-apply
tf-apply: terraform-apply

.PHONY: tf-destroy
tf-destroy: terraform-destroy

.PHONY: tf-out
tf-out: terraform-output

.PHONY: tf-upgrade
tf-upgrade: terraform-upgrade

.PHONY: tf-org-id
tf-org-id: terraform-org-id

# Complete Confluent Cloud setup
.PHONY: cc-setup
cc-setup:
	@echo "${BLUE}${ROCKET} Setting up Confluent Cloud infrastructure...${RESET}"
	@if [ ! -f .env ]; then \
		echo "${YELLOW}${WARNING} Environment variables not set. Running setup-terraform first...${RESET}"; \
		$(MAKE) setup-terraform; \
	fi
	@echo "${BLUE}${INFO} Loading environment variables...${RESET}"
	@source .env || true
	@echo "${BLUE}${INFO} Getting Confluent Cloud organization ID...${RESET}"
	@$(MAKE) terraform-org-id
	@echo "${BLUE}${INFO} Initializing Terraform...${RESET}"
	@$(MAKE) terraform-init
	@echo "${BLUE}${INFO} Planning Terraform changes...${RESET}"
	@$(MAKE) terraform-plan
	@echo "${BLUE}${INFO} Applying Terraform changes...${RESET}"
	@$(MAKE) terraform-apply
	@echo "${BLUE}${INFO} Generating cloud.properties...${RESET}"
	@$(MAKE) terraform-output
	@echo "${GREEN}${CHECK} Confluent Cloud setup complete!${RESET}"

# Teardown Confluent Cloud infrastructure
.PHONY: cc-teardown
cc-teardown:
	@echo "${BLUE}${WARNING} Tearing down Confluent Cloud infrastructure...${RESET}"
	@$(MAKE) terraform-destroy

# Build Gradle project
.PHONY: build
build:
	@echo "${BLUE}${ROCKET} Building project with Gradle...${RESET}"
	./gradlew clean build

# Run Flink Streaming application
.PHONY: run-streaming
run-streaming:
	@echo "${BLUE}${ROCKET} Running Flink Streaming application...${RESET}"
	./gradlew :flink-streaming:run

# Run Flink Table API application
.PHONY: run-sql
run-sql:
	@echo "${BLUE}${ROCKET} Running Flink Table API application...${RESET}"
	./gradlew :flink-table-api:run

# Flink Table API Module
.PHONY: run-sql-status-local
run-sql-status-local:
	@echo "${BLUE}${ROCKET} Running Flight Status Dashboard with Table API (local)...${RESET}"
	./gradlew :flink-table-api:run --args="status local"
	@echo "${GREEN}${CHECK} Flight Status Dashboard completed!${RESET}"

.PHONY: run-sql-routes-local
run-sql-routes-local:
	@echo "${BLUE}${ROCKET} Running Flight Route Analytics with Table API (local)...${RESET}"
	./gradlew :flink-table-api:run --args="routes local"
	@echo "${GREEN}${CHECK} Flight Route Analytics completed!${RESET}"

.PHONY: run-sql-delays-local
run-sql-delays-local:
	@echo "${BLUE}${ROCKET} Running Airline Delay Analytics with Table API (local)...${RESET}"
	./gradlew :flink-table-api:run --args="delays local"
	@echo "${GREEN}${CHECK} Airline Delay Analytics completed!${RESET}"

.PHONY: run-sql-all-local
run-sql-all-local:
	@echo "${BLUE}${ROCKET} Running all SQL use cases with Table API (local)...${RESET}"
	./gradlew :flink-table-api:run --args="all local"
	@echo "${GREEN}${CHECK} All SQL use cases completed!${RESET}"

.PHONY: run-sql-status-cloud
run-sql-status-cloud:
	@echo "${BLUE}${CLOUD} Running Flight Status Dashboard with Table API (cloud)...${RESET}"
	./gradlew :flink-table-api:run --args="status cloud"
	@echo "${GREEN}${CHECK} Flight Status Dashboard completed!${RESET}"

.PHONY: run-sql-routes-cloud
run-sql-routes-cloud:
	@echo "${BLUE}${CLOUD} Running Flight Route Analytics with Table API (cloud)...${RESET}"
	./gradlew :flink-table-api:run --args="routes cloud"
	@echo "${GREEN}${CHECK} Flight Route Analytics completed!${RESET}"

.PHONY: run-sql-delays-cloud
run-sql-delays-cloud:
	@echo "${BLUE}${CLOUD} Running Airline Delay Analytics with Table API (cloud)...${RESET}"
	./gradlew :flink-table-api:run --args="delays cloud"
	@echo "${GREEN}${CHECK} Airline Delay Analytics completed!${RESET}"

.PHONY: run-sql-all-cloud
run-sql-all-cloud:
	@echo "${BLUE}${CLOUD} Running all SQL use cases with Table API (cloud)...${RESET}"
	./gradlew :flink-table-api:run --args="all cloud"
	@echo "${GREEN}${CHECK} All SQL use cases completed!${RESET}"

# Docker Compose targets
.PHONY: docker-up docker-down docker-ps docker-logs docker-restart

docker-up:
	@echo "${BLUE}${ROCKET} Starting Docker containers...${RESET}"
	@if ! command -v docker >/dev/null 2>&1; then \
		echo "${RED}${ERROR} Docker is not installed${RESET}"; \
		exit 1; \
	fi
	docker compose up -d
	@echo "${GREEN}${CHECK} Docker containers started successfully!${RESET}"
	@echo "${YELLOW}${INFO} Kafka is available at localhost:29092${RESET}"
	@echo "${YELLOW}${INFO} Schema Registry is available at http://localhost:8081${RESET}"

docker-down:
	@echo "${BLUE}${INFO} Stopping Docker containers...${RESET}"
	docker compose down
	@echo "${GREEN}${CHECK} Docker containers stopped successfully!${RESET}"

docker-ps:
	@echo "${BLUE}${INFO} Listing running Docker containers...${RESET}"
	docker compose ps

docker-logs:
	@echo "${BLUE}${INFO} Showing Docker container logs...${RESET}"
	@if [ -z "$(SERVICE)" ]; then \
		docker compose logs -f; \
	else \
		docker compose logs -f $(SERVICE); \
	fi

docker-restart:
	@echo "${BLUE}${ROCKET} Restarting Docker containers...${RESET}"
	docker compose restart
	@echo "${GREEN}${CHECK} Docker containers restarted successfully!${RESET}"

# Run CI checks locally
.PHONY: ci-checks
ci-checks:
	@echo "${BLUE}${ROCKET} Running CI checks locally...${RESET}"
	@echo "${BLUE}${INFO} Running code style checks...${RESET}"
	./gradlew checkstyleMain checkstyleTest || echo "${RED}${ERROR} Code style checks failed${RESET}"
	@echo "${BLUE}${INFO} Checking for deprecated API usage...${RESET}"
	./gradlew spotbugsMain spotbugsTest || echo "${RED}${ERROR} Deprecated API checks failed${RESET}"
	@echo "${BLUE}${INFO} Building project...${RESET}"
	./gradlew build
	@echo "${BLUE}${INFO} Running tests...${RESET}"
	./gradlew test
	@echo "${GREEN}${CHECK} CI checks completed!${RESET}"

# Build Docker image
.PHONY: docker-build
docker-build:
	@echo "${BLUE}${ROCKET} Building Docker image...${RESET}"
	@if ! command -v docker >/dev/null 2>&1; then \
		echo "${RED}${ERROR} Docker is not installed${RESET}"; \
		exit 1; \
	fi
	@echo "${BLUE}${INFO} Building project...${RESET}"
	./gradlew shadowJar
	@echo "${BLUE}${INFO} Building Docker image...${RESET}"
	docker build -t flink-for-java-workshop:local .
	@echo "${GREEN}${CHECK} Docker image built successfully!${RESET}"
	@echo "${YELLOW}${INFO} Run with: docker run -it flink-for-java-workshop:local${RESET}"

# Flink Data Generator targets
.PHONY: build-data-generator
build-data-generator:
	@echo "${BLUE}${ROCKET} Building Flink Data Generator...${RESET}"
	./gradlew :flink-data-generator:build
	@echo "${GREEN}${CHECK} Flink Data Generator built successfully!${RESET}"

.PHONY: run-data-generator-local
run-data-generator-local:
	@echo "${BLUE}${ROCKET} Running Flink Data Generator in local environment...${RESET}"
	./gradlew :flink-data-generator:run --args="--env local"
	@echo "${GREEN}${CHECK} Flink Data Generator completed!${RESET}"

.PHONY: run-data-generator-cloud
run-data-generator-cloud:
	@echo "${BLUE}${CLOUD} Running Flink Data Generator in cloud environment...${RESET}"
	./gradlew :flink-data-generator:run --args="--env cloud"
	@echo "${GREEN}${CHECK} Flink Data Generator completed!${RESET}"

.PHONY: run-data-generator-with-props
run-data-generator-with-props:
	@echo "${BLUE}${ROCKET} Running Flink Data Generator with custom properties...${RESET}"
	@if [ -z "$(PROPS)" ]; then \
		echo "${RED}${ERROR} Please specify properties file with PROPS=path/to/properties.${RESET}"; \
		exit 1; \
	fi
	./gradlew :flink-data-generator:run --args="--properties $(PROPS)"
	@echo "${GREEN}${CHECK} Flink Data Generator completed!${RESET}"

# Clean up
.PHONY: clean
clean:
	@echo "${BLUE}${INFO} Cleaning up...${RESET}"
	./gradlew clean
	rm -f .env
	@echo "${GREEN}${CHECK} Cleanup completed!${RESET}"
