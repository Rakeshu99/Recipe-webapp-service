output "backend_url" {
  description = "Backend App Service default hostname"
  value       = "https://${azurerm_linux_web_app.backend.default_hostname}"
}

output "acr_login_server" {
  description = "ACR login server URL"
  value       = azurerm_container_registry.acr.login_server
}

output "key_vault_uri" {
  description = "Key Vault URI"
  value       = azurerm_key_vault.kv.vault_uri
}
