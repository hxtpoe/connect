package models

case class Feed(
                 owner: String,
                 author: String,
                 post: String,
                 time: Int
                 )

object Feed {
//  implicit val bucket = couchbase.bucketOfFeeds
//  implicit val fmt: Format[Feed] = Json.format[Feed]
//
//  def create(owner: String,
//             author: String,
//             post: String,
//             time: Int) = {
//    val uuid = java.util.UUID.randomUUID().toString()
//    bucket.set(uuid, Feed(
//      owner,
//      author,
//      post,
//      time
//    ))
//  }
}