import datasources.{couchbase => cb}
import filters.CorsFilter
import play.api._
import play.api.mvc._
import com.typesafe.config.ConfigFactory
import queue.RabbitMQConnection
import utils.Sender

object Global extends GlobalSettings {
  override def doFilter(action: EssentialAction) = {
    CorsFilter(filters.AuthorizedFilter("private")(action))
  }

  override def onStart(app: Application): Unit = {
    println("App staring...")
    val connection = RabbitMQConnection.getConnection;
    val sendingChannel1 = connection.createChannel();

    Sender.startSending
  }

  override def onStop(app: Application) {
    println("App stoping...")
    Sender.stopEverything
    cb.close
  }
}