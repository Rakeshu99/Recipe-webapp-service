environment         = "dev"
location            = "northeurope"
resource_group_name = "rg-recipe-webapp-dev"
acr_name            = "recipewebappacrdev"
webapp_name         = "recipe-backend-dev"
spring_profile      = "dev"

# PHASE 1 (run first): B1 — no slots, current student subscription tier
app_service_sku     = "B1"
enable_staging_slot = false

# PHASE 2 (run later): upgrade to S1 and enable slot
# app_service_sku     = "S1"
# enable_staging_slot = true

# mongodb_uri is set via env var: TF_VAR_mongodb_uri
# Do NOT commit the real URI here
