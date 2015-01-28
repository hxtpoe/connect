package filters

import com.nimbusds.jose.crypto.MACVerifier
import play.api.mvc._
import play.api.mvc.Results.Forbidden
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object AuthorizedFilter {
  def apply(actionNames: String*) = new AuthorizedFilter(actionNames)
}

class AuthorizedFilter(actionNames: Seq[String]) extends Filter {
  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    if (authorizationRequired(request)) {
      val token = request.headers.get("token").getOrElse(false)
      val validator = new MACVerifier("xx")

      token match {
        case token: String => {
          next(request)
        }
        case _ => Future(Forbidden("token required"))
      }
    }
    else {
      next(request)
    }
  }

  private def authorizationRequired(request: RequestHeader) = {
    val actionInvoked: String = request.tags.getOrElse(play.api.Routes.ROUTE_COMMENTS, "")
    actionNames.contains(actionInvoked)
  }
}
