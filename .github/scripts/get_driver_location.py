import sys
environment = sys.argv[2]
if len(sys.argv) == 1 or sys.argv[1] == "Firefox":
    print(f"::set-output name=driver::/opt/geckodriver")
    print(f"::set-output name=lambda-name::tdr-e2e-tests-{environment}")
else:
    print(f"::set-output name=driver::/opt/chromedriver/chromedriver")
    print(f"::set-output name=lambda-name::tdr-e2e-tests-chrome-{environment}")

