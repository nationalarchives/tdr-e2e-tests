import sys
is_pr = sys.argv[1] == ""
env = "intg" if is_pr else sys.argv[1]
print(f"::set-output name=environment::{env}")
print(f"::set-output name=account_number_secret::{env.upper()}_ACCOUNT_NUMBER")
print(f"::set-output name=user_admin_secret::{env.upper()}_USER_ADMIN_SECRET")
print(f"::set-output name=backend_checks_secret::{env.upper()}_BACKEND_CHECKS_SECRET")
print(f"::set-output name=title_environment::{env.title()}")
print(f"::set-output name=is_pr::{is_pr}")
