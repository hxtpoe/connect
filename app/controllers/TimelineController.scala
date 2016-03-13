package controllers

import models.{DataPartitionable, Timeline}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TimelineController extends Controller with DataPartitionable {
  def timeline(userId: Int, year: Int, day: Int) = Action.async {
    notEmptyTimeline(userId, year, day)
  }

  def latestTimeline(userId: Int) = Action.async {
    notEmptyTimeline(userId, currentYear, currentDayOfYear)
  }

  private def notEmptyTimeline(userId: Int, year: Int, day: Int): Future[Result] = {
    val future = for {
      (posts, day) <- Timeline.firstNotEmpty(userId, currentYear)(day)
    } yield {
      Ok(Json.obj(
        "posts" -> posts,
        "nextPage" -> routes.TimelineController.timeline(userId, previousPageYearNumber(currentYear, day), previousPageDayNumber(currentYear, day)).toString()
      ))
    }
    future.recover {
      case ex: Exception => NotFound(ex.getMessage)
    }
  }

  def calcLatestTimeline(userId: Int) = Action.async {
    Timeline.currentWeekTimeline(s"user::$userId")

    Future {
      Ok("backend action")
    }
  }

  def specifiedTimeline(userId: Int, year: Int, week: Int) = Action.async {
    Timeline.specifiedTimeline(s"user::$userId", year, week)

    Future {
      Ok("backend action")
    }
  }
}