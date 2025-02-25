variable "org_id" {
  type = string
  description = "CC Organization ID"
}

variable "cloud_provider" {
  type = string
  description = "cloud provider for Confluent Cloud"
  default = "AWS"
}

variable "cloud_region" {
  type = string
  description = "cloud provider region"
  default = "us-east-2"
}

variable "cc_env_name" {
    type = string
    description = "CC Environment Name"
    default = "java_flink_workshop"
}

variable "cc_default_kafka_cluster_name" {
    type = string
    description = "Kafka Default Cluster Name"
    default = "workshop"
}

variable "cc_default_flink_compute_pool_name" {
    type = string
    description = "Default Flink Compute Pool Name"
    default = "flink-compute-pool"
}
