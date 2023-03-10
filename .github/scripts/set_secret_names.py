import sys
import os
is_pr = len(sys.argv) == 1
env = "intg" if is_pr else sys.argv[1]
with open(os.environ['GITHUB_OUTPUT'], 'a') as fh:
    print(f"environment={env}", file=fh)
    print(f"account_number_secret={env.upper()}_ACCOUNT_NUMBER", file=fh)
    print(f"title_environment={env.title()}", file=fh)
    print(f"is_pr={is_pr}", file=fh)
