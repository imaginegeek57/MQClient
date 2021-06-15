package com.mq;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class MQClientTest {

    @Container
    public GenericContainer mqContainer = new GenericContainer(DockerImageName.parse("ibmcom/mq:9.1.1.0"))
            .withExposedPorts(1414);

    private MQClient mqClient = new MQClient();


    @Test
    public void mqTest() {


    }
}