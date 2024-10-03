package com.pomegranated.anthill;

public record NotificationEvent<LOGIN_TYPE, NOTIFICATION_TYPE>(LOGIN_TYPE login, NOTIFICATION_TYPE notification) {
    public final static class Builder<LOGIN_TYPE, NOTIFICATION_TYPE> {
        private LOGIN_TYPE login;
        private NOTIFICATION_TYPE notification;

        public Builder<LOGIN_TYPE, NOTIFICATION_TYPE> login(LOGIN_TYPE login) {
            this.login = login;
            return this;
        }
        public Builder<LOGIN_TYPE, NOTIFICATION_TYPE> notification(NOTIFICATION_TYPE notification) {
            this.notification = notification;
            return this;
        }

        public NotificationEvent<LOGIN_TYPE, NOTIFICATION_TYPE> build() {
            return new NotificationEvent<>(login, notification);
        }
    }

    public static <LOGIN_TYPE, NOTIFICATION_TYPE> Builder<LOGIN_TYPE, NOTIFICATION_TYPE> builder() {
        return new Builder<>();
    }
}
