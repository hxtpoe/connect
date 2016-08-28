package datasources

import com.couchbase.client.java.CouchbaseCluster

object newCouchbase {
  val cluster = CouchbaseCluster.create(CouchbaseConfig.bucketIpAddress)
  val bucket = cluster.openBucket("connect")
}
