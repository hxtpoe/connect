package controllers

import akka.util.Timeout
import models.FacebookProfile
import play.api.Play.current
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.libs.ws.WS
import play.api.mvc._
import utils.QueryStringParser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Authentication extends Controller {
  implicit val timeout = Timeout.intToTimeout(60 * 1000)

  case class AuthData(redirectUri: String, code: String)

  object AuthData {
    implicit val fmt = Json.format[AuthData]

    def fromRequest(implicit request: Request[JsValue]) =
      request.body.validate[AuthData]
  }

  def facebook() = Action.async(parse.json) { implicit request =>
    val clientId = "781458691925425"
    val clientSecret = "842c52c8d8780b38d9545ed88513c786"
    val accessTokenUrl = "https://graph.facebook.com/oauth/access_token"
    val graphApiUrl = "https://graph.facebook.com/me"

    AuthData.fromRequest match {
      case JsSuccess(data, _) =>
        val accessTokenData =
          WS.url(accessTokenUrl).withQueryString(
            "redirect_uri" -> data.redirectUri,
            "code" -> data.code,
            "client_id" -> clientId,
            "client_secret" -> clientSecret)
            .get
            .map(_.body)
            .map(QueryStringParser.parse)
            .map(_.get)

        val accessToken = accessTokenData.map(_("access_token"))

        val profile = accessToken.flatMap { t =>
          WS.url(graphApiUrl)
            .withQueryString("access_token" -> t)
            .get
        }.map(_.json.validate[FacebookProfile]).map(_.get)

        val token = profile.map(FacebookProfile.createOrMerge(_))

        token map (x => Ok(Json.obj("token" -> {
          services.JWTService.generate(x.toString.drop(6)) // @ToDo get rid of this funny type conversion by using drop natural key prefix
        })))

      case e: JsError =>
        Future(BadRequest(JsError.toFlatJson(e)))
    }
  }
}