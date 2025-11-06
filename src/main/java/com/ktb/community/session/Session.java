package com.ktb.community.session;

import com.ktb.community.entity.User;
import com.ktb.community.user.UserRole;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Session {
    private final String id;
    private final Instant createdAt;
    private volatile Instant lastAccessAt;
    private final Map<String, Object> data = new ConcurrentHashMap<>();

    public Session(String id) {
        this.id = id;
        this.createdAt = Instant.now();
        this.lastAccessAt = this.createdAt;
    }


    public Map<String, Object> getData() { return Collections.unmodifiableMap(data); }

    public void setAttr(String key, Object value) { data.put(key, value); }
    public Object getAttr(String key) { return data.get(key); }
    public <T> T getAttr(String key, Class<T> type) { return type.cast(data.get(key)); }

    public void touch() { this.lastAccessAt = Instant.now(); }
}
