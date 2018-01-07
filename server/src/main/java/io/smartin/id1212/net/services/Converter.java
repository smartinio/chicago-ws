package io.smartin.id1212.net.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.smartin.id1212.model.components.PlayingCard;
import io.smartin.id1212.net.dto.Action;
import io.smartin.id1212.net.dto.JoinRequest;

import java.util.Arrays;
import java.util.List;

public class Converter {
    private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static Action toAction(String json) throws JsonSyntaxException {
        return gson.fromJson(json, Action.class);
    }

    public static List<PlayingCard> toCards(String json) throws JsonSyntaxException {
        PlayingCard[] cards = gson.fromJson(json, PlayingCard[].class);
        return Arrays.asList(cards);
    }

    public static PlayingCard toCard(String json) throws JsonSyntaxException {
        return gson.fromJson(json, PlayingCard.class);
    }

    public static JoinRequest toJoinRequest(String json) throws JsonSyntaxException {
        return gson.fromJson(json, JoinRequest.class);
    }
}
