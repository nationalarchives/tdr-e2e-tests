#!/bin/bash

# Helper script for managing security group during matrix e2e test runs
# INSERT: Automatically fetches and inserts the current public IP into the specified security group.
# DELETE: Requires an IP address as the second argument to remove from the security group.

GROUP_ID=$(aws ec2 describe-security-groups --filters Name=group-name,Values=frontend-load-balancer-security-group | jq -r '.SecurityGroups[0].GroupId')

check_ip_authorized() {
    local ip=$1
    aws ec2 describe-security-groups --group-id "$GROUP_ID" \
        --query "SecurityGroups[*].IpPermissions[*].IpRanges[?CidrIp=='$ip'].CidrIp" --output text
}

if [ "$1" = "INSERT" ]; then
  CURRENT_IP=$(curl -s https://ipinfo.io/ip)/32
  CURRENT_IP_AUTHORISATION_RESPONSE=$(check_ip_authorized "$CURRENT_IP")
  if [ -z "$CURRENT_IP_AUTHORISATION_RESPONSE" ]; then
    aws ec2 authorize-security-group-ingress --group-id "$GROUP_ID" --protocol tcp --port 443 --cidr "$CURRENT_IP"
    echo "IP $CURRENT_IP has been authorized."
  else
    echo "IP $CURRENT_IP is already authorized."
  fi
elif [ "$1" = "DELETE" ]; then
  if [ -z "$2" ]; then
    echo "ERROR: DELETE operation requires an IP address as the second argument."
    exit 1
  fi
  SUPPLIED_IP="$2/32"
  SUPPLIED_IP_AUTHORISATION_RESPONSE=$(check_ip_authorized "$SUPPLIED_IP")
  echo ">>>>>>>>>>>>$SUPPLIED_IP_AUTHORISATION_RESPONSE"
  if [ -n "$SUPPLIED_IP_AUTHORISATION_RESPONSE" ]; then
    aws ec2 revoke-security-group-ingress --group-id "$GROUP_ID" --protocol tcp --port 443 --cidr "$SUPPLIED_IP"
    echo "IP $SUPPLIED_IP has been revoked."
  else
    echo "IP $SUPPLIED_IP was not authorised, no need to revoke"
  fi
fi
