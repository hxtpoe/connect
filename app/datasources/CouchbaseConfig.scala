package datasources

import play.Play

object CouchbaseConfig {
  def bucketIpAddress = Play.application().configuration().getObjectList("couchbase.buckets").get(0).get("host").toString
}
