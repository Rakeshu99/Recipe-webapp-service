output "resource_group_name" {
  description = "Resource group that contains all resources"
  value       = azurerm_resource_group.main.name
}

# ── ACR ──────────────────────────────────────
output "acr_login_server" {
  description = "ACR login server URL — use as AZURE_ACR_SERVER in GitHub Actions"
  value       = azurerm_container_registry.acr.login_server
}

output "acr_admin_username" {
  description = "ACR admin username — store as AZURE_ACR_USERNAME secret"
  value       = azurerm_container_registry.acr.admin_username
  sensitive   = true
}

output "acr_admin_password" {
  description = "ACR admin password — store as AZURE_ACR_PASSWORD secret"
  value       = azurerm_container_registry.acr.admin_password
  sensitive   = true
}

# ── App Service ───────────────────────────────
output "webapp_name" {
  description = "App Service name — store as AZURE_WEBAPP_NAME secret"
  value       = azurerm_linux_web_app.main.name
}

output "webapp_default_hostname" {
  description = "Production URL"
  value       = "https://${azurerm_linux_web_app.main.default_hostname}"
}

output "staging_slot_hostname" {
  description = "Staging slot URL for smoke tests"
  value       = "https://${azurerm_linux_web_app_slot.staging.default_hostname}"
}

# ── GitHub Actions secrets cheatsheet ────────
output "github_secrets_summary" {
  description = "Values to add as GitHub repository secrets"
  value = {
    AZURE_WEBAPP_NAME      = azurerm_linux_web_app.main.name
    AZURE_RESOURCE_GROUP   = azurerm_resource_group.main.name
    AZURE_ACR_SERVER       = azurerm_container_registry.acr.login_server
    AZURE_ACR_USERNAME     = azurerm_container_registry.acr.admin_username
    # AZURE_ACR_PASSWORD → run: terraform output -raw acr_admin_password
  }
  sensitive = true
}
