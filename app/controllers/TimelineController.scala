package controllers

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone, Locale}

import models.Post
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection.immutable.List
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TimelineController extends Controller {

  def timeline(userId: Int, year: Int, week: Int) = Action.async {
    // refactor!
    val users = (1 to 20).map(s"user::" + _)

    var li: List[Post] = List()
    val futures = users.map(Post.getAll(_, year, week))

    for {
      list <- Future.sequence(futures)
    } yield {
      list.map(_.foreach(element => {
        li = li :+ element
      }))
      Ok(Json.toJson(li.sortBy(_.createdAt).reverse))
    }
  }

  def latestTimeline(userId: Int) = Action.async {
    val weekOfTheYear = new SimpleDateFormat("w", Locale.ENGLISH)
    val year = new SimpleDateFormat("YYYY", Locale.ENGLISH)
    year.setTimeZone(TimeZone.getTimeZone("UTC+0"))
    weekOfTheYear.setTimeZone(TimeZone.getTimeZone("UTC+0"))
    val now = new Date()
    val users = (1 to 100).map(s"user::" + _)

    var li: List[Post] = List()
    val futures = users.map(Post.getAll(_, year.format(now).toInt, weekOfTheYear.format(now).toInt))

    for {
      list <- Future.sequence(futures)
    } yield {
      list.map(_.foreach(element => {
        li = li :+ element
      }))
      Ok(Json.toJson(li.sortBy(_.createdAt).reverse))
    }
  }
}