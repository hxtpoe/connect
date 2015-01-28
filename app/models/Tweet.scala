package models

import play.api.libs.json._
import datasources.{couchbase => cb}
import scala.concurrent.{Future}
import scala.concurrent.ExecutionContext.Implicits.global
import org.reactivecouchbase.client.{OpResult, Counters}
import com.couchbase.client.protocol.views.{ComplexKey, Stale, Query}

case class Tweet(id: Option[String], message: Option[String], author: String, subject: String) {
  def save(): Future[OpResult] = Tweet.save(this)
  def remove(): Future[OpResult] = Tweet.remove(this)
}

object Tweet extends Counters {
  implicit val bucket = cb.bucketOfTweets
  implicit val fmt: Format[Tweet] = Json.format[Tweet]

  def find(id: Long): Future[Option[Tweet]] = {
    bucket.get("tweet:" + id.toString)
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
    incrAndGet("tweets", 1)
  }

  def create(id: Int, tweet: Tweet): Future[OpResult] = {
    val timestamp: Long = System.currentTimeMillis / 1000
    val t: String = "tweet"

    bucket.set[JsValue]("tweet:" + id.toString, Json.obj("author" -> tweet.author, "message" -> tweet.message, "subject" -> tweet.subject) ++ Json.obj("timestamp" -> timestamp, "t" -> t))
  }

  def findAllTweetsByUsername(username: String): Future[List[Tweet]] = {
    bucket.find[Tweet]("tweets", "allTweets")(new Query().setRangeStart(ComplexKey.of("tweet_"+username + "\\u02ad")).setRangeEnd(ComplexKey.of("tweet_"+username)).setDescending(true).setIncludeDocs(true).setLimit(10000).setInclusiveEnd(true).setStale(Stale.OK))
  }

  def findAll(): Future[List[Tweet]] = {
    bucket.find[Tweet]("tweets", "allTweets")(new Query().setIncludeDocs(true).setLimit(1000).setStale(Stale.FALSE))
  }
}