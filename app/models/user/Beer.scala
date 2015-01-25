package models

import play.api.libs.json._
import org.reactivecouchbase.play._
import org.reactivecouchbase.{CouchbaseRWImplicits, CouchbaseBucket}
import scala.concurrent.{Future}
import com.couchbase.client.protocol.views.{Query, Stale, ComplexKey}
import play.api.libs.iteratee.{Enumeratee, Iteratee, Enumerator}
import org.reactivecouchbase.client.{OpResult, ReactiveCouchbaseException, TypedRow}
import play.api.Play.current

case class Beer(id: String, name: String, brewery: String) {
  def save(): Future[OpResult] = Beer.save(this)
  def remove(): Future[OpResult] = Beer.remove(this)
}

object Beer {

  implicit val beerFmt = Json.format[Beer]
  implicit val ec = PlayCouchbase.couchbaseExecutor

  def bucket = PlayCouchbase.bucket("elorap")

  def findById(id: String): Future[Option[Beer]] = {
    bucket.get[Beer](id)
  }

  def findAll(): Future[List[Beer]] = {
    bucket.find[Beer]("beer", "by_name")(new Query().setIncludeDocs(true).setStale(Stale.FALSE))
  }

  def save(beer: Beer): Future[OpResult] = {
    bucket.set[Beer](beer.id, beer)
  }

  def remove(beer: Beer): Future[OpResult] = {
    bucket.delete(beer.id)
  }
}