package controllers

import models.{Tweet => TweetModel}
import play.api.libs.json.{JsError, _}
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Tweet extends Controller {
  def findTweetsByUsername(username: String) = Action.async { request =>
    TweetModel.findAllTweetsByUsername(username) flatMap {
      list => Future {
        Ok(Json.toJson(list))
      }
    }
  }

  def create = Action.async(BodyParsers.parse.json) {
    request =>
      val someJson = request.body.validate[TweetModel]

      someJson.fold(
        errors => {
          Future(BadRequest(Json.obj("status" -> "OK", "message" -> JsError.toFlatJson(errors))))
        },
        json => {
          TweetModel.increment.map {
            case i: Int => {
              TweetModel.create(i, json)
              Ok(Json.obj("status" -> "OK", "message" -> ("tweet saved." + i.toString()))).withHeaders(LOCATION -> ("tweet id: " + i))
            }
          }
        }
      )
  }

  def find(id: Long) = Action.async {
    TweetModel.find(id).map {
      t =>
        t match {
          case None => NotFound("Not found tweet " + id)
          case _ =>
            Ok(Json.toJson(t))
        }
    }
  }

  def update(id: Long) = TODO

  def delete(id: Long) = TODO
}