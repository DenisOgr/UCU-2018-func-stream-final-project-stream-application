package app

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.streams.{StreamsConfig, TopologyTestDriver}
import org.apache.kafka.streams.test.{ConsumerRecordFactory, OutputVerifier}
import com.ovoenergy.kafka.serialization.circe._
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.scalatest.FlatSpec
import ua.ucu.edu.{CirceSerdes, StreamsApp}
import ua.ucu.edu.models.{Signal, SignalEnriched, Weather}

class StreamsAppTestSpec extends FlatSpec {
  val testClass = new StreamsApp
  implicit val encodeWeather: Encoder[Weather] = (a: Weather) => Json.obj(
    ("temp", Json.fromBigDecimal(a.temp)),
    ("clouds", Json.fromBigInt(a.clouds)),
    ("humidity", Json.fromBigDecimal(a.humidity)),
    ("pressure", Json.fromBigDecimal(a.pressure)),
    ("temp_max", Json.fromBigDecimal(a.temp_max)),
    ("temp_min", Json.fromBigDecimal(a.temp_min)),
    ("wind_speed", Json.fromBigDecimal(a.wind_speed)),
  )

  implicit val encodeSignal: Encoder[Signal] = (a: Signal) => Json.obj(
    ("panel_uid", Json.fromString(a.panel_uid)),
    ("sensor_1", Json.fromBigDecimal(a.sensor_1)),
    ("sensor_2", Json.fromBigDecimal(a.sensor_2)),
    ("sensor_3", Json.fromBigDecimal(a.sensor_3)),
    ("sensor_4", Json.fromBigDecimal(a.sensor_4)),
    ("sensor_5", Json.fromBigDecimal(a.sensor_5)),

  )

  implicit val decodeWeather: Decoder[Weather] = (c: HCursor) => for {
    temp <- c.downField("temp").as[BigDecimal]
    clouds <- c.downField("clouds").as[BigInt]
    humidity <- c.downField("humidity").as[BigDecimal]
    pressure <- c.downField("pressure").as[BigDecimal]
    temp_max <- c.downField("temp_max").as[BigDecimal]
    temp_min <- c.downField("temp_min").as[BigDecimal]
    wind_speed <- c.downField("wind_speed").as[BigDecimal]
  } yield {
    Weather(
      temp,
      pressure,
      temp_min,
      temp_max,
      wind_speed,
      clouds,
      humidity
    )

  }

  implicit val decodeSignal: Decoder[Signal] = (c: HCursor) => for {
    panel_uid <- c.downField("panel_uid").as[String]
    sensor_1 <- c.downField("sensor_1").as[BigDecimal]
    sensor_2 <- c.downField("sensor_2").as[BigDecimal]
    sensor_3 <- c.downField("sensor_3").as[BigDecimal]
    sensor_4 <- c.downField("sensor_4").as[BigDecimal]
    sensor_5 <- c.downField("sensor_5").as[BigDecimal]
  } yield {
    Signal(panel_uid, sensor_1, sensor_2, sensor_3, sensor_4, sensor_5)
  }


  val weatherFactory = new ConsumerRecordFactory[String, Weather]("weather",
    new StringSerializer, circeJsonSerializer[Weather]
  )

  val sensorFactory = new ConsumerRecordFactory[String, Signal]("solar",
    new StringSerializer, circeJsonSerializer[Signal])

  it should "merge two streams into one" in {
    val testDriver = new TopologyTestDriver(testClass.joinStreams(), config)

    weatherData.zipWithIndex.foreach { case (w, i) =>
      testDriver.pipeInput(weatherFactory.create("weather", i.toString, w))
    }

    sensorData.zipWithIndex.foreach { case (s, i) =>
      testDriver.pipeInput(sensorFactory.create("solar", i.toString, s))
    }

    implicit val decodeFoo: Decoder[SignalEnriched] = new Decoder[SignalEnriched] {
      final def apply(c: HCursor): Decoder.Result[SignalEnriched] =
        for {
          weather <- c.downField("weather").as[Weather]
          signal <- c.downField("signal").as[Signal]
        } yield {
          SignalEnriched(signal, weather)
        }
    }

    assertValue(SignalEnriched(sensorData(0), weatherData(0)))
    assertValue(SignalEnriched(sensorData(1), weatherData(1)))
    assertValue(SignalEnriched(sensorData(2), weatherData(2)))
    assertValue(SignalEnriched(sensorData(3), weatherData(3)))
    assertValue(SignalEnriched(sensorData(4), weatherData(4)))
    assertValue(SignalEnriched(sensorData(5), weatherData(5)))
    assertValue(SignalEnriched(sensorData(6), weatherData(6)))
    assertValue(SignalEnriched(sensorData(7), weatherData(7)))

    def assertValue(expected: SignalEnriched): Unit = {
      OutputVerifier.compareValue(testDriver.readOutput("sensor_weather_output",
        new StringDeserializer, circeJsonDeserializer[SignalEnriched]), expected)
    }
  }


  val weatherData = List(
    Weather(20, 700, 15, 25, 10, 1, 50),
    Weather(21, 700, 15, 25, 10, 1, 50),
    Weather(22, 700, 15, 25, 10, 1, 50),
    Weather(23, 700, 15, 25, 10, 1, 50),
    Weather(24, 700, 15, 25, 10, 1, 50),
    Weather(25, 700, 15, 25, 10, 1, 50),
    Weather(26, 700, 15, 25, 10, 1, 50),
    Weather(27, 700, 15, 35, 10, 1, 50)
  )

  val sensorData = List(
    Signal("1", 1, 1, 1, 1, 1),
    Signal("2", 2, 2, 2, 2, 2),
    Signal("3", 3, 3, 3, 3, 3),
    Signal("4", 4, 4, 4, 4, 4),
    Signal("5", 5, 5, 5, 5, 5),
    Signal("6", 6, 6, 6, 6, 6),
    Signal("7", 7, 7, 7, 7, 7),
    Signal("8", 8, 8, 8, 8, 8)
  )


  val config: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-app")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "0.0.0.0:9092")
    p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    p
  }
}
