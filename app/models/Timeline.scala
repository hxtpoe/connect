package models

import java.text.SimpleDateFormat
import java.util.Locale

import datasources.couchbase
import play.api.libs.json._

import scala.collection.immutable.List
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

object Timeline extends DataPartitionable {
  type notEmptyTimeLineResultType = (List[Post], Int)
  implicit val bucket = couchbase.bucket

  def get(userId: Int, year: Int)(day: Int): Future[Option[List[Post]]] = {
    for {
      posts <- bucket.get[Option[JsObject]](s"user::$userId::timelines::$year::$day")
    } yield {
      posts match {
        case Some(posts) => Some(posts.get.\("posts").as[List[Post]])
        case None => None
      }
    }
  }

  def firstNotEmpty(userId: Int, year: Int)(day: Int): Future[notEmptyTimeLineResultType] = {
    val prom = Promise[notEmptyTimeLineResultType]

    def recurse(userId: Int, year: Int)(day: Int): Future[Object] = {
      for {
        p <- get(userId, year)(day)
      } yield {
        p match {
          case Some(posts: List[Post]) if posts.nonEmpty => prom.success((posts, day))
          case None if day > 0 => recurse(userId, year)(day - 1)
          case None if day == 0 => prom.failure(new Exception("I went through all year and there is no timeline to show!"))
        }
      }
    }

    recurse(userId, year)(day)
    prom.future
  }


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
      val futures = users.map(uId => Post.getAll(uId.drop(6).toInt, currentYear, week))

      for {
        list <- Future.sequence(futures)
      } yield {
        list.map(_.foreach(element => {
          li = li ::: element
        }))

        val y = li.sortBy(_.createdAt).reverse

        numbersOfDaysInLastWeek.map(numberOfADay => {
          val postsForGivenDay = y.filter({ x =>
            val dt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            dt.parse(x.createdAt.get)

            new SimpleDateFormat("D", Locale.ENGLISH).format(dt.parse(x.createdAt.get)).toInt == numberOfADay // Tue, 09 Feb 2016 23:23:45 +0000
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
}