package generators

import datasources.couchbase
import models.User
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

case class FP(followerId: String, followeeId: String, timestamp: Long, t: String = "followee") {}


object UserGenerator {
  implicit val followPairFormatter: Format[FP] = Json.format[FP]
  val bucket = couchbase.bucket
  val numberOfUsers = 100
  val userStandard = 1

  def runUsers() = {
    Logger.warn("users created")
    for (a <- userStandard to userStandard + numberOfUsers) {
      if (a % 200 == 0) {
        Thread.sleep(500)
      }
      bucket.add[User]("user::" + a.toString, User(
        Some(a.toString),
        a.toString + randomEmail,
        firstname,
        male,
        lastname,
        "http://google.pl/abcdefghijl",
        "en",
        "fb",
        Some(List.range(1, 50).map(n => s"user::$n"))
      ))
    }
  }

  def randInt(): Int = {
    Random.nextInt(numberOfUsers)
  }

  def randomEmail: String = {
    val emails = List(
      "abcdef@wp.fake",
      "abcdef12@inznieria.fake",
      "abcdef12@google.fake",
      "abcdef12@gmail.fake",
      "def12@google.fake",
      "daaaef12@google.fake",
      "wwdaaaef12@google.fake"
    )
    emails(Random.nextInt(emails.size))
  }

  def male: String = {
    val sex = List(
      "male",
      "female"
    )
    sex(Random.nextInt(sex.size))
  }

  def firstname: String = {
    val firstnames = List(
      "Jan",
      "Józef",
      "Paweł",
      "Wacław",
      "Tomek",
      "Tomasz",
      "Weronika",
      "Włodzimierz"
    )
    firstnames(Random.nextInt(firstnames.size))
  }

  def lastname: String = {
    val lastnames = List(
      "Kowalski",
      "Malicki",
      "Brzeziński",
      "Nowak",
      "Jaśkowiak",
      "Prezes",
      "Kaczyński",
      "Kaczkowski",
      "Zapas",
      "Kowal",
      "Jóźwiak",
      "Caban",
      "Kwaśniewski",
      "Lenin"
    )
    lastnames(Random.nextInt(lastnames.size))
  }

  def timestamp() = System.currentTimeMillis / 1000
}
