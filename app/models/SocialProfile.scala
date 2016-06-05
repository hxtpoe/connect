package models

import datasources.{couchbase => cb}
import org.reactivecouchbase.client.Counters
import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class FacebookProfile(id: String,
                           email: String,
                           first_name: String,
                           gender: String,
                           last_name: String,
                           link: String,
                           locale: String
                          )

object FacebookProfile extends Counters {
  implicit val fpFormat: Format[FacebookProfile] = Json.format[FacebookProfile]
  implicit val bucket = cb.bucket

  def increment(): Int = {
    Await.result(incrAndGet("users_counter", 1), 1 second)
  }

  def createOrMerge(profile: FacebookProfile): UserId = {
    Await.result(User.findUserIdByFacebookId(profile.id), 2 second) match {
      case Some(id) => UserId(id.drop(6).toInt)
      case None => {
        val newIdenfifier = increment()
        bucket.set[JsValue](
          "user::" + newIdenfifier.toString, Json.toJson(profile).as[JsObject] ++
            Json.obj(
              "provider" -> "fb",
              "type" -> "user",
              "created_at" -> getTimestamp(),
              "followees" -> List()
            ))
        UserId(newIdenfifier)
      }
    }
  }

  def getTimestamp(): Long = System.currentTimeMillis / 10
}


