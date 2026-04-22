terraform {
  required_version = ">= 1.5.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.100"
    }
  }

  # Uncomment to use Azure Blob as remote state backend
  # backend "azurerm" {
  #   resource_group_name  = "rg-tfstate"
  #   storage_account_name = "sttfstate<unique>"
  #   container_name       = "tfstate"
  #   key                  = "recipe-webapp.terraform.tfstate"
  # }
}

provider "azurerm" {
  features {}
}

# ─────────────────────────────────────────────
# Resource Group
# ─────────────────────────────────────────────
resource "azurerm_resource_group" "main" {
  name     = var.resource_group_name
  location = var.location

  tags = local.common_tags
}

# ─────────────────────────────────────────────
# Azure Container Registry (ACR)
# ─────────────────────────────────────────────
resource "azurerm_container_registry" "acr" {
  name                = var.acr_name          # globally unique, alphanumeric only
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = var.acr_sku           # Basic | Standard | Premium
  admin_enabled       = true                  # needed for App Service pull

  tags = local.common_tags
}

# ─────────────────────────────────────────────
# App Service Plan
# ─────────────────────────────────────────────
resource "azurerm_service_plan" "main" {
  name                = "asp-${var.app_name}-${var.environment}"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  os_type             = "Linux"
  sku_name            = var.app_service_sku   # e.g. "B2", "P1v3"

  tags = local.common_tags
}

# ─────────────────────────────────────────────
# App Service (Web App for Containers)
# ─────────────────────────────────────────────
resource "azurerm_linux_web_app" "main" {
  name                = var.webapp_name       # globally unique
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  service_plan_id     = azurerm_service_plan.main.id

  # Allow App Service to pull from ACR
  identity {
    type = "SystemAssigned"
  }

  site_config {
    always_on = true

    application_stack {
      docker_image_name        = "${azurerm_container_registry.acr.login_server}/${var.image_name}:latest"
      docker_registry_url      = "https://${azurerm_container_registry.acr.login_server}"
      docker_registry_username = azurerm_container_registry.acr.admin_username
      docker_registry_password = azurerm_container_registry.acr.admin_password
    }

    health_check_path                 = "/actuator/health"
    health_check_eviction_time_in_min = 2
  }

  app_settings = {
    # Disable built-in CI (we deploy via slot swap)
    DOCKER_ENABLE_CI                        = "false"
    WEBSITES_ENABLE_APP_SERVICE_STORAGE     = "false"

    # Application-level config — override per environment in tfvars
    SPRING_PROFILES_ACTIVE = var.spring_profile
  }

  logs {
    http_logs {
      retention_in_days = 7
    }
    application_logs {
      file_system_level = "Information"
    }
  }

  tags = local.common_tags
}

# ─────────────────────────────────────────────
# Staging Deployment Slot
# ─────────────────────────────────────────────
resource "azurerm_linux_web_app_slot" "staging" {
  name           = "staging"
  app_service_id = azurerm_linux_web_app.main.id

  identity {
    type = "SystemAssigned"
  }

  site_config {
    always_on = true

    application_stack {
      docker_image_name        = "${azurerm_container_registry.acr.login_server}/${var.image_name}:latest"
      docker_registry_url      = "https://${azurerm_container_registry.acr.login_server}"
      docker_registry_username = azurerm_container_registry.acr.admin_username
      docker_registry_password = azurerm_container_registry.acr.admin_password
    }

    health_check_path                 = "/actuator/health"
    health_check_eviction_time_in_min = 2
  }

  app_settings = {
    DOCKER_ENABLE_CI                    = "false"
    WEBSITES_ENABLE_APP_SERVICE_STORAGE = "false"
    SPRING_PROFILES_ACTIVE              = "staging"
  }

  tags = local.common_tags
}

# ─────────────────────────────────────────────
# ACR Pull Role Assignment for production slot
# (SystemAssigned identity → AcrPull on the registry)
# ─────────────────────────────────────────────
resource "azurerm_role_assignment" "webapp_acr_pull" {
  scope                = azurerm_container_registry.acr.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_linux_web_app.main.identity[0].principal_id

  depends_on = [azurerm_linux_web_app.main]
}

resource "azurerm_role_assignment" "staging_acr_pull" {
  scope                = azurerm_container_registry.acr.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_linux_web_app_slot.staging.identity[0].principal_id

  depends_on = [azurerm_linux_web_app_slot.staging]
}
