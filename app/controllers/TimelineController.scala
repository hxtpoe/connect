package controllers

import java.text.SimpleDateFormat
import java.util.{Date, Locale, TimeZone}

import models.Timeline
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TimelineController extends Controller {
  def timeline(userId: Int, year: Int, day: Int) = Action.async {
    for (posts <- Timeline.get(userId, year, day))
      yield {
        Ok(Json.toJson(posts))
      }
  }

  def latestTimeline(userId: Int) = Action.async {
    for (posts <- Timeline.get(userId, currentYear, currentDayOfYear))
      yield {
        Ok(Json.toJson(posts))
      }
  }

  def calcLatestTimeline(userId: Int) = Action.async {
    Timeline.currentWeekTimeline(userId)

    Future {
      Ok("backend action runned")
    }
  }

  def specifiedTimeline(userId: Int, year: Int, week: Int) = Action.async {
    Timeline.specifiedTimeline(userId, year, week)

    Future {
      Ok("backend action runned")
    }
  }

  def currentWeekOfYear = currentDayOfYear / 7

  def currentDayOfYear: Int = simpleDataFormat("D")

  def currentYear: Int = simpleDataFormat("YYYY")

  def simpleDataFormat(code: String): Int = {
    val now = new Date()
    val day = new SimpleDateFormat(code, Locale.ENGLISH)
    day.setTimeZone(TimeZone.getTimeZone("UTC+0"))
    day.format(now).toInt
  }
}