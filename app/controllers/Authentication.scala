package controllers

import akka.actor.ActorSystem
import akka.util.Timeout
import play.api.Play.current
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.libs.ws.WS
import play.api.mvc._
import utils.QueryStringParser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Authentication extends Controller {
  val system: ActorSystem = ActorSystem("authentication")
  implicit val timeout = Timeout.intToTimeout(60 * 1000)

  case class AuthData(redirectUri: String, code: String)

  object AuthData {
    implicit val fmt = Json.format[AuthData]

    def fromRequest(implicit request: Request[JsValue]) =
      request.body.validate[AuthData]
  }

  def facebook() = Action.async(parse.json) { implicit request =>
    val clientId = "781458691925425"
    val clientSecret = "abc"
    val accessTokenUrl = "https://graph.facebook.com/oauth/access_token"
    val graphApiUrl = "https://graph.facebook.com/me"
    AuthData.fromRequest match {
      case JsSuccess(data, _) =>
        //        Step 1. Exchange authorization code for access token.
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

        //        Step 2. Retrieve information about the current user.
        val profile = accessToken.flatMap { t =>
          WS.url(graphApiUrl)
            .withQueryString("access_token" -> t)
            .get
        }
//                  .map(_.json.validate[FacebookProfile]).map(_.get)

        //        Step 3. update/merge/create our data and fetch user
        //        val user = profile.flatMap(userService ? CreateOrMergeUser(_)).mapTo[User]

        //        Step 4. Generate JWT and send it back to client

        Future(
          println(profile)
        )
        Future(Ok(Json.obj("token" -> "xxx")))

      //        token map { t =>
      //          Created(Json.obj("token" -> t))
      //        } recover {
      //          case e: Exception =>
      //            println(e)
      //            InternalServerError
      //        }
      case e: JsError =>
        Future.successful{BadRequest(JsError.toFlatJson(e))}
    }
  }
}