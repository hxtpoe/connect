package models

import play.Play
import play.api.libs.json._
import datasources.couchbase
import scala.concurrent.Future
import com.couchbase.client.protocol.views.{ComplexKey, Stale, Query}
import scala.concurrent.ExecutionContext.Implicits.global

case class Followee(followeeId: String) {}

object Followee {
  implicit val bucket = couchbase.bucketOfUsers
  implicit val followeeFormatter: Format[Followee] = Json.format[Followee]
  val view = (if (Play.application().isDev) "dev_") + "followees"

  def getFollowees(userId: String, skip: Option[Int]): Future[List[Followee]] = {

    bucket.find[Followee](view, "all")(
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