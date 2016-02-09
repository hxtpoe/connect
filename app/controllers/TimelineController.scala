package controllers

import models.{DataPartitionable, Timeline}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TimelineController extends Controller with DataPartitionable  {
  def timeline(userId: Int, year: Int, day: Int) = Action.async {
    for (posts <- Timeline.get(userId, year, day))
      yield {
        Ok(Json.obj(
          "posts" -> posts,
          "next-page" -> routes.TimelineController.timeline(userId, previousPageYearNumber(year, day), previousPageDayNumber(year, day)).toString()
        ))
      }
  }

  def latestTimeline(userId: Int) = Action.async {
    for (posts <- Timeline.get(userId, currentYear, currentDayOfYear))
      yield {
        Ok(Json.obj(
          "posts" -> posts,
          "next-page" -> routes.TimelineController.timeline(userId, previousPageYearNumber(currentYear, currentDayOfYear), previousPageDayNumber(currentYear, currentDayOfYear)).toString()
        ))
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