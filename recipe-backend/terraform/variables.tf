variable "subscription_id" {
  description = "Azure Subscription ID"
  type        = string
  default     = "ae420492-c95a-4e2f-9a32-846c54cb286c"
}

variable "resource_group_name" {
  description = "Resource group name"
  type        = string
  default     = "rg-recipe-webapp-dev"
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "northeurope"
}

variable "acr_name" {
  description = "Azure Container Registry name"
  type        = string
  default     = "recipewebappacrdev"
}

variable "app_service_name" {
  description = "Backend App Service name"
  type        = string
  default     = "recipe-backend-dev"
}

variable "app_service_sku" {
  description = "App Service SKU — B1 for dev, S1 for staging slots"
  type        = string
  default     = "B1"
}

variable "mongodb_uri" {
  description = "MongoDB Atlas connection string (stored in Key Vault)"
  type        = string
  sensitive   = true
}
