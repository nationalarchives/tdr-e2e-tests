#!/bin/bash

# Helper script for managing security group during matrix e2e test runs
# INSERT: Automatically fetches and inserts the current public IP into the specified security group.
# DELETE: Requires an IP address as the second argument to remove from the security group.

GROUP_ID=$(aws ec2 describe-security-groups --filters Name=group-name,Values=frontend-load-balancer-security-group | jq -r '.SecurityGroups[0].GroupId')

security_group_info=$(aws ec2 describe-security-groups --group-ids "$GROUP_ID")

if [ "$1" = "INSERT" ]; then
  CURRENT_IP=$(curl -s https://ipinfo.io/ip)/32
  if echo "$security_group_info" | grep -q "$CURRENT_IP"; then
    echo "IP $CURRENT_IP is already authorized."
  else
    aws ec2 authorize-security-group-ingress --group-id "$GROUP_ID" --protocol tcp --port 443 --cidr "$CURRENT_IP"
    echo "IP $CURRENT_IP has been authorized."
  fi
elif [ "$1" = "DELETE" ]; then
  if [ -z "$2" ]; then
    echo "ERROR: DELETE operation requires an IP address as the second argument."
    exit 1
  fi
  SUPPLIED_IP="$2/32"
  if echo "$security_group_info" | grep -q "$SUPPLIED_IP"; then
    aws ec2 revoke-security-group-ingress --group-id "$GROUP_ID" --protocol tcp --port 443 --cidr "$SUPPLIED_IP"
    echo "IP $SUPPLIED_IP has been revoked."
  else
    echo "IP $SUPPLIED_IP was not authorised, no need to revoke"
  fi
fi
