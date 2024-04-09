package com.sendi.deliveredrobot.entity.entitySql;

import android.database.Cursor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.sendi.deliveredrobot.entity.Table_Big_Screen;

import com.sendi.deliveredrobot.entity.Table_Face;
import com.sendi.deliveredrobot.entity.Table_Greet_Config;
import com.sendi.deliveredrobot.entity.Table_Guide_Foundation;
import com.sendi.deliveredrobot.entity.Table_Robot_Config;
import com.sendi.deliveredrobot.entity.Table_Shopping_Action;
import com.sendi.deliveredrobot.entity.Table_Shopping_Config;
import com.sendi.deliveredrobot.entity.Table_Touch_Screen;
import com.sendi.deliveredrobot.model.ADVModel;
import com.sendi.deliveredrobot.model.ApplicationModel;
import com.sendi.deliveredrobot.model.BasicModel;
import com.sendi.deliveredrobot.model.ExplainConfigModel;
import com.sendi.deliveredrobot.model.GuideConfigList;
import com.sendi.deliveredrobot.model.GuideSendModel;
import com.sendi.deliveredrobot.model.MapConfig;
import com.sendi.deliveredrobot.model.MyResultModel;
import com.sendi.deliveredrobot.model.RouteMapList;
import com.sendi.deliveredrobot.model.SecondModel;
import com.sendi.deliveredrobot.model.SendRoutesModel;
import com.sendi.deliveredrobot.model.SendShoppingActionModel;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * * @author swn
 * * @describe  查询数据
 */
public class QuerySql {
    /**
     * 笛卡尔积查询数据库讲解路线中的信息(升序排序)
     *
     * @param routeId 当前地图的id
     */
    public static ArrayList<MyResultModel> queryPointDate(int routeId) {
        ArrayList<MyResultModel> list = new ArrayList<>();
        String sql = "SELECT * FROM table_route route "
                + "LEFT JOIN table_point_config point ON " + routeId + " = point.table_route_id "
                + "LEFT JOIN table_big_screen bigscreen ON point.id = bigscreen.table_point_config_id "
                + "LEFT JOIN table_touch_screen touch ON point.id = touch.table_point_config_id "
                + "WHERE route.id = " + routeId + " "
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
                model.setRoutedb_id(cursor.getInt(cursor.getColumnIndex("table_route_id")));
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
                model.setTouch_walkPic(cursor.getString(cursor.getColumnIndex("touch_walkpic")));
                model.setTouch_blockPic(cursor.getString(cursor.getColumnIndex("touch_blockpic")));
                model.setTouch_arrivePic(cursor.getString(cursor.getColumnIndex("touch_arrivepic")));
                model.setTouch_overTaskPic(cursor.getString(cursor.getColumnIndex("touch_overtaskpic")));
                model.setVideolayout(cursor.getInt(cursor.getColumnIndex("videolayout")));
                list.add(model);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }


