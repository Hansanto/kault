path "auth/token/lookup-self" {
  capabilities = ["read"]
}

path "secret/*" {
  capabilities = ["create"]
}
