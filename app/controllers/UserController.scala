package controllers

import javax.ws.rs.PathParam
import com.wordnik.swagger.annotations._
import models.User
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Api(value = "/users")
object UserController extends Controller {

  @ApiOperation(
    nickname = "getUser",
    value = "get user",
    produces = "application/json",
    httpMethod = "GET")
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Success")
    ))
  def getUser(
               @ApiParam(value = "userId") @PathParam("userId") userId: Int) =
    Action.async {
      for {
        user <- User.find(userId.toLong)
      } yield {
        user match {
          case Some(u) => Ok(Json.toJson(user))
          case None => NotFound("not found")
        }
      }
    }
}