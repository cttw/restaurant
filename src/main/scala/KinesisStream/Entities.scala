package KinesisStream


case class Customer (customerName: String, customerId: String)
case class Order (orderName: String, customer: Customer, status: String = "", source: String = "RESTAURANT")
//case class Status (order: Order, status: String)