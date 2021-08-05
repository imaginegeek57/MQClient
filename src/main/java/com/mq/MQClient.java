package com.mq;

import com.ibm.jms.JMSMessage;
import com.ibm.jms.JMSTextMessage;
import com.ibm.mq.jms.*;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.jms.JMSException;
import javax.jms.QueueSender;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class MQClient {
    private static final Logger log = LogManager.getLogger(MQClient.class);


    public static MQQueueSession getSession() throws JMSException {
        MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
        // docker image: ibmcom/mq:9.1.1.0
        connectionFactory.setHostName("s-msk-v-mq01.raiffeisen.ru");
        connectionFactory.setPort(1415);
        connectionFactory.setQueueManager("QMDEV01");
        connectionFactory.setChannel("QAS.DEV01");
        connectionFactory.setTransportType(1);

        MQQueueConnection connection = (MQQueueConnection) connectionFactory.createQueueConnection();
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

    public Cookies authRestTest() {
        Map<String, String> auth = new HashMap<>();
        auth.put("username", "");
        auth.put("password", "");

        return given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .when()
                .body(auth)
                .post("https://s-msk-v-mq01.raiffeisen.ru:9443/ibmmq/rest/v1/login")
                .then()
                .statusCode(204)
                .extract()
                .response()
                .getDetailedCookies();
    }

    public void checkRestTest() {
        given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .cookie(String.valueOf(authRestTest()))
                .when()
                .get("https://s-msk-v-mq01.raiffeisen.ru:9443/ibmmq/rest/v1/ruamdva/qmgr/QMWEB01/queue")
                .then()
                .statusCode(200);
    }

    public void createQueueRestTest(String queueName) {
        Map<String, String> headers = new HashMap<>();
        headers.put("ibm-mq-rest-csrf-token", "v");
        Map<String, String> body = new HashMap<>();
        body.put("name", queueName);

        given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .cookie(String.valueOf(authRestTest()))
                .headers(headers)
                .body(body)
                .when()
                .post("https://s-msk-v-mq01.raiffeisen.ru:9443/ibmmq/rest/v1/ruamdva/qmgr/QMDEV01/queue/")
                .then()
                .statusCode(201);
    }

    public void deleteQueueRestTest(String queueName) {
        Map<String, String> headers = new HashMap<>();
        headers.put("ibm-mq-rest-csrf-token", "v");

        given()
                .relaxedHTTPSValidation()
                .cookie(String.valueOf(authRestTest()))
                .headers(headers)
                .when()
                .delete("https://s-msk-v-mq01.raiffeisen.ru:9443/ibmmq/rest/v1/ruamdva/QMDEV01/queue/" + queueName + "?purge")
                .then()
                .statusCode(204);
    }

    public void clearQueueRestTest(String queueName) {
        Map<String, String> headers = new HashMap<>();
        headers.put("ibm-mq-csrf-token", "v");

        given()
                .relaxedHTTPSValidation()
                .cookie(String.valueOf(authRestTest()))
                .headers(headers)
                .when()
                .delete("https://localhost:9443/ibmmq/console/internal/ibmmq/qmgr/QM1/queue/" + queueName + "/messages")
                .then()
                .statusCode(200);
    }
}