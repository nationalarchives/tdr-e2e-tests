import sys
is_pr = len(sys.argv) == 1
env = "intg" if is_pr else sys.argv[1]
print(f"::set-output name=environment::{env}")
print(f"::set-output name=account_number_secret::{env.upper()}_ACCOUNT_NUMBER")
print(f"::set-output name=title_environment::{env.title()}")
print(f"::set-output name=is_pr::{is_pr}")
