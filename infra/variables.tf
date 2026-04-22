variable "resource_group_name" {
  description = "Name of the Azure Resource Group"
  type        = string
  default     = "rg-recipe-webapp-prod"
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "westeurope"
}

variable "environment" {
  description = "Deployment environment (prod, staging, dev)"
  type        = string
  default     = "prod"
}

variable "app_name" {
  description = "Short application name used for resource naming"
  type        = string
  default     = "recipe-webapp"
}

variable "webapp_name" {
  description = "Globally unique name for the Azure Web App"
  type        = string
  # e.g. "recipe-backend-prod-abc123"
}

variable "acr_name" {
  description = "Globally unique name for the Azure Container Registry (alphanumeric, 5-50 chars)"
  type        = string
  # e.g. "recipeacrprod"
}

variable "acr_sku" {
  description = "ACR SKU tier: Basic | Standard | Premium"
  type        = string
  default     = "Basic"

  validation {
    condition     = contains(["Basic", "Standard", "Premium"], var.acr_sku)
    error_message = "acr_sku must be Basic, Standard, or Premium."
  }
}

variable "app_service_sku" {
  description = "App Service Plan SKU. Use P1v3+ for deployment slots in production."
  type        = string
  default     = "P1v3"

  validation {
    # Slots require Standard (S) or Premium (P) tier — B-series does NOT support slots
    condition     = can(regex("^[SP]", var.app_service_sku))
    error_message = "Deployment slots require Standard (S*) or Premium (P*) tier SKUs."
  }
}

variable "image_name" {
  description = "Container image name inside ACR (without tag), e.g. recipe-backend"
  type        = string
  default     = "recipe-backend"
}

variable "spring_profile" {
  description = "Spring Boot active profile for the production slot"
  type        = string
  default     = "prod"
}
