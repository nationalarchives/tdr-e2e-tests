package runners

import io.cucumber.junit.{Cucumber, CucumberOptions}
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("classpath:features/"),
  tags = Array("@upload"), //Array("not @Wip", "not @Integration")
  glue = Array("classpath:steps/"),
  plugin = Array("pretty", "html:target/cucumber/html", "json:target/cucumber.json"))
class TestRunner {

}
