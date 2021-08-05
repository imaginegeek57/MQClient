package com.mq;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import org.junit.jupiter.api.Test;
import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

//@Testcontainers
class MQClientTest {

//    @Container
//    public GenericContainer mqContainer = new GenericContainer(DockerImageName.parse("ibmcom/mq:9.1.1.0"))
//            .withExposedPorts(1414);

    private final MQClient mqClient = new MQClient();


    @Test
    public void sendMessageTest() throws JMSException {
//        mqClient.createQueueRestTest("TEST.QUEUE.TF2");

        mqClient.send("some test msg 1", "TEST.QUEUE.TF");
        mqClient.send("some test msg 2", "TEST.QUEUE.TF");
        mqClient.send("some test msg 3", "TEST.QUEUE.TF");
        mqClient.send("some test msg 4", "TEST.QUEUE.TF");

        mqClient.getMessages("TEST.QUEUE.TF");
        mqClient.getMessages("TEST.QUEUE.TF");
//        mqClient.getMessages("TEST.QUEUE.TF");
//        mqClient.getMessages("TEST.QUEUE.TF");
//        mqClient.getMessages("TEST.QUEUE.TF");
    }

    @Test
    public void checkConnection() {
        mqClient.checkRestTest();
    }

    @Test
    public void clearQueueTest() {
        mqClient.clearQueueRestTest("test.queue");
    }

    @Test
    public void deleteQueueTest() {
        mqClient.deleteQueueRestTest("TEST.QUEUE.TF");
    }

}