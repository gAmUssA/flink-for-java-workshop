# output "schema_registry_url" {
#   value = data.confluent_schema_registry_cluster.advanced.rest_endpoint
# }

output "kafka_boostrap_servers" {
  value = replace(confluent_kafka_cluster.kafka_cluster.bootstrap_endpoint, "SASL_SSL://", "")
}

output "cloud" {
    value = var.cloud_provider
}

output "region" {
    value = var.cloud_region
}

output "organization-id" {
    value = replace(var.org_id, "\"", "")
}

output "environment-id" {
    value = confluent_environment.cc_env.id
}

output "compute-pool-id" {
    value = confluent_flink_compute_pool.main_flink_pool.id
}

output "kafka_sasl_jaas_config" {
    value = "org.apache.kafka.common.security.plain.PlainLoginModule required username='${confluent_api_key.kafka_developer_kafka_api_key.id}' password='${nonsensitive(confluent_api_key.kafka_developer_kafka_api_key.secret)}';"
}

output "registry_url" {
    value = data.confluent_schema_registry_cluster.advanced.rest_endpoint
}

output "registry_key" {
    value = confluent_api_key.sr_manager_kafka_api_key.id
}

output "registry_secret" {
    value = nonsensitive(confluent_api_key.sr_manager_kafka_api_key.secret)
}

output "flink-api-key" {
    value = confluent_api_key.flink_developer_api_key.id
}

output "flink-api-secret" {
    value = nonsensitive(confluent_api_key.flink_developer_api_key.secret)
}
