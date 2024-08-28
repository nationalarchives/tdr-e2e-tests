#!/bin/bash

IP_SET_NAME="tdr-apps-intg-whitelist"
SCOPE="REGIONAL"
REGION="eu-west-2"
NEW_IP=$(curl -s https://ipinfo.io/ip)/32

echo "The environment is: $2"
echo "The environment setName is: tdr-apps-$2-whitelist"
echo "New Ip: $NEW_IP"

# Get the current IP set details
echo "Fetching current IP set details..."
IP_SET_ID=$(aws wafv2 list-ip-sets --scope "$SCOPE" --region "$REGION" | jq -r --arg ipset_name "$IP_SET_NAME" '.IPSets[] | select(.Name==$ipset_name) | .Id')
IP_SET_DETAILS=$(aws wafv2 get-ip-set --name "$IP_SET_NAME" --scope "$SCOPE" --id "$IP_SET_ID" --region "$REGION")

# Extract existing IPs and lock token from the IP set details
#IP_SET_ID=$(echo "$IP_SET_DETAILS" | jq -r '.IPSet.Id')
EXISTING_IPS=$(echo "$IP_SET_DETAILS" | jq -r '.IPSet.Addresses[]')
LOCK_TOKEN=$(echo "$IP_SET_DETAILS" | jq -r '.LockToken')

# Append/remove the new IP to the list of existing IPs
if [ "$1" = "INSERT" ]; then
  UPDATED_IPS=$(echo "$EXISTING_IPS" | tr '\n' ' ')
  UPDATED_IPS="$UPDATED_IPS $NEW_IP"
  echo "ORIGINAL_IPS=\"$EXISTING_IPS\"" >> "$GITHUB_ENV"
elif [ "$1" = "DELETE" ]; then
  UPDATED_IPS="$ORIGINAL_IPS"
fi

echo "ip setId" $IP_SET_ID
#echo "Existing Ips: $EXISTING_IPS"
#echo "LOCK Token: $LOCK_TOKEN"
echo "Updated Ips: $UPDATED_IPS"

# Update the IP set with the new IP address
echo "Updating IP set with new IP address $NEW_IP"
aws wafv2 update-ip-set --name "$IP_SET_NAME" --scope "$SCOPE" --id "$IP_SET_ID" --addresses $UPDATED_IPS --lock-token "$LOCK_TOKEN" --region "$REGION"
