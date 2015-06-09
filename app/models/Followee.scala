package models

import java.util

import com.couchbase.client.java.view.{AsyncViewResult, AsyncViewRow, Stale => NewStale, ViewQuery}
import com.couchbase.client.protocol.views.{ComplexKey, Query, Stale}
import datasources.couchbase
import org.reactivecouchbase.play.PlayCouchbase
import play.Play
import play.api.Play.current
import play.api.libs.json._
import rx.Observable
import rx.functions.{Action1, Func1}
import scala.collection.immutable.List

import scala.concurrent.{Future, Promise}

case class Followee(followeeId: String) {}

object Followee {
  implicit val bucket = couchbase.bucketOfUsers
  implicit val followeeFormatter: Format[Followee] = Json.format[Followee]

  implicit val ec = PlayCouchbase.couchbaseExecutor

  val view = (if (Play.application().isDev) "dev_" else "") + "followees"

  def followees2(userId: String, skip: Option[Int]): Future[List[String]] = {
    val p = Promise[List[String]]()

    val result = datasources.newCouchbase.bucket.async()
      .query(ViewQuery.from(view, "all").limit(40).stale(NewStale.TRUE).startKey(userId + "_").endKey(userId + "_" + "\\u02ad"))

    val elorap = result.flatMap(new Func1[AsyncViewResult, Observable[AsyncViewRow]] {
      override def call(result: AsyncViewResult) = {
        result.rows()
      }
    })
      .map[String](new Func1[AsyncViewRow, String] {
      override def call(row: AsyncViewRow) = {
        row.id()
      }
    })

    elorap.toList.subscribe(
      new Action1[util.List[String]] {
        override def call(t1: util.List[String]): Unit = {
          val x = scala.collection.JavaConversions.asScalaBuffer(t1)
          p.success(x.toIndexedSeq.toList)
        }
      }
    )

    p.future
  }

  def followees(userId: String, skip: Option[Int]): Future[List[Followee]] = {
    bucket.find[Followee](view, "all")(
      new Query()
        .setRangeStart(ComplexKey.of(userId + "_"))
        .setRangeEnd(ComplexKey.of(userId + "_" + "\\u02ad"))
        .setIncludeDocs(true)
        .setInclusiveEnd(true)
        .setSkip(skip getOrElse 0)
        .setStale(Stale.OK)
        .setLimit(40))
  }
}