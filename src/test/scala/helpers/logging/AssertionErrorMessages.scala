package helpers.logging

object AssertionErrorMessages {
  def elementMissingMessage(selector: String): String = s"'$selector' element missing"

  def doesNotMatchExpected(actual: String, expected: String) = s"actual: $actual, expected: $expected"

  def doesNotContain(actual: String, contains: String) = s"'$actual' does not contain '$contains'"
}
