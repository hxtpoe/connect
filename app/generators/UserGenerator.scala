package generators

import datasources.couchbase
import models.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import play.api.libs.json._

case class FP(followerId: String, followeeId: String, timestamp: Long, t: String = "fp") {}


object UserGenerator {
  implicit val followPairFormatter: Format[FP] = Json.format[FP]
  val bucket = couchbase.bucketOfUsers
  val numerOfUsers = 3000

  def run() = {
    for (a <- 1000 to numerOfUsers) {
      if (a % 100 == 0) {
        println(a)
        Thread.sleep(1000)
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

  def run2() = {
    for {
      a <- 100 to 105
      b <- 1 to 2000
    } {
      val followerId = b.toString
      val followeeId = a.toString

      bucket.add[FP](followerId + "_" + followeeId, FP(followerId, followeeId, timestamp))
      if (b % 100 == 0) {
        println(a)
        println(b)
        Thread.sleep(1000)
      }
    }
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

  def timestamp() = Random.nextInt(10000000).toLong
}
