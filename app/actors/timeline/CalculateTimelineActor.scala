package actors.timeline

import akka.actor.Actor
import models.UserId

class CalculateTimelineActor extends Actor {
  def receive = {
    case userId: UserId => {
      models.Timeline.currentWeekTimeline(userId)
    }

    case list: List[UserId] => {
      val it = list.grouped(30)

      while (it.hasNext) {
        it.next().map(models.Timeline.currentWeekTimeline(_))
        Thread.sleep(1000) // ;)
      }
    }
  }
}