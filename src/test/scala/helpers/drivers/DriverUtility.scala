package helpers.drivers

import com.typesafe.config.{Config, ConfigFactory}
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
import org.openqa.selenium.html5.WebStorage
import org.openqa.selenium.remote.{LocalFileDetector, RemoteWebDriver}

import java.net.URL

object DriverUtility {
  val configuration: Config = ConfigFactory.load()
  val firefoxBinaryLocation: String = configuration.getString("firefox.binary.location")

  val chromeOptions: ChromeOptions = {
    val chromeOptions: ChromeOptions = new ChromeOptions

    chromeOptions.setHeadless(true)
    chromeOptions.addArguments("--no-sandbox")
    chromeOptions.addArguments("--disable-dev-shm-usage")
    chromeOptions.addArguments("--verbose")
    chromeOptions
  }

  val firefoxOptions: FirefoxOptions = {
    val firefoxOptions = new FirefoxOptions
    firefoxOptions.setHeadless(true)
    firefoxOptions.setBinary(firefoxBinaryLocation)
    firefoxOptions.addArguments("--no-sandbox")
    firefoxOptions.addArguments("--disable-dev-shm-usage")
    firefoxOptions.addArguments("--verbose")
    firefoxOptions
  }

  def initDriver: RemoteWebDriver = {
    val options = System.getProperty("browser") match {
      case "Chrome" => chromeOptions
      case "Firefox" => firefoxOptions
    }
    val nodeUrl = System.getProperty("selenium.node.url")
    val remoteWebDriver = new RemoteWebDriver(new URL(nodeUrl), options)
    remoteWebDriver.setFileDetector(new LocalFileDetector())
    remoteWebDriver
  }
}
