import requests
import os
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")
if GITHUB_TOKEN == "":
    print("GITHUB_TOKEN is empty")
url = "https://api.github.com/repos/nationalarchives/tdr-e2e-tests/actions/workflows/ci.yml/runs"
headers = {"Authorization": f"Bearer {GITHUB_TOKEN}"}
resp = requests.get(url, header=headers)
print(f"Status Code: {resp.status_code}")
print(f"Response: {resp.text}")
last_status = [x for x in resp["workflow_runs"] if x["event"] == "workflow_dispatch" and x["status"] == "completed"][0]["conclusion"]
with open(os.environ['GITHUB_OUTPUT'], 'a') as fh:
    print(f"last_status={last_status}", file=fh)
