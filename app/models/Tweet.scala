package models

import play.api.libs.json._
import datasources.{couchbase => cb}
import scala.concurrent.{Future}
import org.reactivecouchbase.client.{OpResult, Counters}
import com.couchbase.client.protocol.views.{ComplexKey, Stale, Query}
import scala.concurrent.ExecutionContext.Implicits.global

case class Tweet(id: Option[String], message: String, author: String) {
  def save(): Future[OpResult] = Tweet.save(this)
  def remove(): Future[OpResult] = Tweet.remove(this)
}

object Tweet extends Counters {
  implicit val bucket = cb.bucketOfTweets
  implicit val fmt: Format[Tweet] = Json.format[Tweet]

  val counterKey = "tweets_counter"

  def find(id: String): Future[Option[Tweet]] = {
    bucket.get("tweet:" + id)
  }

  def save(tweet: Tweet): Future[OpResult] = {
    bucket.set[Tweet](1.toString, tweet)
  }

  def remove(tweet: Tweet): Future[OpResult] = {
    bucket.delete("1")
  }

  def remove(id: Int): Future[OpResult] = {
    bucket.delete(id.toString)
  }

  def increment(): Future[Int] = {
    incrAndGet(counterKey, 1)
  }

  def create(id: String, tweet: Tweet): Future[OpResult] = {
    val timestamp: Long = System.currentTimeMillis / 1000
    val t: String = "tweet"

    bucket.set[JsValue]("tweet:" + id,
      Json.obj(
        "author" -> tweet.author,
        "message" -> tweet.message
      ) ++ Json.obj(
        "timestamp" -> timestamp, "t" -> t
      ))
  }

  def findAllTweetsByUsername(username: String): Future[List[Tweet]] = {
    bucket.find[Tweet]("tweets", "allTweets")(
      new Query()
        .setRangeStart(ComplexKey.of("tweet_" + username + "\\u02ad"))
        .setRangeEnd(ComplexKey.of("tweet_" + username))
        .setDescending(true)
        .setIncludeDocs(true)
        .setLimit(25)
        .setInclusiveEnd(true)
        .setStale(Stale.UPDATE_AFTER))
  }

  def findAll(): Future[List[Tweet]] = {
    bucket.find[Tweet]("tweets", "allTweets")(
      new Query()
        .setIncludeDocs(true)
        .setLimit(25)
        .setStale(Stale.FALSE))
  }
}