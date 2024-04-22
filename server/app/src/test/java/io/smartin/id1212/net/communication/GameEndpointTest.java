package io.smartin.id1212.net.communication;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import io.smartin.id1212.model.components.Hand;
import io.smartin.id1212.model.components.Player;
import io.smartin.id1212.model.components.PlayingCard;
import io.smartin.id1212.model.components.Round;
import io.smartin.id1212.net.dto.*;
import io.smartin.id1212.net.services.Converter;
import io.smartin.id1212.testutils.WebSocketClientActor;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.websocket.server.WsSci;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static io.smartin.id1212.net.dto.Action.ActionType.*;
import static io.smartin.id1212.net.dto.Message.MessageType.PONG;
import static org.junit.jupiter.api.Assertions.*;

public class GameEndpointTest {
    private static ActorSystem actorSystem;
    private static Tomcat tomcat;

    public static void startTomcat() throws Exception {
        tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setSilent(true);
        tomcat.getConnector().setAsyncTimeout(120000);

        // Create a standard context and set the WebSocket initializer
        var path = new File("").getAbsolutePath();
        StandardContext ctx = (StandardContext) tomcat.addWebapp("", path);
        ctx.addServletContainerInitializer(new WsSci(), null);

        tomcat.start();
    }

    public static class Actor {
        public ActorRef ref;
        public TestKit kit;
        public String id;

        public Snapshot snapshot;
    }

    private static Actor createActor(TestKit probe) {
        return new Actor() {
            {
                ref = actorSystem.actorOf(Props.create(WebSocketClientActor.class, probe.getRef()));
                kit = probe;
            }
        };
    }

    @BeforeAll
    public static void setUpAll() throws Exception {
        System.setProperty("env", "test");
        startTomcat();
        actorSystem = ActorSystem.create("web_socket_test_automation_system");
    }

    @AfterAll
    public static void tearDown() throws LifecycleException {
        actorSystem.terminate();
        tomcat.stop();
        actorSystem = null;
        tomcat = null;
    }

    @Test
    public void pingTest() {
        new TestKit(actorSystem) {
            {
                var actor = createActor(this);
                var request = Converter.toJson(new Action(PING, "hi"));
                var response = Converter.toJson(new Message(PONG, "hi"));

                actor.ref.tell(request, getRef());

                expectMsg(Duration.ofSeconds(1), response);

                expectNoMessage();
            }
        };
    }

