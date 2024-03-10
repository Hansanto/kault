#!/bin/bash

# Change the current directory during the execution of this script to retrieve relative files
cd "$(dirname "$0")" || exit 1

SERVICE_ACCOUNT="vault"
NAMESPACE="kault-test"

kubectl create namespace $NAMESPACE
kubectl create serviceaccount $SERVICE_ACCOUNT -n $NAMESPACE
kubectl apply -f permissions.yml

KUBERNETES_TOKEN=$(kubectl create token $SERVICE_ACCOUNT -n $NAMESPACE)
echo "$KUBERNETES_TOKEN" > token.tmp
