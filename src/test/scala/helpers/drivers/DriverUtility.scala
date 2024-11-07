package helpers.drivers

import com.typesafe.config.{Config, ConfigFactory}
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
import org.openqa.selenium.html5.WebStorage
import org.openqa.selenium.remote.RemoteWebDriver

object DriverUtility {
  val driverLocation: String = System.getenv("DRIVER_LOCATION")

  val configuration: Config = ConfigFactory.load()
  val firefoxBinaryLocation: String = configuration.getString("firefox.binary.location")

  val chromeOptions: ChromeOptions = {
    val chromeOptions: ChromeOptions = new ChromeOptions

    chromeOptions.addArguments("--headless=new")
    chromeOptions.addArguments("--no-sandbox")
    chromeOptions.addArguments("--disable-dev-shm-usage")
    chromeOptions.addArguments("--verbose")
//    chromeOptions.setExperimentalOption("download.default_directory", "/tmp")
    System.setProperty("webdriver.chrome.driver", driverLocation)

    chromeOptions
  }

  val firefoxOptions: FirefoxOptions = {
    val firefoxOptions = new FirefoxOptions
    firefoxOptions.addPreference("browser.download.dir", "/tmp")
    firefoxOptions.addPreference("browser.download.useDownloadDir", true)
    firefoxOptions.addPreference("browser.download.folderList", 2)

    firefoxOptions.setBinary(firefoxBinaryLocation)
    firefoxOptions.addArguments("-headless")
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
