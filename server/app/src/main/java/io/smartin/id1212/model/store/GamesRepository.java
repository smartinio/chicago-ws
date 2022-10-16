package io.smartin.id1212.model.store;

import io.smartin.id1212.exceptions.key.AlreadyStartedException;
import io.smartin.id1212.exceptions.NicknameException;
import io.smartin.id1212.exceptions.key.UnknownInvitationKeyException;
import io.smartin.id1212.model.components.ChicagoGame;
import io.smartin.id1212.model.components.Player;

import java.util.*;

import static io.smartin.id1212.config.Strings.UNKNOWN_KEY;

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
        ChicagoGame game = new ChicagoGame(invitationKey);
        player.setGame(game);
        game.setInitialPlayer(player);
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
}
