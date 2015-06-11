package models

import java.util

import com.couchbase.client.java.view.{AsyncViewResult, AsyncViewRow, Stale => NewStale, ViewQuery}
import datasources.couchbase
import org.reactivecouchbase.play.PlayCouchbase
import play.Play
import play.api.Play.current
import play.api.libs.json._
import rx.Observable
import rx.functions.{Action1, Func1}

import scala.collection.immutable.List
import scala.concurrent.{Future, Promise}

case class Follower(followerId: String) {}

object Follower {
  implicit val bucket = couchbase.bucketOfUsers
  implicit val followerFormatter: Format[Follower] = Json.format[Follower]
  implicit val ec = PlayCouchbase.couchbaseExecutor

  val defaultLimit = 20;

  val view = (if (Play.application().isDev) "dev_" else "") + "followers"

  def followers(userId: String, skip: Option[Int]): Future[List[String]] = {

    val followeePromise = Promise[List[String]]()

    val result = datasources.newCouchbase.bucket.async()
      .query(
        ViewQuery
          .from(view, "allV2")
          .limit(defaultLimit)
          .stale(NewStale.UPDATE_AFTER)
          .key(userId)
          .inclusiveEnd(true)
      )

    val ids = result.flatMap(new Func1[AsyncViewResult, Observable[AsyncViewRow]] {
      override def call(result: AsyncViewResult) = {
        result.rows()
      }
    })
      .map[String](new Func1[AsyncViewRow, String] {
      override def call(row: AsyncViewRow) = {
        row.value.toString
      }
    })

    ids.toList.subscribe(
      new Action1[util.List[String]] {
        override def call(t1: util.List[String]): Unit = {
          val bufferOfIds = scala.collection.JavaConversions.asScalaBuffer(t1)
          followeePromise.success(bufferOfIds.toIndexedSeq.toList)
        }
      },
      new Action1[Throwable] {
        override def call(t1: Throwable): Unit = {
          followeePromise.failure(t1)
        }
      }
    )

    followeePromise.future

  }
}