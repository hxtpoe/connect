package actors.timeline

import akka.actor.Actor
import models.DataPartitionable

class CalculateCurrentTimelineActor extends Actor with DataPartitionable {
  def receive = {
    case userId : String => {
      List.range(1, currentWeekOfYear + 1).reverse.map(
        models.Timeline.specifiedTimeline(s"user::$userId", 2016, _)
      )
    }
  }
}