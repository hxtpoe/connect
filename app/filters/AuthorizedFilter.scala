package filters

import play.api.mvc._
import play.api.mvc.Results.Forbidden
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object AuthorizedFilter {
  def apply(actionNames: String*) = new AuthorizedFilter(actionNames)
}

class AuthorizedFilter(actionNames: Seq[String]) extends Filter {
  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    if(authorizationRequired(request)) {
      Future(Forbidden("auth required"))
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
