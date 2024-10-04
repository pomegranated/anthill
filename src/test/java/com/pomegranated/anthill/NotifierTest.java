package com.pomegranated.anthill;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

public class NotifierTest {
    private final Notifier<Integer> notifier = new Notifier<>();
    private final NotificationEvent<Integer, String> notificationEvent = NotificationEvent
            .<Integer, String>builder()
            .login(1)
            .notification(Notifier.SUCCESSFUL_SESSION_OPENING)
            .build();

    @Test
    public void constructors() {
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
                .create(notifier.createSessionByLogin(1))
                .expectNext(notificationEvent)
                .thenCancel()
                .verify();
    }

    @Test
    public void sendNotificationEventWithLogin() {
        Flux<NotificationEvent<Integer, ?>> session = notifier.createSessionByLogin(1);
        StepVerifier
                .create(session)
                .expectNext(notificationEvent)
                .then(() -> notifier.sendNotificationEventWithLogin(notificationEvent))
                .expectNext(notificationEvent)
                .then(() -> notifier.sendNotificationEventWithLogin(notificationEvent))
                .expectNext(notificationEvent)
                .thenCancel()
                .verify();

        try {
            notifier.sendNotificationEventWithLogin(notificationEvent);
        }
        catch (RuntimeException e) {
            return;
        }
        fail();
    }

    @Test
    public void heartbeat() {
        Notifier<Integer> notifierWithHeartbeat = new Notifier<>(0, 1, TimeUnit.SECONDS);
        notifierWithHeartbeat.setHeartbeat(!notifierWithHeartbeat.isHeartbeat());
        StepVerifier
                .create(notifierWithHeartbeat.createSessionByLogin(1))
                .expectNext(notificationEvent)
                .expectNext(
                    NotificationEvent
                        .<Integer, String>builder()
                        .login(1)
                        .notification(Notifier.HEARTBEAT_MESSAGE)
                        .build()
                )
                .thenCancel()
                .verify();

        notifierWithHeartbeat.setHeartbeat(!notifierWithHeartbeat.isHeartbeat());
        StepVerifier
                .create(notifierWithHeartbeat.createSessionByLogin(1))
                .expectNext(notificationEvent)
                .expectNoEvent(Duration.ofSeconds(10))
                .thenCancel()
                .verify();
    }
}
