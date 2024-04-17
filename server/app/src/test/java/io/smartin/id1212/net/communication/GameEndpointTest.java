package io.smartin.id1212.net.communication;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

import io.smartin.id1212.net.dto.Action;
import io.smartin.id1212.net.dto.Message;
import io.smartin.id1212.net.services.Converter;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.websocket.server.WsSci;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.smartin.id1212.testutils.WebSocketClientActor;

import java.io.File;
import java.time.Duration;

import static io.smartin.id1212.net.dto.Action.ActionType.*;
import static io.smartin.id1212.net.dto.Message.MessageType.PONG;

public class GameEndpointTest {
    private static ActorSystem actorSystem;
    private static Tomcat tomcat;

    public static void startTomcat() throws Exception {
        tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        // Create a standard context and set the WebSocket initializer
        var path = new File("").getAbsolutePath();
        System.out.println(path);
        StandardContext ctx = (StandardContext) tomcat.addWebapp("", path);
        ctx.addServletContainerInitializer(new WsSci(), null);

        tomcat.start();
    }

    @BeforeAll
    public static void setUp() throws Exception {
        startTomcat();
        actorSystem = ActorSystem.create("web_socket_test_automation_system");
    }

    @AfterAll
    public static void tearDown() throws LifecycleException {
        actorSystem.terminate();
        actorSystem = null;
        tomcat.stop();
        tomcat = null;
    }

    @Test
    public void pingTest() {
        new TestKit(actorSystem) {
            {
                final var clientReference = actorSystem.actorOf( Props.create(WebSocketClientActor.class) );
                final var req = Converter.toJson(new Action(PING, "ping"));
                final var res = Converter.toJson(new Message(PONG, "pong"));

                clientReference.tell(req, getRef());

                expectMsg(Duration.ofSeconds(1), res);

                expectNoMessage();
            }
        };
    }
}

