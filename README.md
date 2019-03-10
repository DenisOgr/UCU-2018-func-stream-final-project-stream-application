# UCU-2018-func-stream-final-project-stream-application

## Build connector from source
You need to clone next repositories:
- https://github.com/DenisOgr/UCU-2018-func-stream-final-project-weather-provider (Weather provider)
- https://github.com/DenisOgr/UCU-2018-func-stream-final-project-kafka-provider-solar-generator (Solar signal provider)

Build jars using instruction inside each project and copy jars to directory jars/

## Run
Using docker-compose:
```jshelllanguage
   cd {root_path}
   docker-compose up -d 
```


## Manage connector inside Kafka
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
  "tasks.max": "10",
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
  "tasks.max": "10",
  "PanelUIDs": "'8d5e230a-e05d-4823-b7a8-8a553330d259', '02b92cfd-6143-49b9-ab74-0eb57b4263b9'"
}'
```

Remove/stop weather connector:
```
make remove_weather_connect
```
Remove/stop weather connector:
```
make remove_solar_connect
```


## Getting logs from Kafka Connect 
This is very useful during development:
```
cd {root_path}
docker-compose logs -f connect
```
 
## Getting messages from weather/solar topic
This is very useful during development:

```
cd {root_path}
docker-compose exec broker /usr/bin/kafka-console-consumer --bootstrap-server localhost:9092 \
        --topic weather \
        --from-beginning \
        --formatter kafka.tools.DefaultMessageFormatter \
        --property print.key=true \
        --property print.value=true \
        --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
        --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

```
cd {root_path}
docker-compose exec broker /usr/bin/kafka-console-consumer --bootstrap-server localhost:9092 \
        --topic solar \
        --from-beginning \
        --formatter kafka.tools.DefaultMessageFormatter \
        --property print.key=true \
        --property print.value=true \
        --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
        --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
```