package com.sonhoai.groups.DataModels;

import android.support.annotation.Nullable;

public class GroupUser {
    private String id;
    private String name;
    private String ratingGood;
    private String ratingBad;
    private String idGroup;

    public GroupUser() {
    }

    public GroupUser(
            String id,
            String name,
            @Nullable String ratingGood,
            @Nullable String ratingBad,
            @Nullable String idGroup
    ) {
        this.id = id;
        this.name = name;
        this.ratingGood = ratingGood;
        this.ratingBad = ratingBad;
        this.idGroup = idGroup;
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

    public String getRatingGood() {
        return ratingGood;
    }

    public void setRatingGood(String ratingGood) {
        this.ratingGood = ratingGood;
    }

    public String getRatingBad() {
        return ratingBad;
    }

    public void setRatingBad(String ratingBad) {
        this.ratingBad = ratingBad;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    @Override
    public String toString() {
        return "GroupUser{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ratingGood='" + ratingGood + '\'' +
                ", ratingBad='" + ratingBad + '\'' +
                '}';
    }
}
