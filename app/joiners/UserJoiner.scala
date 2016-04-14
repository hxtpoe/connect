package joiners

import models.{PostJoin, Post}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object UserJoiner {
  def apply(inputList: List[Post]) = {
    val usersIds = inputList.map(post => post.userId.get)
    val uniqueUserProfileList = usersIds.distinct.map(id => id -> models.User.findBase(id.replace("user::", ""))).toMap

    for {
      profilesMap <- Future.traverse(uniqueUserProfileList.toMap) { case (k, fv) => fv.map(k -> _) } map (_.toMap)
    } yield {
      inputList.map(element => PostJoin(element, profilesMap.get(element.userId.get).flatten.get))
    }
  }
}