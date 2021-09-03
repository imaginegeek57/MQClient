package com.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.jms.JMSMessage;
import com.ibm.jms.JMSTextMessage;
import com.ibm.mq.jms.*;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
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
        connectionFactory.setHostName("s-msk-v-mq01.raiffeisen.ru");
        connectionFactory.setPort(1420);
        connectionFactory.setQueueManager("QMWEB01");
        connectionFactory.setChannel("DEV.WEB01");
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

    public Cookies authRest() {
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
        Map<String, String> headers = new HashMap<>();
        headers.put("ibm-mq-rest-csrf-token", "v");

        given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .cookie(String.valueOf(authRest()))
                .headers(headers)
                .when()
                .get("https://s-msk-v-mq01.raiffeisen.ru:9443/ibmmq/rest/v1/admin/qmgr/QMWEB01/queue")
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
                .cookie(String.valueOf(authRest()))
                .headers(headers)
                .body(body)
                .when()
                .post("https://s-msk-v-mq01.raiffeisen.ru:9443/ibmmq/rest/v1/admin/qmgr/QMWEB01/queue")
                .then()
                .statusCode(201);
    }

    public void deleteQueueRestTest(String queueName) {
        Map<String, String> headers = new HashMap<>();
        headers.put("ibm-mq-rest-csrf-token", "v");

        given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .cookie(String.valueOf(authRest()))
                .headers(headers)
                .when()
                .delete("https://s-msk-v-mq01.raiffeisen.ru:9443/ibmmq/rest/v1/admin/qmgr/QMWEB01/queue/" + queueName + "?purge")
                .then()
                .statusCode(204);
    }

    public void clearQueueRestTest(String queueName) throws JsonProcessingException {
        Map<String, String> headers = new HashMap<>();
        headers.put("ibm-mq-rest-csrf-token", "v");

        given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .cookie(String.valueOf(authRest()))
                .headers(headers)
                .body(convertMap(queueName))
                .when()
                .post("https://s-msk-v-mq01.raiffeisen.ru:9443/ibmmq/rest/v2/admin/action/qmgr/QMWEB01/mqsc")
                .then()
                .statusCode(200);
    }

    public void sendMessageRest(String queueName, String message) {
        Map<String, String> headers = new HashMap<>();
        headers.put("ibm-mq-rest-csrf-token", "v");

        given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .cookie(String.valueOf(authRest()))
                .headers(headers)
                .body(message)
                .when()
                .post("https://s-msk-v-mq01.raiffeisen.ru:9443/ibmmq/rest/v1/messaging/qmgr/QMWEB01/queue/" + queueName + "/message")
                .then()
                .statusCode(201);

        log.info("Send message: " + message);
        System.out.println("Send message: " + message);
    }

    public void getMessageRest(String queueName) {
        Map<String, String> headers = new HashMap<>();
        headers.put("ibm-mq-rest-csrf-token", "v");

        Response response = given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .cookie(String.valueOf(authRest()))
                .headers(headers)
                .when()
                .delete("https://s-msk-v-mq01.raiffeisen.ru:9443/ibmmq/rest/v1/messaging/qmgr/QMWEB01/queue/" + queueName + "/message")
                .then()
                .statusCode(200)
                .extract()
                .response();

        log.info("Received message: " + response.getBody().asString());
        System.out.println("Received message: " + response.getBody().asString());
    }

    public String convertMap(String queueName) throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> map2 = new HashMap<>();
        map2.put("command", "CLEAR QLOCAL(" + queueName + ")");
        map.put("type", "runCommand");
        map.put("command", "string");
        map.put("parameters", map2);

        return new ObjectMapper().writeValueAsString(map);
    }
}