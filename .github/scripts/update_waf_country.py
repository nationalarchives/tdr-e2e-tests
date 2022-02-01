import boto3
import sys

client = boto3.client("wafv2")
ip_set_id = client.list_ip_sets(Scope='REGIONAL')['IPSets'][0]['ARN']
rule_groups = client.list_rule_groups(Scope='REGIONAL')['RuleGroups'][0]
rule_group_id = rule_groups['Id']
lock_token = rule_groups['LockToken']
country_codes = ["GB", "US"] if sys.argv[1] == "ADD" else ["GB"]
client.update_rule_group(
    Name='waf-rule-group',
    Scope='REGIONAL',
    Id=rule_group_id,
    Description='Restrict acces to TDR Apps',
    Rules=[
        {
            "Name": "waf-rule-restricted-uri",
            "Priority": 20,
            "Statement": {
                "AndStatement": {
                    "Statements": [
                        {
                            "ByteMatchStatement": {
                                "SearchString": "YXV0aC9hZG1pbg==",
                                "FieldToMatch": {
                                    "UriPath": {}
                                },
                                "TextTransformations": [
                                    {
                                        "Priority": 10,
                                        "Type": "NONE"
                                    }
                                ],
                                "PositionalConstraint": "CONTAINS"
                            }
                        },
                        {
                            "NotStatement": {
                                "Statement": {
                                    "IPSetReferenceStatement": {
                                        "ARN": ip_set_id
                                    }
                                }
                            }
                        }
                    ]
                }
            },
            "Action": {
                "Block": {}
            },
            "VisibilityConfig": {
                "SampledRequestsEnabled": False,
                "CloudWatchMetricsEnabled": False,
                "MetricName": "url-restrictions"
            }
        },
        {
            "Name": "geo-match-restrictions",
            "Priority": 30,
            "Statement": {
                "GeoMatchStatement": {
                    "CountryCodes": country_codes
                }
            },
            "Action": {
                "Allow": {}
            },
            "VisibilityConfig": {
                "SampledRequestsEnabled": False,
                "CloudWatchMetricsEnabled": False,
                "MetricName": "waf-geo-match"
            }
        }
    ],
    VisibilityConfig={
        'SampledRequestsEnabled': False,
        'CloudWatchMetricsEnabled': False,
        'MetricName': 'string'
    },
    LockToken=lock_token,
)
