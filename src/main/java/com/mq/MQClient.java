package com.mq;

import com.ibm.mq.jms.MQConnectionFactory;
import javax.jms.*;


public class MQClient {

    private static MQConnectionFactory initFactory(MQConnectionFactory connectionFactory) throws JMSException {
        // docker image: ibmcom/mq:9.1.1.0
        connectionFactory.setHostName("localhost");
        connectionFactory.setPort(1414);
        connectionFactory.setQueueManager("QM1");
        connectionFactory.setChannel("DEV.ADMIN.SVRCONN");
        connectionFactory.setTransportType(1);

        return connectionFactory;
    }

    private static Connection createConnection() {
        MQConnectionFactory connectionFactory = new MQConnectionFactory();
        Connection connection = null;
        try {
            connection = initFactory(connectionFactory).createConnection("admin", "passw0rd");
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return connection;
    }


    public static void send(String message) throws JMSException {
        // Producer
        try {
            Session session = createConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("DEV.QUEUE.1");
            Message msg = session.createTextMessage(message);
            MessageProducer producer = session.createProducer(queue);
            System.out.println("Sending text: " + message);
            producer.send(msg);
//    }
//
//    public static void get() throws JMSException {
//        // Consumer
//        Session session = createConnection().createSession(false,
//                Session.AUTO_ACKNOWLEDGE);
//        Queue queue = session.createQueue("DEV.QUEUE.1");

//        MessageConsumer consumer = session.createConsumer(queue);

            QueueReceiver queueReceiver = (QueueReceiver) session.createConsumer(queue);


            Message message2 = queueReceiver.receive();


//        Message message2 = consumer.receive();
//        String id = message2.getJMSMessageID();
            System.out.println(message2);

//        MessageConsumer consumer = session.createConsumer(queue);
            System.out.println("check 1");
//        consumer.receive().

//        TextMessage textMsg = (TextMessage) consumer.receive();
            System.out.println("check 2");

//        System.out.println(textMsg);
//        System.out.println("Received: " + textMsg.getText());
//        session.close();
        } finally {
            if (createConnection() != null) {
                createConnection().close();
            }
        }
    }

    public static void main(String[] args) throws JMSException {
        send("some test message");
//        get();
    }


}
