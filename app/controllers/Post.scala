package controllers

import com.wordnik.swagger.annotations._
import models.Post
import play.api.libs.json.{JsError, _}
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import javax.ws.rs.PathParam

@Api(value = "/posts", description = "Operations about posts")
object PostController extends Controller {

  @ApiOperation(
    nickname = "findByUsername",
    value = "find posts by username",
    response = classOf[Post],
    responseContainer = "List",
    produces = "application/json",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success"),
    new ApiResponse(code = 400, message = "Bad Request")))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "page", value = "page offset", required = false, dataType = "Int", paramType = "query")))
  def findByUsername(
                            @ApiParam(value = "username of the user to fetch") @PathParam("username") username: String) =
    Action.async { request =>
      Post.findAllByUsername(username) flatMap {
        list => Future {
          Ok(Json.toJson(list))
        }
      }
    }

  @ApiOperation(nickname = "create", value = "create tweet")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success"),
    new ApiResponse(code = 400, message = "Bad Request")))
  def create = Action.async(BodyParsers.parse.json) {
    request =>
      val someJson = request.body.validate[Post]

      someJson.fold(
        errors => {
          Future(BadRequest(Json.obj("status" -> "OK", "message" -> JsError.toFlatJson(errors))))
        },
        json => {
          Post.increment.map {
            case i: Int => {
              Post.create(i.toString, json)
              Ok(Json.obj("status" -> "OK", "message" -> ("tweet saved." + i.toString()))).withHeaders(LOCATION -> ("tweet id: " + i))
            }
          }
        }
      )
  }

  @ApiOperation(value = "find tweet by ID",
    notes = "returns a tweet based on ID",
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

  def update(id: Long) = TODO

  def delete(id: Long) = TODO
}