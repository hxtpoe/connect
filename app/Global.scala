import datasources.{couchbase => cb}
import filters.CorsFilter
import models.{Post, User}
import play.api._
import play.api.mvc._

object Global extends GlobalSettings {
  override def doFilter(action: EssentialAction) = {
    CorsFilter(filters.AuthorizedFilter("private")(action))
  }

  override def onStart(app: Application): Unit = {
    println("App staring...")
//    val connection = RabbitMQConnection.getConnection
//    val sendingChannel1 = connection.createChannel()
//    Sender.startSending



    User.init()
    Post.init()


  }

  override def onStop(app: Application) {
    datasources.newCouchbase.cluster.disconnect()
    println("App stoping...")
//    Sender.stopEverything
    cb.close
  }
}