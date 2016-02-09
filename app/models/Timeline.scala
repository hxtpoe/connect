package models

import java.text.SimpleDateFormat
import java.util.{Date, Locale, TimeZone}

import datasources.couchbase
import play.api.libs.json._

import scala.collection.immutable.List
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Timeline {
  implicit val bucket = couchbase.bucket

  def currentWeekTimeline(userId: String) = {
    val numbersOfDaysInLastWeek = List.range(1 + 7 * (currentDayOfYear / 7), currentDayOfYear + 1) match {
      case List() => List.range(currentDayOfYear - 6, currentDayOfYear + 1)
      case x => x
    }
    calc(userId, numbersOfDaysInLastWeek, currentYear, currentWeekOfYear)
  }

  def specifiedTimeline(userId: String, year: Int, week: Int) = {
    val numbersOfDaysInLastWeek = List.range(week * 7 - 6, week * 7 + 1)
    calc(userId, numbersOfDaysInLastWeek, year, week)
  }

  def calc(userId: String, numbersOfDaysInLastWeek: List[Int], year: Int, week: Int) = {
    val id = userId.drop(12) // change to UserIdType!

    for {
      users <- User.find(id).map(_.get.followees).map(_.get)
    } yield {

      var li: List[Post] = List()
      val futures = users.map(Post.getAll(_, currentYear, week))

      for {
        list <- Future.sequence(futures)
      } yield {
        list.map(_.foreach(element => {
          li = li :+ element
        }))

        val y = li.sortBy(_.createdAt).reverse

        //      println(numbersOfDaysInLastWeek)

        numbersOfDaysInLastWeek.map(numberOfADay => {
          val postsForGivenDay = y.filter({ x =>
            val dt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(x.createdAt.get)
            new SimpleDateFormat("D", Locale.ENGLISH).format(dt).toInt == numberOfADay
          })

          postsForGivenDay match {
            case Nil => {}
            case _ => bucket.set(s"user::$id::timelines::$year::$numberOfADay", Json.obj("posts" ->
              postsForGivenDay
            ))
          }

          postsForGivenDay
        })
      }
    }
  }


  def get(userId: Int, year: Int, day: Int): Future[List[Post]] = {
    for (
      posts <- bucket.get[Option[JsObject]](s"user::$userId::timelines::$year::$day")
    ) yield {
      posts match {
        case Some(posts) => posts.get.\("posts").as[List[Post]]
        case None => List()
      }
    }
  }

  ///

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
