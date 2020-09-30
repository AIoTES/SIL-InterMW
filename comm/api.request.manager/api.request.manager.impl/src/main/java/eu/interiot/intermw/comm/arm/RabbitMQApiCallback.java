package eu.interiot.intermw.comm.arm;

import eu.interiot.intermw.comm.broker.rabbitmq.QueueImpl;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.ApiCallback;
import eu.interiot.intermw.commons.model.Client;
import eu.interiot.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RabbitMQApiCallback implements ApiCallback<Message> {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQApiCallback.class);
    private QueueImpl queue;
    private String clientId;

    public RabbitMQApiCallback(String clientId, QueueImpl queue) throws MiddlewareException {
        this.queue = queue;
        this.clientId = clientId;
        String queueName = queue.createQueue(clientId);
        logger.debug("RabbitMQApiCallback has been created successfully. Messages will be sent to the queue {}.", queueName);
    }

    @Override
    public void handle(Message message) throws IOException {
        queue.sendMessage(message, clientId);
    }

    @Override
    public void stop() {
        // nothing has to be done
    }

    @Override
    public void update(Client client) throws Exception {

    }
}
