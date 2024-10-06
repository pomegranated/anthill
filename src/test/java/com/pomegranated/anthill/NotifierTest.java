package com.pomegranated.anthill;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

public class NotifierTest {
    private final Integer login;
    private final Notifier<Integer> notifier;
    private final NotificationEvent<Integer, String> notificationEvent;

    public NotifierTest() {
        login = 1;
        notifier = new Notifier<>(0, 1, TimeUnit.SECONDS);
        notificationEvent = NotificationEvent
                .<Integer, String>builder()
                .login(login)
                .notification(Notifier.SUCCESSFUL_SESSION_OPENING)
                .build();
    }

    @Test
    public void constructors() {
        new Notifier<>();
        new Notifier<>(16, 0.75f, 16);
        new Notifier<>(24, 24, TimeUnit.HOURS);
        new Notifier<>(
                16,
                0.75f,
                16,
                24,
                24,
                TimeUnit.HOURS
        );
    }

    @Test
    public void createSessionByLogin() {
        StepVerifier
                .create(notifier.createSessionByLogin(login))
                .expectNext(notificationEvent)
                .thenCancel()
                .verify();
    }

    @Test
    public void sendNotificationEventWithLogin() {
        StepVerifier
                .create(notifier.createSessionByLogin(login))
                .expectNext(notificationEvent)
                .then(() -> notifier.sendNotificationEventWithLogin(notificationEvent))
                .expectNext(notificationEvent)
                .then(() -> notifier.sendNotificationEventWithLogin(notificationEvent))
                .expectNext(notificationEvent)
                .thenCancel()
                .verify();
    }

    @Test
    public void sendNotificationEventWithLoginWithNonExistentSession() {
        try {
            notifier.sendNotificationEventWithLogin(notificationEvent); // session does not exist
        }
        catch (RuntimeException ignored) {
            return;
        }
        fail();
    }

    @Test
    @Order(1)
    public void heartbeatIsEnabled() {
        notifier.setHeartbeat(!notifier.isHeartbeat());
        StepVerifier
                .create(notifier.createSessionByLogin(login))
                .expectNext(notificationEvent)
                .expectNext(
                        NotificationEvent
                                .<Integer, String>builder()
                                .login(login)
                                .notification(Notifier.HEARTBEAT_MESSAGE)
                                .build()
                )
                .thenCancel()
                .verify();
        notifier.setHeartbeat(!notifier.isHeartbeat());
    }
}
