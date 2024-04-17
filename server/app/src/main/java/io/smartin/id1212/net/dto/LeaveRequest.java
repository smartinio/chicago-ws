package io.smartin.id1212.net.dto;

import com.google.gson.annotations.Expose;

public record LeaveRequest(@Expose String playerId, @Expose String key) { }
