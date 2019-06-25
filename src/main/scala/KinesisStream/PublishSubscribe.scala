package KinesisStream

import java.nio.ByteBuffer
import java.text.SimpleDateFormat

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy}
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry
import redis.RedisClient
import com.contxt.kinesis.KinesisSource
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import Actions.{generateBill, serveOrder}
import akka.stream.alpakka.kinesis.scaladsl.KinesisSink

class PublishSubscribe {
  
//  implicit val system: ActorSystem = ActorSystem()
//  implicit val materializer: Materializer = ActorMaterializer()
  
  val objectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)
  
  
  implicit val amazonKinesisAsync: com.amazonaws.services.kinesis.AmazonKinesisAsync =
    AmazonKinesisAsyncClientBuilder.defaultClient()
  
  
  val flowSettings = StreamSettings.flowSettings
  
  
  def createRecord(order: Order): PutRecordsRequestEntry = {
    new PutRecordsRequestEntry().withData(ByteBuffer.wrap(objectMapper.writeValueAsBytes(order)).asReadOnlyBuffer()).withPartitionKey("part-2")
  }
  
  def subscribe = {
    KinesisSource(StreamSettings.consumerConfig).map(record => {record.markProcessed()
      println(s" received data from stream : ${record.data.utf8String}")
      record.data.utf8String
    })
  }
  
  
  def publish : Flow[Order, PutRecordsRequestEntry, NotUsed] = {
    Flow[Order].map[PutRecordsRequestEntry](order => {println(s"writing data to stream:  ${order.orderName} => ${order.status}")
      createRecord(order)}
      )
  }
  
}
