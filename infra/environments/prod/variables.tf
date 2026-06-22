variable "aws_region" {
  type    = string
  default = "eu-west-1"
}

variable "project" {
  type    = string
  default = "ai-study-assistant"
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "aws_account_id" {
  type        = string
  description = "AWS account ID where ECR repo is hosted"
  default     = "123456789012" # MUST BE CHANGED BY USER
}

variable "db_username" {
  type    = string
  default = "postgres"
}

variable "image_tag" {
  type    = string
  default = "latest"
}

