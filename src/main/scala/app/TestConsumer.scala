package app

import java.util
import java.util.{Properties, UUID}

import com.ovoenergy.kafka.serialization.circe._
import org.apache.kafka.common.serialization.StringDeserializer

import scala.io.Source

// Import the Circe generic support
import io.circe.generic.auto._
import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.collection.JavaConverters._
import io.circe.generic.auto._, io.circe.syntax._


object TestConsumer extends App{

  private val kafkaProperties: Properties = new Properties()
  kafkaProperties.put("bootstrap.servers", "0.0.0.0:9092")
  kafkaProperties.put("max.poll.records", "100")
  kafkaProperties.put("group.id", UUID.randomUUID().toString())
  kafkaProperties.put("auto.offset.reset", "earliest")
  kafkaProperties.put("auto.commit.enable",  "true")

  val kafkaConsumer = new KafkaConsumer(
    kafkaProperties,
    new StringDeserializer(),
    circeJsonDeserializer[Weather]
  )

  kafkaConsumer.subscribe(util.Arrays.asList("w_string"))
  println("Start polling")

  while(true){
    val records = kafkaConsumer.poll(10)
    records.asScala.foreach(record => {
      println(record.key() + ":" + record.value() + ":" + record.offset())
      println("Weather as JSON" + record.value().asJson)
    })

  }

}
object Test extends App {
  val bufferedSource = Source.fromURL("https://gist.githubusercontent.com/DenisOgr/f8fa530777f5db138aca0af22d861fcf/raw/80abdb992327272fbee321ca068988c2c1d47b19/data_v3.csv")
  var data = bufferedSource.getLines().drop(1).map(_.split(",")).toList
  for (d <- data) {
    println("Data point: " + d.mkString(" "))
  }
}