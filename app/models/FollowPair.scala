package models

import java.util.UUID

import play.api.libs.json._
import datasources.couchbase
import scala.concurrent.Future
import org.reactivecouchbase.client.Counters
import com.couchbase.client.protocol.views.{ComplexKey, Stale, Query}
import scala.concurrent.ExecutionContext.Implicits.global

case class FollowPair(followerId: String, followeeId: String, timestamp: Long, t: String = "fp") {}

object FollowPair extends Counters {
  implicit val bucket = couchbase.bucketOfUsers
  implicit val followPairFormatter: Format[FollowPair] = Json.format[FollowPair]


  def countFollowers(userId: String): Future[Option[Int]] = {
    bucket.get[Int](userId + "_follows")
  }

  def follow(followerId: String, followeeId: String): Unit = {
    val key = followerId + "_" + followeeId

    bucket.get[FollowPair](key) onSuccess {
      case None => {
        bucket.get[Int](followerId + "_follows") onSuccess {
          case Some(_) => bucket.incrAndGet(followerId + "_follows", 1)
          case None => bucket.set[Int](followerId + "_follows", 1)
        }
        bucket.set(key, FollowPair(followerId, followeeId, timestamp))
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