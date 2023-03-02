import requests
import os
resp = requests.get("https://api.github.com/repos/nationalarchives/tdr-e2e-tests/actions/workflows/ci.yml/runs").json()
last_status = [x for x in resp["workflow_runs"] if x["event"] == "workflow_dispatch" and x["status"] == "completed"][0]["conclusion"]
with open(os.environ['GITHUB_OUTPUT'], 'a') as fh:
    print(f"last_status={last_status}", file=fh)
