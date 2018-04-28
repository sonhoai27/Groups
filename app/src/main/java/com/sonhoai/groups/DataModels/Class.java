package com.sonhoai.groups.DataModels;

import android.support.annotation.Nullable;

public class Class {
    private String id;
    private String name;
    private String info;
    private String idUser;
    private String user;

    public Class(@Nullable String id, String name, String info, String idUser, @Nullable String user) {
        this.id = id;
        this.name = name;
        this.info = info;
        this.idUser = idUser;
        this.user = user;
    }

    public Class(){}

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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Class{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", info='" + info + '\'' +
                ", idUser='" + idUser + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
