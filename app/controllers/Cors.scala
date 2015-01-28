package controllers

import play.api.mvc._

object Cors extends Controller {

  def preflight(all: String) = Action {
    Ok("").withHeaders(
      "Access-Control-Allow-Origin" -> "*",
      "Allow" -> "*",
      "Access-Control-Allow-Methods" -> "POST, GET, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "x-requested-with, Content-Type, token, origin, if-none-match, authorization, accept, client-security-token")
  }
}