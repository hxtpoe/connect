package generators

import actors.timeline.CalculateCurrentTimelineActor
import akka.actor.Props
import datasources.couchbase
import models.{Timeline, DataPartitionable, UserId}
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka

object TimelineGenerator extends DataPartitionable {
  val bucket = couchbase.bucket
  val numberOfUsers = UserGenerator.numberOfUsers

  def uuid() = java.util.UUID.randomUUID().toString()

  val CalculateCurrentTimelineActor = Akka.system.actorOf(Props[CalculateCurrentTimelineActor], name = "RefreshGeneratorTimelineActor")

  def run(int: Int) = {
    for {
      i <- 1 to numberOfUsers
      week <- currentWeekOfYear - int to currentWeekOfYear
    } {
      if (i % 10 == 0) {
        Thread.sleep(1000)
      }
      Timeline.specifiedTimeline(UserId(i), year, week)
    }
    Logger.warn("timeline created")
  }
}