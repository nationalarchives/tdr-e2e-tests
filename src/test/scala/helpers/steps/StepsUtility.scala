package helpers.steps

import java.nio.file.{Files, Path}
import java.security.MessageDigest
import helpers.keycloak.UserCredentials
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.{By, StaleElementReferenceException, WebDriver}

object StepsUtility {
  def waitForElementTitle(webDriver: WebDriver, title: String, elementClassName: String): Any = {
    new WebDriverWait(webDriver, 180)
      .ignoring(classOf[StaleElementReferenceException])
      /*Ignore stale references exceptions.
      These seem to happen when Selenium selects an element which then disappears when the user is redirected to the next page,
      such as from the upload page to the file checks page. In this case, we only want to check the element on the second page,
      so it doesn't matter if the same element on the first page has disappeared.*/
      .until((driver: WebDriver) => {
        val panel = webDriver.findElement(By.className(elementClassName)).getText
        panel == title
      })
  }

  def userLogin(webDriver: WebDriver, userCredentials: UserCredentials): Unit = {
    enterUserCredentials(webDriver, userCredentials)
    val clickableElement = webDriver.findElement(By.cssSelector("[name='login']"))
    clickableElement.click()
  }

  def enterUserCredentials(webDriver: WebDriver, userCredentials: UserCredentials): Unit = {
    val userNameElement = webDriver.findElement(By.cssSelector("[name='username']"))
    val passwordElement = webDriver.findElement(By.cssSelector("[name='password']"))

    userNameElement.sendKeys(userCredentials.userName)
    passwordElement.sendKeys(userCredentials.password)
  }

  def elementHasClassHide(id: String, webDriver: WebDriver): Boolean = {
    val element = webDriver.findElement(By.cssSelector(s"#$id"))
    element.getAttribute("class").contains("hide")
  }

  def elementHasClassDisabled(id: String, webDriver: WebDriver): Boolean = {
    val element = webDriver.findElement(By.cssSelector(s"#$id"))
    element.getAttribute("class").contains("govuk-button--disabled")
  }

  def elementHasAttributeHidden(id: String, webDriver: WebDriver): Boolean = {
    val element = webDriver.findElement(By.cssSelector(s"#$id"))
    Option(element.getAttribute("hidden")).isDefined
  }

  def calculateTestFileChecksum(filePath: Path): String = {
    val messageDigester: MessageDigest = MessageDigest.getInstance("SHA-256")

    val arr = Files readAllBytes filePath
    val checksum = messageDigester digest arr
    checksum.map("%02x" format _).mkString
  }
}
