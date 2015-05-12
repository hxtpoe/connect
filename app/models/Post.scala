package models

import com.couchbase.client.protocol.views.{ComplexKey, Query, Stale}
import datasources.{couchbase => cb}
import org.reactivecouchbase.client.OpResult
import play.Play
import play.api.libs.json._
import scala.collection.immutable.List
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Post(id: Option[String], message: String, author: String, timestamp: Option[Int]) {
}

object Post {
  implicit val bucket = cb.bucketOfPosts
  implicit val timelineBucket = cb.bucketOfTimelines
  implicit val fmt: Format[Post] = Json.format[Post]

  val hotView = (if (Play.application().isDev) "dev_") + "hotPosts"
  val coldView = (if (Play.application().isDev) "dev_") + "posts"

  def find(id: String): Future[Option[Post]] = {
    bucket.get(id)
  }

  def create(id: String, post: Post): Future[OpResult] = {
    val timestamp: Long = System.currentTimeMillis / 1000
    val followers = Follower.followers(1000001.toString, None)

    bucket.add[JsValue](id,
      Json.obj(
        "author" -> post.author,
        "message" -> post.message
      ) ++ Json.obj(
        "timestamp" -> timestamp, "t" -> "post"
      ))
  }

  def getCached(userId: String): Future[Option[List[Post]]] = {
    timelineBucket.get[List[Post]](userId)
  }

  def getAndCache(ownerId: String, usersIds: List[String]): Future[List[Post]] = {
    for {
      timeline <- calculateTimeline(usersIds)
    } yield {
      timelineBucket.set(ownerId, timeline)
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
        .setRangeStart(ComplexKey.of(JsArray(Seq(JsString(userId), JsNumber(1136734444)))))
        .setRangeEnd(ComplexKey.of(JsArray(Seq(JsString(userId), JsNumber(1936734444)))))
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

  def findAllByUsername(userId: String): Future[List[Post]] = {
    bucket.find[Post](hotView, "all")(
      new Query()
        .setKey(ComplexKey.of(userId))
        .setDescending(true)
        .setIncludeDocs(true)
        .setLimit(25)
        .setInclusiveEnd(true)
        .setStale(Stale.UPDATE_AFTER))
  }

  def findAll(): Future[List[Post]] = {
    bucket.find[Post](hotView, "allPosts")(
      new Query()
        .setIncludeDocs(true)
        .setLimit(25)
        .setStale(Stale.FALSE))
  }
}