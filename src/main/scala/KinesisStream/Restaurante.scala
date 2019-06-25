package KinesisStream

import KinesisStream.Actions.{acknowledge, generateBill, isCustomerAllowedToOrder, serveOrder}
import KinesisStream.PublishSubscribe
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.alpakka.kinesis.scaladsl.KinesisSink
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import StreamSettings.flowSettings
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder
import redis.RedisClient

object Restaurante extends App {
  
  implicit val system = StreamSettings.system
  implicit val materializer = ActorMaterializer()
  
  implicit val amazonKinesisAsync: com.amazonaws.services.kinesis.AmazonKinesisAsync =
    AmazonKinesisAsyncClientBuilder.defaultClient()
  
  val redis = RedisClient()
  
  system.registerOnTermination(amazonKinesisAsync.shutdown())
  
  val objectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)
  
  val obj = new PublishSubscribe
  val kinesisSource = obj.subscribe
  val filterIpadOrders = Flow[Order].filter(order => order.source == "IPAD")
  
  // Graph to read orders from stream
  kinesisSource.map(orderStatus => objectMapper.readValue(orderStatus, classOf[Order]))
    .via(filterIpadOrders)
    .filter(order => isCustomerAllowedToOrder(order))
    .map(order => { acknowledge(order)
                    if (order.orderName == "BILL") generateBill(order)
                    order
                  }
        )
    .filter(order => order.orderName != "BILL")
    .to(Sink.foreach(order => serveOrder(order))).run()
  
  
  // producer graph to write the order status to stream
  val source = Source.actorRef[Order](10, OverflowStrategy.fail)
  val flow = obj.publish
  
  val replyStatus = source.via(flow).to(KinesisSink("rd_test_stream", flowSettings)).run()
  
}
