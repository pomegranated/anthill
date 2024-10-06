# <span style="color:red">Warning!</span>
<span style="color:red">At the moment, it only works with the reactive stack.</span>

# Tested with
| framework   | dependency                  |
|:------------|:----------------------------|
| Spring Boot | spring-boot-starter-webflux |

# The most important issues
- [add servlet stack support](https://github.com/pomegranated/anthill/issues/8)

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
