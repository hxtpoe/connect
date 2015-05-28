package models

import java.util.UUID

import play.api.libs.json._
import datasources.couchbase
import scala.concurrent.Future
import org.reactivecouchbase.client.Counters
import com.couchbase.client.protocol.views.{ComplexKey, Stale, Query}
import scala.concurrent.ExecutionContext.Implicits.global

case class FollowPair(followerId: String, followeeId: String, timestamp: Long, t: String = "fp") {
}

object FollowPair extends Counters {
  implicit val bucket = couchbase.bucketOfUsers
  implicit val fmt: Format[FollowPair] = Json.format[FollowPair]

  def getFollowers(userId: String, skip: Option[Int]): Future[List[FollowPair]] = {
    bucket.find[FollowPair]("followers", "all")(
      new Query()
        .setRangeStart(ComplexKey.of(userId))
        .setRangeEnd(ComplexKey.of(userId + "\\u02ad"))
        .setIncludeDocs(true)
        .setInclusiveEnd(true)
        .setSkip(skip getOrElse 0)
        .setStale(Stale.FALSE)
        .setLimit(25))
  }

  def countFollowers(userId: String): Future[Option[Int]] = {
    bucket.get[Int](userId + "_follows")
  }

  def follow(followerId: String, followeeId: String): Unit = {
    val key = followerId + "_" + followeeId

    bucket.get(key) onSuccess {
      case None => {
        bucket.get[Int](followerId + "_follows") onSuccess {
          case Some(_) => bucket.incrAndGet(followerId + "_follows", 1)
          case None => bucket.set[Int](followerId + "_follows", 1)
        }
        bucket.set[FollowPair](key, FollowPair(followerId, followeeId, timestamp))
      }
    }
  }

  def unfollow(followerId: String, followeeId: String) = {
    val key = followerId + "_" + followeeId

    bucket.get(key) onSuccess {
      case Some(_) => {
        bucket.decrAndGet(followerId + "_follows", 1)
        bucket.delete(key)
      }
    }
  }

  def generateId() = UUID.randomUUID().toString

  def timestamp() = System.currentTimeMillis / 1000
}