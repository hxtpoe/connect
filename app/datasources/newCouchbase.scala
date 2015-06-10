package datasources

import com.couchbase.client.java.CouchbaseCluster

import rx.lang.scala.Observable

object newCouchbase {
  val cluster = CouchbaseCluster.create("192.168.10.42")

  val bucket = cluster.openBucket("connect_users")

  def hello(names: String*) {
    Observable.from(names) subscribe { n =>
      println(s"Hello $n!")
    }
  }
}
