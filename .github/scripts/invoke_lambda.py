import boto3
from botocore.client import  Config
import sys
import json
from time import time

start = time()

config = Config(connect_timeout=600, read_timeout=600)
client = boto3.client("lambda", config=config)
environment = sys.argv[1]
file = sys.argv[2]
browser = sys.argv[3]

payload = bytes(json.dumps({"feature": file, "browser": browser}), "utf-8")
print("Calling lambda")
print(start)
response = client.invoke(FunctionName=f"tdr-e2e-tests-{environment}", Payload=payload)
if 'FunctionError' in response and response['FunctionError'] is not None:
    exit(1)
print(time() - start)
