package controllers

import java.text.SimpleDateFormat

import generators.{TimelineGenerator, PostGenerator, UserGenerator}
import models.DataPartitionable
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GenerateController extends Controller with DataPartitionable {
  def time() =
    Action.async { request =>
      Future {
        Ok(s"$currentWeekOfYear : $currentDayOfYear")
      }
    }

  def users() =
    Action.async { request =>
      Future {
        UserGenerator.runUsers()
        Ok("users created")
      }
    }

  def posts() =
    Action.async { request =>
      Future {
        PostGenerator.runPosts()
        Ok("posts created")
      }
    }

  def timeline(int: Int) =
    Action.async { request =>
      Future {
        TimelineGenerator.run(int)
        Ok("timeline created")
      }
    }
}