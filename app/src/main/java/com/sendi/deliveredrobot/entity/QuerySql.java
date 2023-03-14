package com.sendi.deliveredrobot.entity;

import android.database.Cursor;

import com.sendi.deliveredrobot.model.ExplainConfigModel;
import com.sendi.deliveredrobot.model.MyResultModel;
import com.sendi.deliveredrobot.model.RouteMapList;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class QuerySql {
    /**
     * 笛卡尔积查询数据库中讲解配置
     * @param routeId 当前地图的id
     */
    public static List<MyResultModel> queryMyData(int routeId) {
        List<MyResultModel> list = new ArrayList<>();
        String sql = "SELECT * FROM routedb route "
                + "LEFT JOIN pointconfigvodb point ON " + routeId + " = point.routedb_id "
                + "LEFT JOIN bigscreenconfigdb bigscreen ON point.id = bigscreen.pointconfigvodb_id "
                + "LEFT JOIN touchscreenconfigdb touch ON point.id = touch.pointconfigvodb_id";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 构造实体对象
                MyResultModel model = new MyResultModel();
                model.setRootmapname(cursor.getString(cursor.getColumnIndex("rootmapname")));
                model.setRoutename(cursor.getString(cursor.getColumnIndex("routename")));
                model.setBackgroundpic(cursor.getString(cursor.getColumnIndex("backgroundpic")));
                model.setIntroduction(cursor.getString(cursor.getColumnIndex("introduction")));
                model.setTimestamp(cursor.getLong(cursor.getColumnIndex("timestamp")));
                model.setExplanationtext(cursor.getString(cursor.getColumnIndex("explanationtext")));
                model.setWalkvoice(cursor.getString(cursor.getColumnIndex("walkvoice")));
                model.setScope(cursor.getInt(cursor.getColumnIndex("scope")));
                model.setName(cursor.getString(cursor.getColumnIndex("name")));
                model.setWalktext(cursor.getString(cursor.getColumnIndex("walktext")));
                model.setExplanationvoice(cursor.getString(cursor.getColumnIndex("explanationvoice")));
                model.setRoutedb_id(cursor.getInt(cursor.getColumnIndex("routedb_id")));
                model.setBig_fontbackground(cursor.getString(cursor.getColumnIndex("fontbackground")));
                model.setBig_fontcontent(cursor.getString(cursor.getColumnIndex("fontcontent")));
                model.setBig_pictype(cursor.getInt(cursor.getColumnIndex("pictype")));
                model.setBig_picplaytime(cursor.getInt(cursor.getColumnIndex("picplaytime")));
                model.setBig_videoaudio(cursor.getInt(cursor.getColumnIndex("videoaudio")));
                model.setBig_fontlayout(cursor.getInt(cursor.getColumnIndex("fontlayout")));
                model.setBig_imagefile(cursor.getString(cursor.getColumnIndex("imagefile")));
                model.setBig_fontcolor(cursor.getString(cursor.getColumnIndex("fontcolor")));
                model.setBig_fontsize(cursor.getInt(cursor.getColumnIndex("fontsize")));
                model.setBig_type(cursor.getInt(cursor.getColumnIndex("type")));
                model.setBig_videofile(cursor.getString(cursor.getColumnIndex("videofile")));
                model.setBig_textposition(cursor.getInt(cursor.getColumnIndex("textposition")));
                model.setTouch_fontbackground(cursor.getString(cursor.getColumnIndex("touch_fontbackground")));
                model.setTouch_fontcontent(cursor.getString(cursor.getColumnIndex("touch_fontcontent")));
                model.setTouch_pictype(cursor.getInt(cursor.getColumnIndex("touch_pictype")));
                model.setTouch_picplaytime(cursor.getInt(cursor.getColumnIndex("touch_picplaytime")));
                model.setTouch_fontlayout(cursor.getInt(cursor.getColumnIndex("touch_fontlayout")));
                model.setTouch_imagefile(cursor.getString(cursor.getColumnIndex("touch_imagefile")));
                model.setTouch_fontcolor(cursor.getString(cursor.getColumnIndex("touch_fontcolor")));
                model.setTouch_fontsize(cursor.getInt(cursor.getColumnIndex("touch_fontsize")));
                model.setTouch_type(cursor.getInt(cursor.getColumnIndex("touch_type")));
                model.setTouch_textposition(cursor.getInt(cursor.getColumnIndex("touch_textposition")));
                list.add(model);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }


    /**
     * 查询某个总图下所有的路线
     * @param rootMapName 当前使用的地图名字
     */
    public static List<RouteMapList> queryRoute(String rootMapName) {
        List<RouteMapList> list = new ArrayList<>();
        String sql = "SELECT * FROM routedb WHERE routedb.rootmapname =  " +"'" + rootMapName + "'";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 构造实体对象
                RouteMapList model = new RouteMapList();
                model.setId(cursor.getInt(cursor.getColumnIndex("id")));
                model.setRootMapName(cursor.getString(cursor.getColumnIndex("rootmapname")));
                model.setRouteName(cursor.getString(cursor.getColumnIndex("routename")));
                model.setBackGroundPic(cursor.getString(cursor.getColumnIndex("backgroundpic")));
                model.setIntroduction(cursor.getString(cursor.getColumnIndex("introduction")));
                model.setTimeStamp(cursor.getLong(cursor.getColumnIndex("timestamp")));
                list.add(model);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    /**
     * 查询时间戳
     * @param routeName 路径名字
     */
    public static Long queryTime(String routeName){
        long Time = 0L;
        String sql = "SELECT routedb.timestamp FROM routedb WHERE routedb.routename = " +"'" + routeName + "'";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Time = cursor.getLong(cursor.getColumnIndex("timestamp"));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return Time;
    }

    /**
     * 查询routeDB的id
     * @param routeName 路径名字
     */
    public static int routeDB_id(String routeName){
        int route_id = 0;
        String sql = "SELECT routedb.id FROM routedb WHERE routedb.routename = "+"'"+routeName+"'";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                route_id = cursor.getInt(cursor.getColumnIndex("id"));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return route_id;
    }

    /**
     * 查询pointconfigvodb的id
     * @param routeName 路径名字
     */
    public static int pointConfigVoDB_id(String routeName){
        int pointConfigVoDB_id = 0;
        String sql = "SELECT pointconfigvodb.id FROM pointconfigvodb WHERE pointconfigvodb.routedb_id = (SELECT routedb.id FROM routedb WHERE routedb.routename = "+"'"+routeName+"'" +" )";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                pointConfigVoDB_id = cursor.getInt(cursor.getColumnIndex("id"));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return pointConfigVoDB_id;
    }

    /**
     * 查询讲解配置中的信息
     */
    public static List<ExplainConfigModel> QueryExplainConfig(){
        List<ExplainConfigModel> list = new ArrayList<>();
        String sql = "SELECT * FROM explainconfigdb";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 构造实体对象
                ExplainConfigModel model = new ExplainConfigModel();
                model.setId(cursor.getInt(cursor.getColumnIndex("id")));
                model.setPointListText(cursor.getString(cursor.getColumnIndex("pointlisttext")));
                model.setInterruptionText(cursor.getString(cursor.getColumnIndex("interruptiontext")));
                model.setEndText(cursor.getString(cursor.getColumnIndex("endtext")));
                model.setStayTime(cursor.getInt(cursor.getColumnIndex("staytime")));
                model.setRouteListText(cursor.getString(cursor.getColumnIndex("routelisttext")));
                model.setStartText(cursor.getString(cursor.getColumnIndex("starttext")));
                model.setSlogan(cursor.getString(cursor.getColumnIndex("slogan")));
                model.setTimeStamp(cursor.getLong(cursor.getColumnIndex("timestamp")));
                list.add(model);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

}
