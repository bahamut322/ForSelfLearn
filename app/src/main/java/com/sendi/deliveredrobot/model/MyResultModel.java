package com.sendi.deliveredrobot.model;

import androidx.annotation.NonNull;

public class MyResultModel {

    /**
     * rootmapname : 总图A
     * routename : 路线A
     * backgroundpic : /mnt/sdcard/X8ROBOT/routeBackground线路背景图.jpg
     * introduction : 这是路线A的简介
     * timestamp : 1234567890111
     * explanationtext : 讲解文字
     * walkvoice : 途径音频.mp3
     * scope : 1
     * name : 山海关
     * walktext : 欢迎
     * explanationvoice : 讲解音频.mp3
     * routedb_id : 1
     * big_fontbackground : null
     * big_fontcontent : null
     * big_pictype : 0
     * big_picplaytime : 0
     * big_videoaudio : 1
     * big_fontlayout : 0
     * big_imagefile : null
     * big_fontcolor : null
     * big_fontsize : 0
     * big_type : 2
     * big_videofile : /6/ODQyNzEwNzIyLm1wNDE2NzYyNzcwNzQ0Mjk.mp4
     * big_textposition : 0
     * touch_fontbackground : #00000
     * touch_fontcontent : null
     * touch_pictype : 0
     * touch_picplaytime : 0
     * touch_fontlayout : 1
     * touch_imagefile : 0
     * touch_fontcolor : null
     * touch_fontsize : null
     * touch_type : 0
     * touch_textposition : 2
     */

    private String rootmapname;
    private String routename;
    private String backgroundpic;
    private String introduction;
    private long timestamp;
    private String explanationtext;
    private String walkvoice;
    private int scope;
    private String name;
    private String walktext;
    private String explanationvoice;
    private int routedb_id;
    private Object big_fontbackground;
    private Object big_fontcontent;
    private int big_pictype;
    private int big_picplaytime;
    private int big_videoaudio;
    private int big_fontlayout;
    private Object big_imagefile;
    private Object big_fontcolor;
    private int big_fontsize;
    private int big_type;
    private String big_videofile;
    private int big_textposition;
    private String touch_fontbackground;
    private String touch_fontcontent;
    private int touch_pictype;
    private int touch_picplaytime;
    private int touch_fontlayout;
    private String touch_imagefile;
    private String touch_fontcolor;
    private int touch_fontsize;
    private int touch_type;
    private int touch_textposition;
    private String touch_walkPic;
    private String touch_blockPic;
    private String touch_arrivePic;
    private String touch_overTaskPic;

    private int videolayout;

    public int getVideolayout() {
        return videolayout;
    }

    public void setVideolayout(int videolayout) {
        this.videolayout = videolayout;
    }

    public String getTouch_walkPic() {
        return touch_walkPic;
    }

    public void setTouch_walkPic(String touch_walkPic) {
        this.touch_walkPic = touch_walkPic;
    }

    public String getTouch_blockPic() {
        return touch_blockPic;
    }

    public void setTouch_blockPic(String touch_blockPic) {
        this.touch_blockPic = touch_blockPic;
    }

    public String getTouch_arrivePic() {
        return touch_arrivePic;
    }

    public void setTouch_arrivePic(String touch_arrivePic) {
        this.touch_arrivePic = touch_arrivePic;
    }

    public String getTouch_overTaskPic() {
        return touch_overTaskPic;
    }

    public void setTouch_overTaskPic(String touch_overTaskPic) {
        this.touch_overTaskPic = touch_overTaskPic;
    }

    public String getRootmapname() {
        return rootmapname;
    }

    public void setRootmapname(String rootmapname) {
        this.rootmapname = rootmapname;
    }

    public String getRoutename() {
        return routename;
    }

    public void setRoutename(String routename) {
        this.routename = routename;
    }

    public String getBackgroundpic() {
        return backgroundpic;
    }

