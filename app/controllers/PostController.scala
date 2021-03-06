package controllers

import javax.ws.rs.PathParam

import actors.timeline.CalculateTimelineActor
import akka.actor.Props
import com.wordnik.swagger.annotations._
import models.{UserId, DataPartitionable, Follower, Post}
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.json.{JsError, _}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Api(value = "/posts")
object PostController extends Controller with DataPartitionable {

  def system = play.api.libs.concurrent.Akka.system

  val CalculateTimelineActor = Akka.system.actorOf(Props[CalculateTimelineActor], name = "CalculateTimelineActor")

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
            Post.create(uuid, UserId(userId), json)

            for {
              followees <- Follower.followersIds(UserId(userId), None)
            } yield {
              val userIdsList: List[UserId] = followees :+ UserId(userId)


              CalculateTimelineActor ! userIdsList
            }

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
      case None => NotFound("Not found post " + id)
      case t =>
        Ok(Json.toJson(t))
    }
  }

  def posts(userId: Int, year: Int, week: Int) = Action.async {
    notEmptyPosts(UserId(userId), year, week)
  }

  def latestPosts(userId: Int) = Action.async {
    notEmptyPosts(UserId(userId), currentYear, currentWeekOfYear)
  }

  private def notEmptyPosts(userId: UserId, year: Int, week: Int): Future[Result] = {
    val future = for {
      (posts, weekOfPosts) <- Post.firstNotEmpty(userId, currentYear)(week)
      postsWithBaseUserProfile <- joiners.UserJoiner(posts)
    } yield {

      Ok(Json.obj(
        "posts" -> postsWithBaseUserProfile,
        "nextPage" -> routes.PostController.posts(userId.id, previousPageYearNumber(currentYear, weekOfPosts), previousPageWeekNumber(currentYear, weekOfPosts - 1)).toString()
      ))
    }
    future.recover {
      case ex: Exception => NotFound(ex.getMessage)
    }
  }

  def update(id: Long) = TODO

  def delete(id: Long) = TODO
}