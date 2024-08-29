#!/bin/bash

IP_SET_NAME="tdr-apps-intg-whitelist"
SCOPE="REGIONAL"
REGION="eu-west-2"
NEW_IP=$(curl -s https://ipinfo.io/ip)/32

echo "The environment is: $2"
echo "The environment setName is: tdr-apps-$2-whitelist"
echo "New Ip: $NEW_IP"

# Get the current IP set details
fetch_ip_set_details() {
  echo "Fetching current IP set details..."
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
  local backoff=5  # Initial backoff time in seconds

  while [ $count -lt $retries ]; do
    echo "Attempting to update IP set (try $((count + 1))/$retries)..."
    aws wafv2 update-ip-set --name "$IP_SET_NAME" --scope "$SCOPE" --id "$IP_SET_ID" --addresses $UPDATED_IPS --lock-token "$LOCK_TOKEN" --region "$REGION"

    if [ $? -eq 0 ]; then
      success=true
      break
    else
      echo "Update failed due to optimistic lock. Retrying..."
      sleep $((backoff + RANDOM % 5))  # Exponential backoff with a random jitter
      backoff=$((backoff * 2))  # Double the backoff time
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

# Fetch initial IP set details
fetch_ip_set_details

# Append/remove the new IP to/from the list of existing IPs
if [ "$1" = "INSERT" ]; then
  UPDATED_IPS=$(echo "$EXISTING_IPS" | tr '\n' ' ')
  UPDATED_IPS="$UPDATED_IPS $NEW_IP"
  echo ORIGINAL_IPS="$EXISTING_IPS" >> "$GITHUB_ENV" #Unable to process file command 'env' successfully. Invalid format '10.106.16.113/32'
elif [ "$1" = "DELETE" ]; then
  UPDATED_IPS="$ORIGINAL_IPS" # This could potentially contain newly added IPs because each e2e test adds its own IP
fi

echo "IP set ID: $IP_SET_ID"
echo "Updated IPs: $UPDATED_IPS"

# Attempt to update the IP set
update_ip_set
