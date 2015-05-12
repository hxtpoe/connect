package datasources

import com.couchbase.client.java.CouchbaseCluster
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment

object newCouchbase {
  val cluster = CouchbaseCluster.create(DefaultCouchbaseEnvironment
    .builder()
    .queryEnabled(true)
    .build(), "192.168.10.42")
  val bucket = cluster.openBucket("connect_users")
}
