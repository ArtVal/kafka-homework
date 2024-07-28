import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Producer {
    public static String topic1Name = "test1";
    public static String topic2Name = "test2";
    public static void main(String[] args) {
        produce();
    }

    public static void produce() {
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9091");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "hw3");
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        producer.initTransactions();
        producer.beginTransaction();
        String k = "commit key";
        String v = "commit message";
        for (int i = 0; i < 5; i++) {
            producer.send(new ProducerRecord<>(topic1Name, k + i, topic1Name + " " + v + i));
            producer.send(new ProducerRecord<>(topic2Name, k + i, topic2Name + " " + v + i));
        }
        producer.commitTransaction();
        producer.beginTransaction();
        String k1 = "abort key";
        String v1 = "abort message";
        for (int i = 0; i < 2; i++) {
            producer.send(new ProducerRecord<>(topic1Name, k1 + i, topic1Name + " " + v1 + i));
            producer.send(new ProducerRecord<>(topic2Name, k1 + i, topic2Name + " " + v1 + i));
        }
        producer.abortTransaction();

        producer.close();
    }
}
