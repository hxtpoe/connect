package datasources

import org.reactivecouchbase.ReactiveCouchbaseDriver

object couchbase {
  implicit val driver = ReactiveCouchbaseDriver()
  implicit val bucketOfUsers = driver.bucket("connect_users")
  implicit val bucketOfPosts = driver.bucket("connect_posts")

  def close = {
    driver.shutdown()
  }
}
