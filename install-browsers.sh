#!/usr/bin/bash

chrome_version='101.0.4951.67'
chrome_link_version='101049'
chrome_driver="101.0.4951.41"
firefox_version="100.0.2"
gecko_driver="0.31.0"

mkdir -p "/opt/firefox"
curl -Lo "/opt/firefox/firefox-$firefox_version.tar.bz2" "http://ftp.mozilla.org/pub/firefox/releases/$firefox_version/linux-x86_64/en-US/firefox-$firefox_version.tar.bz2"
tar -jxf "/opt/firefox/firefox-$firefox_version.tar.bz2" -C "/opt/firefox/"
mv "/opt/firefox/firefox" "/opt/firefox/firefox-temp"
mv /opt/firefox/firefox-temp/* /usr/bin/
rm -rf "/opt/firefox/firefox-$firefox_version.tar.bz2"

curl -Lo "/opt/geckodriver.tar.gz" "https://github.com/mozilla/geckodriver/releases/download/v$gecko_driver/geckodriver-v$gecko_driver-linux64.tar.gz"
tar -zxf "/opt/geckodriver.tar.gz" -C "/opt/"
chmod +x "/opt/geckodriver"
rm -rf "/opt/geckodriver.tar.gz"

mkdir -p "/opt/chrome/$chrome_version"
curl -Lo "/opt/chrome/$chrome_version/chrome-linux.zip" "https://www.googleapis.com/download/storage/v1/b/chromium-browser-snapshots/o/Linux_x64%2F$chrome_link_version%2Fchrome-linux.zip?alt=media"
unzip -q "/opt/chrome/$chrome_version/chrome-linux.zip" -d "/opt/chrome/$chrome_version/"
mv /opt/chrome/$chrome_version/chrome-linux/* /usr/bin/
rm -rf /opt/chrome/$chrome_version/chrome-linux "/opt/chrome/$chrome_version/chrome-linux.zip"

mkdir -p "/opt/chromedriver/"
curl -Lo "/opt/chromedriver/chromedriver_linux64.zip" "https://chromedriver.storage.googleapis.com/$chrome_driver/chromedriver_linux64.zip"
unzip -q "/opt/chromedriver/chromedriver_linux64.zip" -d "/opt/chromedriver/"
chmod +x "/opt/chromedriver/chromedriver"
rm -rf "/opt/chromedriver/chromedriver_linux64.zip"





