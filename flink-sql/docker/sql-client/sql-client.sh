#!/bin/bash

# ANSI color codes for better readability
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
RESET='\033[0m'

echo -e "${BLUE}🚀 Starting Flink SQL Client...${RESET}"

# Wait for JobManager to be ready
echo -e "${YELLOW}⏳ Waiting for JobManager to be ready...${RESET}"
until $(curl --output /dev/null --silent --fail http://${FLINK_JOBMANAGER_HOST}:${FLINK_JOBMANAGER_PORT}/overview); do
    printf '.'
    sleep 2
done
echo -e "\n${GREEN}✅ JobManager is ready!${RESET}"

# Wait for Schema Registry
echo -e "${YELLOW}⏳ Waiting for Schema Registry to be ready...${RESET}"
until $(curl --output /dev/null --silent --head --fail ${SCHEMA_REGISTRY_URL}); do
    printf '.'
    sleep 2
done
echo -e "\n${GREEN}✅ Schema Registry is ready!${RESET}"

# Wait for Kafka
echo -e "${YELLOW}⏳ Waiting for Kafka to be ready...${RESET}"
# A simple check to see if Kafka is responsive
bootstrap_server=$(echo $KAFKA_BOOTSTRAP_SERVERS | cut -d',' -f1)
host=$(echo $bootstrap_server | cut -d':' -f1)
port=$(echo $bootstrap_server | cut -d':' -f2)

until nc -z $host $port; do
    printf '.'
    sleep 2
done
echo -e "\n${GREEN}✅ Kafka is ready!${RESET}"

# Set Flink SQL environment variables
# export SQL_CLIENT_OPTS="-Dlog4j.configurationFile=/opt/flink/conf/log4j-console.properties"
# export SQL_CLIENT_DEFAULTS="-e execution.runtime-mode=streaming -e execution.attached=true -e execution.result-mode=table -e table.dynamic-table-options.enabled=true"

# Execute SQL file if provided, otherwise start interactive mode
if [ -n "$SQL_FILE" ] && [ -f "/opt/sql/$SQL_FILE" ]; then
    echo -e "${BLUE}▶️ Executing SQL file: $SQL_FILE${RESET}"
    echo -e "${YELLOW}SQL Content:${RESET}"
    cat /opt/sql/$SQL_FILE
    echo ""
    /opt/flink/bin/sql-client.sh $SQL_CLIENT_DEFAULTS -f /opt/sql/$SQL_FILE
else
    echo -e "${BLUE}🔍 Starting interactive SQL client...${RESET}"
    echo -e "${YELLOW}Available SQL files:${RESET}"
    find /opt/sql -type f -name "*.sql" | sort
    echo ""
    /opt/flink/bin/sql-client.sh $SQL_CLIENT_DEFAULTS
fi
