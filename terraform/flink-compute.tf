

resource "confluent_flink_compute_pool" "main_flink_pool" {
  display_name     = "main_flink_pool"
  cloud            = var.cloud_provider
  region           = var.cloud_region
  max_cfu          = 5
  environment {
    id = confluent_environment.cc_env.id
  }
}

data "confluent_flink_region" "main_flink_region" {
  cloud            = var.cloud_provider
  region           = var.cloud_region
}

resource "confluent_service_account" "flink_developer" {
  display_name = "${var.cc_env_name}-flink_developer"
  description  = "Service account for flink developer"
}

resource "confluent_role_binding" "fd_flink_developer" {
  principal   = "User:${confluent_service_account.flink_developer.id}"
  role_name   = "FlinkDeveloper"
  crn_pattern = confluent_environment.cc_env.resource_name

  depends_on = [ confluent_flink_compute_pool.main_flink_pool]
}

resource "confluent_role_binding" "fd_kafka_write" {
  principal   = "User:${confluent_service_account.flink_developer.id}"
  role_name   = "DeveloperWrite"
  crn_pattern = "${confluent_kafka_cluster.kafka_cluster.rbac_crn}/kafka=${confluent_kafka_cluster.kafka_cluster.id}/topic=*"

  depends_on = [ confluent_kafka_cluster.kafka_cluster]
}

resource "confluent_role_binding" "fd_kafka_read" {
  principal   = "User:${confluent_service_account.flink_developer.id}"
  role_name   = "DeveloperRead"
  crn_pattern = "${confluent_kafka_cluster.kafka_cluster.rbac_crn}/kafka=${confluent_kafka_cluster.kafka_cluster.id}/topic=*"

  depends_on = [ confluent_kafka_cluster.kafka_cluster]
}

resource "confluent_role_binding" "fd_schema_registry_write" {
  principal   = "User:${confluent_service_account.flink_developer.id}"
  role_name   = "DeveloperWrite"
  crn_pattern = "${data.confluent_schema_registry_cluster.advanced.resource_name}/subject=*"
}

resource "confluent_role_binding" "fd_schema_registry_read" {
  principal   = "User:${confluent_service_account.flink_developer.id}"
  role_name   = "DeveloperRead"
  crn_pattern = "${data.confluent_schema_registry_cluster.advanced.resource_name}/subject=*"
}

resource "confluent_api_key" "flink_developer_api_key" {
  display_name = "flink_developer_api_key"
  description  = "Flink Developer API Key that is owned by 'flink_developer' service account"
  owner {
    id          = confluent_service_account.flink_developer.id
    api_version = confluent_service_account.flink_developer.api_version
    kind        = confluent_service_account.flink_developer.kind
  }

  managed_resource {
    id          = data.confluent_flink_region.main_flink_region.id
    api_version = data.confluent_flink_region.main_flink_region.api_version
    kind        = data.confluent_flink_region.main_flink_region.kind

    environment {
      id = confluent_environment.cc_env.id
    }
  }

  depends_on = [
    confluent_service_account.flink_developer
  ]
}