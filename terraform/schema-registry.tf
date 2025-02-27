# Add Stream Governance, Schema Registry.
data "confluent_schema_registry_cluster" "advanced" {
  environment {
    id = confluent_environment.cc_env.id
  }
  depends_on = [confluent_kafka_cluster.kafka_cluster]
}

# ---------------------------------------------------------------------------
# API KEY and Role for management of Schema Registry
# ---------------------------------------------------------------------------
resource "confluent_service_account" "sr_manager" {
  display_name = "${var.cc_env_name}-sr_manager"
  description  = "Service account to manage Kafka cluster"
}

resource "confluent_role_binding" "sr_manager_data_steward" {
  principal   = "User:${confluent_service_account.sr_manager.id}"
  role_name   = "DataSteward"
  crn_pattern = confluent_environment.cc_env.resource_name

  depends_on = [data.confluent_schema_registry_cluster.advanced]
}

resource "confluent_api_key" "sr_manager_kafka_api_key" {
  display_name = "sr_manager_kafka_api_key"
  description  = "SR API Key that is owned by 'sr_manager' service account"
  owner {
    id          = confluent_service_account.sr_manager.id
    api_version = confluent_service_account.sr_manager.api_version
    kind        = confluent_service_account.sr_manager.kind
  }

  managed_resource {
    id          = data.confluent_schema_registry_cluster.advanced.id
    api_version = data.confluent_schema_registry_cluster.advanced.api_version
    kind        = data.confluent_schema_registry_cluster.advanced.kind

    environment {
      id = confluent_environment.cc_env.id
    }
  }

  depends_on = [
    confluent_role_binding.sr_manager_data_steward
  ]
}
