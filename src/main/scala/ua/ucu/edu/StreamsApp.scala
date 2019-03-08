package ua.ucu.edu

import java.io.FileInputStream
import java.util
import java.util.{Properties, UUID}
import collection.JavaConverters._

import ua.ucu.edu.models.{Signal, SignalEnriched, Weather}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Serde, StringDeserializer}
import org.apache.kafka.streams.scala.{Serdes, StreamsBuilder}
import org.apache.kafka.streams.scala.kstream.{Consumed, Joined, KStream, Produced}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}



// Import the Circe generic support
import io.circe.generic.auto._

class StreamsApp {
  def joinStreams(): Topology = {

    import org.apache.kafka.streams.scala.Serdes._

    val builder = new StreamsBuilder

    implicit val weatherValueSerde: Serde[Weather] = CirceSerdes.serde[Weather]
    val weather = builder.table[String, Weather]("weather")(Consumed.`with`[String, Weather])

    implicit val signalValueSerde: Serde[Signal] = CirceSerdes.serde[Signal]
    val sensorStream = builder.stream[String, Signal]("solar")(Consumed.`with`[String, Signal])

    val enrichedDataStream: KStream[String, SignalEnriched] = sensorStream.join(weather)(( signal: Signal, weather: Weather) => {
      println(s"sensor :  $signal weather: $weather")
      SignalEnriched(signal = signal, weather = weather)
    })(Joined.`with`[String, Signal, Weather])

    enrichedDataStream.to("sensor_weather_output")(Produced.`with`[String, SignalEnriched](Serdes.String, CirceSerdes.serde[SignalEnriched]))
    builder.build()
  }
}

object StreamsApp extends App {
  val props = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test_app")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "0.0.0.0:29092")
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  props.put("max.poll.records", "100")

  val newTopicProps = new Properties()
  newTopicProps.load(new FileInputStream("kafkatopics.properties"))

  val localKafkaAdmin = AdminClient.create(props)

  val createTopic = (name : String) => {
    new NewTopic(name,
      Integer.parseInt(newTopicProps.getProperty("partitions")),
      Integer.parseInt(newTopicProps.getProperty("replication")).toShort)
  }

  val topics = List("weather", "solar", "sensor_weather_output").map(createTopic).asJava

  val topicStatus = localKafkaAdmin.createTopics(topics).values()

  val app = new StreamsApp
  val streams = new KafkaStreams(app.joinStreams(), props)
  streams.cleanUp()
  streams.start()

  sys.ShutdownHookThread {
    streams.close()
  }

}