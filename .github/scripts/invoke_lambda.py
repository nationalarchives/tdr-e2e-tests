import boto3
from botocore.client import  Config
import sys
import json

config = Config(connect_timeout=600, read_timeout=600)
client = boto3.client("lambda", config=config)
environment = sys.argv[1]
file = sys.argv[2]
url = sys.argv[3]
browser = sys.argv[4]

payload = bytes(json.dumps({"feature": file, "browser": browser}), "utf-8")
response = client.invoke(FunctionName=f"tdr-e2e-tests-{environment}", Payload=payload)
if 'FunctionError' in response and response['FunctionError'] is not None:
    exit(1)
