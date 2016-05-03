package actors.timeline

import akka.actor.Actor

class CalculateTimelineActor extends Actor {
  def receive = {
    case userId : String => {
      models.Timeline.currentWeekTimeline(s"user::$userId")
    }
  }
}