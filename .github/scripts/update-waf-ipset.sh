#!/bin/bash

# Check if correct number of arguments are passed
#if [ "$#" -ne 6 ]; then
#    echo "Usage: $0 <IPSetName> <Scope> <IPSetId> <Region> <NewIP/CIDR> <AWSProfile>"
#    exit 1
#fi

# Assign arguments to variables
IP_SET_NAME="tdr-apps-intg-whitelist"
SCOPE="REGIONAL"
REGION="eu-west-2"
NEW_IP=$(curl -s https://ipinfo.io/ip)/32

echo "The environment is: $1"
echo "The environment setName is: tdr-apps-$1-whitelist"

# Get the current IP set details
echo "Fetching current IP set details..."
IP_SET_ID=$(aws wafv2 list-ip-sets --scope "$SCOPE" --region "$REGION" | jq -r --arg ipset_name "$IP_SET_NAME" '.IPSets[] | select(.Name==$ipset_name) | .Id')
IP_SET_DETAILS=$(aws wafv2 get-ip-set --name "$IP_SET_NAME" --scope "$SCOPE" --id "$IP_SET_ID" --region "$REGION")

if [ $? -ne 0 ]; then
    echo "Error fetching IP set details. Please check your parameters and try again."
    exit 1
fi

# Extract existing IPs and lock token from the IP set details
#IP_SET_ID=$(echo "$IP_SET_DETAILS" | jq -r '.IPSet.Id')
EXISTING_IPS=$(echo "$IP_SET_DETAILS" | jq -r '.IPSet.Addresses[]')
LOCK_TOKEN=$(echo "$IP_SET_DETAILS" | jq -r '.LockToken')

# Append the new IP to the list of existing IPs
UPDATED_IPS=$(echo "$EXISTING_IPS" | tr '\n' ' ')
UPDATED_IPS="$UPDATED_IPS $NEW_IP"

echo "ip setId" $IP_SET_ID
echo "Existing Ips: $EXISTING_IPS"
echo "LOCK Token: $LOCK_TOKEN"
echo "New Ip: $NEW_IP"

# Update the IP set with the new IP address
#echo "Updating IP set with new IP address..."
#aws wafv2 update-ip-set --name "$IP_SET_NAME" --scope "$SCOPE" --id "$IP_SET_ID" --addresses $UPDATED_IPS --lock-token "$LOCK_TOKEN" --region "$REGION" --profile "$AWS_PROFILE"
#
#if [ $? -eq 0 ]; then
#    echo "Successfully added $NEW_IP to the IP set."
#else
#    echo "Failed to update the IP set. Please check your parameters and try again."
#fi
