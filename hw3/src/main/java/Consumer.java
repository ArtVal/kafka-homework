import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class Consumer {
    public static void main(String[] args) {
        readTopics();
    }
    public static void readTopics() {
        Properties props = new Properties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9091");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-1");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        ConsumerRecords<String, String> records = null;
        try (KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props)) {
            System.out.println("TRANSACTIONAL CONSUMER\n---------------------------------");
            kafkaConsumer.subscribe(Arrays.asList(Producer.topic1Name, Producer.topic2Name));
            while (!(records = kafkaConsumer.poll(Duration.ofSeconds(10))).isEmpty()) {
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("%s: %s".formatted(record.topic(), record.value()));
                }
            }
        }
    }
}
