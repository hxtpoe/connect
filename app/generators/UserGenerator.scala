package generators

import datasources.couchbase
import models.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import play.api.libs.json._
import play.api.Logger

case class FP(followerId: String, followeeId: String, timestamp: Long, t: String = "followee") {}


object UserGenerator {
  implicit val followPairFormatter: Format[FP] = Json.format[FP]
  val bucket = couchbase.bucketOfUsers
  val numerOfUsers = 1000
  val userStandard = 1000000

  def runUsers() = {
    Logger.warn("users created")
    for (a <- userStandard to userStandard + numerOfUsers) {
      if (a % 200 == 0) {
        //        Logger.info(s"users generate... $a")
        Thread.sleep(500)
      }
      bucket.add[User](a.toString, User(
        Some(a.toString),
        a.toString + randomEmail,
        firstname,
        male,
        lastname,
        "http://google.pl/abcdefghijl",
        "en",
        "fb"
      ))
    }
  }

  def runFollowees() = {
    def create(a: Int, b: Int, sleep: Int): Unit = {
      val followerId = b.toString
      val followeeId = a.toString
      val t = timestamp

      bucket.add[FP](followerId + "_" + t + "_" + followeeId, FP(followerId, followeeId, t))
      if (b % (250) == 0) {
        Thread.sleep(sleep)
      }
    }
    var c = 0
    Logger.warn("0.1%")
    for {
      a <- userStandard to userStandard + (numerOfUsers * 0.001).toInt
      b <- userStandard to userStandard + 1000
    } {
      create(a, b, 250)
      c = c + 1
    }

    Logger.warn("1%")
    for {
      a <- userStandard to userStandard + (numerOfUsers * 0.01).toInt
      b <- userStandard to userStandard + 500
    } {
      create(a, b, 250)
      c = c + 1
    }
    Logger.warn(s"Done:  " + (c + numerOfUsers))
  }

  def randInt(): Int = {
    Random.nextInt(numerOfUsers)
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
      "Weronika"
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
      "Kowal"
    )
    lastnames(Random.nextInt(lastnames.size))
  }

  def timestamp() = System.currentTimeMillis / 1000
}
