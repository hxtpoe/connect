package datasources

import org.reactivecouchbase.ReactiveCouchbaseDriver

object couchbase {
  implicit val driver = ReactiveCouchbaseDriver()
  implicit val bucketOfUsers = driver.bucket("connect_users")
  implicit val bucketOfTweets = driver.bucket("connect_tweets")

  def close = {
    driver.shutdown()
  }
}
