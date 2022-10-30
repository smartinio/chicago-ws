package io.smartin.id1212.model.store;

import io.smartin.id1212.exceptions.key.AlreadyStartedException;
import io.smartin.id1212.exceptions.NicknameException;
import io.smartin.id1212.exceptions.game.NotInGameException;
import io.smartin.id1212.exceptions.key.UnknownInvitationKeyException;
import io.smartin.id1212.model.components.ChicagoGame;
import io.smartin.id1212.model.components.Player;

import java.util.*;

import static io.smartin.id1212.config.Strings.UNKNOWN_KEY;
import static io.smartin.id1212.config.Strings.INVALID_PLAYER;;

public class GamesRepository {
    private final Map<UUID, ChicagoGame> games = new HashMap<>();
    private static GamesRepository ourInstance = new GamesRepository();
    public static GamesRepository getInstance() {
        return ourInstance;
    }

    private GamesRepository() {
    }

    public void removeGame(UUID key) {
        synchronized (games) {
            games.remove(key);
        }
    }

    public void createGame(Player player) {
        UUID invitationKey = UUID.randomUUID();
        ChicagoGame game = new ChicagoGame(invitationKey, player);
        player.setGame(game);
        addGame(invitationKey, game);
    }

    public void joinGame(Player player, String key) throws UnknownInvitationKeyException, NicknameException, AlreadyStartedException {
        UUID invitationKey;
        try {
            invitationKey = UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            throw new UnknownInvitationKeyException(UNKNOWN_KEY);
        }
        ChicagoGame game = games.get(invitationKey);
        if (game == null) {
            throw new UnknownInvitationKeyException(UNKNOWN_KEY);
        }
        if (game.hasPlayerWithName(player.getName())) {
            throw new NicknameException("A player with the nickname '" + player.getName() + "' already exists in that game");
        }
        player.setGame(game);
        addParticipant(invitationKey, player);
    }

    private void addParticipant(UUID key, Player player) throws AlreadyStartedException {
        games.get(key).addParticipant(player);
    }

    private void addGame(UUID key, ChicagoGame game) {
        synchronized (games) {
            games.put(key, game);
        }
    }

    public ChicagoGame findGame(String key) throws UnknownInvitationKeyException {
        UUID invitationKey;

        try {
            invitationKey = UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            throw new UnknownInvitationKeyException(UNKNOWN_KEY);
        }

        return games.get(invitationKey);
    }

    public ChicagoGame findGameForPlayer(String key, String playerId) throws UnknownInvitationKeyException, NotInGameException {
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
