package models

import com.couchbase.client.protocol.views.{ComplexKey, Query, Stale}
import datasources.couchbase
import org.reactivecouchbase.play.PlayCouchbase
import play.Play
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.Future

case class Follower(followerId: String) {}

object Follower {
  implicit val bucket = couchbase.bucketOfUsers
  implicit val followerFormatter: Format[Follower] = Json.format[Follower]
  implicit val ec = PlayCouchbase.couchbaseExecutor

  val view = (if (Play.application().isDev) "dev_" else "") + "followers"

  def followers(userId: String, skip: Option[Int]): Future[List[Follower]] = {

    bucket.find[Follower](view, "all")(
      new Query()
        .setRangeStart(ComplexKey.of(userId))
        .setRangeEnd(ComplexKey.of(userId + "\\u02ad"))
        .setIncludeDocs(true)
        .setInclusiveEnd(true)
        .setSkip(skip getOrElse 0)
        .setStale(Stale.OK)
        .setLimit(10))
  }
}