resource "confluent_kafka_cluster" "kafka_cluster" {
  display_name = var.cc_default_kafka_cluster_name
  availability = "SINGLE_ZONE"
  cloud        = var.cloud_provider
  region       = var.cloud_region
  standard {}
  environment {
    id = confluent_environment.cc_env.id
  }

  depends_on = [confluent_environment.cc_env]
}

# ---------------------------------------------------------------------------
# API KEY and Role for Administration of Kafka
# ---------------------------------------------------------------------------
resource "confluent_service_account" "kafka_manager" {
  display_name = "${var.cc_env_name}-kafka_manager"
  description  = "Service account to manage Kafka cluster"
}

resource "confluent_role_binding" "kafka_manager_kafka_cluster_admin" {
  principal   = "User:${confluent_service_account.kafka_manager.id}"
  role_name   = "CloudClusterAdmin"
  crn_pattern = confluent_kafka_cluster.kafka_cluster.rbac_crn
}

resource "confluent_api_key" "kafka_manager_kafka_api_key" {
  display_name = "kafka_manager_kafka_api_key"
  description  = "Kafka API Key that is owned by 'kafka_manager' service account"
  owner {
    id          = confluent_service_account.kafka_manager.id
    api_version = confluent_service_account.kafka_manager.api_version
    kind        = confluent_service_account.kafka_manager.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.kafka_cluster.id
    api_version = confluent_kafka_cluster.kafka_cluster.api_version
    kind        = confluent_kafka_cluster.kafka_cluster.kind

    environment {
      id = confluent_environment.cc_env.id
    }
  }

  depends_on = [
    confluent_environment.cc_env,
    confluent_role_binding.kafka_manager_kafka_cluster_admin
  ]
}

# ---------------------------------------------------------------------------
# API KEY and Role for Developers on Kafka
# ---------------------------------------------------------------------------

resource "confluent_service_account" "kafka_developer" {
  display_name = "${var.cc_env_name}-kafka_developer"
  description  = "Service account for developer using Kafka cluster"
}

resource "confluent_role_binding" "kafka_developer_read_all_topics" {
  principal   = "User:${confluent_service_account.kafka_manager.id}"
  role_name   = "DeveloperRead"
  crn_pattern = "${confluent_kafka_cluster.kafka_cluster.rbac_crn}/kafka=${confluent_kafka_cluster.kafka_cluster.id}/topic=*"
}

resource "confluent_role_binding" "kafka_developer_write_all_topics" {
  principal   = "User:${confluent_service_account.kafka_manager.id}"
  role_name   = "DeveloperWrite"
  crn_pattern = "${confluent_kafka_cluster.kafka_cluster.rbac_crn}/kafka=${confluent_kafka_cluster.kafka_cluster.id}/topic=*"
}

resource "confluent_api_key" "kafka_developer_kafka_api_key" {
  display_name = "kafka_developer_kafka_api_key"
  description  = "Kafka API Key that is owned by 'kafka_developer' service account"
  owner {
    id          = confluent_service_account.kafka_developer.id
    api_version = confluent_service_account.kafka_developer.api_version
    kind        = confluent_service_account.kafka_developer.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.kafka_cluster.id
    api_version = confluent_kafka_cluster.kafka_cluster.api_version
    kind        = confluent_kafka_cluster.kafka_cluster.kind

    environment {
      id = confluent_environment.cc_env.id
    }
  }

  depends_on = [
    confluent_role_binding.kafka_developer_read_all_topics,
    confluent_role_binding.kafka_developer_write_all_topics
  ]
}
