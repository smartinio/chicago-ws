package io.smartin.id1212.testutils;

import akka.actor.ActorRef;
import akka.actor.UntypedAbstractActor;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketClientActor extends UntypedAbstractActor {

    /** The web socket connection of the current client. */
    private final WebSocketClientEndpoint webSocketClientEndpoint;
    /** The reference of the sender actor.*/
    private ActorRef senderRef;

    /** Creates the web socket client as an actor.*/
    public WebSocketClientActor(ActorRef actor) throws URISyntaxException, IOException, DeploymentException {
        var webSocketURI = new URI("ws://localhost:8080/game");

        this.webSocketClientEndpoint = new WebSocketClientEndpoint(webSocketURI);

        this.senderRef = actor;

        this.webSocketClientEndpoint.setOnMessageHandler(
                message -> this.senderRef.tell( message, getSelf() )
        );
    }

    /** Is called when a message is received from another actor.
     *
     * @param message The received message.
     */
    @Override
    public void onReceive(Object message) throws IOException {
        this.senderRef = getSender();
        this.webSocketClientEndpoint.sendMessage( (String) message );
    }
}