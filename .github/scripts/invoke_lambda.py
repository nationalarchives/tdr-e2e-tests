import boto3
import sys

client = boto3.client("lambda")
environment = sys.argv[1]
response = client.invoke(FunctionName=f"tdr-e2e-tests-{environment}")

print(response)
if response['FunctionError'] is not None:
    exit(1)
