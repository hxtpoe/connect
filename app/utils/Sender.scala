package utils

import java.util.concurrent.TimeUnit

import actors.{PublishingActor, SendingActor}
import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Akka
import queue.RabbitMQConnection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object Sender {

  val connection = RabbitMQConnection.getConnection
  val sendingChannel = connection.createChannel()

  val channels = Set(sendingChannel)

  def startSending() = {
    sendingChannel.queueDeclare(Config.RABBITMQ_QUEUE, false, false, false, null)

    for (a <- 1 until 3) {
      Akka.system.scheduler.schedule(FiniteDuration(1, TimeUnit.MILLISECONDS)
        , FiniteDuration(300, TimeUnit.MILLISECONDS)
        , Akka.system.actorOf(
          Props(new SendingActor(channel = sendingChannel, queue = Config.RABBITMQ_QUEUE)))
        , "MSG to Queue " + a)
    }

    Akka.system.scheduler.schedule(FiniteDuration(50, TimeUnit.MILLISECONDS)
      , FiniteDuration(300, TimeUnit.MILLISECONDS)
      , Akka.system.actorOf(
        Props(new PublishingActor(channel = sendingChannel, exchange = Config.RABBITMQ_EXCHANGEE)))
      , "MSG to Exchange")
  }

  def stopEverything() = {
    channels.foreach(channel => channel.close())
    connection.close()
  }
}