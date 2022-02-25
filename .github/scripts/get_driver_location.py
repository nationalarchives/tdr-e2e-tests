import sys
if len(sys.argv) == 1 or sys.argv[1] == "Firefox":
    print("/usr/local/share/gecko_driver/geckodriver")
else:
    print("/usr/local/share/chrome_driver/chromedriver")
