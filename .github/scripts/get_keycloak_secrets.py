import sys
import boto3

is_pr = len(sys.argv) == 1
env = "intg" if is_pr else sys.argv[1]


def get_client_secret(client_secret_path):
    ssm_client = boto3.client("ssm")
    response = ssm_client.get_parameter(
        Name=client_secret_path,
        WithDecryption=True
    )
    return response["Parameter"]["Value"]


user_admin_secret = get_client_secret(f"/{env}/keycloak/user_admin_client/secret")
backend_checks_secret = get_client_secret(f"/{env}/keycloak/backend_checks_client/secret")

print(f"::set-output name=user_admin_secret::{user_admin_secret}")
print(f"::set-output name=backend_checks_secret::{backend_checks_secret}")
print(f"::add-mask::{backend_checks_secret}")
print(f"::add-mask::{user_admin_secret}")
