import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWith}
import akka.stream.{ActorMaterializer, ClosedShape, SourceShape}
import akka.{Done, NotUsed, kafka}
import ch.qos.logback.classic.{Level, Logger}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.slf4j.LoggerFactory

import java.time.Instant
import scala.concurrent.Future

object Main {
  implicit val system = ActorSystem("hw4")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[Logger]
    .setLevel(Level.ERROR)
  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("akka.kafka.consumer")
  val producerConf = config.getConfig("akka.kafka.producer")

  val producerSettings = ProducerSettings(producerConf, new StringSerializer, new StringSerializer)
  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer); ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG
  val consumer: Source[Int, Consumer.Control] = Consumer.plainSource(consumerSettings, Subscriptions.topics("test")).map(_.value().toInt)
  val producer: Sink[ProducerRecord[String, String], Future[Done]] = Producer.plainSink(producerSettings)
  val kafkaSource: Source[ProducerRecord[String, String], NotUsed] = Source(1 to 5).map(value => new ProducerRecord[String, String]("test", value.toString))

  val graph = GraphDSL.create(){ implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    val input: SourceShape[Int] = builder.add(consumer)
    val mulTo10 = builder.add(Flow[Int].map(_ * 10))
    val mulTo2 = builder.add(Flow[Int].map(_ * 2))
    val mulTo3 = builder.add(Flow[Int].map(_ * 3))
    val broadcast = builder.add(Broadcast[Int](3))
    val zip = builder.add(ZipWith((a: Int, b: Int, c: Int) => a + b + c))
    val output = builder.add(Sink.foreach((value: Int) => println(s"${Instant.now()}  $value")))

    builder.add(kafkaSource) ~> builder.add(producer)
    input ~> broadcast ~> mulTo10 ~> zip.in0
    broadcast ~> mulTo2 ~> zip.in1
    broadcast ~> mulTo3 ~> zip.in2

    zip.out ~> output

    ClosedShape
  }

  def main(args: Array[String]) : Unit ={
    RunnableGraph.fromGraph(graph).run()
    Thread.sleep(60000)
    system.terminate()
  }
}