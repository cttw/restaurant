package KinesisStream

import akka.actor.ActorSystem
import akka.stream.alpakka.kinesis.KinesisFlowSettings
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{InitialPositionInStream, KinesisClientLibConfiguration}
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration

import scala.concurrent.duration._


object StreamSettings {
  
  val system = ActorSystem("RESTAURANT")
  
  val flowSettings = KinesisFlowSettings
    .create()
    .withParallelism(1)
    .withMaxBatchSize(500)
    .withMaxRecordsPerSecond(1000)
    .withMaxBytesPerSecond(1000000)
    .withMaxRetries(5)
    .withBackoffStrategy(KinesisFlowSettings.Exponential)
    .withRetryInitialTimeout(100.milli)
  
//  val producerConfig = new KinesisProducerConfiguration().
//    setCredentialsProvider(new DefaultAWSCredentialsProviderChain).
//    setRegion("us-east-2")
  
  
  val consumerConfig = new KinesisClientLibConfiguration(
      "restaurant",
      "rd_test_stream",
      new DefaultAWSCredentialsProviderChain,
      s"kinesisWorker-${java.util.UUID.randomUUID().toString}"
    )
    .withRegionName("us-east-2")
    .withCallProcessRecordsEvenForEmptyRecordList(true)
    .withInitialPositionInStream(InitialPositionInStream.TRIM_HORIZON)
}
