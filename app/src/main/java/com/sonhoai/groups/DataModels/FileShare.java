package com.sonhoai.groups.DataModels;

import android.support.annotation.Nullable;

public class FileShare {
    private String id;
    private String idUser;
    private String nameUser;
    private String content;
    private String date;
    private String nameFile;

    public FileShare() {
    }

    public FileShare(@Nullable String id, String idUser, String nameUser, String content, String date, String nameFile) {
        this.id = id;
        this.idUser = idUser;
        this.nameUser = nameUser;
        this.content = content;
        this.date = date;
        this.nameFile = nameFile;
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

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    @Override
    public String toString() {
        return "FileShare{" +
                "id='" + id + '\'' +
                ", idUser='" + idUser + '\'' +
                ", nameUser='" + nameUser + '\'' +
                ", content='" + content + '\'' +
                ", date='" + date + '\'' +
                ", nameFile='" + nameFile + '\'' +
                '}';
    }
}
