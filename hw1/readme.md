1. Установил Java JDK
2. Создал docker-compose.yml
3. Запустил Zookeeper и Kafka Broker `docker compose -f .\docker-compose.yml up -d`
4. Создал топик `docker exec -ti hw1-kafka-1 /usr/bin/kafka-topics --create --topic test --partitions 1 --replication-factor 1 --bootstrap-server kafka:9092`
5. Запустил консольный продюсер `docker exec -ti hw1-kafka-1 /usr/bin/kafka-console-producer --topic test --bootstrap-server kafka:9092`
6. Записал несколько сообщений в топик, прервал - `Ctrl+Z`
7. Прочитал сообщения из топика `docker exec -ti hw1-kafka-1 /usr/bin/kafka-console-consumer --from-beginning --topic test --bootstrap-server kafka:9092`
