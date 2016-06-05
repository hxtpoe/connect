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

  def get(userId: UserId, year: Int)(day: Int): Future[Option[List[Post]]] = {
    for {
      posts <- bucket.get[Option[JsObject]](s"user::$userId::timelines::$year::$day")
    } yield {
      posts match {
        case Some(posts) => Some(posts.get.\("posts").as[List[Post]])
        case None => None
      }
    }
  }

  def firstNotEmpty(userId: UserId, year: Int)(day: Int): Future[notEmptyTimeLineResultType] = {
    val prom = Promise[notEmptyTimeLineResultType]

    def recurse(userId: UserId, year: Int)(day: Int): Future[Object] = {
      for {
        p <- get(userId, year)(day)
      } yield {
        p match {
          // @ToDo That Some(Nil) is funny..
          case Some(posts: List[Post]) if posts.nonEmpty => prom.success((posts, day))
          case Some(posts: List[Post]) if posts == Nil => recurse(userId, year)(day - 1)
          case None if day > 0 => recurse(userId, year)(day - 1)
          case None if day == 0 => prom.failure(new Exception("I went through all year and there is no timeline to show!"))
        }
      }
    }

    recurse(userId, year)(day)
    prom.future
  }


  def currentWeekTimeline(userId: UserId) = {
    val numbersOfDaysInLastWeek = List.range(1 + 7 * (currentDayOfYear / 7), currentDayOfYear + 1) match {
      case List() => List.range(currentDayOfYear - 6, currentDayOfYear + 1)
      case x => x
    }

    calc(userId, numbersOfDaysInLastWeek, currentYear, currentWeekOfYear)
  }

  def specifiedTimeline(userId: UserId, year: Int, week: Int) = {
    val numbersOfDaysInTheWeek = List.range((week + 1) * 7 - 6, (week + 1) * 7 + 1).filter(_ <= currentDayOfYear)

    calc(userId, numbersOfDaysInTheWeek, year, week)
  }

  def calc(userId: UserId, numbersOfDaysInLastWeek: List[Int], year: Int, week: Int) = {
    import models.UserIdConvertions.convertToString

    for {
      myFollowees <- User.find(userId).map(_.get.followees).map(_.get)
      users = (myFollowees :+ userId).distinct
    } yield {

      var li: List[Post] = List()
      val futures = users.map(uId => Post.getAll(uId, currentYear, week))

      for {
        list <- Future.sequence(futures)
      } yield {
        list.foreach(_.foreach(element => {
          li = li ::: element
        }))

        val y = li.sortBy(_.createdAt).reverse

        numbersOfDaysInLastWeek.map(numberOfADay => {
          val postsForGivenDay = y.filter({ x =>
            val dt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH) // @ToDo extract to static pattern
            dt.parse(x.createdAt.get)

            new SimpleDateFormat("D", Locale.ENGLISH).format(dt.parse(x.createdAt.get)).toInt == numberOfADay // Tue, 09 Feb 2016 23:23:45 +0000
          })

          val key = userId + s"::timelines::$year::$numberOfADay"

          postsForGivenDay match {
            case Nil => {
              bucket.delete(key)
            }
            case _ => {
              bucket.set(key, Json.obj("posts" ->
                postsForGivenDay
              ))
            }
          }

          postsForGivenDay
        })
      }
    }
  }
}