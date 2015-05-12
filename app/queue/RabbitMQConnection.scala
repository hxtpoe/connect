package queue

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import utils.Config

object RabbitMQConnection {

  private val connection: Connection = null;

  def getConnection(): Connection = {
    connection match {
      case null => {
        val factory = new ConnectionFactory()
        factory.setHost(Config.RABBITMQ_HOST)
        factory.setUsername(Config.RABBITMQ_USERNAME)
        factory.setPort(Config.RABBITMQ_PORT)
        factory.setVirtualHost(Config.RABBITMQ_VHOST)
        factory.setPassword(Config.RABBITMQ_PASSWORD)
        factory.newConnection()
      }
      case _ => connection
    }
  }
}