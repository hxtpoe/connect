package models

import java.util.UUID

import datasources.couchbase
import org.reactivecouchbase.play.PlayCouchbase
import play.Play
import play.api.Play.current
import play.api.libs.json._

import scala.concurrent.Future

case class FollowPair(followerId: String, followeeId: String, timestamp: Long, t: String = "followee") {}

object FollowPair {
  implicit val bucket = couchbase.bucketOfUsers
  implicit val followPairFormatter: Format[FollowPair] = Json.format[FollowPair]
  implicit val ec = PlayCouchbase.couchbaseExecutor

  val view = (if (Play.application().isDev) "dev_" else "") + "followers"

  def countFollowers(userId: String): Future[Option[Int]] = {
    bucket.get[Int](userId + "_follows")
  }

  def follow(followerId: String, followeeId: String): Unit = {
    val t = timestamp()
    val key = followerId + "_" + t.toString + "_" + followeeId

    bucket.set(key, FollowPair(followerId, followeeId, t))
  }

  def unfollow(followerId: String, followeeId: String) = {
    val key = followerId + "_" + followeeId
    bucket.delete(key)
    // check
  }

  def generateId() = UUID.randomUUID().toString

  def timestamp() = System.currentTimeMillis / 1000
}