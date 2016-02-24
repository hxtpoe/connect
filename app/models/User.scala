package models

import com.couchbase.client.java.view._
import com.couchbase.client.protocol.views.{Stale, ComplexKey, Query}
import datasources.{couchbase => cb}
import org.reactivecouchbase.client.{RawRow, OpResult}
import play.Play
import play.api.libs.json.{Json, Format}
import rx.Observable
import rx.functions.{Action1, Func1}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Promise, Future}

case class RegisteredAccount(
                              email: String,
                              password: String
                            )

case class SocialAccounts(
                           facebook: Option[FacebookProfile]
                         )

case class User(
                 id: Option[String],
                 email: String,
                 first_name: String,
                 gender: String,
                 last_name: String,
                 link: String,
                 locale: String,
                 provider: String,
                 followees: Option[List[String]]
               )

object User {
  implicit val bucket = cb.bucket
  implicit val fmt: Format[User] = Json.format[User]
  val viewName = (if (Play.application().isDev) "dev_") + "users"


  def find(id: String): Future[Option[User]] = {
    bucket.get("user::" + id)
  }

  def findUserIdByFacebookId(fbId: String): Future[Option[String]] = {
    bucket.rawSearch(viewName, "byFacebook")(
      new Query()
        .setRangeStart(ComplexKey.of(fbId))
        .setRangeEnd(ComplexKey.of(fbId + "\\u02ad"))
        .setIncludeDocs(true)
        .setLimit(1)
        .setInclusiveEnd(true)
        .setStale(Stale.FALSE)
    ).headOption.map {
      case Some(RawRow(_, id, _, _)) => id
      case None => None
    }
  }

  def follow(userId: String, followeeId: String) = {
    for {
      user <- User.find(userId)
    } yield {
      val followees = user.get.followees.getOrElse(List())
      if (!followees.contains("user::" + followeeId)) {
        val newFolloweesList = followees :+ ("user::" + followeeId)
        bucket.set("user::" + userId,
          User(
            user.get.id,
            user.get.email,
            user.get.first_name,
            user.get.gender,
            user.get.last_name,
            user.get.link,
            user.get.locale,
            user.get.provider,
            Some(newFolloweesList)
          ))
      }
    }
  }

  def unfollow(userId: String, followeeId: String) = {
    for {
      user <- User.find(userId)
    } yield {
      val followees = user.get.followees.getOrElse(List())
      if (followees.contains(followeeId.toInt)) {
        val newFolloweesList = followees.take(followees.indexOf(followeeId.toInt)) ++ followees.drop(followees.indexOf(followeeId.toInt) + 1)

        bucket.set(userId,
          User(
            user.get.id,
            user.get.email,
            user.get.first_name,
            user.get.gender,
            user.get.last_name,
            user.get.link,
            user.get.locale,
            user.get.provider,
            Some(newFolloweesList)
          ))
      }
    }
  }

  def numberOfUsers(): Future[Int] = {
    val p = Promise[Int]()

    val result = datasources.newCouchbase.bucket.async()
      .query(
        ViewQuery
          .from(viewName, "count")
          .reduce(true)
          .limit(1)
          .stale(com.couchbase.client.java.view.Stale.TRUE)
          .inclusiveEnd(true)
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

  def save(user: User): Future[OpResult] = {
    bucket.set[User](1.toString, user)
  }

  def remove(id: Int): Future[OpResult] = {
    bucket.delete(id.toString)
  }

  def init() = {
    bucket.get("users_counter") onSuccess {
      case None => {
        bucket.set[Int]("users_counter", 102)
        //        generators.UserGenerator.runUsers()
      }
    }
  }
}