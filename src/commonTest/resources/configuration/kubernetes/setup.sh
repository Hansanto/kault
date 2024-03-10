#!/bin/bash

# Change the current directory during the execution of this script to retrieve relative files
cd "$(dirname "$0")" || exit 1

kubectl create namespace kault-test
kubectl create serviceaccount vault -n kault_test
kubectl apply -f permissions.yml
