package sd.p07.support;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/** GIVEN - Kafka client boilerplate, so the day briefs are about semantics, not configuration. */
public final class KafkaSupport {

    private KafkaSupport() {
    }

    public static void createTopic(String bootstrapServers, String topic, int partitions) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (Admin admin = Admin.create(props)) {
            admin.createTopics(List.of(new NewTopic(topic, partitions, (short) 1))).all().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted creating " + topic, e);
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof org.apache.kafka.common.errors.TopicExistsException)) {
                throw new IllegalStateException("could not create " + topic, e);
            }
        }
    }

    public static KafkaProducer<String, String> producer(String bootstrapServers,
                                                         Map<String, Object> overrides) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.putAll(overrides);
        return new KafkaProducer<>(props);
    }

    public static KafkaConsumer<String, String> consumer(String bootstrapServers, String groupId,
                                                         Map<String, Object> overrides) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10_000);
        props.putAll(overrides);
        return new KafkaConsumer<>(props);
    }
}
