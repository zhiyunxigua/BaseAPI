package com.xigua.baseAPI.pluginmessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

public interface PluginMessageChannel {

    String getIdentifier();

    Result handleProxyCall(
            byte[] data,
            UUID sourceUuid,
            String sourceUsername,
            Identity sourceIdentity
    );

    Result handleServerCall(byte[] data, UUID playerUuid, String playerUsername);

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class Result {
        private static final Result FORWARD = new Result(true, null);
        private static final Result HANDLED = new Result(false, null);

        private final boolean allowed;
        private final String reason;

        public static Result forward() {
            return FORWARD;
        }

        public static Result handled() {
            return HANDLED;
        }

        public static Result kick(String reason) {
            return new Result(false, reason);
        }
    }

    enum Identity {
        UNKNOWN,
        SERVER,
        PLAYER
    }
}
