package controllers

import javax.ws.rs.{PathParam, QueryParam}

import actors.timeline.CalculateCurrentTimelineActor
import akka.actor.Props
import com.wordnik.swagger.annotations._
import models.{Follower, User}
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global


@Api(value = "/followees")
object FollowController extends Controller {

  def system = play.api.libs.concurrent.Akka.system

  val CalculateCurrentTimelineActor = Akka.system.actorOf(Props[CalculateCurrentTimelineActor], name = "RefreshTimelineActor")

  def getFollowees(
                    @ApiParam(value = "userId") @PathParam("userId") userId: String,
                    @ApiParam(value = "skip") @QueryParam("skip") skip: Option[Int]) =
    Action.async {
      for {
        user <- User.find(userId.toString)
        f <- User.getFollowees(user.get, skip)
      } yield {
        Ok(Json.obj(
          "followees" -> f,
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
        followers <- Follower.followers(userId, skip)
      } yield {
        Ok(Json.obj(
          "rows" -> Json.toJson(followers)
        ))
      }
    }

  @ApiOperation(
    nickname = "getRichFollowers",
    value = "get full followers",
    produces = "application/json",
    httpMethod = "GET")
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Success")
    ))
  def getRichFollowers(
                        @ApiParam(value = "userId") @PathParam("userId") userId: String,
                        @ApiParam(value = "skip") @QueryParam("skip") skip: Option[Int]) =
    Action.async {
      for {
        followers <- Follower.followersIds(userId.toString, skip)
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
      request => {
        User.follow(followerId.toString, followeeId.toString)  map {
          case true => CalculateCurrentTimelineActor ! s"user::$followerId"
        }

        Ok("added")
      }
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
      request => {
        User.unfollow(followerId.toString, followeeId.toString) map {
          case true => CalculateCurrentTimelineActor ! s"user::$followerId"
        }

        Ok("unfollowed")
      }
    }
}