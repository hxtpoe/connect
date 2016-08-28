package generators

import datasources.couchbase
import models.{UserId, Timeline, DataPartitionable}
import play.api.Logger

object TimelineGenerator extends DataPartitionable {
  val bucket = couchbase.bucket
  val numberOfUsers = UserGenerator.numberOfUsers

  def uuid() = java.util.UUID.randomUUID().toString()

  def run() = {
    Logger.warn("timeline created")

    for {
      i <- 1 to numberOfUsers
      week <- (currentWeekOfYear - 10) to currentWeekOfYear
    } {
      println(currentWeekOfYear)
      Thread.sleep(30)
      Timeline.specifiedTimeline(UserId(i), year, week)
    }
  }
}