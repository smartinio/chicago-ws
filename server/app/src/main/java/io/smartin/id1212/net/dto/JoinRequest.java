package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

public record JoinRequest(@Expose String nickname, @Expose String key) { }
