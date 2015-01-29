package filters

import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CorsFilter extends Filter {
  override def apply(next: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    next(request).map {
      result => result.withHeaders("Access-Control-Allow-Origin" -> "*",
        "Allow" -> "*",
        "Access-Control-Allow-Methods" -> "POST, GET, OPTIONS, PUT, DELETE",
        "Access-Control-Allow-Headers" -> "x-requested-with, Content-Type, token, if-none-match, origin, authorization, accept, client-security-token"
      )
    }
  }
}