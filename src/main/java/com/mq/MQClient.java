package com.mq;

import com.ibm.jms.JMSMessage;
import com.ibm.jms.JMSTextMessage;
import com.ibm.mq.jms.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.jms.JMSException;
import javax.jms.QueueSender;
import javax.jms.Session;
import java.util.UUID;

public class MQClient {
    private static final Logger log = LogManager.getLogger(MQClient.class);

    public static MQQueueSession getSession() throws JMSException {
        MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
        // docker image: ibmcom/mq:9.1.1.0
        connectionFactory.setHostName("localhost");
        connectionFactory.setPort(1414);
        connectionFactory.setQueueManager("QM1");
        connectionFactory.setChannel("DEV.ADMIN.SVRCONN");
        connectionFactory.setTransportType(1);

        MQQueueConnection connection = (MQQueueConnection) connectionFactory.createQueueConnection("admin", "passw0rd");
        connection.start();
        return (MQQueueSession) connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public MQQueue createQueue(String queueName) throws JMSException {
        return (MQQueue) getSession().createQueue(queueName);
    }

    public void send(String message, String queueName) throws JMSException {
        JMSTextMessage msg = (JMSTextMessage) getSession().createTextMessage(message);
        msg.setJMSCorrelationID(UUID.randomUUID().toString());
        QueueSender producer = getSession().createSender(createQueue(queueName));
        producer.send(msg);
        log.info("Send message with producer:" + msg.toString());
    }

    public void getMessages(String queueName) throws JMSException {
        MQQueueReceiver receiver = (MQQueueReceiver) getSession().createReceiver(createQueue(queueName));
        JMSMessage receivedMessage = (JMSMessage) receiver.receive(1000);
        System.out.println("Received message:" + receivedMessage);
        getSession().close();
        receiver.close();
    }

}