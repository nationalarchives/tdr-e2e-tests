import requests
import os
WORKFLOW_PAT = os.getenv("WORKFLOW_PAT")
if not WORKFLOW_PAT:
    raise ValueError("WORKFLOW_PAT environment variable is not set.")
print(os.getenv("TESTING_ENV"))
url = "https://api.github.com/repos/nationalarchives/tdr-e2e-tests/actions/workflows/ci.yml/runs"
headers = {"Authorization": f"Bearer {WORKFLOW_PAT}"}
resp = requests.get(url, headers=headers)
print(f"Status Code: {resp.status_code}")
print(f"Response: {resp.text}")
last_status = [x for x in resp["workflow_runs"] if x["event"] == "workflow_dispatch" and x["status"] == "completed"][0]["conclusion"]
with open(os.environ['GITHUB_OUTPUT'], 'a') as fh:
    print(f"last_status={last_status}", file=fh)
