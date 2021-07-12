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

    private MQClient mqClient = new MQClient();


    @Test
    public void sendMessageTest() throws JMSException {
        createQueueRestTest("test.queue");

        mqClient.send("some test msg 1", "test.queue");
        mqClient.send("some test msg 2", "test.queue");
        mqClient.send("some test msg 3", "test.queue");

        mqClient.getMessages("test.queue");
        mqClient.getMessages("test.queue");
    }

    @Test
    public void clearQueueTest() {
        clearQueueRestTest("test.queue");
    }

    @Test
    public void deleteQueueTest() {
        deleteQueueRestTest("test.queue");
    }

    public Cookies authRestTest() {
        Map<String, String> auth = new HashMap<>();
        auth.put("username", "admin");
        auth.put("password", "passw0rd");

        return RestAssured.given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .when()
                .body(auth)
                .post("https://localhost:9443/ibmmq/rest/v2/login")
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
                .get("https://localhost:9443/ibmmq/rest/v1/admin/qmgr")
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
                .post("https://localhost:9443/ibmmq/rest/v1/admin/qmgr/QM1/queue/")
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
                .delete("https://localhost:9443/ibmmq/rest/v1/admin/qmgr/QM1/queue/" + queueName + "?purge")
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