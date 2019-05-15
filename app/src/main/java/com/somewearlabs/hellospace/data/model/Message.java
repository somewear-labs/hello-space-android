package com.somewearlabs.hellospace.data.model;

import android.support.annotation.NonNull;

import java.util.Date;

public class Message {
    private String email;
    private String phone;
    private Date timestamp;
    private String content;

    public Message(@NonNull String email, @NonNull String phone, @NonNull Date timestamp, @NonNull String content) {
        this.email = email;
        this.phone = phone;
        this.timestamp = timestamp;
        this.content = content;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getPhone() {
        return phone;
    }

    @NonNull
    public Date getTimestamp() {
        return timestamp;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", timestamp=" + timestamp +
                ", content='" + content + '\'' +
                '}';
    }
}
