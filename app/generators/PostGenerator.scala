package generators

import datasources.couchbase
import models.{DataPartitionable, Post, UserId}
import play.api.Logger
import play.api.libs.json._

object PostGenerator extends DataPartitionable {
  implicit val followPairFormatter: Format[FP] = Json.format[FP]
  val bucket = couchbase.bucket
  val numberOfUsers = UserGenerator.numberOfUsers
  val daysWithPosts = 5

  def uuid() = java.util.UUID.randomUUID().toString()

  def runPosts() = {
    Logger.warn("posts created")

    val range = 1 to numberOfUsers toList

    for {
      i <- 1 to numberOfUsers
      msgId <- 1 to 2
      week <- (currentWeekOfYear - 15) to currentWeekOfYear
    } {
      Thread.sleep(5)
      Post.createWithDate(uuid, UserId(i), Json.obj(("message", JsString(sampleMsg(msgId)))).as[Post], week)
    }
  }

  def timestamp() = System.currentTimeMillis / 1000

  val sampleMsg = Map(
    1 -> "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum",
    2 -> "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
    3 -> "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"
  )
}