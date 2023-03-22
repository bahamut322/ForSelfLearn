package com.sendi.deliveredrobot.entity;

import android.database.Cursor;

import com.sendi.deliveredrobot.model.BasicModel;
import com.sendi.deliveredrobot.model.ExplainConfigModel;
import com.sendi.deliveredrobot.model.MyResultModel;
import com.sendi.deliveredrobot.model.RouteMapList;
import com.sendi.deliveredrobot.model.SendRoutesModel;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 *  * @author swn
 *  * @describe  查询数据
 */
public class QuerySql {
    /**
     * 笛卡尔积查询数据库讲解路线中的信息(升序排序)
     * @param routeId 当前地图的id
     */
    public static ArrayList<MyResultModel> queryMyData(int routeId) {
        ArrayList<MyResultModel> list = new ArrayList<>();
        String sql = "SELECT * FROM routedb route "
                + "LEFT JOIN pointconfigvodb point ON " + routeId + " = point.routedb_id "
                + "LEFT JOIN bigscreenconfigdb bigscreen ON point.id = bigscreen.pointconfigvodb_id "
                + "LEFT JOIN touchscreenconfigdb touch ON point.id = touch.pointconfigvodb_id "
                + "ORDER BY scope ASC";
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
     * 机器人当前总图下配置路线列表(用于发送MQTT)
     * @param rootMapName 当前总图名字
     * @return
     */
    public static List<SendRoutesModel> QueryRoutesSendMessage(String rootMapName) {
        List<SendRoutesModel> list = new ArrayList<>();
        String sql = "SELECT * FROM routedb route "
                + "LEFT JOIN pointconfigvodb point ON (SELECT id FROM routedb WHERE routedb.rootmapname = "+"'"+rootMapName +"'"+" )  = point.routedb_id  ";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 构造实体对象
                SendRoutesModel model = new SendRoutesModel();
                model.setRouteName(cursor.getString(cursor.getColumnIndex("routename")));
                model.setRouteTimeStamp(cursor.getLong(cursor.getColumnIndex("timestamp")));
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
    public static ExplainConfigModel QueryExplainConfig(){
        ExplainConfigModel model = new ExplainConfigModel();
        String sql = "SELECT * FROM explainconfigdb";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 构造实体对象
                model.setId(cursor.getInt(cursor.getColumnIndex("id")));
                model.setPointListText(cursor.getString(cursor.getColumnIndex("pointlisttext")));
                model.setInterruptionText(cursor.getString(cursor.getColumnIndex("interruptiontext")));
                model.setEndText(cursor.getString(cursor.getColumnIndex("endtext")));
                model.setStayTime(cursor.getInt(cursor.getColumnIndex("staytime")));
                model.setRouteListText(cursor.getString(cursor.getColumnIndex("routelisttext")));
                model.setStartText(cursor.getString(cursor.getColumnIndex("starttext")));
                model.setSlogan(cursor.getString(cursor.getColumnIndex("slogan")));
                model.setTimeStamp(cursor.getLong(cursor.getColumnIndex("timestamp")));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return model;
    }

    /**
     * 查询时间戳
     * @return
     */
    public static long advTimeStamp(){
        long timestamp = 0;
        String sql = "SELECT timestamp FROM advertisingconfigdb";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return timestamp;
    }

    public static int QueryBasicId(){
        int basic_id = 0;
        String sql = "SELECT id FROM basicsetting";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                basic_id = cursor.getInt(cursor.getColumnIndex("id"));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return basic_id;
    }
    public static BasicModel QueryBasic() {
        BasicModel model = new BasicModel();
        String sql = "SELECT * FROM basicsetting";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 构造实体对象
                model.setId(cursor.getInt(cursor.getColumnIndex("id")));
                boolean etique = intToBoolean(cursor.getInt(cursor.getColumnIndex("etiquette")));
                model.setEtiquette(etique);
                model.setExplanationFinish(cursor.getInt(cursor.getColumnIndex("explanationfinish")));
                model.setGoExplanationPoint(cursor.getFloat(cursor.getColumnIndex("goexplanationpoint")));
                model.setVoiceVolume(cursor.getFloat(cursor.getColumnIndex("voicevolume")));
                boolean IdentifyVip = intToBoolean(cursor.getInt(cursor.getColumnIndex("identifyvip")));
                model.setIdentifyVip(IdentifyVip);
                model.setPatrolStayTime(cursor.getInt(cursor.getColumnIndex("patrolstaytime")));
                model.setUnArrive(cursor.getInt(cursor.getColumnIndex("unarrive")));
                boolean whetherInterrupt = intToBoolean(cursor.getInt(cursor.getColumnIndex("whetherinterrupt")));
                model.setWhetherInterrupt(whetherInterrupt);
                model.setDefaultValue(cursor.getString(cursor.getColumnIndex("defaultvalue")));
                model.setPatrolSpeed(cursor.getFloat(cursor.getColumnIndex("patrolspeed")));
                model.setWhetherTime(cursor.getInt(cursor.getColumnIndex("whethertime")));
                model.setError(cursor.getString(cursor.getColumnIndex("error")));
                model.setTempMode(cursor.getInt(cursor.getColumnIndex("tempmode")));
                boolean Intelligent = intToBoolean(cursor.getInt(cursor.getColumnIndex("intelligent")));
                model.setIntelligent(Intelligent);
                model.setVideoVolume(cursor.getFloat(cursor.getColumnIndex("videovolume")));
                model.setLeadingSpeed(cursor.getFloat(cursor.getColumnIndex("leadingspeed")));
                model.setRobotMode(cursor.getString(cursor.getColumnIndex("robotmode")));
                model.setPatrolContent(cursor.getString(cursor.getColumnIndex("patrolcontent")));
                model.setStayTime(cursor.getInt(cursor.getColumnIndex("staytime")));
                model.setSpeechSpeed(cursor.getInt(cursor.getColumnIndex("speechspeed")));
                boolean VoiceAnnouncements = intToBoolean(cursor.getInt(cursor.getColumnIndex("voiceannouncements")));
                model.setVoiceAnnouncements(VoiceAnnouncements);
                boolean Expression = intToBoolean(cursor.getInt(cursor.getColumnIndex("expression")));
                model.setExpression(Expression);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return model;
    }

    /**
     * 将查询到的int转为Boolean
     * @param data 数据
     */
    private static Boolean  intToBoolean(int data){
        if (data == 1){
            return true;
        }else {
            return false;
        }
    }
}
