package com.pomegranated.anthill;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class Notifier<LOGIN_TYPE> {
    @Getter
    @Setter
    private boolean isHeartbeat = false;

    private final static String SESSION_ALREADY_EXISTS = "session already exists";
    private final static String SESSION_DOES_NOT_EXIST = "session does not exist";
    private final static String SUCCESSFUL_SESSION_OPENING = "successful session opening";
    private final static String SUCCESSFUL_SESSION_REMOVAL = "successful session removal";
    private final static String SUCCESSFUL_NOTIFICATION_SENDING = "successful notification sending";
    private final static String HEARTBEAT_MESSAGE = "heartbeat";
    private final static String VALIDATION_ERROR = "validation error";

    private final Map<LOGIN_TYPE, FluxSink<NotificationEvent<LOGIN_TYPE, ?>>> sessions;

    public Notifier() {
        sessions = new ConcurrentHashMap<>();
        heartbeat();
    }
    public Notifier(int initCapacity) {
        sessions = new ConcurrentHashMap<>(initCapacity);
        heartbeat();
    }
    public Notifier(int initCapacity, float loadFactor) {
        sessions = new ConcurrentHashMap<>(initCapacity, loadFactor);
        heartbeat();
    }
    public Notifier(int initCapacity, float loadFactor, int concurrentLevel) {
        sessions = new ConcurrentHashMap<>(initCapacity, loadFactor, concurrentLevel);
        heartbeat();
    }

    public Flux<NotificationEvent<LOGIN_TYPE, ?>> createSessionByLogin(LOGIN_TYPE login) {
        return Flux.create(fluxSink -> {
            sessions.compute(login, (k, v) -> {
                if (sessions.containsKey(k)) {
                    throw new RuntimeException(SESSION_ALREADY_EXISTS + String.format(" (login=%s)", login));
                }
                return fluxSink;
            });

            fluxSink.next(
                    NotificationEvent
                            .<LOGIN_TYPE, String>builder()
                            .login(login)
                            .notification(SUCCESSFUL_SESSION_OPENING)
                            .build()
            );
            log.debug(SUCCESSFUL_SESSION_OPENING + " (login={})", login);

            fluxSink.onCancel(() -> {
                sessions.remove(login);
                log.debug(SUCCESSFUL_SESSION_REMOVAL + " (login={})", login);
            });

            fluxSink.onRequest(longConsumer ->
                    log.debug(SUCCESSFUL_NOTIFICATION_SENDING + " (login={})", login));
        });
    }

    public void sendNotificationEventWithLogin(NotificationEvent<LOGIN_TYPE, ?> notificationEvent) {
        if (notificationEvent.login() == null) {
            throw new RuntimeException(VALIDATION_ERROR + " (login is null)");
        }
        sessions.compute(notificationEvent.login(), (k, v) -> {
            if (!sessions.containsKey(k)) {
                throw new RuntimeException(
                        SESSION_DOES_NOT_EXIST + String.format(" (login=%s)", notificationEvent.login())
                );
            }
            v.next(notificationEvent);
            return v;
        });
    }

    private void heartbeat() {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
                () -> {
                    if (isHeartbeat) {
                        sessions.forEach((k, v) -> v.next(
                                NotificationEvent
                                        .<LOGIN_TYPE, String>builder()
                                        .login(k)
                                        .notification(HEARTBEAT_MESSAGE)
                                        .build()
                        ));
                    }
                },
                24,
                24,
                TimeUnit.HOURS
        );
    }
}
