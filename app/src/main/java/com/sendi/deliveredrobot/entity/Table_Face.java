package com.sendi.deliveredrobot.entity;

import com.google.gson.annotations.SerializedName;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class Table_Face extends LitePalSupport {
    @SerializedName("id")
    private int face_id;
    @Column(nullable = false, defaultValue = "unKnow")
    private String name;
    private String faceFeat;

    public int getFace_id() {
        return face_id;
    }

    public void setFace_id(int face_id) {
        this.face_id = face_id;
    }

    public String getFaceFeat() {
        return faceFeat;
    }

    public void setFaceFeat(String faceFeat) {
        this.faceFeat = faceFeat;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getSexual() {
        return faceFeat;
    }

    public void setSexual(String sexual) {
        this.faceFeat = sexual;
    }

}

