package models

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import com.couchbase.client.protocol.views.{ComplexKey, Query, Stale}
import datasources.{couchbase => cb}
import org.reactivecouchbase.client.OpResult
import play.Play
import play.api.libs.json._
import play.cache.Cache

import scala.collection.immutable.List
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Post(
                 id: Option[String],
                 message: String,
                 timestamp: Option[Int],
                 uuid: Option[String],
                 userId: Option[String],
                 createdAt: Option[String],
                 docType: Option[String]
               ) {
}

object Post {
  implicit val bucket = cb.bucket
  implicit val fmt: Format[Post] = Json.format[Post]

  val hotView = (if (Play.application().isDev) "dev_") + "hotPosts"
  val coldView = (if (Play.application().isDev) "dev_") + "posts"

  val timestamp: Long = System.currentTimeMillis / 1000
  val date = new java.util.Date()

  def find(id: String): Future[Option[Post]] = {
    bucket.get(id)
  }

  def getAll(userId: String, year: Int, week: Int): Future[List[Post]] = {
    val key = s"$userId::posts::$year::$week"
//    val cached = Cache.get(key).asInstanceOf[List[Post]]

//    if(cached != null) {
//      Future(cached)
//    } else {
      for (
        posts <- bucket.get[Option[JsObject]](s"$userId::posts::$year::$week")
      ) yield {
        posts match {
          case Some(posts) => {
            val test = posts.get.\("posts").as[List[Post]].sortBy(_.createdAt).reverse
            Cache.set(key, test)
            test
          }
          case None => List()
        }
      }
//    }
  }

  def create(id: String, userId: String, post: Post) = {
    val now = new Date()
    val dateFormatGmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH) // RFC 2822
    dateFormatGmt.format(now)

    val documentId = userId + "::posts::" + year + "::" + weekOfYear

    for (
      storedPost <- bucket.get[JsObject](documentId)
    ) yield {

      storedPost.getOrElse(JsArray()) match {
        case array if array == JsArray() => add(post, id, userId, documentId, dateFormatGmt.format(now)) // create
        case posts => append(storedPost.get, post, id, userId, documentId, dateFormatGmt.format(now)) //append
      }
    }
  }

  def add(post: Post, uuid: String, userId: String, docId: String, stringDate: String): Future[OpResult] = {
    val newPost =
      Json.obj(// why is this Json, not an object
        "posts" ->
          Json.arr(
            Json.obj(
              "uuid" -> uuid,
              "message" -> post.message,
              "userId" -> userId
            ) ++ Json.obj(
              "createdAt" -> stringDate.toString,
              "docType" -> "post")))

    bucket.add[JsValue](docId, newPost)
  }

  def append(storedPost: JsObject, post: Post, uuid: String, userId: String, docId: String, stringDate: String): Future[OpResult] = {
    val newPost =
      Json.obj(
        "uuid" -> uuid,
        "message" -> post.message,
        "userId" -> userId
      ) ++ Json.obj(
        "createdAt" -> stringDate.toString,
        "docType" -> "post")

    val newListOfPosts = (storedPost \ "posts").as[JsArray] :+ newPost

    bucket.set[JsObject](docId,
      Json.obj(
        "posts" -> newListOfPosts
      )
    )
  }

  def getCached(userId: String): Future[Option[List[Post]]] = {
    bucket.get[List[Post]](userId)
  }

  def getAndCache(ownerId: String, usersIds: List[String]): Future[List[Post]] = {
    for {
      timeline <- calculateTimeline(usersIds)
    } yield {
      bucket.set(ownerId, timeline)
      timeline
    }
  }

  def calculateTimeline(usersIds: List[String]): Future[List[Post]] = {
    var li: List[Post] = List()
    val futures = usersIds.map(fastUserPosts)

    for {
      list <- Future.sequence(futures)
    } yield {
      list.map(_.foreach(element => {
        li = li :+ element
      }))
      li
        .sortBy(_.timestamp)
        .reverse
    }
  }

  def fastUserPosts(userId: String): Future[List[Post]] =
    bucket.get[List[Post]]("posts_" + userId).flatMap { cached =>
      cached.map(list => Future.successful(list))
        .getOrElse(userPosts(userId))
    }

  def userPosts(userId: String): Future[List[Post]] = {
    val result = bucket.find[Post](hotView, "byAuthorWithTimestamp")(
      new Query()
        .setRangeStart(ComplexKey.of(JsArray(Seq(JsString(userId), JsNumber(1353930689)))))
        .setRangeEnd(ComplexKey.of(JsArray(Seq(JsString(userId), JsNumber(1653930689)))))
        .setDescending(false)
        .setIncludeDocs(true)
        .setLimit(25)
        .setInclusiveEnd(true)
        .setStale(Stale.OK))

    result map (cached => cached match {
      case list => bucket.set("posts_" + userId, list)
    })

    result
  }

  def weekOfYear = dayOfYear / 7

  def dayOfYear: Int = simpleDataFormat("D")

  def year: Int = simpleDataFormat("YYYY")

  def simpleDataFormat(code: String): Int = {
    val now = new Date()
    val day = new SimpleDateFormat(code, Locale.ENGLISH)
    day.format(now).toInt
  }
}

trait DataPartitionable {
  def currentWeekOfYear = currentDayOfYear / 7

  def currentDayOfYear: Int = simpleDataFormat("D")

  def currentYear: Int = simpleDataFormat("YYYY")

  def simpleDataFormat(code: String): Int = {
    val now = new Date()
    val day = new SimpleDateFormat(code, Locale.ENGLISH)
    day.format(now).toInt
  }

  def previousPageDayNumber(year: Int, day: Int): Int = {
    day - 1 match {
      case 0 => if ((year - 1) % 4 == 0 && (year - 1) % 400 == 0) 366 else 365
      case _ => day - 1
    }
  }

  def previousPageYearNumber(year: Int, day: Int): Int = {
    day - 1 match {
      case 0 => year - 1
      case _ => year
    }
  }

  def dayOfYear: Int = simpleDataFormat("D")

  def year: Int = simpleDataFormat("YYYY")
}