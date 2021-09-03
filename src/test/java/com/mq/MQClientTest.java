package com.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import javax.jms.JMSException;

//@Testcontainers
class MQClientTest {

//    @Container
//    public GenericContainer mqContainer = new GenericContainer(DockerImageName.parse("ibmcom/mq:9.1.1.0"))
//            .withExposedPorts(1414);

    private final MQClient mqClient = new MQClient();

    @Test
    public void sendMessageTest() throws JMSException {
        mqClient.createQueueRestTest("TEST.QUEUE.TF1");

//        mqClient.send("some test msg 1", "TEST.QUEUE.TF1");
//        mqClient.send("some test msg 2", "TEST.QUEUE.TF1");

        mqClient.sendMessageRest("TEST.QUEUE.TF1", "some test msg 1");
        mqClient.sendMessageRest("TEST.QUEUE.TF1", "some test msg 2");
        mqClient.sendMessageRest("TEST.QUEUE.TF1", "some test msg 3");
        mqClient.sendMessageRest("TEST.QUEUE.TF1", "some test msg 4");

        mqClient.getMessageRest("TEST.QUEUE.TF1");
        mqClient.getMessageRest("TEST.QUEUE.TF1");
    }

    @Test
    public void checkConnection() {
        mqClient.checkRestTest();
    }

    @Test
    public void createQueue() {
        mqClient.createQueueRestTest("TEST.QUEUE.TF");
    }

    @Test
    public void clearQueueTest() throws JsonProcessingException {
        mqClient.clearQueueRestTest("TEST.QUEUE.TF1");
    }

    @Test
    public void deleteQueue() {
        mqClient.deleteQueueRestTest("TEST.QUEUE.TF1");
    }
}