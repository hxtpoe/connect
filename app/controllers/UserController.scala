package controllers

import javax.ws.rs.PathParam

import com.wordnik.swagger.annotations._
import models.{UserId, User}
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
  def user(
            @ApiParam(value = "userId") @PathParam("userId") userId: Int) =
    Action.async {
      for {
        user <- User.newfind(userId.toString)
      } yield {
        user match {
          case Some(u) => Ok(Json.toJson(user))
          case None => NotFound("User not found!")
        }
      }
    }

  @ApiOperation(
    nickname = "getExtendedUser",
    value = "get extended  user",
    produces = "application/json",
    httpMethod = "GET")
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Success")
    ))
  def extendedUser(
            @ApiParam(value = "userId") @PathParam("userId") userId: Int) =
    Action.async {
      for {
        user <- User.find(UserId(userId))
      } yield {
        user match {
          case Some(u) => Ok(Json.toJson(user))
          case None => NotFound("User not found!")
        }
      }
    }

  def usersCount() = Action.async {
    for {
      count <- User.numberOfUsers()
    } yield {
       Ok(Json.obj("count" -> count))
    }
  }
}