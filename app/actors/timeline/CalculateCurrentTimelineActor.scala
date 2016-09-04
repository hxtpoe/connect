package actors.timeline

import akka.actor.Actor
import models.{UserId, DataPartitionable}

class CalculateCurrentTimelineActor extends Actor with DataPartitionable {
  def receive = {
    case userId: UserId => {
      (currentWeekOfYear - 3 to currentWeekOfYear).map(
        models.Timeline.specifiedTimeline(userId, 2016, _)
      )
    }
  }
}