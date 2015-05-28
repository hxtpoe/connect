package controllers

import javax.ws.rs.{QueryParam, PathParam}

import com.wordnik.swagger.annotations._
import models.{Follower, Followee, FollowPair}
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

@Api(value = "/followers")
object FollowController extends Controller {

  @ApiOperation(
    nickname = "getFollowees",
    value = "get followees",
    produces = "application/json",
    httpMethod = "GET")
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Success")
    ))
  def getFollowees(
                    @ApiParam(value = "userId") @PathParam("userId") userId: String,
                    @ApiParam(value = "skip") @QueryParam("skip") skip: Option[Int]) =
    Action.async {
      for {
        followers <- Followee.followees(userId.toString, skip)
        counter <- FollowPair.countFollowers(userId.toString)
      } yield {
        Ok(Json.obj(
          "rows" -> Json.toJson(followers),
          "count" -> Json.toJson(counter)
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
        FollowPair.follow(followerId.toString, followeeId.toString)
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
        FollowPair.unfollow(followerId.toString, followeeId.toString)
        Ok("unfollowed")
    }
}