package controllers

import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FeedController extends Controller {
  def feed(owner: String, author: String, post: String, time: Int) =
    Action.async { request =>
      Future {
//        Feed.create(owner, author, post, time)
        Ok("created")
      }
    }
}