package helpers.drivers

import com.typesafe.config.ConfigFactory
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
import org.openqa.selenium.html5.WebStorage
import org.openqa.selenium.remote.RemoteWebDriver

object DriverUtility {

  val driverLocation: String = System.getenv("DRIVER_LOCATION")

  val chromeOptions: ChromeOptions = {
    val chromeOptions: ChromeOptions = new ChromeOptions

    chromeOptions.setHeadless(true)
    chromeOptions.addArguments("--no-sandbox")
    chromeOptions.addArguments("--disable-dev-shm-usage")
    chromeOptions.addArguments("--verbose")
    System.setProperty("webdriver.chrome.driver", driverLocation)

    chromeOptions
  }

  val firefoxOptions: FirefoxOptions = {
    val firefoxOptions = new FirefoxOptions
    firefoxOptions.setHeadless(true)
    firefoxOptions.setBinary("/usr/bin/firefox")
    firefoxOptions.addArguments("--no-sandbox")
    firefoxOptions.addArguments("--disable-dev-shm-usage")
    firefoxOptions.addArguments("--verbose")
    System.setProperty("webdriver.gecko.driver", driverLocation)

    firefoxOptions
  }

  def initDriver: RemoteWebDriver with WebStorage = {
    ConfigFactory.load.getString("browser") match {
      case "chrome" => new ChromeDriver(chromeOptions)
      case "firefox" => new FirefoxDriver(firefoxOptions)
    }

  }
}
