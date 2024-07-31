package org.example;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;


public class Counter {
    private static final Logger log = LoggerFactory.getLogger(Counter.class);
    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();
        Serde<String> stringSerde = Serdes.String();
        var countResult = builder.stream("events", Consumed.with(stringSerde, stringSerde))
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                .count();
        countResult.toStream().foreach((k,v) -> log.info("Window {}: {}", k, v));
        var topology = builder.build();
        log.info("{}", topology.describe());
        try (KafkaStreams kafkaStreams = new KafkaStreams(
                topology,
                new StreamsConfig(Map.of(
                        StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9091",
                        StreamsConfig.APPLICATION_ID_CONFIG, "hw4")))){
            kafkaStreams.start();
            log.info("Consumer started");
            Thread.sleep(20000);
            log.info("Consumer stopped");
        } catch (InterruptedException e) {
            log.warn(e.getMessage());
        }
    }
}