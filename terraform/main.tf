# Configure the Confluent Provider
terraform {
  backend "local" {
    workspace_dir = ".tfstate/terraform.state"
  }

  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "2.22.0"
    }
  }
}

provider "confluent" {
}

resource "confluent_environment" "cc_env" {
  display_name = var.cc_env_name

  stream_governance {
    package = "ESSENTIALS"
  }

  lifecycle {
    prevent_destroy = false
  }
}