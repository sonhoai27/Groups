package com.sonhoai.groups.DataModels;

public class Message {
    private String id;
    private String idUser;
    private String nameUser;
    private String content;
    private String date;

    public Message() {
    }

    public Message(String id, String idUser, String nameUser, String content, String date) {
        this.id = id;
        this.idUser = idUser;
        this.nameUser = nameUser;
        this.content = content;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
