package com.sendi.deliveredrobot.entity;

import org.litepal.crud.LitePalSupport;

/**
 *  @author swn
 *  @describe 讲解配置(讲解中目标点内容)
 */
public class Table_Point_Config extends LitePalSupport {
    private String name;//点名
    private String walkText;//途径播报内容-播报语(200)
    private String explanationText;//讲解播报内容-播报语(200)
    private String walkVoice;//途径音频.mp3
    private String explanationVoice;//讲解音频.mp3
    private int scope;//排序
    private Table_Big_Screen bigScreenConfigDB;//讲解大屏配置
    private Table_Touch_Screen touchScreenConfigDB;//主屏幕讲解配置

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWalkText() {
        return walkText;
    }

    public void setWalkText(String walkText) {
        this.walkText = walkText;
    }

    public String getExplanationText() {
        return explanationText;
    }

    public void setExplanationText(String explanationText) {
        this.explanationText = explanationText;
    }

    public String getWalkVoice() {
        return walkVoice;
    }

    public void setWalkVoice(String walkVoice) {
        this.walkVoice = walkVoice;
    }

    public String getExplanationVoice() {
        return explanationVoice;
    }

    public void setExplanationVoice(String explanationVoice) {
        this.explanationVoice = explanationVoice;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public Table_Big_Screen getBigScreenConfigDB() {
        return bigScreenConfigDB;
    }

    public void setBigScreenConfigDB(Table_Big_Screen bigScreenConfigDB) {
        this.bigScreenConfigDB = bigScreenConfigDB;
    }

    public Table_Touch_Screen getTouchScreenConfigDB() {
        return touchScreenConfigDB;
    }

    public void setTouchScreenConfigDB(Table_Touch_Screen touchScreenConfigDB) {
        this.touchScreenConfigDB = touchScreenConfigDB;
    }
}
