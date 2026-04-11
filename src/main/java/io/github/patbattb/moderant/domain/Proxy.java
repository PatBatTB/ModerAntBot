package io.github.patbattb.moderant.domain;

import org.jetbrains.annotations.NotNull;

public record Proxy (
        @NotNull java.net.Proxy.Type type,
        @NotNull String host,
        int port,
        Proxy.Auth auth) {

    public record Auth (@NotNull String login, @NotNull String pass) {

    }
}
