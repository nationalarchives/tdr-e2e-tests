import boto3
import sys
import json

client = boto3.client("lambda")
environment = sys.argv[1]
file = sys.argv[2]
url = sys.argv[3]
browser = sys.argv[4]

payload = {"feature": file, "nodeUrl": f"http://{url}:4444", "browser": browser}
response = client.invoke(FunctionName=f"tdr-e2e-tests-{environment}", Payload=bytes(json.dumps(payload), "utf-8"))
if 'FunctionError' in response and response['FunctionError'] is not None:
    exit(1)
