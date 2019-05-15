package com.somewearlabs.hellospace.data.model;

import android.support.annotation.Nullable;

public class UserItem {
    private int id;
    @Nullable private Object item;
    private String status;

    UserItem(int id, String status) {
        this.id = id;
        this.item = null;
        this.status = status;
    }

    public void setItem(@Nullable Object item) {
        this.item = item;
    }

    public void update(UserItem other) {
        if (item == null) {
            item = other.item;
        }
        status = other.status;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", item=" + item +
                '}';
    }
}
