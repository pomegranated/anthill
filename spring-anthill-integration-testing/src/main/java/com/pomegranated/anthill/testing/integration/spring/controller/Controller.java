package com.pomegranated.anthill.testing.integration.spring.controller;

import com.pomegranated.anthill.NotificationEvent;
import com.pomegranated.anthill.Notifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/anthill")
public class Controller {
    private final Notifier<Integer> notifier = new Notifier<>(0, 5, TimeUnit.SECONDS);

    @PostMapping("/heartbeat")
    public void setHeartbeat() {
        notifier.setHeartbeat(!notifier.isHeartbeat());
    }

    @PostMapping(value = "/session", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificationEvent<Integer, ?>> createSessionByLogin(@RequestParam Integer login) {
        return notifier.createSessionByLogin(login);
    }

    @PostMapping(value = "/session/{login}")
    public void sendNotificationEventWithLogin(@PathVariable Integer login, @RequestParam String notification) {
        notifier.sendNotificationEventWithLogin(
                NotificationEvent
                        .<Integer, String>builder()
                        .login(login)
                        .notification(notification)
                        .build()
        );
    }
}
