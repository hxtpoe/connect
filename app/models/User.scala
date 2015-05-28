package models

import com.couchbase.client.protocol.views.{Stale, ComplexKey, Query}
import datasources.{couchbase => cb}
import org.reactivecouchbase.client.{RawRow, OpResult}
import play.api.libs.json.{Json, Format}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

case class RegisteredAccount(
                              email: String,
                              password: String
                              )

case class SocialAccounts(
                           facebook: Option[FacebookProfile]
                           )

case class User(
                 id: Option[Int],
                 emails: Set[String]
                 )

object User {
  implicit val bucket = cb.bucketOfUsers
  implicit val fmt: Format[User] = Json.format[User]

  def find(id: Long): Future[Option[User]] = {
    bucket.get(id.toString)
  }

  def findUserIdByFacebookId(fbId: String): Future[Option[String]] = {
    bucket.rawSearch("users", "byFacebook")(
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

  def save(tweet: User): Future[OpResult] = {
    bucket.set[User](1.toString, tweet)
  }

  def remove(tweet: User): Future[OpResult] = {
    bucket.delete("1")
  }

  def remove(id: Int): Future[OpResult] = {
    bucket.delete(id.toString)
  }

  def init() = {
    bucket.get("users_counter") onSuccess {
      case None => bucket.set[Int]("users_counter", 0)
    }
  }
}