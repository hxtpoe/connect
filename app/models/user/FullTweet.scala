package models

import java.util.Date

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._
import datasources.{couchbase => cb}
import scala.concurrent.{Future}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.iteratee.Iteratee
import scala.util.parsing.json.JSONObject
import scala.util.{Success, Failure}
import org.reactivecouchbase.client.{Counters, TypedRow}
import com.couchbase.client.protocol.views.{Stale, Query}

case class FullTweet(message: String, author: String, subject: String)

object FullTweet extends Counters {
  implicit val bucket = cb.bucket
  implicit val fmt: Format[FullTweet] = Json.format[FullTweet]

  def findAll(): Future[List[TypedRow[FullTweet]]] = {
    bucket.search("tweets", "allTweets")(new Query().setLimit(100)).toList
  }

  def tweetFromParams(map: Map[String, String]): Option[FullTweet] = {
    for {msg <- map.get("message")
         author <- map.get("author")
         subject <- map.get("subject")}
    yield FullTweet(msg, author, subject)
  }
}
