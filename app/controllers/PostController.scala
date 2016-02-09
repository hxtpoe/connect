package controllers

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone, Locale}

import actors.timeline.CalculateTimelineActor
import akka.actor.Props
import com.wordnik.swagger.annotations._
import models.{Follower, Post}
import play.api.libs.concurrent.Akka
import play.api.libs.json.{JsError, _}
import play.api.mvc._
import scala.concurrent.Future
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import javax.ws.rs.PathParam

@Api(value = "/posts")
object PostController extends Controller {

  def system = play.api.libs.concurrent.Akka.system

  val myActor = Akka.system.actorOf(Props[CalculateTimelineActor], name = "CalculateTimelineActor")

  @ApiOperation(nickname = "create", value = "create post")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success"),
    new ApiResponse(code = 400, message = "Bad Request")))
  def create(userId: Int) = Action.async(BodyParsers.parse.json) {
    request =>
      val someJson = request.body.validate[Post]

      Future {
        someJson.fold(
          errors => {
            BadRequest(Json.obj("status" -> "OK", "message" -> JsError.toFlatJson(errors)))
          },
          json => {
            val uuid = java.util.UUID.randomUUID().toString()
            Post.create(uuid, "user::" + userId.toString, json)

            for {
              followees <- Follower.followers(userId.toString, None)
            } yield {
              followees.map(myActor ! _.toString())
            }
            Ok(Json.obj("status" -> JsString("created: " + uuid.toString()))).withHeaders(LOCATION -> ("id: " + uuid))
          }
        )
      }
  }

  @ApiOperation(value = "find post by ID",
    notes = "returns the post based on ID",
    response = classOf[Post],
    produces = "application/json",
    httpMethod = "GET"
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success"),
    new ApiResponse(code = 400, message = "Bad Request")))
  def find(
            @ApiParam(value = "ID of the pet to fetch") @PathParam("postId") id: String) = Action.async {
    Post.find(id).map {
      t =>
        t match {
          case None => NotFound("Not found post " + id)
          case _ =>
            Ok(Json.toJson(t))
        }
    }
  }

  def posts(userId: Int, year: Int, week: Int) = Action.async {
    for (
      posts <- Post.getAll(s"user::$userId", year, week)
    ) yield {
      Ok(Json.toJson(posts))
    }
  }

  def latestPosts(userId: Int) = Action.async {
    for (
      posts <- Post.getAll(s"user::$userId", year, dayOfYear / 7)
    ) yield {
      Ok(Json.toJson(posts))
    }
  }

  def update(id: Long) = TODO

  def delete(id: Long) = TODO

  def dayOfYear: Int = simpleDataFormat("D")

  def year: Int = simpleDataFormat("YYYY")

  def simpleDataFormat(code: String): Int = {
    val now = new Date()
    val day = new SimpleDateFormat(code, Locale.ENGLISH)
    day.setTimeZone(TimeZone.getTimeZone("UTC+0"))
    day.format(now).toInt
  }
}