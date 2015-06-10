package controllers

import javax.ws.rs.{PathParam, QueryParam}

import com.wordnik.swagger.annotations._
import models.{FollowPair, Followee, Follower}
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
  def getFolloweesV1(
                      @ApiParam(value = "userId") @PathParam("userId") userId: String,
                      @ApiParam(value = "skip") @QueryParam("skip") skip: Option[Int]) =
    Action.async {
      for {
        followers <- Followee.followees1(userId.toString, skip)
        counter <- Followee.numberOfFollowees(userId.toString)
      } yield {

        val simpleList = followers.map(_.followeeId)

        Ok(Json.obj(
          "rows" -> Json.toJson(simpleList),
          "count" -> Json.toJson(counter)
        ))
      }
    }

  def getFolloweesV2(
                      @ApiParam(value = "userId") @PathParam("userId") userId: String,
                      @ApiParam(value = "skip") @QueryParam("skip") skip: Option[Int]) = Action.async {

    for {
      x <- Followee.followees2(userId.toString, skip)
      counter <- Followee.numberOfFollowees(userId.toString)
    } yield {

      Ok(Json.obj(
        "rows" -> Json.toJson(x),
        "count" -> Json.toJson(counter)
      ))
    }
  }

  def getFollowees(
                    @ApiParam(value = "userId") @PathParam("userId") userId: String,
                    @ApiParam(value = "skip") @QueryParam("skip") skip: Option[Int]) = Action.async {

    for {
      x <- Followee.followees(userId.toString, skip)
      counter <- Followee.numberOfFollowees(userId.toString)
    } yield {

      Ok(Json.obj(
        "rows" -> Json.toJson(x),
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