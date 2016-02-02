package controllers

import javax.ws.rs.{PathParam, QueryParam}

import com.wordnik.swagger.annotations._
import models.{Follower, User}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Api(value = "/followers")
object FollowController extends Controller {
  def getFollowees(
                    @ApiParam(value = "userId") @PathParam("userId") userId: String,
                    @ApiParam(value = "skip") @QueryParam("skip") skip: Option[Int]) =
    Action.async {
      for {
        user <- User.find(userId.toString)
      } yield {
        Ok(Json.obj(
          "rows" -> user.get.followees.getOrElse(List()).slice(skip.getOrElse(0), skip.getOrElse(0) + 10),
          "count" -> user.get.followees.getOrElse(List()).size
        ))
      }
    }

  @ApiOperation(
    nickname = "getFollowers",
    value = "get followers",
    produces = "application/json",
    httpMethod = "GET")
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Success")
    ))
  def getFollowers(
                    @ApiParam(value = "userId") @PathParam("userId") userId: String,
                    @ApiParam(value = "skip") @QueryParam("skip") skip: Option[Int]) =
    Action.async {
      for {
        followers <- Follower.followers(userId.toString, skip)
      } yield {
        Ok(Json.obj(
          "rows" -> Json.toJson(followers)
        ))
      }
    }

  @ApiOperation(
    nickname = "follow",
    value = "add followee",
    responseContainer = "List",
    httpMethod = "PUT")
  @ApiResponses(
    Array(
      new ApiResponse(code = 201, message = "Created"),
      new ApiResponse(code = 400, message = "Bad request")
    ))
  def follow(
              @ApiParam(value = "followerId") @PathParam("followerId") followerId: Int,
              @ApiParam(value = "followeeId") @PathParam("followeeId") followeeId: Int) =
    Action {
      request =>
        User.follow(followerId.toString, followeeId.toString)
        Ok("added")
    }

  @ApiOperation(
    nickname = "unfollow",
    value = "remove followee",
    produces = "application/json",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success"),
    new ApiResponse(code = 400, message = "Bad request")
  ))
  def unfollow(
                @ApiParam(value = "followerId") @PathParam("followerId") followerId: Int,
                @ApiParam(value = "followeeId") @PathParam("followeeId") followeeId: Int) =
    Action {
      request =>
        User.unfollow(followerId.toString, followeeId.toString)
        Ok("unfollowed")
    }
}