    @Test
    public void fullGameTest() {
        // Create players
        var owner = createActor(new TestKit(actorSystem));

        var otherActors = new Actor[] {
            createActor(new TestKit(actorSystem)),
            createActor(new TestKit(actorSystem)),
            createActor(new TestKit(actorSystem))
        };

        Actor[] actors = new Actor[otherActors.length + 1];
        actors[0] = owner;
        System.arraycopy(otherActors, 0, actors, 1, otherActors.length);

        // Create new game
        var rules = new GameCreation.GameRules(
            true,
            false,
            2,
            45,
            GameCreation.OneOpenMode.FINAL,
            5,
            10
        );

        var gameCreation = Converter.toJson(new GameCreation("P1", rules));
        var newGameAction = Converter.toJson(new Action(NEW_GAME, gameCreation));

        owner.ref.tell(newGameAction, owner.kit.getRef());

        // Have remaining players join the game
        awaitLatestSnapshot(owner);
        var key = owner.snapshot.game.getInvitationKey();

        var playerNo = 2;
        for (var player : otherActors) {
            send(player, JOIN_GAME, new JoinRequest("P" + playerNo, key));
            awaitLatestSnapshot(player);
            player.id = player.snapshot.me.getId();
            playerNo++;
        }

        awaitLatestSnapshot(owner);
        assertEquals(4, owner.snapshot.game.getPlayers().size());

        // Start the game
        sendReceiveAll(owner, START_GAME, null, actors);
        assertEquals(Round.GamePhase.TRADING, getGamePhase(owner));

        while (owner.snapshot.game.getStarted() && getGamePhase(owner) == Round.GamePhase.TRADING) {

            System.out.println("=========== NEW ROUND ===========");

            // Trading
            while (getGamePhase(owner) == Round.GamePhase.TRADING) {
                var currentActor = getCurrentActor(actors);

                var ooplayer = currentActor.snapshot.game.oneOpen.getPlayer();
                System.out.println("oneOpen player:" + (ooplayer != null ? ooplayer.getName() : null));

                if (currentActor.snapshot.game.oneOpen.isOpenFor(currentActor.snapshot.me)) {
                    var accept = currentActor.snapshot.me.getName().equals("P2");
                    sendReceiveAll(currentActor, RESPOND_ONE_OPEN, accept, actors);
                    continue;
                }

                if (currentActor.snapshot.game.resetOthersScore.isPendingFor(currentActor.snapshot.me)) {
                    var accept = currentActor.snapshot.me.getName().equals("P2");
                    sendReceiveAll(currentActor, RESPOND_RESET_OTHERS_SCORE, accept, actors);
                    continue;
                }

                var hand = new Hand(new HashSet<>(currentActor.snapshot.myHand));

                var cards = currentActor.snapshot.myHand;
                Object[] cardsToThrow = new Object[] {};

                switch (hand.getPokerHand().getType()) {
                    case STRAIGHT_FLUSH, FOUR_OF_A_KIND, FULL_HOUSE, FLUSH, STRAIGHT -> {}
                    case THREE_OF_A_KIND -> {
                        var triplet = nOfKind(cards, 3);
                        assertEquals(3, triplet.size());
                        cardsToThrow = cards.stream().filter(c -> !triplet.contains(c)).toArray();
                    }
                    case TWO_PAIR -> {
                        var single = nOfKind(cards, 1);
                        assertEquals(1, single.size());
                        cardsToThrow = single.toArray();
                    }
                    case PAIR -> {
                        var pair = nOfKind(cards, 1);
                        cardsToThrow = cards.stream().filter(c -> !pair.contains(c)).toArray();
                    }
                    default -> {
                        var strongestSuit = Arrays
                                .stream(PlayingCard.Suit.values())
                                .map(s -> cards.stream().filter(c -> c.getSuit().equals(s)).toList())
                                .max(Comparator.comparingInt(List::size))
                                .orElseThrow()
                                .getFirst()
                                .getSuit();

                        cardsToThrow = cards.stream().filter(c -> !c.getSuit().equals(strongestSuit)).toArray();
                    }
                }

                sendReceiveAll(currentActor, THROW, cardsToThrow, actors);
            }

            // Chicago question, first player asked with flush and ace will accept
            while (getGamePhase(owner) == Round.GamePhase.CHICAGO) {
                var currentActor = getCurrentActor(actors);
                var hand = new Hand(new HashSet<>(currentActor.snapshot.myHand));

                var accept = switch (hand.getPokerHand().getType()) {
                    case STRAIGHT_FLUSH, FOUR_OF_A_KIND, FULL_HOUSE, FLUSH, STRAIGHT, THREE_OF_A_KIND -> !currentActor.snapshot.me.getHasTakenChicago();
                    default -> false;
                };

                sendReceiveAll(currentActor, CHICAGO, accept, actors);
            }

            System.out.println("=========== DONE TRADING ===========");

            // Playing
            while (getGamePhase(owner) == Round.GamePhase.PLAYING) {
                var currentActor = getCurrentActor(actors);
                var currentTrick = currentActor.snapshot.game.getCurrentRound().tricks.getLast();
                assertNotNull(currentTrick);
                var startingMove = currentTrick.getStartingMove();
                var startingSuit = startingMove != null ? startingMove.getCard().getSuit() : null;

                PlayingCard card;

                if (startingSuit == null) {
                    card = currentActor.snapshot.myHand
                        .stream()
                        .max(Comparator.comparing(PlayingCard::getValue))
                        .orElseThrow();
                } else {
                    card = currentActor.snapshot.myHand
                        .stream()
                        .filter(c -> c.getSuit() == startingSuit)
                        .max(Comparator.comparing(PlayingCard::getValue))
                        .orElse(null);

                    if (card == null) {
                        card = currentActor.snapshot.myHand
                            .stream()
                            .min(Comparator.comparing(PlayingCard::getValue))
                            .orElseThrow();
                    }
                }

                assertNotNull(card);

                sendReceiveAll(currentActor, MOVE, card, actors);
            }

            System.out.println("=========== DONE PLAYING ===========");

            // Round over
            while (getGamePhase(owner) == Round.GamePhase.AFTER) {
                var currentActor = getCurrentActor(actors);

                if (currentActor.snapshot.game.resetOthersScore.isPendingFor(currentActor.snapshot.me)) {
                    var accept = currentActor.snapshot.me.getName().equals("P2");
                    sendReceiveAll(owner, RESPOND_RESET_OTHERS_SCORE, accept, actors);
                    continue;
                }

                if (currentActor.snapshot.game.getStarted()) {
                    var currentDealer = getCurrentDealer(actors);
                    sendReceiveAll(currentDealer, DEAL_CARDS, null, actors);
                }
            }
        }

        System.out.println("GAME OVER!");
        assertTrue(owner.snapshot.game.getPlayers().stream().anyMatch(p -> p.getScore() >= 52 && p.getHasTakenChicago()));
    }

    private static Round.GamePhase getGamePhase(Actor owner) {
        var round = owner.snapshot.game.getCurrentRound();
        return round != null ? round.getPhase() : null;
    }

    private static List<PlayingCard> nOfKind(List<PlayingCard> cards, int n) {
        return cards.stream().filter(c -> cards.stream().filter(c2 -> c2.getValue() == c.getValue()).count() == n).toList();
    }

    private static Actor getCurrentActor(Actor[] actors) {
        return Arrays.stream(actors).filter(a -> a.snapshot.myTurn).findFirst().orElseThrow();
    }

    private static Actor getCurrentDealer(Actor[] actors) {
        return Arrays.stream(actors).filter(a -> a.snapshot.imDealing).findFirst().orElseThrow();
    }

    private static void sendReceiveAll(Actor player, Action.ActionType type, Object value, Actor[] actors) {
        send(player, type, value);
        actors[0].snapshot.game.getPlayers().stream().map(Player::getScore).forEach(System.out::println);
        actors[0].snapshot.game.getPlayers().stream().map(Player::getHasTakenChicago).forEach(System.out::println);
        awaitAllSnapshots(actors);
    }

    private static void send(Actor player, Action.ActionType type, Object value) {
        var json = Converter.toJson(value);
        var action = Converter.toJson(new Action(type, json));
        player.ref.tell(action, player.kit.getRef());
    }

    private static void awaitAllSnapshots(Actor[] actors) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (var player : actors) {
            awaitLatestSnapshot(player);
        }
    }

    private static void awaitLatestSnapshot(Actor player) {
        var message = player.kit.expectMsgClass(Duration.ofSeconds(2), String.class);

        while (player.kit.msgAvailable()) {
            message = player.kit.expectMsgClass(String.class);
        }

        player.snapshot = Converter.messageToSnapshot(message);
    }
}

