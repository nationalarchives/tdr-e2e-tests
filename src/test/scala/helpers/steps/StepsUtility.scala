package helpers.steps

import org.openqa.selenium.chrome.ChromeOptions

object StepsUtility {

  val getChromeOptions: ChromeOptions = {
    val chromeOptions: ChromeOptions = new ChromeOptions
    val chromeDriverLocation = System.getenv("CHROME_DRIVER")

    chromeOptions.setHeadless(true)
    chromeOptions.addArguments("--no-sandbox")
    chromeOptions.addArguments("--disable-dev-shm-usage")
    chromeOptions.addArguments("--verbose")
    System.setProperty("webdriver.chrome.driver", chromeDriverLocation)

    chromeOptions
  }
}
