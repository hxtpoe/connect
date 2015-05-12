package datasources

import org.reactivecouchbase.ReactiveCouchbaseDriver

object couchbase {
  implicit val driver = ReactiveCouchbaseDriver()
  implicit val bucketOfUsers = driver.bucket("connect_users")
  implicit val bucketOfPosts = driver.bucket("connect_posts")
  implicit val bucketOfTimelines = driver.bucket("connect_timelines")

  def close = {
    driver.shutdown()
  }
}
