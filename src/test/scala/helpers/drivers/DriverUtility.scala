package helpers.drivers

import com.typesafe.config.{Config, ConfigFactory}
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
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
    val nodeIp = System.getProperty("selenium.node.ip")
    val remoteWebDriver = new RemoteWebDriver(new URL(s"http://$nodeIp:4444"), options)

    remoteWebDriver.setFileDetector(new LocalFileDetector())
    remoteWebDriver
  }
}
