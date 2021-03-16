package helpers.steps

import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest
import java.util.UUID

import helpers.graphql.GraphqlUtility
import helpers.keycloak.UserCredentials
import org.openqa.selenium.{By, WebDriver}

object StepsUtility {

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
