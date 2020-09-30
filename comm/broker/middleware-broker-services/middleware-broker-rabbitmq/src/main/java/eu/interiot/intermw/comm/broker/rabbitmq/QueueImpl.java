package eu.interiot.intermw.comm.broker.rabbitmq;

import eu.interiot.intermw.comm.broker.BrokerContext;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.message.Message;
import eu.interiot.message.exceptions.MessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QueueImpl extends AbstractRabbitService {

    private Logger log = LoggerFactory.getLogger(QueueImpl.class);

    public QueueImpl() throws MiddlewareException {
        this.broker = BrokerContext.getBroker("rabbitmq");
        super.initConnection(broker);
    }

    public String createQueue(String clientId) throws BrokerException {

        ConnectionFactory cf = ResourceManager.getInstance(broker.getConfiguration()).getConnectionFactory();
        RabbitAdmin rabbitAdmin = new RabbitAdmin(cf);
        String queueName = getQueueName(clientId);

        if (rabbitAdmin.getQueueProperties(queueName) == null) {
            Queue queue = new org.springframework.amqp.core.Queue(queueName, true, false, false);
            rabbitAdmin.declareQueue(queue);
            log.debug("A queue {} has been created in RabbitMQ for the client {}.", queueName, clientId);
        } else {
            log.debug("Queue {} already exists.", queueName);
        }
        return queueName;
    }

    public void deleteQueue(String clientId) throws BrokerException {

        ConnectionFactory cf = ResourceManager.getInstance(broker.getConfiguration()).getConnectionFactory();
        RabbitAdmin rabbitAdmin = new RabbitAdmin(cf);
        String queueName = getQueueName(clientId);
        rabbitAdmin.deleteQueue(queueName);
        log.debug("The queue {} has been deleted.", queueName);
    }

    public Message consumeMessage(String clientId) throws IOException, MessageException {
        String queueName = getQueueName(clientId);
        org.springframework.amqp.core.Message rabbitMessage = template.receive(queueName);
        if (rabbitMessage != null) {
            String messageString = new String(rabbitMessage.getBody(), StandardCharsets.UTF_8);
            return new Message(messageString);
        } else {
            return null;
        }
    }

    public Message consumeMessage(String clientId, long timeoutMillis) throws IOException, MessageException {
        String queueName = getQueueName(clientId);
        org.springframework.amqp.core.Message rabbitMessage = template.receive(queueName, timeoutMillis);
        if (rabbitMessage != null) {
            String messageString = new String(rabbitMessage.getBody(), StandardCharsets.UTF_8);
            return new Message(messageString);
        } else {
            return null;
        }
    }

    public List<String> consumeMessages(String clientId, int limit) throws IOException, MessageException {
        String queueName = getQueueName(clientId);
        List<String> messages = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            org.springframework.amqp.core.Message rabbitMessage = template.receive(queueName);
            if (rabbitMessage != null) {
                String messageString = new String(rabbitMessage.getBody(), StandardCharsets.UTF_8);
                messages.add(messageString);
            } else {
                break;
            }
        }

        return messages;
    }

    public List<String> consumeMessagesBlocking(String clientId, int limit) {
        String queueName = getQueueName(clientId);
        if (limit < 1) {
            limit = 1;
        }
        List<String> messages = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            org.springframework.amqp.core.Message rabbitMessage;
            if (i == 0) {
                rabbitMessage = template.receive(queueName, -1);
            } else {
                rabbitMessage = template.receive(queueName);
            }

            if (rabbitMessage == null) {
                break;
            }
            String messageString = new String(rabbitMessage.getBody(), StandardCharsets.UTF_8);
            messages.add(messageString);
        }

        return messages;
    }

    public void sendMessage(Message message, String clientId) throws IOException {
        String queueName = getQueueName(clientId);
        String messageString = message.serializeToJSONLD();
        org.springframework.amqp.core.Message rabbitMessage = MessageBuilder.withBody(messageString.getBytes()).build();
        template.send(queueName, rabbitMessage);
        log.debug("Message has been published to an exchange with routing key {}.", queueName);
    }

    public void sendMessage(String serializedMessage, String clientId) {
        String queueName = getQueueName(clientId);
        org.springframework.amqp.core.Message rabbitMessage = MessageBuilder.withBody(serializedMessage.getBytes()).build();
        template.send(queueName, rabbitMessage);
        log.debug("Message has been published to an exchange with routing key {}.", queueName);
    }

    private String getQueueName(String clientId) {
        return "client-" + clientId.replaceAll("[:/#]+", "_");
    }
}
