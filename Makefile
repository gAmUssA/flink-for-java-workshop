# 🛠️ Flink for Java Workshop - Setup Makefile 🛠️

# Colors for better readability
BLUE=\033[0;34m
GREEN=\033[0;32m
YELLOW=\033[1;33m
RED=\033[0;31m
BOLD=\033[1m
RESET=\033[0m

# Emojis for better readability
CHECK=✅
WARNING=⚠️
ERROR=❌
INFO=ℹ️
ROCKET=🚀
COFFEE=☕
CLOCK=🕒
CLOUD=☁️

# Default target
.PHONY: help
help:
	@echo "${BLUE}${ROCKET} Flink for Java Workshop - Setup Makefile ${RESET}"
	@echo ""
	@echo "${GREEN}${BOLD}Available targets:${RESET}"
	@echo "  ${YELLOW}setup-mac${RESET}        - Install all required dependencies on macOS using Brewfile"
	@echo "  ${YELLOW}setup-linux${RESET}      - Install all required dependencies on Linux"
	@echo "  ${YELLOW}setup-terraform${RESET}  - Setup Terraform for Confluent Cloud"
	@echo "  ${YELLOW}terraform-init${RESET}   - Initialize Terraform"
	@echo "  ${YELLOW}terraform-plan${RESET}   - Plan Terraform changes"
	@echo "  ${YELLOW}terraform-apply${RESET}  - Apply Terraform changes"
	@echo "  ${YELLOW}clean${RESET}            - Clean up temporary files"
	@echo "  ${YELLOW}check-prereqs${RESET}    - Check if all prerequisites are installed"
	@echo "  ${YELLOW}update-brew-deps${RESET} - Update Homebrew dependencies using Brewfile"
	@echo "  ${YELLOW}ci-checks${RESET}       - Run CI checks locally"
	@echo "  ${YELLOW}docker-build${RESET}    - Build Docker image locally"
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
	@echo "${BLUE}${INFO} Tapping Confluent repository...${RESET}"
	brew tap confluentinc/tap
	@echo "${BLUE}${INFO} Updating dependencies from Brewfile...${RESET}"
	brew bundle || echo "${YELLOW}${WARNING} Some Brewfile installations failed. Check output for details.${RESET}"
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
	@echo "${YELLOW}${INFO} Installing/Updating Homebrew packages using Brewfile...${RESET}"
	@if ! command -v brew >/dev/null 2>&1; then \
		echo "${RED}${ERROR} Homebrew is not installed. Please install it first: https://brew.sh${RESET}"; \
		exit 1; \
	fi
	@echo "${BLUE}${INFO} Updating Homebrew...${RESET}"
	brew update
	@echo "${BLUE}${INFO} Tapping Confluent repository...${RESET}"
	brew tap confluentinc/tap
	@echo "${BLUE}${INFO} Installing dependencies from Brewfile...${RESET}"
	brew bundle || echo "${YELLOW}${WARNING} Some Brewfile installations failed. Check output for details.${RESET}"
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
	sudo apt-get install -y software-properties-common
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
	@echo "${YELLOW}${INFO} Please enter your Confluent Cloud credentials:${RESET}"
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

.PHONY: terraform-plan
terraform-plan:
	@echo "${BLUE}${CLOCK} Planning Terraform changes...${RESET}"
	cd terraform && terraform plan -out "tfplan"

.PHONY: terraform-apply
terraform-apply:
	@echo "${BLUE}${ROCKET} Applying Terraform changes...${RESET}"
	cd terraform && terraform apply

# Generate cloud.properties from Terraform output
.PHONY: generate-cloud-properties
generate-cloud-properties:
	@echo "${BLUE}${CLOUD} Generating cloud.properties from Terraform output...${RESET}"
	cd terraform && terraform output -json | jq -r 'to_entries | map( {key: .key|tostring|split("_")|join("."), value: .value} ) | map("client.\(.key)=\(.value.value)") | .[]' > ../cloud.properties
	@echo "${GREEN}${CHECK} cloud.properties generated!${RESET}"

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

# Run Flink SQL application
.PHONY: run-sql
run-sql:
	@echo "${BLUE}${ROCKET} Running Flink SQL application...${RESET}"
	./gradlew :flink-sql:run

# Start local Docker environment
.PHONY: start-docker
start-docker:
	@echo "${BLUE}${ROCKET} Starting Docker environment...${RESET}"
	docker-compose up -d
	@echo "${GREEN}${CHECK} Docker environment started!${RESET}"
	@echo "${YELLOW}${INFO} Kafka is available at localhost:29092${RESET}"
	@echo "${YELLOW}${INFO} Schema Registry is available at http://localhost:8081${RESET}"
	@echo "${YELLOW}${INFO} Flink Dashboard is available at http://localhost:8080${RESET}"

# Stop local Docker environment
.PHONY: stop-docker
stop-docker:
	@echo "${BLUE}${INFO} Stopping Docker environment...${RESET}"
	docker-compose down
	@echo "${GREEN}${CHECK} Docker environment stopped!${RESET}"

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

# Clean up
.PHONY: clean
clean:
	@echo "${BLUE}${INFO} Cleaning up...${RESET}"
	./gradlew clean
	rm -f .env
	@echo "${GREEN}${CHECK} Cleanup completed!${RESET}"
