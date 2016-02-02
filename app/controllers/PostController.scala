package controllers

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone, Locale}

import com.wordnik.swagger.annotations._
import models.Post
import play.api.libs.json.{JsError, _}
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import javax.ws.rs.PathParam

@Api(value = "/posts")
object PostController extends Controller {

  def backendTimeline(userId: Int) =
    Action.async { request =>
      Post.getAndCache(userId.toString, (1 to 20).map(_.toString).toList) flatMap {
        list => Future {
          Ok(Json.toJson(list))
        }
      }
    }

  def timeline(userId: Int) =
    Action.async { request =>
      Post.getCached(userId.toString) flatMap {
        list => Future {
          Ok(Json.toJson(list))
        }
      }
    }

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
    val weekOfTheYear = new SimpleDateFormat("w", Locale.ENGLISH)
    val year = new SimpleDateFormat("YYYY", Locale.ENGLISH)
    year.setTimeZone(TimeZone.getTimeZone("UTC+0"))
    weekOfTheYear.setTimeZone(TimeZone.getTimeZone("UTC+0"))

    val now = new Date()

    for (
      posts <- Post.getAll(s"user::$userId", year.format(now).toInt, weekOfTheYear.format(now).toInt)
    ) yield {
      Ok(Json.toJson(posts))
    }
  }

  def update(id: Long) = TODO

  def delete(id: Long) = TODO
}