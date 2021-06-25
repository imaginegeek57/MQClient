package com.mq;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.jms.JMSException;
import java.io.IOException;
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
    public void mqTest() throws JMSException, IOException {
        createQueueRestTest("test.queue");

        mqClient.send("some test msg 1", "test.queue");
        mqClient.send("some test msg 2", "test.queue");
        mqClient.send("some test msg 3", "test.queue");

        mqClient.getMessages("test.queue");
        mqClient.getMessages("test.queue");

        removeQueueRestTest("test.queue");
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void removeQueueRestTest(String queueName) {
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
}