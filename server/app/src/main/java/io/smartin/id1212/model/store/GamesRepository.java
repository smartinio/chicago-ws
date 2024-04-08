package io.smartin.id1212.model.store;

import io.smartin.id1212.exceptions.key.AlreadyStartedException;
import io.smartin.id1212.exceptions.NicknameException;
import io.smartin.id1212.exceptions.game.NotInGameException;
import io.smartin.id1212.exceptions.key.UnknownInvitationKeyException;
import io.smartin.id1212.model.components.ChicagoGame;
import io.smartin.id1212.model.components.Player;
import io.smartin.id1212.net.dto.GameCreation.GameRules;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.util.*;

import static io.smartin.id1212.config.Strings.UNKNOWN_KEY;
import static io.smartin.id1212.config.Strings.INVALID_PLAYER;;

public class GamesRepository {
    private final Map<String, ChicagoGame> games = new HashMap<>();
    private static GamesRepository ourInstance = new GamesRepository();

    public static GamesRepository getInstance() {
        return ourInstance;
    }

    private GamesRepository() {
    }

    public void removeGame(String key) {
        synchronized (games) {
            games.remove(key);
        }
    }

    public void createGame(Player player, GameRules rules) {
        Random random = new Random();
        char[] alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
        String invitationKey = NanoIdUtils.randomNanoId(random, alphabet, 10);
        ChicagoGame game = new ChicagoGame(invitationKey, player, rules);
        player.setGame(game);
        addGame(invitationKey, game);
    }

    public void joinGame(Player player, String invitationKey)
            throws UnknownInvitationKeyException, NicknameException, AlreadyStartedException {

        ChicagoGame game = games.get(invitationKey);
        if (game == null) {
            throw new UnknownInvitationKeyException(UNKNOWN_KEY);
        }
        if (game.hasPlayerWithName(player.getName())) {
            throw new NicknameException(
                    "A player with the nickname '" + player.getName() + "' already exists in that game");
        }
        player.setGame(game);
        addParticipant(invitationKey, player);
    }

    private void addParticipant(String key, Player player) throws AlreadyStartedException {
        games.get(key).addParticipant(player);
    }

    private void addGame(String key, ChicagoGame game) {
        synchronized (games) {
            games.put(key, game);
        }
    }

    public ChicagoGame findGame(String key) throws UnknownInvitationKeyException {
        return games.get(key);
    }

    public ChicagoGame findGameForPlayer(String key, String playerId)
            throws UnknownInvitationKeyException, NotInGameException {
        var game = findGame(key);

        if (game == null) {
            throw new UnknownInvitationKeyException(UNKNOWN_KEY);
        }

        if (!game.hasPlayerWithId(playerId)) {
            throw new NotInGameException(INVALID_PLAYER);
        }

        return game;
    }
}
