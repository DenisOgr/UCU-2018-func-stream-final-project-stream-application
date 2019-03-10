control_url = http://0.0.0.0:9021

.PHONY: .remove_weather_connect
remove_weather_connect:
	curl -X DELETE ${control_url}/2.0/management/connect/connectors/weather-connector

.PHONY: .remove_solar_connect
remove_solar_connect:
	curl -X DELETE ${control_url}/2.0/management/connect/connectors/solar-connector

.PHONY: .set_topic_partition
set_topic_partition:
	@echo For topic: \"$(topic)\" set number of partitions: \"$(num)\"
	@docker-compose exec broker /usr/bin/kafka-topics --alter --zookeeper zookeeper --topic ${topic} --partitions ${num}
	@make describe_of_topic

.PHONY: .set_connect_workers #TODO
set_connect_workers:
	@docker-compose run connect-config /app/set_connect_workers --topic=${topic} --num=${num}

.PHONY: .set_app_scale
set_app_scale:
	@docker-compose scale app=$(num)

.PHONY: .logs_connect
logs_connect:
	@docker-compose logs -f connect

.PHONY: .list_of_topics
list_of_topics:
	@docker-compose exec broker /usr/bin/kafka-topics --list --zookeeper zookeeper


.PHONY: .describe_of_topic
describe_of_topic:
	@docker-compose exec broker /usr/bin/kafka-topics  --zookeeper zookeeper --describe --topic ${topic}


