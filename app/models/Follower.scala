package models

import play.Play
import play.api.libs.json._
import datasources.couchbase
import scala.concurrent.Future
import com.couchbase.client.protocol.views.{ComplexKey, Stale, Query}
import scala.concurrent.ExecutionContext.Implicits.global

case class Follower(followerId: String) {}

object Follower {
  implicit val bucket = couchbase.bucketOfUsers
  implicit val followerFormatter: Format[Follower] = Json.format[Follower]
  val view = (if (Play.application().isDev) "dev_") + "followers"

  def followers(userId: String, skip: Option[Int]): Future[List[Follower]] = {

    bucket.find[Follower](view, "all")(
      new Query()
        .setRangeStart(ComplexKey.of(userId))
        .setRangeEnd(ComplexKey.of(userId + "\\u02ad"))
        .setIncludeDocs(true)
        .setInclusiveEnd(true)
        .setSkip(skip getOrElse 0)
        .setStale(Stale.FALSE)
        .setLimit(25))
  }
}