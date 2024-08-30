#!/bin/bash

IP_SET_NAME="tdr-apps-${2:-intg}-whitelist"
SCOPE="REGIONAL"
REGION="eu-west-2"
NEW_IP=$(curl -s https://ipinfo.io/ip)/32

# Get the current IP set details
fetch_ip_set_details() {
  echo "Fetching WAF IPSet details for: $IP_SET_NAME"
  IP_SET_ID=$(aws wafv2 list-ip-sets --scope "$SCOPE" --region "$REGION" | jq -r --arg ipset_name "$IP_SET_NAME" '.IPSets[] | select(.Name==$ipset_name) | .Id')
  IP_SET_DETAILS=$(aws wafv2 get-ip-set --name "$IP_SET_NAME" --scope "$SCOPE" --id "$IP_SET_ID" --region "$REGION")
  EXISTING_IPS=$(echo "$IP_SET_DETAILS" | jq -r '.IPSet.Addresses[]')
  LOCK_TOKEN=$(echo "$IP_SET_DETAILS" | jq -r '.LockToken')
}

# Update the IP set with retries on optimistic lock failure
update_ip_set() {
  local retries=3
  local count=0
  local success=false

  while [ $count -lt $retries ]; do
    echo "Attempting to update IP set (try $((count + 1))/$retries)..."
    aws wafv2 update-ip-set --name "$IP_SET_NAME" --scope "$SCOPE" --id "$IP_SET_ID" --addresses $UPDATED_IPS --lock-token "$LOCK_TOKEN" --region "$REGION"

    if [ $? -eq 0 ]; then
      success=true
      break
    else
      echo "Update failed due to optimistic lock. Retrying..."
      sleep $((RANDOM % 20 + 10))  # Random sleep between 10 and 30 seconds
      fetch_ip_set_details
    fi

    count=$((count + 1))
  done

  if [ "$success" = true ]; then
    echo "IP set updated successfully."
  else
    echo "Failed to update IP set after $retries attempts."
    exit 1
  fi
}

if [ "$1" = "GET" ]; then
  fetch_ip_set_details
  echo "$EXISTING_IPS" > waf-ip.txt
elif [ "$1" = "INSERT" ]; then
  fetch_ip_set_details
  UPDATED_IPS=$(echo "$EXISTING_IPS" | tr '\n' ' ')
  UPDATED_IPS="$UPDATED_IPS $NEW_IP"
  update_ip_set
elif [ "$1" = "DELETE" ]; then
  fetch_ip_set_details
  EXISTING_IPS=$(cat waf-ip-artifacts/waf-ip.txt)
  UPDATED_IPS=$(echo "$EXISTING_IPS" | tr '\n' ' ')
  update_ip_set
fi
