import sys
if sys.argv[1] == "" or sys.argv == "Firefox":
    print("/usr/local/share/gecko_driver/geckodriver")
else:
    print("/usr/local/share/chrome_driver/chromedriver")
