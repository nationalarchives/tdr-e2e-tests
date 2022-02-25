GROUP_ID=$(aws ec2 describe-security-groups --filters Name=group-name,Values=frontend-load-balancer-security-group | jq -r '.SecurityGroups[0].GroupId')
IP=$(curl https://ipinfo.io/ip)
if [ $1 = "INSERT" ]
then
  aws ec2 authorize-security-group-ingress --group-id $GROUP_ID --protocol tcp --port 443 --cidr $IP/32
else
  aws ec2 revoke-security-group-ingress --group-id $GROUP_ID --protocol tcp --port 443 --cidr $IP/32
fi
