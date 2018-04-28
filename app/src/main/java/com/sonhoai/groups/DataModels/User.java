package com.sonhoai.groups.DataModels;

import android.support.annotation.Nullable;

public class User {
    private String id;
    private String name;
    private String nodeKey;

    public User() {
    }

    public User(@Nullable String id, String name, @Nullable String nodeKey) {
        this.id = id;
        this.name = name;
        this.nodeKey = nodeKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
