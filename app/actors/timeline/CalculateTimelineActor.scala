package actors.timeline

import akka.actor.Actor
import models.UserId

class CalculateTimelineActor extends Actor {
  def receive = {
    case userId: UserId => {
      models.Timeline.currentWeekTimeline(userId)
    }
  }
}