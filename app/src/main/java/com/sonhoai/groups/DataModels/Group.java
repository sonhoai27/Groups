package com.sonhoai.groups.DataModels;

public class Group {
    private String id;
    private String idClass;
    private String name;
    private String content;
    private String date;
    private String idUser;

    public Group() {
    }

    public Group(String id, String idClass, String name, String content, String date, String idUser) {
        this.id = id;
        this.idClass = idClass;
        this.name = name;
        this.content = content;
        this.date = date;
        this.idUser = idUser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdClass() {
        return idClass;
    }

    public void setIdClass(String idClass) {
        this.idClass = idClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", idClass='" + idClass + '\'' +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                ", date='" + date + '\'' +
                ", idUser='" + idUser + '\'' +
                '}';
    }
}
