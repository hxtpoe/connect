package datasources

import org.reactivecouchbase.ReactiveCouchbaseDriver

object couchbase {
  implicit val driver = ReactiveCouchbaseDriver()
  implicit val bucket = driver.bucket("connect")

  def close = {
    driver.shutdown()
  }
}
