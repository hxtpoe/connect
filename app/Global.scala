import java.util.TimeZone

import datasources.{couchbase => cb}
import filters.CorsFilter
import play.api._
import play.api.mvc._

object Global extends GlobalSettings {
  override def doFilter(action: EssentialAction) = {
    CorsFilter(filters.AuthorizedFilter("private")(action))
  }

  override def onStart(app: Application): Unit = {
    println("App staring...")
    TimeZone.setDefault(TimeZone.getTimeZone("UTC+0"))

    System.setProperty("user.timezone", "GMT")

    //    val connection = RabbitMQConnection.getConnection
    //    val sendingChannel1 = connection.createChannel()
    //    Sender.startSending
    //    cb.bucket.get("users_counter").map({
    //      case None =>
    //      {
    //        UserGenerator.runUsers()
    //        PostGenerator.runPosts()
    //        TimelineGenerator.run()
    //      }
    //    })
  }

  override def onStop(app: Application) {
    datasources.newCouchbase.cluster.disconnect()
    println("App stoping...")
    //    Sender.stopEverything
    cb.close
  }
}