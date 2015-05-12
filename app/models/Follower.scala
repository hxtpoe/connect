package models

import java.util
import com.couchbase.client.java.view.{Stale => NewStale, DefaultAsyncViewRow, AsyncViewResult, AsyncViewRow, ViewQuery}
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

  val defaultLimit = 10

  val view = (if (Play.application().isDev) "dev_" else "") + "followers"

  def followers(userId: String, skip: Option[Int]): Future[List[String]] = {

    val followeePromise = Promise[List[String]]()

    val result = datasources.newCouchbase.bucket.async()
      .query(
        ViewQuery
          .from(view, "all")
          .limit(3000)
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
        row.id.toString
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

  def numberOfFollowers(userId: String): Future[Int] = {
    val p = Promise[Int]()

    val result = datasources.newCouchbase.bucket.async()
      .query(
        ViewQuery
          .from(view, "count")
          .reduce(true)
          .limit(1)
          .stale(NewStale.TRUE)
          .inclusiveEnd(true)
          .key(userId)
      )

    val ids = result.flatMap(new Func1[AsyncViewResult, Observable[AsyncViewRow]] {
      override def call(result: AsyncViewResult) = {
        result.rows().firstOrDefault(new DefaultAsyncViewRow(datasources.newCouchbase.bucket.async(), "0", "0", 0))
      }
    })

    val numer = ids.map[Int](new Func1[AsyncViewRow, Int] {
      override def call(row: AsyncViewRow) = {
        row.value().asInstanceOf[Int]
      }
    })

    numer.subscribe(
      new Action1[Int] {
        override def call(t1: Int): Unit = {
          p.success(t1)
        }
      },
      new Action1[Throwable] {
        override def call(t1: Throwable): Unit = {
          p.failure(t1)
        }
      }
    )

    p.future
  }
}