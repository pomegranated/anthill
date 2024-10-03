package com.pomegranated.anthill;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class Notifier<LOGIN_TYPE> {
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;

    @Getter
    private boolean isHeartbeat = false;
    public void setHeartbeat(boolean isHeartbeat) {
        if (this.isHeartbeat == isHeartbeat) {
            return;
        }
        this.isHeartbeat = isHeartbeat;
        if (isHeartbeat) {
            scheduledFuture = heartbeat();
        }
        else {
            scheduledFuture.cancel(true);
        }
    }

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
        ((ScheduledThreadPoolExecutor) scheduledExecutorService).setRemoveOnCancelPolicy(true);
    }
    public Notifier(int initCapacity) {
        sessions = new ConcurrentHashMap<>(initCapacity);
        ((ScheduledThreadPoolExecutor) scheduledExecutorService).setRemoveOnCancelPolicy(true);
    }
    public Notifier(int initCapacity, float loadFactor) {
        sessions = new ConcurrentHashMap<>(initCapacity, loadFactor);
        ((ScheduledThreadPoolExecutor) scheduledExecutorService).setRemoveOnCancelPolicy(true);
    }
    public Notifier(int initCapacity, float loadFactor, int concurrentLevel) {
        sessions = new ConcurrentHashMap<>(initCapacity, loadFactor, concurrentLevel);
        ((ScheduledThreadPoolExecutor) scheduledExecutorService).setRemoveOnCancelPolicy(true);
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

    private ScheduledFuture<?> heartbeat() {
        return scheduledExecutorService.scheduleWithFixedDelay(
                () -> sessions.forEach((k, v) -> v.next(
                        NotificationEvent
                                .<LOGIN_TYPE, String>builder()
                                .login(k)
                                .notification(HEARTBEAT_MESSAGE)
                                .build()
                )),
                24,
                24,
                TimeUnit.HOURS
        );
    }
}
