package services

import akka.actor.{Actor, ActorLogging}
import models.SocialProfile

class UserService() extends Actor with ActorLogging {

  import services.UserService._

  def receive = {
    case CreateOrMergeUser(p) =>
//      val query = BSONDocument(
//        "emails" -> BSONDocument(
//          "$elemMatch" -> BSONDocument(
//            "$regex" -> p.email
//          )
//        )
//      ) // Must use $elemMatch due to a bug in Mongodb https://jira.mongodb.org/browse/SERVER-15245
//    val modify = Update(BSONDocument(
//        "$push" -> BSONDocument("emails" -> p.email),
//        "$set" -> BSONDocument(s"socialAccounts.${p.provider}" -> p)
//      ), true)
//      val cmd = FindAndModify("users", query, modify, true)
//      val user: Future[User] = db.command(cmd).filter(_.isDefined).map(_.get).map(User.bsonFormat.read)
//      val replyTo = sender()
//      user.foreach(replyTo ! _)
//      user.onFailure {
//        case e =>
//          log.error(e.toString)
//      }
  }
}

object UserService {

  case class CreateOrMergeUser(profile: SocialProfile)

//  def props(db: DB) = Props(new UserService(db))
}