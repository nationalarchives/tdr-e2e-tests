import boto3
import sys

stage = sys.argv[1]
client = boto3.client('ecs')
ec2_client = boto3.client("ec2")
cluster = f"file_format_build_{stage}"
task = "test-e2e-proxy"
security_groups = ec2_client.describe_security_groups()['SecurityGroups']
filtered_security_groups = list(filter(lambda sg: sg['GroupName'] == "tdr-lambda-signed_cookies", security_groups))
security_groups = [security_group['GroupId'] for security_group in filtered_security_groups]
subnets = [subnet['SubnetId'] for subnet in ec2_client.describe_subnets(Filters=[
    {
        'Name': 'tag:Name',
        'Values': [
            'tdr-private-subnet-0-' + stage,
            'tdr-private-subnet-1-' + stage,
        ]
    },
])['Subnets']]

response = client.run_task(
    cluster=cluster,
    taskDefinition=task,
    launchType="FARGATE",
    platformVersion="1.4.0",
    networkConfiguration={
        'awsvpcConfiguration': {
            'subnets': subnets,
            'securityGroups': security_groups
        }
    }
)
task_arn = response["tasks"][0]["taskArn"]
waiter = client.get_waiter('tasks_running')

waiter.wait(cluster=cluster, tasks=[task_arn])

describe_response = client.describe_tasks(cluster=cluster, tasks=[task_arn])
ip = describe_response["tasks"][0]["containers"][0]["networkInterfaces"][0]["privateIpv4Address"]

print(f"::set-output name=node-ip::{ip}")
