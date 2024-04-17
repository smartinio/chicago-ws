package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

public record RejoinRequest(@Expose String playerId, @Expose String key) { }
