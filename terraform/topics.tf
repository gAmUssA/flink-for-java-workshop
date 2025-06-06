resource "confluent_kafka_topic" "flights_avro" {

  topic_name = "flights"

  kafka_cluster {
    id = confluent_kafka_cluster.kafka_cluster.id
  }
  rest_endpoint = confluent_kafka_cluster.kafka_cluster.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }

  partitions_count = 10

  lifecycle {
    prevent_destroy = false
  }
}

resource "confluent_subject_config" "flights_value_avro" {
  subject_name = "${confluent_kafka_topic.flights_avro.topic_name}-value"

  schema_registry_cluster {
    id = data.confluent_schema_registry_cluster.advanced.id
  }
  rest_endpoint = data.confluent_schema_registry_cluster.advanced.rest_endpoint

  credentials {
    key    = confluent_api_key.env-manager-schema-registry-api-key.id
    secret = confluent_api_key.env-manager-schema-registry-api-key.secret
  }

  compatibility_level = "BACKWARD"

  lifecycle {
    prevent_destroy = false
  }
}