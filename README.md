# UCU-2018-func-stream-final-project-stream-application

## Architecture / design of system :
Our application consists of:
* Weather provider (Kafka connect) https://github.com/DenisOgr/UCU-2018-func-stream-final-project-weather-provider
* Solar provider (Kafka connect) https://github.com/DenisOgr/UCU-2018-func-stream-final-project-kafka-provider-solar-generator
* Stream app (Kafka streams) https://github.com/DenisOgr/UCU-2018-func-stream-final-project-stream-application
* Data endpoint: https://gist.github.com/DenisOgr/f8fa530777f5db138aca0af22d861fcf

The Weather provider implements the Kafka connect interface. Using https://github.com/snowplow/scala-weather and https://openweathermap.org/ for collecting weather data. Weather collection points https://gist.github.com/DenisOgr/f8fa530777f5db138aca0af22d861fcf
* Topic: weather,
* Key: STRING/UID (ex: a2478b93-068b-42a2-b84f-3560d193d78e),
* Value: JSON (case class Weather), ex:
     ```
     {
      "temp" : 272.15,
      "pressure" : 1018,
      "temp_min" : 272.15,
      "temp_max" : 272.15,
      "wind_speed" : 6,
      "clouds" : 75,
      "humidity" : 86
     }
     ```


Solar provider implements the Kafka connect interface. Generates sensory data for https://gist.github.com/DenisOgr/f8fa530777f5db138aca0af22d861fcf
*  Topic: solar
*  Key: STRING/UID (ex: a2478b93-068b-42a2-b84f-3560d193d78e)
*  Value: JSON (case class Signal), ex:
     ```
     {
      "panel_uid" : a2478b93-068b-42a2-b84f-3560d193d78e,
      "sensor_1" : 1018,
      "sensor_2" : 272.15,
      "sensor_3" : 2.15,
      "sensor_4" : 6.54,
      "sensor_5" : 3.44
     }
    ```
Stream app. Using Kafka streams.
* Topic: solar
*  Key: STRING/UID (ex: a2478b93-068b-42a2-b84f-3560d193d78e)
* Value: JSON , ex:
```
{
      "temp" : 272.15,
      "pressure" : 1018,
      "temp_min" : 272.15,
      "temp_max" : 272.15,
      "wind_speed" : 6,
      "clouds" : 75,
      "humidity" : 86,
      "wheather" : {
         "panel_uid" : a2478b93-068b-42a2-b84f-3560d193d78e,
         "sensor_1" : 1018,
         "sensor_2" : 272.15,
         "sensor_3" : 2.15,
         "sensor_4" : 6.54,
         "sensor_5" : 3.44
     }
}

```
## Scaling
To implement the project, we use Kafka connect, Kafka and docker. Therefore, scaling can be done using these two technologies.
Kafka connect is the manager of the number of workers himself, so using REST you can change the number of keys. Also Kafka connect automaticly increase tasks.
In Kafka, it makes no sense to increase the number of workers in a group (each connection is one group) without increasing the number of topic partitions.
Stream application can be scaled using docker.

#### Practice:
To make a scale (increase) you need:
 
* Increasing the number of patricians in the topics:
  ```
  make set_topic_partition topic=solar|wheater|stream num=2
  ```
* Increase the number of connects :
  ```
  make set_connect_workers connect=solar|wheater num=2
  ```
* Increase the number of headlights application
  ```
  make set_app_scale num=3
  ```


### How to test
#### Briefly:
In order to run, you need to build the connections in to "fat jar" and put them in a folder ({current_dir}/jars), so that when the broker service starts, these connections are available inside Kafka.
Set up connections (via REST) for streaming weaher or solar sensors
Run Stream app.

#### More details:
To build connections from source codes (more information in README inside connection repository):
```
// In each connection dir
make build
make assembly
//copy .jar to {current_dir}/jars
```
So, you need add 2 jar to Kafka Conenct.

Run 
```
docker-compose up -d
```

Configure/create connection:
For Solar provider
```
  make set_connect_workers connect=solar num=1
 ```
 For Weather provider
```
  make set_connect_workers connect=wheater num=1
 ```

Thats all!

### Additionally:
You can view the results of the stream using the control panel (web, GUI) 0.0.0.0:9021
See the connect service logs (for debauch of connections): 
```
make logs_connect
```

# For developers:
#### Manage connector inside Kafka
Kafka Connect service supports a REST API for managing connectors. ()[https://docs.confluent.io/current/connect/references/restapi.html]




Add weather connector:
```
curl -X PUT \
  http://0.0.0.0:9021/2.0/management/connect/connectors/weather-connector/config \
  -H 'Content-Type: application/json' \
  -d '{
  "connector.class": "ua.ucu.edu.WeatherConnector",
  "name": "weather-connector",
  "value.converter": "org.apache.kafka.connect.storage.StringConverter",
  "AppId": "<API KEY FROM https://home.openweathermap.org/api_keys>",
  "KafkaTopic": "weather",
  "DataFile": "https://gist.githubusercontent.com/DenisOgr/f8fa530777f5db138aca0af22d861fcf/raw/80abdb992327272fbee321ca068988c2c1d47b19/data_v3.csv"
}'
```
Add solar connector:
```
curl -X PUT \
  http://0.0.0.0:9021/2.0/management/connect/connectors/solar-connector/config \
  -H 'Content-Type: application/json' \
  -d '{
  "connector.class": "ua.ucu.edu.SolarPanelConnector",
  "name": "solar-connector",
  "value.converter": "org.apache.kafka.connect.storage.StringConverter",
  "KafkaTopic": "solar",
  "PanelUIDs": "'8d5e230a-e05d-4823-b7a8-8a553330d259', '02b92cfd-6143-49b9-ab74-0eb57b4263b9'"
}'
```

Remove/stop weather connector:
```
curl -X DELETE \
  http://0.0.0.0:9021/2.0/management/connect/connectors/weather-connector
```
Remove/stop weather connector:
```
curl -X DELETE \
  http://0.0.0.0:9021/2.0/management/connect/connectors/solar-connector
```

