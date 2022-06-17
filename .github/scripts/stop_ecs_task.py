import boto3
import sys

stage = sys.argv[1]
task_id = sys.argv[2]
client = boto3.client('ecs')
cluster = f"consignmentapi_{stage}"
response = client.stop_task(cluster=cluster, task=task_id)
