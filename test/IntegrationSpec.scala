import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

  "Application" should {

    "work from within a browser" in new WithBrowser {

      browser.goTo("http://localhost:" + port + "/api-docs")

      browser.pageSource must contain("{\"apiVersion\":\"0.2\",\"swaggerVersion\":\"1.2\",\"apis\":[{\"path\":\"/posts\",\"description\":\"Operations about posts\"}]}")
    }
  }
}
