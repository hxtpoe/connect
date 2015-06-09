package controllers

import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HealthCheckController extends Controller {
  def hc() = Action.async {
    Future {Ok("ok")}
  }
}