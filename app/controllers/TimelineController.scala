package controllers

import models.{UserId, DataPartitionable, Timeline}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TimelineController extends Controller with DataPartitionable {
  def timeline(userId: Int, year: Int, day: Int) = Action.async {
    notEmptyTimeline(UserId(userId), year, day)
  }

  def latestTimeline(userId: Int) = Action.async {
    notEmptyTimeline(UserId(userId), currentYear, currentDayOfYear)
  }

  private def notEmptyTimeline(userId: UserId, year: Int, day: Int): Future[Result] = {
    val future = for {
      (posts, day) <- Timeline.firstNotEmpty(userId, currentYear)(day)
      postsWithBaseUserProfile <- joiners.UserJoiner(posts)
    } yield {
      Ok(Json.obj(
        "posts" -> postsWithBaseUserProfile,
        "nextPage" -> routes.TimelineController.timeline(userId.id, previousPageYearNumber(currentYear, day), previousPageDayNumber(currentYear, day)).toString()
      ))
    }
    future.recover {
      case ex: Exception => NotFound(ex.getMessage)
    }
  }

  def calcLatestTimeline(userId: Int) = Action.async {
    Timeline.currentWeekTimeline(UserId(userId))

    Future {
      Ok("backend action")
    }
  }

  def specifiedTimeline(userId: Int, year: Int, week: Int) = Action.async {
    Timeline.specifiedTimeline(UserId(userId), year, week)

    Future {
      Ok("backend action")
    }
  }
}