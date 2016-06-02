package controllers

import javax.ws.rs.{PathParam, QueryParam}

import actors.timeline.CalculateCurrentTimelineActor
import akka.actor.Props
import com.wordnik.swagger.annotations._
import models.{Follower, User, UserId}
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
                    @ApiParam(value = "userId") @PathParam("userId") userId: Int,
                    @ApiParam(value = "skip") @QueryParam("skip") skip: Option[Int]) =
    Action.async {
      for {
        user <- User.find(UserId(userId))
        f <- User.getFollowees(user.get, skip)
      } yield {

        Ok(Json.obj(
          "followees" -> Json.toJson(f.map{ case(id, user) => (id.toString, user)}),
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
                    @ApiParam(value = "userId") @PathParam("userId") userId: Int,
                    @ApiParam(value = "skip") @QueryParam("skip") skip: Option[Int]) =
    Action.async {
      for {
        followers <- Follower.followers(UserId(userId), skip)
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
                        @ApiParam(value = "userId") @PathParam("userId") userId: Int,
                        @ApiParam(value = "skip") @QueryParam("skip") skip: Option[Int]) =
    Action.async {
      for {
        followers <- Follower.followersIds(UserId(userId), skip)
      } yield {

        implicit val writer = new Writes[UserId] {
          def writes(t: UserId): JsValue = {
            JsNumber(t.id)
          }
        }

        implicit val reader = new Reads[UserId] {
          def reads(json: JsValue): JsResult[UserId] = {
            for {
              id <- (json).validate[Int]
            } yield UserId(id)
          }
        }

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
        User.follow(UserId(followerId), UserId(followeeId)) map {
          case true => CalculateCurrentTimelineActor ! UserId(followerId)
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
        User.unfollow(UserId(followerId), UserId(followeeId)) map {
          case true => CalculateCurrentTimelineActor ! UserId(followerId)
        }

        Ok("unfollowed")
      }
    }
}