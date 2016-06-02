package models

import com.couchbase.client.java.view._
import com.couchbase.client.protocol.views.{ComplexKey, Query, Stale}
import datasources.{couchbase => cb}
import org.reactivecouchbase.client.{OpResult, RawRow}
import play.Play
import play.api.libs.json._
import rx.Observable
import rx.functions.{Action1, Func1}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

// Custom validation helpers

// Combinator syntax


case class RegisteredAccount(
                              email: String,
                              password: String
                            )

case class SocialAccounts(
                           facebook: Option[FacebookProfile]
                         )

case class BaseUser(
                     id: Option[String],
                     first_name: String,
                     gender: String,
                     last_name: String,
                     link: String
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
                 followees: Option[List[UserId]]
               )

object BaseUser {
  implicit val fmt: Format[BaseUser] = Json.format[BaseUser]
}

object JsonFormatters {

}

object User {

  import UserIdConvertions._

  implicit val bucket = cb.bucket

  implicit val writer = new Writes[UserId] {
    def writes(t: UserId): JsValue = {
      JsNumber(t.id)
    }
  }

  implicit val reader = new Reads[UserId] {
    def reads(json: JsValue): JsResult[UserId] = {
      for {
        id <- (json).validate[Int]
      } yield UserId(id)
    }
  }

  implicit val fmt: Format[User] = Json.format[User]

  val viewName = (if (Play.application().isDev) "dev_") + "users"


  def find(id: UserId): Future[Option[User]] = {
    bucket.get[User](id)
  }

  def findBase(id: String): Future[Option[BaseUser]] = {
    bucket.get[BaseUser]("user::" + id)
  }

  def findBase(id: UserId): Future[Option[BaseUser]] = {
    bucket.get[BaseUser](id)
  }

  def getFollowees(user: User, skip: Option[Int] = Some(0)) = {
    val followees = user.followees.getOrElse(List())
    val followeesIds = followees.slice(skip.getOrElse(0), skip.getOrElse(0) + 40)
    val map = followeesIds.map(id => id -> User.findBase(UserId(id))).toMap // id.drop(6) should disapear..

    for {
      profilesMap <- Future.traverse(map) { case (k, fv) => fv.map(k -> _) } map (_.toMap)
    } yield {
      profilesMap
    }
  }

  def newfind(id: String): Future[Option[BaseUser]] = {
    bucket.get[BaseUser]("user::" + id)
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

  def follow(userId: UserId, followeeId: UserId): Future[Boolean] = {
    val p = Promise[Boolean]
    for {
      user <- User.find(userId)
    } yield {
      val followees = user.get.followees.getOrElse(List())
      if (!followees.contains(followeeId)) {
        val newFolloweesList = (followees :+ followeeId)

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
        true
      } else {
        false
      }
    }
  }

  def unfollow(userId: UserId, followeeId: UserId): Future[Boolean] = {
    for {
      user <- User.find(userId)
    } yield {
      val followees = user.get.followees.getOrElse(List())
      if (followees.contains(followeeId)) {
        val newFolloweesList = followees.take(followees.indexOf(followeeId)) ++ followees.drop(followees.indexOf(followeeId) + 1)

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
        true
      } else {
        false
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
    bucket.get[Int]("users_counter") onSuccess {
      case None => {
        bucket.set[Int]("users_counter", 102)
        generators.UserGenerator.runUsers()
      }
    }
  }
}

case class UserId(id: Int) {


  override def toString = id.toString
}

object UserIdConvertions {
  implicit def convertToString(userId: UserId): String = {
    s"user::${userId.id}"
  }

  implicit def stringToUserId(userId: String): UserId = {
    UserId(userId.toInt)
  }

  implicit def convertToInt(userId: UserId): Int = userId.id
}