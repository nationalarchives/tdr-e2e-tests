import sys
import os
with open(os.environ['GITHUB_OUTPUT'], 'a') as fh:
    if len(sys.argv) == 1 or sys.argv[1] == "Firefox":
        print(f"driver=/usr/local/share/gecko_driver/geckodriver", file=fh)
        print(f"browser=firefox", file=fh)
    else:
        print(f"driver=/usr/local/share/chrome_driver/chromedriver", file=fh)
        print(f"browser=chrome", file=fh)