    /**
     * 机器人当前总图下配置路线列表(用于发送MQTT)
     *
     * @param rootMapName 当前总图名字
     * @return
     */
    public static List<SendRoutesModel> QueryRoutesSendMessage(String rootMapName) {
        List<SendRoutesModel> list = new ArrayList<>();
        String sql = "SELECT DISTINCT route.routename, route.timestamp " +
                "FROM table_route route " +
                "LEFT JOIN table_point_config point " +
                "ON route.id = point.table_route_id " +
                "WHERE route.rootmapname = " + "'" + rootMapName + "'";
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

    public static String routeName() {
        String sql = "SELECT table_route.rootmapname FROM table_Route ";
        String name = "";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                name = (cursor.getString(cursor.getColumnIndex("rootmapname")));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return name;
    }

    /**
     * 查询某个总图下所有的路线
     *
     * @param rootMapName 当前使用的地图名字
     */
    public static List<RouteMapList> queryRoute(String rootMapName) {
        List<RouteMapList> list = new ArrayList<>();
        String sql = "SELECT * FROM table_route WHERE table_route.rootmapname =  " + "'" + rootMapName + "'";
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
     *
     * @param routeName 路径名字
     */
    public static Long queryTime(String routeName) {
        long Time = 0L;
        String sql = "SELECT table_route.timestamp FROM table_route WHERE table_route.routename = " + "'" + routeName + "'";
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
     *
     * @param routeName 路径名字
     */
    public static int routeDB_id(String routeName) {
        int route_id = 0;
        String sql = "SELECT table_route.id FROM table_route WHERE table_route.routename = " + "'" + routeName + "'";
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
     * 查询table_point_config的id
     *
     * @param routeName 路径名字
     */
    public static int pointConfigVoDB_id(String routeName) {
        int pointConfigVoDB_id = 0;
        String sql = "SELECT table_point_config.id FROM table_point_config WHERE table_point_config.table_route_id = (SELECT table_route.id FROM table_route WHERE table_route.routename = " + "'" + routeName + "'" + " )";
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
    public static ExplainConfigModel QueryExplainConfig() {
        ExplainConfigModel model = new ExplainConfigModel();
        String sql = "SELECT * FROM table_explain_config";
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
     *
     * @return
     */
    public static long advTimeStamp() {
        long timestamp = 0;
        String sql = "SELECT timestamp FROM table_advertising";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return timestamp;
    }

    public static int QueryBasicId() {
        int basic_id = 0;
        String sql = "SELECT id FROM table_basic";
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
        String sql = "SELECT * FROM table_basic";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 构造实体对象
                model.setId(cursor.getInt(cursor.getColumnIndex("id")));
                boolean etique = intToBoolean(cursor.getInt(cursor.getColumnIndex("etiquette")));
                model.setEtiquette(etique);
                model.setExplanationFinish(cursor.getInt(cursor.getColumnIndex("explanationfinish")));
                model.setGoExplanationPoint(cursor.getFloat(cursor.getColumnIndex("goexplanationpoint")));
                model.setVoiceVolume(cursor.getInt(cursor.getColumnIndex("voicevolume")));
                boolean IdentifyVip = intToBoolean(cursor.getInt(cursor.getColumnIndex("identifyvip")));
                model.setIdentifyVip(IdentifyVip);
                model.setPatrolStayTime(cursor.getInt(cursor.getColumnIndex("patrolstaytime")));
                model.setUnArrive(cursor.getInt(cursor.getColumnIndex("unarrive")));
                boolean whetherInterrupt = intToBoolean(cursor.getInt(cursor.getColumnIndex("explaininterrupt")));
                model.setExplainInterrupt(whetherInterrupt);
                model.setDefaultValue(cursor.getString(cursor.getColumnIndex("defaultvalue")));
                model.setPatrolSpeed(cursor.getFloat(cursor.getColumnIndex("patrolspeed")));
                model.setExplainWhetherTime(cursor.getInt(cursor.getColumnIndex("explainwhethertime")));
                model.setError(cursor.getString(cursor.getColumnIndex("error")));
                model.setTempMode(cursor.getInt(cursor.getColumnIndex("tempmode")));
                boolean Intelligent = intToBoolean(cursor.getInt(cursor.getColumnIndex("intelligent")));
                model.setIntelligent(Intelligent);
                model.setVideoVolume(cursor.getInt(cursor.getColumnIndex("videovolume")));
                model.setLeadingSpeed(cursor.getFloat(cursor.getColumnIndex("leadingspeed")));
                model.setRobotMode(cursor.getString(cursor.getColumnIndex("robotmode")));
                model.setPatrolContent(cursor.getString(cursor.getColumnIndex("patrolcontent")));
                model.setStayTime(cursor.getInt(cursor.getColumnIndex("staytime")));
                model.setSpeechSpeed(cursor.getInt(cursor.getColumnIndex("speechspeed")));
                boolean VoiceAnnouncements = intToBoolean(cursor.getInt(cursor.getColumnIndex("voiceannouncements")));
                model.setVoiceAnnouncements(VoiceAnnouncements);
                boolean Expression = intToBoolean(cursor.getInt(cursor.getColumnIndex("expression")));
                model.setGoBusinessPoint(cursor.getFloat(cursor.getColumnIndex("gobusinesspoint")));
                model.setBusinessWhetherTime(cursor.getInt(cursor.getColumnIndex("businesswhethertime")));
                boolean businessInterrupt = intToBoolean(cursor.getInt(cursor.getColumnIndex("businessinterrupt")));
                model.setBusinessInterrupt(businessInterrupt);
                model.setExpression(Expression);
                model.setOneKeyCallPhone(cursor.getInt(cursor.getColumnIndex("onekeycallphone")));
                model.setExplainFinishedNotGoBack(cursor.getInt(cursor.getColumnIndex("explainfinishednotgoback")));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return model;
    }

    public static ADVModel ADV() {
        ADVModel advModel = new ADVModel();
        String sql = "SELECT * FROM table_advertising";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 构造实体对象
                advModel.setType(cursor.getInt(cursor.getColumnIndex("type")));
                advModel.setPicType(cursor.getInt(cursor.getColumnIndex("pictype")));
                advModel.setPicPlayTime(cursor.getInt(cursor.getColumnIndex("picplaytime")));
                advModel.setFontContent(cursor.getString(cursor.getColumnIndex("fontcontent")));
                advModel.setFontColor(cursor.getString(cursor.getColumnIndex("fontcolor")));
                advModel.setFontSize(cursor.getInt(cursor.getColumnIndex("fontsize")));
                advModel.setFontLayout(cursor.getInt(cursor.getColumnIndex("fontlayout")));
                advModel.setFontBackGround(cursor.getString(cursor.getColumnIndex("fontbackground")));
                advModel.setTextPosition(cursor.getInt(cursor.getColumnIndex("textposition")));
                advModel.setVideoAudio(cursor.getInt(cursor.getColumnIndex("videoaudio")));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return advModel;
    }

    /**
     * 机器人基础配置
     *
     * @return
     */
    public static Table_Robot_Config robotConfig() {
        Table_Robot_Config robotConfigModel = new Table_Robot_Config();
        String sql = "SELECT * FROM table_robot_config";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
//                robotConfigModel.setId(cursor.getInt(cursor.getColumnIndex("id")));
                robotConfigModel.setSleep(cursor.getInt(cursor.getColumnIndex("sleep")));
                robotConfigModel.setPassword((cursor.getString(cursor.getColumnIndex("password"))) != null ? (cursor.getString(cursor.getColumnIndex("password"))) : "8888");
                robotConfigModel.setMapName(cursor.getString(cursor.getColumnIndex("mapname")));
                robotConfigModel.setWakeUpList(cursor.getString(cursor.getColumnIndex("wakeuplist")));
                robotConfigModel.setSleepTime(cursor.getInt(cursor.getColumnIndex("sleeptime")));
                robotConfigModel.setWakeUpWord(cursor.getString(cursor.getColumnIndex("wakeupword")));
                robotConfigModel.setChargePointName(cursor.getString(cursor.getColumnIndex("chargepointname")));
                robotConfigModel.setWaitingPointName(cursor.getString(cursor.getColumnIndex("waitingpointname")));
                robotConfigModel.setSlogan((cursor.getString(cursor.getColumnIndex("slogan"))) != null ? (cursor.getString(cursor.getColumnIndex("slogan"))) : "欢迎使用多功能服务机器人");
                robotConfigModel.setPhoneConfigJsonArray(cursor.getString(cursor.getColumnIndex("phoneconfigjsonarray")));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return robotConfigModel;
    }

    /**
     * 查询导购配置的基本信息
     * （功能名称、完成任务的提示、中断结束任务之后的提示、首次进入提示）
     * 其他内容get出来为空
     */
    public static Table_Shopping_Config ShoppingConfig() {
        Table_Shopping_Config configDB = new Table_Shopping_Config();
        String sql = "SELECT * FROM table_shopping_config";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                //功能名称
                configDB.setName(cursor.getString(cursor.getColumnIndex("name")));
                //完成任务的提示
                configDB.setCompletePrompt(cursor.getString(cursor.getColumnIndex("completeprompt")));
                //中断结束任务之后的提示
                configDB.setInterruptPrompt(cursor.getString(cursor.getColumnIndex("interruptprompt")));
                //首次进入提示
                configDB.setFirstPrompt(cursor.getString(cursor.getColumnIndex("firstprompt")));
                //时间戳
                configDB.setBaseTimeStamp(cursor.getLong(cursor.getColumnIndex("basetimestamp")));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return configDB;
    }

    public static int selectShoppingId() {
        int id = 0;
        String sql = "SELECT id FROM table_shopping_config ";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                //ID
                id = cursor.getInt(cursor.getColumnIndex("id"));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return id;
    }

    /**
     * 更具总图名字查询导购点
     *
     * @param rootMapName 导购名字
     */
    public static ArrayList<Table_Shopping_Action> SelectShoppingAction(String rootMapName) {
        ArrayList<Table_Shopping_Action> listAction = new ArrayList<>();
        String sql = "SELECT * FROM table_shopping_action actionpoint " +
                "LEFT JOIN table_big_screen bigscreen ON actionpoint.id = bigscreen.table_shopping_action_id " +
                "LEFT JOIN table_touch_screen touch ON actionpoint.id = touch.table_shopping_action_id " +
                "WHERE actionpoint.rootmapname = ?";
        Cursor cursor = LitePal.findBySQL(sql, rootMapName);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Table_Shopping_Action actionDB = new Table_Shopping_Action();
                actionDB.setActionType(cursor.getInt(cursor.getColumnIndex("actiontype")));
                actionDB.setPointName(cursor.getString(cursor.getColumnIndex("pointname")));
                actionDB.setWaitingTime(cursor.getInt(cursor.getColumnIndex("waitingtime")));
                actionDB.setName(cursor.getString(cursor.getColumnIndex("name")));
                actionDB.setStandText(cursor.getString(cursor.getColumnIndex("standtext")));
                actionDB.setArriveText(cursor.getString(cursor.getColumnIndex("arrivetext")));
                actionDB.setMoveText(cursor.getString(cursor.getColumnIndex("movetext")));
                actionDB.setTimestamp(cursor.getLong(cursor.getColumnIndex("timestamp")));
                actionDB.setRootMapName(cursor.getString(cursor.getColumnIndex("rootmapname")));

                Table_Big_Screen bigScreenConfig = new Table_Big_Screen();
                bigScreenConfig.setType(cursor.getInt(cursor.getColumnIndex("type")));
                bigScreenConfig.setPicType(cursor.getInt(cursor.getColumnIndex("pictype")));
                bigScreenConfig.setPicPlayTime(cursor.getInt(cursor.getColumnIndex("picplaytime")));
                bigScreenConfig.setFontContent(cursor.getString(cursor.getColumnIndex("fontcontent")));
                bigScreenConfig.setFontColor(cursor.getString(cursor.getColumnIndex("fontcolor")));
                bigScreenConfig.setFontSize(cursor.getInt(cursor.getColumnIndex("fontsize")));
                bigScreenConfig.setFontLayout(cursor.getInt(cursor.getColumnIndex("fontlayout")));
                bigScreenConfig.setFontBackGround(cursor.getString(cursor.getColumnIndex("fontbackground")));
                bigScreenConfig.setTextPosition(cursor.getInt(cursor.getColumnIndex("textposition")));
                bigScreenConfig.setVideoAudio(cursor.getInt(cursor.getColumnIndex("videoaudio")));
                bigScreenConfig.setVideoFile(cursor.getString(cursor.getColumnIndex("videofile")));
                bigScreenConfig.setImageFile(cursor.getString(cursor.getColumnIndex("imagefile")));
                bigScreenConfig.setVideolayout(cursor.getInt(cursor.getColumnIndex("videolayout")));
                actionDB.setBigScreenConfig(bigScreenConfig);

                Table_Touch_Screen touchScreenConfig = new Table_Touch_Screen();
                touchScreenConfig.setTouch_type(cursor.getInt(cursor.getColumnIndex("touch_type")));
                touchScreenConfig.setTouch_picType(cursor.getInt(cursor.getColumnIndex("touch_pictype")));
                touchScreenConfig.setTouch_picPlayTime(cursor.getInt(cursor.getColumnIndex("touch_picplaytime")));
                touchScreenConfig.setTouch_fontContent(cursor.getString(cursor.getColumnIndex("touch_fontcontent")));
                touchScreenConfig.setTouch_fontColor(cursor.getString(cursor.getColumnIndex("touch_fontcolor")));
                touchScreenConfig.setTouch_fontSize(cursor.getInt(cursor.getColumnIndex("touch_fontsize")));
                touchScreenConfig.setTouch_fontLayout(cursor.getInt(cursor.getColumnIndex("touch_fontlayout")));
                touchScreenConfig.setTouch_fontBackGround(cursor.getString(cursor.getColumnIndex("touch_fontbackground")));
                touchScreenConfig.setTouch_textPosition(cursor.getInt(cursor.getColumnIndex("touch_textposition")));
                touchScreenConfig.setTouch_imageFile(cursor.getString(cursor.getColumnIndex("touch_imagefile")));
                touchScreenConfig.setTouch_walkPic(cursor.getString(cursor.getColumnIndex("touch_walkpic")));
                touchScreenConfig.setTouch_blockPic(cursor.getString(cursor.getColumnIndex("touch_blockpic")));
                touchScreenConfig.setTouch_arrivePic(cursor.getString(cursor.getColumnIndex("touch_arrivepic")));
                touchScreenConfig.setTouch_overTaskPic(cursor.getString(cursor.getColumnIndex("touch_overtaskpic")));
                actionDB.setTouchScreenConfig(touchScreenConfig);

                listAction.add(actionDB);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return listAction;
    }

    public static ArrayList<Table_Face> faceMessage(){
        ArrayList<Table_Face> listAction = new ArrayList<>();
        String sql = "SELECT * FROM table_face";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Table_Face faceTips = new Table_Face();
                faceTips.setSexual(cursor.getString(cursor.getColumnIndex("facefeat")));
                faceTips.setName(cursor.getString(cursor.getColumnIndex("name")));
                listAction.add(faceTips);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return listAction;

    }

    /**
     * 查询单个导购点的所有信息
     *
     * @param rootMapName 总图名字
     * @param name        导购点名字：自己后台拟定的
     */
    public static Table_Shopping_Action SelectActionData(String rootMapName, String name, int type) {
        Table_Shopping_Action listAction = new Table_Shopping_Action();
        String sql = "SELECT * FROM table_shopping_action actionpoint " +
                "LEFT JOIN table_big_screen bigscreen ON actionpoint.id = bigscreen.table_shopping_action_id " +
                "LEFT JOIN table_touch_screen touch ON actionpoint.id = touch.table_shopping_action_id " +
                "WHERE actionpoint.rootmapname = ? AND actionpoint.name = ? AND actionpoint.actiontype = ?";
        Cursor cursor = LitePal.findBySQL(sql, rootMapName, name, type + "");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                listAction.setActionType(cursor.getInt(cursor.getColumnIndex("actiontype")));
                listAction.setPointName(cursor.getString(cursor.getColumnIndex("pointname")));
                listAction.setWaitingTime(cursor.getInt(cursor.getColumnIndex("waitingtime")));
                listAction.setName(cursor.getString(cursor.getColumnIndex("name")));
                listAction.setStandText(cursor.getString(cursor.getColumnIndex("standtext")));
                listAction.setArriveText(cursor.getString(cursor.getColumnIndex("arrivetext")));
                listAction.setMoveText(cursor.getString(cursor.getColumnIndex("movetext")));
                listAction.setTimestamp(cursor.getLong(cursor.getColumnIndex("timestamp")));
                listAction.setRootMapName(cursor.getString(cursor.getColumnIndex("rootmapname")));

                Table_Big_Screen bigScreenConfig = new Table_Big_Screen();
                bigScreenConfig.setType(cursor.getInt(cursor.getColumnIndex("type")));
                bigScreenConfig.setPicType(cursor.getInt(cursor.getColumnIndex("pictype")));
                bigScreenConfig.setPicPlayTime(cursor.getInt(cursor.getColumnIndex("picplaytime")));
                bigScreenConfig.setFontContent(cursor.getString(cursor.getColumnIndex("fontcontent")));
                bigScreenConfig.setFontColor(cursor.getString(cursor.getColumnIndex("fontcolor")));
                bigScreenConfig.setFontSize(cursor.getInt(cursor.getColumnIndex("fontsize")));
                bigScreenConfig.setFontLayout(cursor.getInt(cursor.getColumnIndex("fontlayout")));
                bigScreenConfig.setFontBackGround(cursor.getString(cursor.getColumnIndex("fontbackground")));
                bigScreenConfig.setTextPosition(cursor.getInt(cursor.getColumnIndex("textposition")));
                bigScreenConfig.setVideoAudio(cursor.getInt(cursor.getColumnIndex("videoaudio")));
                bigScreenConfig.setVideoFile(cursor.getString(cursor.getColumnIndex("videofile")));
                bigScreenConfig.setImageFile(cursor.getString(cursor.getColumnIndex("imagefile")));
                bigScreenConfig.setVideolayout(cursor.getInt(cursor.getColumnIndex("videolayout")));

                listAction.setBigScreenConfig(bigScreenConfig);

                Table_Touch_Screen touchScreenConfig = new Table_Touch_Screen();
                touchScreenConfig.setTouch_type(cursor.getInt(cursor.getColumnIndex("touch_type")));
                touchScreenConfig.setTouch_picType(cursor.getInt(cursor.getColumnIndex("touch_pictype")));
                touchScreenConfig.setTouch_picPlayTime(cursor.getInt(cursor.getColumnIndex("touch_picplaytime")));
                touchScreenConfig.setTouch_fontContent(cursor.getString(cursor.getColumnIndex("touch_fontcontent")));
                touchScreenConfig.setTouch_fontColor(cursor.getString(cursor.getColumnIndex("touch_fontcolor")));
                touchScreenConfig.setTouch_fontSize(cursor.getInt(cursor.getColumnIndex("touch_fontsize")));
                touchScreenConfig.setTouch_fontLayout(cursor.getInt(cursor.getColumnIndex("touch_fontlayout")));
                touchScreenConfig.setTouch_fontBackGround(cursor.getString(cursor.getColumnIndex("touch_fontbackground")));
                touchScreenConfig.setTouch_textPosition(cursor.getInt(cursor.getColumnIndex("touch_textposition")));
                touchScreenConfig.setTouch_imageFile(cursor.getString(cursor.getColumnIndex("touch_imagefile")));
                touchScreenConfig.setTouch_walkPic(cursor.getString(cursor.getColumnIndex("touch_walkpic")));
                touchScreenConfig.setTouch_blockPic(cursor.getString(cursor.getColumnIndex("touch_blockpic")));
                touchScreenConfig.setTouch_arrivePic(cursor.getString(cursor.getColumnIndex("touch_arrivepic")));
                touchScreenConfig.setTouch_overTaskPic(cursor.getString(cursor.getColumnIndex("touch_overtaskpic")));
                listAction.setTouchScreenConfig(touchScreenConfig);

            } while (cursor.moveToNext());
            cursor.close();
        }
        return listAction;
    }

    public static List<GuideConfigList> selectGuideConfig(String rootMapName, String pointName) {
        List<GuideConfigList> guideList = new ArrayList<>();
        if (rootMapName == null || pointName == null) {
            // Handle the case where parameters are null
            return guideList; // Return an empty list or throw an exception
        }
        String sql = "SELECT * FROM table_guide_point_pic WHERE table_guide_point_pic.mapname = ? AND table_guide_point_pic.pointname = ?";
        Cursor cursor = LitePal.findBySQL(sql, rootMapName, pointName);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                GuideConfigList guideConfig = new GuideConfigList();
                guideConfig.setMapName(cursor.getString(cursor.getColumnIndex("mapname")));
                guideConfig.setMapTimeStamp(cursor.getLong(cursor.getColumnIndex("maptimestamp")));
                guideConfig.setPointName(cursor.getString(cursor.getColumnIndex("pointname")));
                guideConfig.setGuidePicUrl(cursor.getString(cursor.getColumnIndex("guidepicurl")));
                guideConfig.setPointTimeStamp(cursor.getLong(cursor.getColumnIndex("pointtimestamp")));
                guideList.add(guideConfig);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return guideList;
    }


    public static List<GuideConfigList> selectGuideList(String rootMapName) {
        List<GuideConfigList> guideList = new ArrayList<>();
        String sql = "SELECT * FROM table_guide_point_pic WHERE table_guide_point_pic.mapname = ? ";
        Cursor cursor = LitePal.findBySQL(sql, rootMapName);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                GuideConfigList config = new GuideConfigList();
                config.setMapName(cursor.getString(cursor.getColumnIndex("mapname")));
                config.setMapTimeStamp(cursor.getLong(cursor.getColumnIndex("maptimestamp")));
                config.setPointName(cursor.getString(cursor.getColumnIndex("pointname")));
                config.setGuidePicUrl(cursor.getString(cursor.getColumnIndex("guidepicurl")));
                config.setPointTimeStamp(cursor.getLong(cursor.getColumnIndex("pointtimestamp")));
                guideList.add(config);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return guideList;
    }


    public static ArrayList<SendShoppingActionModel> SelectAndSendActionTime() {
        ArrayList<SendShoppingActionModel> listAction = new ArrayList<>();
        String sql = "SELECT * FROM table_shopping_action actionpoint " +
                "LEFT JOIN table_big_screen bigscreen ON actionpoint.id = bigscreen.table_shopping_action_id " +
                "LEFT JOIN table_touch_screen touch ON actionpoint.id = touch.table_shopping_action_id ";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                SendShoppingActionModel sendShoppingActionModel = new SendShoppingActionModel();
                sendShoppingActionModel.setName(cursor.getString(cursor.getColumnIndex("name")));
                sendShoppingActionModel.setTimeStamp(cursor.getLong(cursor.getColumnIndex("timestamp")));
                sendShoppingActionModel.setMapName(cursor.getString(cursor.getColumnIndex("rootmapname")));
                listAction.add(sendShoppingActionModel);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return listAction;
    }

    public static GuideSendModel sendGuideConfig() {
        GuideSendModel sendList = new GuideSendModel();
        String sql = "SELECT table_guide_point_pic.mapname, MAX(table_guide_point_pic.maptimestamp) AS max_timestamp FROM table_guide_point_pic GROUP BY table_guide_point_pic.mapname;";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            // 创建一个 MapConfig 列表
            List<MapConfig> mapConfigs = new ArrayList<>();
            do {
                // 从 cursor 中获取数据
                String mapName = cursor.getString(cursor.getColumnIndex("mapname"));
                long mapTimeStamp = cursor.getLong(cursor.getColumnIndex("max_timestamp"));

                // 创建 MapConfig 对象并添加到列表中
                MapConfig mapConfig = new MapConfig(mapName, mapTimeStamp);
                mapConfigs.add(mapConfig);
            } while (cursor.moveToNext());
            // 将 MapConfig 列表设置到 GuideSendModel 对象中
            sendList = new GuideSendModel(sendGuideTimeStamp(), mapConfigs);
            cursor.close();
        }
        return sendList;
    }

    public static Long sendGuideTimeStamp() {
        long time = 0L;
        String sql = "SELECT table_guide_foundation.timestamp FROM table_guide_foundation";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                time = cursor.getLong(cursor.getColumnIndex("timestamp"));

            } while (cursor.moveToNext());
            cursor.close();
        }
        return time;
    }

    public static Table_Guide_Foundation selectGuideFouConfig() {
        Table_Guide_Foundation ConfigList = new Table_Guide_Foundation();
        String sql = "SELECT * FROM table_guide_foundation foundation\n" +
                "    LEFT JOIN table_big_screen bigscreen ON foundation.id = bigscreen.table_guide_foundation_id \n" +
                "    LEFT JOIN table_touch_screen touch ON foundation.id = touch.table_guide_foundation_id ";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {

                ConfigList.setInterruptPrompt(cursor.getString(cursor.getColumnIndex("interruptprompt")));
                ConfigList.setArrivePrompt(cursor.getString(cursor.getColumnIndex("arriveprompt")));
                ConfigList.setFirstPrompt(cursor.getString(cursor.getColumnIndex("firstprompt")));
                ConfigList.setMovePrompt(cursor.getString(cursor.getColumnIndex("moveprompt")));
                ConfigList.setTimeStamp(cursor.getLong(cursor.getColumnIndex("timestamp")));

                Table_Big_Screen bigScreenConfig = new Table_Big_Screen();
                bigScreenConfig.setType(cursor.getInt(cursor.getColumnIndex("type")));
                bigScreenConfig.setPicType(cursor.getInt(cursor.getColumnIndex("pictype")));
                bigScreenConfig.setPicPlayTime(cursor.getInt(cursor.getColumnIndex("picplaytime")));
                bigScreenConfig.setFontContent(cursor.getString(cursor.getColumnIndex("fontcontent")));
                bigScreenConfig.setFontColor(cursor.getString(cursor.getColumnIndex("fontcolor")));
                bigScreenConfig.setFontSize(cursor.getInt(cursor.getColumnIndex("fontsize")));
                bigScreenConfig.setFontLayout(cursor.getInt(cursor.getColumnIndex("fontlayout")));
                bigScreenConfig.setFontBackGround(cursor.getString(cursor.getColumnIndex("fontbackground")));
                bigScreenConfig.setTextPosition(cursor.getInt(cursor.getColumnIndex("textposition")));
                bigScreenConfig.setVideoAudio(cursor.getInt(cursor.getColumnIndex("videoaudio")));
                bigScreenConfig.setVideoFile(cursor.getString(cursor.getColumnIndex("videofile")));
                bigScreenConfig.setImageFile(cursor.getString(cursor.getColumnIndex("imagefile")));
                bigScreenConfig.setVideolayout(cursor.getInt(cursor.getColumnIndex("videolayout")));
                ConfigList.setBigScreenConfig(bigScreenConfig);

                Table_Touch_Screen touchScreenConfig = new Table_Touch_Screen();
                touchScreenConfig.setTouch_type(cursor.getInt(cursor.getColumnIndex("touch_type")));
                touchScreenConfig.setTouch_picType(cursor.getInt(cursor.getColumnIndex("touch_pictype")));
                touchScreenConfig.setTouch_picPlayTime(cursor.getInt(cursor.getColumnIndex("touch_picplaytime")));
                touchScreenConfig.setTouch_fontContent(cursor.getString(cursor.getColumnIndex("touch_fontcontent")));
                touchScreenConfig.setTouch_fontColor(cursor.getString(cursor.getColumnIndex("touch_fontcolor")));
                touchScreenConfig.setTouch_fontSize(cursor.getInt(cursor.getColumnIndex("touch_fontsize")));
                touchScreenConfig.setTouch_fontLayout(cursor.getInt(cursor.getColumnIndex("touch_fontlayout")));
                touchScreenConfig.setTouch_fontBackGround(cursor.getString(cursor.getColumnIndex("touch_fontbackground")));
                touchScreenConfig.setTouch_textPosition(cursor.getInt(cursor.getColumnIndex("touch_textposition")));
                touchScreenConfig.setTouch_imageFile(cursor.getString(cursor.getColumnIndex("touch_imagefile")));
                touchScreenConfig.setTouch_walkPic(cursor.getString(cursor.getColumnIndex("touch_walkpic")));
                touchScreenConfig.setTouch_blockPic(cursor.getString(cursor.getColumnIndex("touch_blockpic")));
                touchScreenConfig.setTouch_arrivePic(cursor.getString(cursor.getColumnIndex("touch_arrivepic")));
                touchScreenConfig.setTouch_overTaskPic(cursor.getString(cursor.getColumnIndex("touch_overtaskpic")));
                ConfigList.setTouchScreenConfig(touchScreenConfig);
            } while (cursor.moveToNext());
        }
        return ConfigList;
    }

