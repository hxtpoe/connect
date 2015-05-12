package models

import play.api.libs.json._
import datasources.{couchbase => cb}
import scala.concurrent.{Future}
import org.reactivecouchbase.client.{OpResult, Counters}
import com.couchbase.client.protocol.views.{ComplexKey, Stale, Query}
import scala.concurrent.ExecutionContext.Implicits.global

case class Post(id: Option[String], message: String, author: String) {
  def save(): Future[OpResult] = Post.save(this)

  def remove(): Future[OpResult] = Post.remove(this)
}

object Post extends Counters {
  implicit val bucket = cb.bucketOfPosts
  implicit val fmt: Format[Post] = Json.format[Post]

  val counterKey = "posts_counter"

  def find(id: String): Future[Option[Post]] = {
    bucket.get("post:" + id)
  }

  def save(tweet: Post): Future[OpResult] = {
    bucket.set[Post](1.toString, tweet)
  }

  def remove(tweet: Post): Future[OpResult] = {
    bucket.delete("1")
  }

  def remove(id: Int): Future[OpResult] = {
    bucket.delete(id.toString)
  }

  def increment(): Future[Int] = {
    incrAndGet(counterKey, 1)
  }

  def create(id: String, tweet: Post): Future[OpResult] = {
    val timestamp: Long = System.currentTimeMillis / 1000
    val post: String = "post"

    bucket.set[JsValue]("post:" + id,
      Json.obj(
        "author" -> tweet.author,
        "message" -> tweet.message
      ) ++ Json.obj(
        "timestamp" -> timestamp, "t" -> post
      ))
  }

  def findAllByUsername(username: String): Future[List[Post]] = {
    bucket.find[Post]("posts", "allPosts")(
      new Query()
        .setRangeStart(ComplexKey.of("post_" + username + "\\u02ad"))
        .setRangeEnd(ComplexKey.of("post_" + username))
        .setDescending(true)
        .setIncludeDocs(true)
        .setLimit(25)
        .setInclusiveEnd(true)
        .setStale(Stale.UPDATE_AFTER))
  }

  def findAll(): Future[List[Post]] = {
    bucket.find[Post]("posts", "allPosts")(
      new Query()
        .setIncludeDocs(true)
        .setLimit(25)
        .setStale(Stale.FALSE))
  }
}