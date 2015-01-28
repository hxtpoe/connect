package services

import models.{FacebookProfile}
import datasources.{couchbase => cb}
import models.Tweet._
import org.reactivecouchbase.client.OpResult
import play.api.Logger
import play.api.libs.json.{Json, JsValue, JsObject}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object UserService {

}