    /**
     * 查询问答配置
     *
     * @return
     */
    public static String selectQaConfig() {
        String qaJson = "";
        String sql = "SELECT qajson FROM table_qa_config";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                qaJson = cursor.getString(cursor.getColumnIndex("qajson"));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return qaJson;
    }

    /**
     * 查询迎宾配置
     */
    public static Table_Greet_Config selectGreetConfig(){
        Table_Greet_Config greetConfig = new Table_Greet_Config();
        String sql = "SELECT * FROM table_greet_config table_greet\n" +
                "LEFT JOIN table_big_screen bigscreen ON table_greet.id = bigscreen.table_greet_config_id \n" +
                "LEFT JOIN table_touch_screen touch ON table_greet.id = touch.table_greet_config_id ";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor !=null && cursor.moveToFirst()){
            do {
                greetConfig.setGreetPoint(cursor.getString(cursor.getColumnIndex("greetpoint")));
                greetConfig.setFirstPrompt(cursor.getString(cursor.getColumnIndex("firstprompt")));
                greetConfig.setStrangerPrompt(cursor.getString(cursor.getColumnIndex("strangerprompt")));
                greetConfig.setVipPrompt(cursor.getString(cursor.getColumnIndex("vipprompt")));
                greetConfig.setExitPrompt(cursor.getString(cursor.getColumnIndex("exitprompt")));
                greetConfig.setTimeStamp(cursor.getLong(cursor.getColumnIndex("timestamp")));

                Table_Big_Screen bigScreenConfig = new Table_Big_Screen();
                bigScreenConfig.setType(cursor.getInt(cursor.getColumnIndex("type")));
                bigScreenConfig.setPicType(cursor.getInt(cursor.getColumnIndex("pictype")));
                bigScreenConfig.setPicPlayTime(cursor.getInt(cursor.getColumnIndex("picplaytime")));
                bigScreenConfig.setFontContent(cursor.getString(cursor.getColumnIndex("fontcontent")));
                bigScreenConfig.setFontColor(cursor.getString(cursor.getColumnIndex("fontcolor")));
                bigScreenConfig.setFontSize(cursor.getInt(cursor.getColumnIndex("fontsize")));
                bigScreenConfig.setFontLayout(cursor.getInt(cursor.getColumnIndex("fontlayout")));
                bigScreenConfig.setFontBackGround(cursor.getString(cursor.getColumnIndex("fontbackground")));
                bigScreenConfig.setTextPosition(cursor.getInt(cursor.getColumnIndex("textposition")));
                bigScreenConfig.setVideoAudio(cursor.getInt(cursor.getColumnIndex("videoaudio")));
                bigScreenConfig.setVideoFile(cursor.getString(cursor.getColumnIndex("videofile")));
                bigScreenConfig.setImageFile(cursor.getString(cursor.getColumnIndex("imagefile")));
                bigScreenConfig.setVideolayout(cursor.getInt(cursor.getColumnIndex("videolayout")));
                greetConfig.setBigScreenConfig(bigScreenConfig);

                Table_Touch_Screen touchScreenConfig = new Table_Touch_Screen();
                touchScreenConfig.setTouch_type(cursor.getInt(cursor.getColumnIndex("touch_type")));
                touchScreenConfig.setTouch_picType(cursor.getInt(cursor.getColumnIndex("touch_pictype")));
                touchScreenConfig.setTouch_picPlayTime(cursor.getInt(cursor.getColumnIndex("touch_picplaytime")));
                touchScreenConfig.setTouch_fontContent(cursor.getString(cursor.getColumnIndex("touch_fontcontent")));
                touchScreenConfig.setTouch_fontColor(cursor.getString(cursor.getColumnIndex("touch_fontcolor")));
                touchScreenConfig.setTouch_fontSize(cursor.getInt(cursor.getColumnIndex("touch_fontsize")));
                touchScreenConfig.setTouch_fontLayout(cursor.getInt(cursor.getColumnIndex("touch_fontlayout")));
                touchScreenConfig.setTouch_fontBackGround(cursor.getString(cursor.getColumnIndex("touch_fontbackground")));
                touchScreenConfig.setTouch_textPosition(cursor.getInt(cursor.getColumnIndex("touch_textposition")));
                touchScreenConfig.setTouch_imageFile(cursor.getString(cursor.getColumnIndex("touch_imagefile")));
                touchScreenConfig.setTouch_walkPic(cursor.getString(cursor.getColumnIndex("touch_walkpic")));
                touchScreenConfig.setTouch_blockPic(cursor.getString(cursor.getColumnIndex("touch_blockpic")));
                touchScreenConfig.setTouch_arrivePic(cursor.getString(cursor.getColumnIndex("touch_arrivepic")));
                touchScreenConfig.setTouch_overTaskPic(cursor.getString(cursor.getColumnIndex("touch_overtaskpic")));
                greetConfig.setTouchScreenConfig(touchScreenConfig);

            }while (cursor.moveToNext());
        }
        return greetConfig;

    }

    public static JSONArray queryAppletIdList() {
        JSONArray jsonArray = new JSONArray();
        String sql = "SELECT appletid,timestamp FROM table_applet_config";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", cursor.getInt(cursor.getColumnIndex("appletid")));
                jsonObject.put("timeStamp", cursor.getLong(cursor.getColumnIndex("timestamp")));
                jsonArray.add(jsonObject);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return jsonArray;
    }

    public static List<ApplicationModel> queryApplicationModelList(){
        ArrayList<ApplicationModel> list = new ArrayList<>();
        String sql = "SELECT\n" +
                "ac.icon as ac_icon,\n" +
                "ac.name as ac_name,\n" +
                "ac.type as ac_type,\n" +
                "ac.url as ac_url,\n" +
                "ac.title as ac_title,\n" +
                "ac.content as ac_content,\n" +
                "ac.packagename as ac_packagename,\n" +
                "bs.fontbackground as bs_fontbackground,\n" +
                "bs.picplaytime as bs_picplaytime,\n" +
                "bs.fontlayout as bs_fontlayout,\n" +
                "bs.imagefile as bs_imagefile,\n" +
                "bs.fontsize as bs_fontsize,\n" +
                "bs.type as bs_type,\n" +
                "bs.videofile as bs_videofile,\n" +
                "bs.textposition as bs_textposition,\n" +
                "bs.videolayout as bs_videolayout,\n" +
                "bs.fontcontent as bs_fontcontent,\n" +
                "bs.pictype as bs_pictype,\n" +
                "bs.videoaudio as bs_videoaudio,\n" +
                "bs.fontcolor as bs_fontcolor\n" +
                "FROM table_applet_config as ac\n" +
                "LEFT JOIN table_big_screen as bs ON ac.id = bs.table_applet_config_id;";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String icon = cursor.getString(cursor.getColumnIndex("ac_icon"));
                String url = cursor.getString(cursor.getColumnIndex("ac_url"));
                String name = cursor.getString(cursor.getColumnIndex("ac_name"));
                int appletType = cursor.getInt(cursor.getColumnIndex("ac_type"));
                String title = cursor.getString(cursor.getColumnIndex("ac_title"));
                String content = cursor.getString(cursor.getColumnIndex("ac_content"));
                String packageName = cursor.getString(cursor.getColumnIndex("ac_packagename"));
                String fontBackground = cursor.getString(cursor.getColumnIndex("bs_fontbackground"));
                int picPlayTime = cursor.getInt(cursor.getColumnIndex("bs_picplaytime"));
                int fontLayout = cursor.getInt(cursor.getColumnIndex("bs_fontlayout"));
                String imageFile = cursor.getString(cursor.getColumnIndex("bs_imagefile"));
                int fontSize = cursor.getInt(cursor.getColumnIndex("bs_fontsize"));
                int type = cursor.getInt(cursor.getColumnIndex("bs_type"));
                String videoFile = cursor.getString(cursor.getColumnIndex("bs_videofile"));
                int textPosition = cursor.getInt(cursor.getColumnIndex("bs_textposition"));
                int videoLayout = cursor.getInt(cursor.getColumnIndex("bs_videolayout"));
                String fontContent = cursor.getString(cursor.getColumnIndex("bs_fontcontent"));
                int picType = cursor.getInt(cursor.getColumnIndex("bs_pictype"));
                int videoAudio = cursor.getInt(cursor.getColumnIndex("bs_videoaudio"));
                String fontColor = cursor.getString(cursor.getColumnIndex("bs_fontcolor"));
                ApplicationModel model = new ApplicationModel(
                        name,
                        url,
                        icon,
                        appletType,
                        title,
                        content,
                        packageName,
                        new SecondModel(
                          picPlayTime,
                          videoFile != null ? videoFile: imageFile,
                          type,
                          textPosition,
                          fontLayout,
                          fontContent,
                          fontBackground,
                          fontColor,
                          fontSize,
                          picType,
                          videoLayout,
                          videoAudio,
                          false
                        )
                );
                list.add(model);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    /**
     * 将查询到的int转为Boolean
     *
     * @param data 数据
     */
    private static Boolean intToBoolean(int data) {
        return data == 1;
    }
}
