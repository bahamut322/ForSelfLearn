package com.sendi.deliveredrobot.entity;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class FaceTips extends LitePalSupport {
    private int id;
    @Column(nullable = false, defaultValue = "unKnow")
    private String name;
    @Column(defaultValue = "unKnow")
    private String sexual;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getSexual() {
        return sexual;
    }

    public void setSexual(String sexual) {
        this.sexual = sexual;
    }

}

