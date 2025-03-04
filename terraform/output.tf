output "acks" {
  value = "all"
}

output "bootstrap_servers" {
  value = replace(confluent_kafka_cluster.kafka_cluster.bootstrap_endpoint, "SASL_SSL://", "")
}

output "client_dns_lookup" {
  value = "use_all_dns_ips"
}

output "environment" {
  value = "cloud"
}

output "security_protocol" {
  value = "SASL_SSL"
}

output "sasl_mechanism" {
  value = "PLAIN"
}

output "sasl_jaas_config" {
  value = "org.apache.kafka.common.security.plain.PlainLoginModule required username='${confluent_api_key.app-manager-kafka-api-key.id}' password='${nonsensitive(confluent_api_key.app-manager-kafka-api-key.secret)}';"
}

output "schema_registry_url" {
  value = data.confluent_schema_registry_cluster.advanced.rest_endpoint
}

output "schema_registry_basic_auth_credentials_source" {
  value = "USER_INFO"
}

output "schema_registry_basic_auth_user_info" {
  value = "${confluent_api_key.env-manager-schema-registry-api-key.id}:${nonsensitive(confluent_api_key.env-manager-schema-registry-api-key.secret)}"
}

output "session_timeout_ms" {
  value = 45000
}

output "topic_name" {
  value = confluent_kafka_topic.flights_avro.topic_name
}