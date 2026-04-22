# Terraform: Azure App Service + Slots + ACR

## File Structure

```
terraform/
├── main.tf               # Resources: RG, ACR, App Service Plan, Web App, Staging Slot
├── variables.tf          # All input variables with descriptions & validation
├── outputs.tf            # ACR credentials, hostnames, GitHub secrets cheatsheet
├── locals.tf             # Shared tags
└── terraform.tfvars.example  # Copy → terraform.tfvars and fill in values
```

---

## Quick Start

### 1. Prerequisites

```bash
az login
az account set --subscription "<your-subscription-id>"
terraform -version   # >= 1.5.0
```

### 2. Configure variables

```bash
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars — set webapp_name and acr_name to globally unique values
```

### 3. Deploy infrastructure

```bash
terraform init
terraform plan -out=tfplan
terraform apply tfplan
```

### 4. Retrieve GitHub Actions secrets

```bash
# Print all secrets (sensitive values are redacted by default)
terraform output -json github_secrets_summary

# Get ACR password explicitly
terraform output -raw acr_admin_password
```

Add these to your GitHub repo under **Settings → Secrets and variables → Actions**:

| Secret name           | Source                                  |
|-----------------------|-----------------------------------------|
| `AZURE_WEBAPP_NAME`   | `terraform output webapp_name`          |
| `AZURE_RESOURCE_GROUP`| value from tfvars                       |
| `AZURE_ACR_SERVER`    | `terraform output acr_login_server`     |
| `AZURE_ACR_USERNAME`  | `terraform output -raw acr_admin_username` |
| `AZURE_ACR_PASSWORD`  | `terraform output -raw acr_admin_password` |
| `AZURE_CREDENTIALS`   | Service principal JSON (see below)      |
| `AZURE_CLIENT_ID`     | For OIDC login (production swap)        |
| `AZURE_TENANT_ID`     | For OIDC login                          |
| `AZURE_SUBSCRIPTION_ID` | For OIDC login                        |

### 5. Create a service principal for GitHub Actions

```bash
# For AZURE_CREDENTIALS (staging deploy job)
az ad sp create-for-rbac \
  --name "sp-recipe-github-actions" \
  --role contributor \
  --scopes /subscriptions/<SUB_ID>/resourceGroups/<RG_NAME> \
  --sdk-auth
```

Paste the JSON output as the `AZURE_CREDENTIALS` secret.

---

## Key Design Decisions

### SKU: P1v3 minimum
Deployment slots require **Standard or Premium** tier. B-series (Basic) does **not** support slots. P1v3 gives slots + better cold-start performance.

### ACR Admin vs Managed Identity
`admin_enabled = true` is used for simplicity. The `azurerm_role_assignment` resources also grant the App Service's SystemAssigned identity the `AcrPull` role — you can switch to managed identity pull and disable admin once verified.

### Blue/Green swap
The staging → production swap is zero-downtime. The old production slot becomes staging after the swap, acting as an instant rollback target (uncomment the rollback step in `prod.yml` to auto-rollback on health failure).

---

## Destroying Resources

```bash
terraform destroy
```
