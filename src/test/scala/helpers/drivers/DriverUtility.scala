package helpers.drivers

import com.typesafe.config.{Config, ConfigFactory}
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
import org.openqa.selenium.html5.WebStorage
import org.openqa.selenium.remote.RemoteWebDriver

import java.net.URL

object DriverUtility {
  val driverLocation: String = System.getenv("DRIVER_LOCATION")

  val configuration: Config = ConfigFactory.load()
  val firefoxBinaryLocation: String = configuration.getString("firefox.binary.location")

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
    firefoxOptions.setBinary(firefoxBinaryLocation)
    firefoxOptions.addArguments("--no-sandbox")
    firefoxOptions.addArguments("--disable-dev-shm-usage")
    firefoxOptions.addArguments("--verbose")
    System.setProperty("webdriver.gecko.driver", driverLocation)

    firefoxOptions
  }

  def initDriver: RemoteWebDriver = {
    ConfigFactory.load.getString("browser") match {
      case "chrome" => new ChromeDriver(chromeOptions)
      case "firefox" => new RemoteWebDriver(new URL("http://localhost:9001"), firefoxOptions)
    }
  }
}
