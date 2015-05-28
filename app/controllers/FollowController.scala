package controllers

import javax.ws.rs.PathParam

import com.wordnik.swagger.annotations._
import models.{User, FollowPair}
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

@Api(value = "/followers", description = "of user")
object FollowController extends Controller {

  @ApiOperation(
    nickname = "getFollowees",
    value = "get followees",
    response = classOf[User],
    responseContainer = "List",
    produces = "application/json",
    httpMethod = "GET")
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Success")
    ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "page", value = "page offset", required = false, dataType = "Int", paramType = "query")))
  def getFollowees(
                    @ApiParam(value = "userId") @PathParam("userId") userId: String,
                    @ApiParam(value = "skip") @PathParam("skip") skip: Option[Int]) =
    Action.async {
      for {
        followers <- FollowPair.getFollowers(userId.toString, skip)
        counter <- FollowPair.countFollowers(userId.toString)
      } yield {
        Ok(Json.obj(
          "rows" -> Json.toJson(followers),
          "count" -> Json.toJson(counter)
        ))
      }
    }

  @ApiOperation(
    nickname = "follow",
    value = "add follower",
    responseContainer = "List",
    httpMethod = "POST")
  @ApiResponses(
    Array(
      new ApiResponse(code = 201, message = "Created")
    ))
  def follow(
              @ApiParam(value = "followerId") @PathParam("followerId") followerId: String,
              @ApiParam(value = "followeeId") @PathParam("userToFollowId") followeeId: String) =
    Action {
      request =>
        FollowPair.follow(followerId, followeeId)
        Ok("added")
    }

  @ApiOperation(
    nickname = "follow",
    value = "remove follower",
    produces = "application/json",
    httpMethod = "POST")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success")
  ))
  def unfollow(
                @ApiParam(value = "followerId") @PathParam("followerId") followerId: String,
                @ApiParam(value = "followeeId") @PathParam("userToFollowId") followeeId: String) =
    Action {
      request =>
        FollowPair.unfollow(followerId, followeeId)
        Ok("unfollowed")
    }

}