package KinesisStream

import java.nio.ByteBuffer

import akka.stream.alpakka.kinesis.scaladsl.KinesisSink
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry
import com.contxt.kinesis.ScalaKinesisProducer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import Restaurante.replyStatus
import Restaurante.redis

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object Actions {
  
  val objectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)
  
  implicit val system = StreamSettings.system
  implicit val amazonKinesisAsync: com.amazonaws.services.kinesis.AmazonKinesisAsync =
    AmazonKinesisAsyncClientBuilder.defaultClient()
  
  
  def replyWithOrderStatus(orderName:String, customer: Customer, status: String) ={
    val order = Order(orderName, customer, status)
//    order.source = "RESTAURANT"
    
//    println(s"replying for stauts $status")
    
    replyStatus ! order

  }
  
  def isCustomerAllowedToOrder (order: Order): Boolean = {
    var allowed = false
//    redis.hget("customers", order.customer.customerId).wait()
    val futureValue = redis.hget("customers", order.customer.customerId)
//        .onComplete({
//      case Success(Some(value)) => if (value.utf8String == "0") {
//        replyWithOrderStatus(order, "BLOCKED")
//        allowed = false
//        println(s"customer ${order.customer.customerName} not allowed to order more")
//      }
//        else allowed = true
//
//      case Success(None) => println("could not fetch cusotmer's status")
//      case Failure(exception) => println(s"EXCEPTION occured while checking eligibility: $exception")
//      allowed
//    })
    val y = Await.result(futureValue, 2 seconds).get.utf8String
    if (y == "0"){
      replyWithOrderStatus(order.orderName,order.customer, "BLOCKED")
      allowed = false
    }
    else allowed = true
    println(s"returning $allowed")
    allowed
  }
  
  def acknowledge(order: Order) ={
    println("inside acknowledge")
    replyWithOrderStatus(order.orderName, order.customer, "QUEUED")
  }
  
  def generateBill(order: Order) = {
    replyWithOrderStatus(order.orderName, order.customer,  "BILL_GENERATION_IN-PROGRESS")
    println(s"Order Status for customer ${order.customer.customerName} : BILL_GENERATION_IN-PROGRESS")
    Thread.sleep(5000)
    
    val x = redis.hmset("customers", Map(order.customer.customerId -> "0"))
    Await.result(x, 2 seconds)
    println(s"Order Status for customer ${order.customer.customerName} : BILL GENERATED")
    replyWithOrderStatus(order.orderName, order.customer, "BILL GENERATED")
  
  }
  
  def serveOrder(order: Order) = {
    Thread.sleep(5000)
    replyWithOrderStatus(order.orderName, order.customer,"SERVED")
    println(s"Order Status for customer ${order.customer.customerName} for item ${order.orderName} : SERVED")
  }
}