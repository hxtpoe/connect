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
  }

  override def onStop(app: Application) {
    cb.close
  }
}