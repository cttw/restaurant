//package KinesisStream
//
//import java.text.SimpleDateFormat
//
//import akka.actor.ActorSystem
//import akka.stream.ActorMaterializer
//import akka.stream.alpakka.kinesis.KinesisFlowSettings
//import akka.stream.scaladsl.Sink
//import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
//import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{InitialPositionInStream, KinesisClientLibConfiguration}
//import com.contxt.kinesis.KinesisSource
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.module.scala.DefaultScalaModule
//import redis.RedisClient
//
//import scala.concurrent.duration._
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.util.Success
//
//
//object Restaurant extends App {
//
//  val flowSettings = KinesisFlowSettings
//    .create()
//    .withParallelism(1)
//    .withMaxBatchSize(500)
//    .withMaxRecordsPerSecond(1000)
//    .withMaxBytesPerSecond(1000000)
//    .withMaxRetries(5)
//    .withBackoffStrategy(KinesisFlowSettings.Exponential)
//    .withRetryInitialTimeout(100.milli)
//
//  val consumerConfig = new KinesisClientLibConfiguration(
//                                                          "atLeastOnceApp1",
//                                                          "rd_test_stream",
//                                                          new DefaultAWSCredentialsProviderChain,
//                                                          s"kinesisWorker-${java.util.UUID.randomUUID().toString}"
//                                                        )
//    .withRegionName("us-east-2")
//    .withCallProcessRecordsEvenForEmptyRecordList(true)
//    .withInitialPositionInStream(InitialPositionInStream.TRIM_HORIZON)
//
//
//  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//  //  case class KeyMessage(key: String, message: String, markProcessed: () => Unit)
//
//  case class Customer (customerName: String, customerId: String)
//  case class Order (orderName: String, customer: Customer)
//  case class OrderStatus (order: Order, status: String)
//
//  val objectMapper = new ObjectMapper()
//  objectMapper.setDateFormat(dateFormat)
//  objectMapper.registerModule(DefaultScalaModule)
//
//  implicit val system = ActorSystem("Main")
//  implicit val materializer = ActorMaterializer()
//
//  val redis = RedisClient()
//
//  //  KinesisSource(consumerConfig).map { kinesisRecord =>
//  //    KeyMessage(
//  //      kinesisRecord.partitionKey, kinesisRecord.data.utf8String, kinesisRecord.markProcessed
//  //    )
//  //  }.map(message => message.message.asInstanceOf[Order]).map(order => order.orderName)
//  //    .to(Sink.foreach(println))
//  //    .run()
//
////    KinesisSource(consumerConfig).map(record => {
////      record.markProcessed()
////      record.data.utf8String
////    }).to(Sink.foreach(x => println(x))).run()
//
//  KinesisSource(consumerConfig).map(record => {record.markProcessed()
//  record.data.utf8String})
//    .filter( _.startsWith("OrderStatus")).map(orderStatus => objectMapper.readValue(orderStatus, classOf[OrderStatus]))
//
//
//  KinesisSource(consumerConfig).map(record => {
//    record.markProcessed()
//    record.data.utf8String}).
//    map(order => objectMapper.readValue(order, classOf[Order]))
//    .map(order => { println(s"Order Status for customer ${order.customer.customerName} for item ${order.orderName} : QUEUED")
//      order}
//        )
//    //    .to(Sink.foreach(order => {Thread.sleep(5000)
//    //                               println(s"Order Status for ${order.name} => SERVED")}))
//    //    .run()
//    //    .to(Sink.foreach(order => {
//    //      redis.hget("customers", order.customerId).onComplete({
//    //      case Success(Some(value)) => if (value.utf8String == "BILL_GENERATED") {
//    //                                      println(s"Can not accept more orders from Cusotmer with customer Id ${order.customerId}\n" +
//    //                                        "Customer has already requested for bill")
//    //                                    }
//    //
//    //                                  else  value.utf8String match {
//    //                                                  case "BILL" =>  {
//    //                                                    Thread.sleep(5000)
//    //                                                    println(s"Status for customer-ID ${order.customerId} with order ${order.name} => BILL_GENERATED")
//    //                                                    redis.hmset("customers", Map(order.customerId -> "BILL_GENERATED"))
//    //                                                  }
//    //                                                  case _ => println(s"Status for customer-ID ${order.customerId} with order ${order.name} => SERVED")
//    //                                  }
//    //      case Success(None) => println(s"status for customer-ID ${order.customerId} with ${order.name} not found")
//    //    })
//    //
//    //  })).run()
//    .to(Sink.foreach(order => {
//    order.orderName match {
//      case "BILL" => {
//        println(s"Order Status for customer ${order.customer.customerName} : BILL_GENERATION_IN-PROGRESS")
//        Thread.sleep(5000)
//        //        redis.hmset("customers", Map(order.customer.customerId -> "0"))
//        redis.hmset("customers", Map(order.customer.customerId -> "0"))
//        println(s"Order Status for customer ${order.customer.customerName} : BILL GENERATED")
//
//      }
//
//      case _ => redis.hget("customers", order.customer.customerId).onComplete({
//        case Success(Some(value)) => if (value.utf8String == "0")
//          println(s"Customer ${order.customer.customerName} has already requested for Bill. Cannot accept further Orders")
//        else {
//          Thread.sleep(5000)
//          println(s"Order Status for customer ${order.customer.customerName} for item ${order.orderName} : SERVED")
//        }
//        case Success(None) => println(s"Order Status for customer ${order.customer.customerName} for item ${order.orderName} : NOT AVAILABLE")
//      })
//    }
//  })).run()
//}
