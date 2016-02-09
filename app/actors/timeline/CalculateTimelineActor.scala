package actors.timeline

import akka.actor.Actor

class CalculateTimelineActor extends Actor {
  def receive = {
    case userId : String => {
//      println(s"string $userId")
      models.Timeline.currentWeekTimeline(s"user::$userId")
    }
  }
}