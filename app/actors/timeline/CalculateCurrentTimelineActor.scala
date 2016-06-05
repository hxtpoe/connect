package actors.timeline

import akka.actor.Actor
import models.{UserId, DataPartitionable}

class CalculateCurrentTimelineActor extends Actor with DataPartitionable {
  def receive = {
    case userId: UserId => {
      List.range(currentWeekOfYear - 10, currentWeekOfYear + 1).reverse.map(
        models.Timeline.specifiedTimeline(userId, 2016, _)
      )
    }
  }
}