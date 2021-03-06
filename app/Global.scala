import java.util.TimeZone

import datasources.{couchbase => cb}
import filters.CorsFilter
import models.User
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

    TimeZone.setDefault(TimeZone.getTimeZone("UTC+0"))
  }

  override def onStop(app: Application) {
    datasources.newCouchbase.cluster.disconnect()
    println("App stoping...")
    //    Sender.stopEverything
    cb.close
  }
}