# How to use
## add Maven dependency
```xml
<dependency>
  <groupId>com.pomegranated</groupId>
  <artifactId>anthill</artifactId>
  <version>1.0.0</version>
</dependency>
```

## init notifier
```java
import com.pomegranated.anthill.Notifier;

Notifier<Integer> notifier = new Notifier<>();
```

## create session
```java
Flux<NotificationEvent<Integer, ?>> session = notifier.createSessionByLogin(1);
```

## send event
```java
import com.pomegranated.anthill.NotificationEvent;

sendNotificationEventWithLogin(
    NotificationEvent
        .<Integer, String>builder()
        .login(1)
        .notification("notification")
        .build()
);
```

## set heartbeat
```java
notifier.setHeartbeat(!notifier.isHeartbeat());
```
