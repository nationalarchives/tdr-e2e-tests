import sys
if len(sys.argv) == 1 or sys.argv[1] == "Firefox":
    print(f"::set-output name=driver::/usr/local/share/gecko_driver/geckodriver")
    print(f"::set-output name=browser::firefox")
else:
    print(f"::set-output name=driver::/usr/local/share/chrome_driver/chromedriver")
    print(f"::set-output name=browser::chrome")