    public void setBackgroundpic(String backgroundpic) {
        this.backgroundpic = backgroundpic;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getExplanationtext() {
        return explanationtext;
    }

    public void setExplanationtext(String explanationtext) {
        this.explanationtext = explanationtext;
    }

    public String getWalkvoice() {
        return walkvoice;
    }

    public void setWalkvoice(String walkvoice) {
        this.walkvoice = walkvoice;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWalktext() {
        return walktext;
    }

    public void setWalktext(String walktext) {
        this.walktext = walktext;
    }

    public String getExplanationvoice() {
        return explanationvoice;
    }

    public void setExplanationvoice(String explanationvoice) {
        this.explanationvoice = explanationvoice;
    }

    public int getRoutedb_id() {
        return routedb_id;
    }

    public void setRoutedb_id(int routedb_id) {
        this.routedb_id = routedb_id;
    }

    public Object getBig_fontbackground() {
        return big_fontbackground;
    }

    public void setBig_fontbackground(Object big_fontbackground) {
        this.big_fontbackground = big_fontbackground;
    }

    public Object getBig_fontcontent() {
        return big_fontcontent;
    }

    public void setBig_fontcontent(Object big_fontcontent) {
        this.big_fontcontent = big_fontcontent;
    }

    public int getBig_pictype() {
        return big_pictype;
    }

    public void setBig_pictype(int big_pictype) {
        this.big_pictype = big_pictype;
    }

    public int getBig_picplaytime() {
        return big_picplaytime;
    }

    public void setBig_picplaytime(int big_picplaytime) {
        this.big_picplaytime = big_picplaytime;
    }

    public int getBig_videoaudio() {
        return big_videoaudio;
    }

    public void setBig_videoaudio(int big_videoaudio) {
        this.big_videoaudio = big_videoaudio;
    }

    public int getBig_fontlayout() {
        return big_fontlayout;
    }

    public void setBig_fontlayout(int big_fontlayout) {
        this.big_fontlayout = big_fontlayout;
    }

    public Object getBig_imagefile() {
        return big_imagefile;
    }

    public void setBig_imagefile(Object big_imagefile) {
        this.big_imagefile = big_imagefile;
    }

    public Object getBig_fontcolor() {
        return big_fontcolor;
    }

    public void setBig_fontcolor(Object big_fontcolor) {
        this.big_fontcolor = big_fontcolor;
    }

    public int getBig_fontsize() {
        return big_fontsize;
    }

    public void setBig_fontsize(int big_fontsize) {
        this.big_fontsize = big_fontsize;
    }

    public int getBig_type() {
        return big_type;
    }

    public void setBig_type(int big_type) {
        this.big_type = big_type;
    }

    public String getBig_videofile() {
        return big_videofile;
    }

    public void setBig_videofile(String big_videofile) {
        this.big_videofile = big_videofile;
    }

    public int getBig_textposition() {
        return big_textposition;
    }

    public void setBig_textposition(int big_textposition) {
        this.big_textposition = big_textposition;
    }

    public String getTouch_fontbackground() {
        return touch_fontbackground;
    }

    public void setTouch_fontbackground(String touch_fontbackground) {
        this.touch_fontbackground = touch_fontbackground;
    }

    public String getTouch_fontcontent() {
        return touch_fontcontent;
    }

    public void setTouch_fontcontent(String touch_fontcontent) {
        this.touch_fontcontent = touch_fontcontent;
    }

    public int getTouch_pictype() {
        return touch_pictype;
    }

    public void setTouch_pictype(int touch_pictype) {
        this.touch_pictype = touch_pictype;
    }

    public int getTouch_picplaytime() {
        return touch_picplaytime;
    }

    public void setTouch_picplaytime(int touch_picplaytime) {
        this.touch_picplaytime = touch_picplaytime;
    }

    public int getTouch_fontlayout() {
        return touch_fontlayout;
    }

    public void setTouch_fontlayout(int touch_fontlayout) {
        this.touch_fontlayout = touch_fontlayout;
    }

    public String getTouch_imagefile() {
        return touch_imagefile;
    }

    public void setTouch_imagefile(String touch_imagefile) {
        this.touch_imagefile = touch_imagefile;
    }

    public String getTouch_fontcolor() {
        return touch_fontcolor;
    }

    public void setTouch_fontcolor(String touch_fontcolor) {
        this.touch_fontcolor = touch_fontcolor;
    }

    public int getTouch_fontsize() {
        return touch_fontsize;
    }

    public void setTouch_fontsize(int touch_fontsize) {
        this.touch_fontsize = touch_fontsize;
    }

    public int getTouch_type() {
        return touch_type;
    }

    public void setTouch_type(int touch_type) {
        this.touch_type = touch_type;
    }

    public int getTouch_textposition() {
        return touch_textposition;
    }

    public void setTouch_textposition(int touch_textposition) {
        this.touch_textposition = touch_textposition;
    }

}
