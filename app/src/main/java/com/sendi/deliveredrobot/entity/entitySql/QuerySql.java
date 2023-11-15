package com.sendi.deliveredrobot.entity.entitySql;

import android.database.Cursor;
import android.util.Log;

import com.sendi.deliveredrobot.entity.BigScreenConfigDB;

import com.sendi.deliveredrobot.entity.GuideFoundationConfigDB;
import com.sendi.deliveredrobot.entity.RobotConfigSql;
import com.sendi.deliveredrobot.entity.ShoppingActionDB;
import com.sendi.deliveredrobot.entity.ShoppingConfigDB;
import com.sendi.deliveredrobot.entity.TouchScreenConfigDB;
import com.sendi.deliveredrobot.model.ADVModel;
import com.sendi.deliveredrobot.model.BasicModel;
import com.sendi.deliveredrobot.model.ExplainConfigModel;
import com.sendi.deliveredrobot.model.GuideConfigList;
import com.sendi.deliveredrobot.model.GuidePointList;
import com.sendi.deliveredrobot.model.GuideSendModel;
import com.sendi.deliveredrobot.model.MapConfig;
import com.sendi.deliveredrobot.model.MyResultModel;
import com.sendi.deliveredrobot.model.RouteMapList;
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
        String sql = "SELECT * FROM routedb route "
                + "LEFT JOIN pointconfigvodb point ON " + routeId + " = point.routedb_id "
                + "LEFT JOIN bigscreenconfigdb bigscreen ON point.id = bigscreen.pointconfigvodb_id "
                + "LEFT JOIN touchscreenconfigdb touch ON point.id = touch.pointconfigvodb_id "
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
                "FROM routedb route " +
                "LEFT JOIN pointconfigvodb point " +
                "ON route.id = point.routedb_id " +
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
        String sql = "SELECT routedb.rootmapname FROM routedb ";
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
        String sql = "SELECT * FROM routedb WHERE routedb.rootmapname =  " + "'" + rootMapName + "'";
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
        String sql = "SELECT routedb.timestamp FROM routedb WHERE routedb.routename = " + "'" + routeName + "'";
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
        String sql = "SELECT routedb.id FROM routedb WHERE routedb.routename = " + "'" + routeName + "'";
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
     *
     * @param routeName 路径名字
     */
    public static int pointConfigVoDB_id(String routeName) {
        int pointConfigVoDB_id = 0;
        String sql = "SELECT pointconfigvodb.id FROM pointconfigvodb WHERE pointconfigvodb.routedb_id = (SELECT routedb.id FROM routedb WHERE routedb.routename = " + "'" + routeName + "'" + " )";
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
     *
     * @return
     */
    public static long advTimeStamp() {
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

    public static int QueryBasicId() {
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
            } while (cursor.moveToNext());
            cursor.close();
        }
        return model;
    }

    public static ADVModel ADV() {
        ADVModel advModel = new ADVModel();
        String sql = "SELECT * FROM advertisingconfigdb";
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
    public static RobotConfigSql robotConfig() {
        RobotConfigSql robotConfigModel = new RobotConfigSql();
        String sql = "SELECT * FROM robotconfigsql";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
//                robotConfigModel.setId(cursor.getInt(cursor.getColumnIndex("id")));
                robotConfigModel.setSleep(cursor.getInt(cursor.getColumnIndex("sleep")));
                robotConfigModel.setPassword(cursor.getString(cursor.getColumnIndex("password")));
                robotConfigModel.setMapName(cursor.getString(cursor.getColumnIndex("mapname")));
                robotConfigModel.setWakeUpList(cursor.getString(cursor.getColumnIndex("wakeuplist")));
                robotConfigModel.setSleepTime(cursor.getInt(cursor.getColumnIndex("sleeptime")));
                robotConfigModel.setChargePointName(cursor.getString(cursor.getColumnIndex("chargepointname")));
                robotConfigModel.setWaitingPointName(cursor.getString(cursor.getColumnIndex("waitingpointname")));
            } while (cursor.moveToNext());
            cursor.close();
        } else if (!cursor.moveToFirst() || cursor.getString(cursor.getColumnIndex("password")) == null) {
            robotConfigModel.setPassword("8888");
        }
        return robotConfigModel;
    }

    /**
     * 查询导购配置的基本信息
     * （功能名称、完成任务的提示、中断结束任务之后的提示、首次进入提示）
     * 其他内容get出来为空
     */
    public static ShoppingConfigDB ShoppingConfig() {
        ShoppingConfigDB configDB = new ShoppingConfigDB();
        String sql = "SELECT * FROM shoppingconfigdb";
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
        String sql = "SELECT id FROM shoppingconfigdb ";
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
    public static ArrayList<ShoppingActionDB> SelectShoppingAction(String rootMapName) {
        ArrayList<ShoppingActionDB> listAction = new ArrayList<>();
        String sql = "SELECT * FROM shoppingactiondb actionpoint " +
                "LEFT JOIN bigscreenconfigdb bigscreen ON actionpoint.id = bigscreen.shoppingactiondb_id " +
                "LEFT JOIN touchscreenconfigdb touch ON actionpoint.id = touch.shoppingactiondb_id " +
                "WHERE actionpoint.rootmapname = ?";
        Cursor cursor = LitePal.findBySQL(sql, rootMapName);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ShoppingActionDB actionDB = new ShoppingActionDB();
                actionDB.setActionType(cursor.getInt(cursor.getColumnIndex("actiontype")));
                actionDB.setPointName(cursor.getString(cursor.getColumnIndex("pointname")));
                actionDB.setWaitingTime(cursor.getInt(cursor.getColumnIndex("waitingtime")));
                actionDB.setName(cursor.getString(cursor.getColumnIndex("name")));
                actionDB.setStandText(cursor.getString(cursor.getColumnIndex("standtext")));
                actionDB.setArriveText(cursor.getString(cursor.getColumnIndex("arrivetext")));
                actionDB.setMoveText(cursor.getString(cursor.getColumnIndex("movetext")));
                actionDB.setTimestamp(cursor.getLong(cursor.getColumnIndex("timestamp")));
                actionDB.setRootMapName(cursor.getString(cursor.getColumnIndex("rootmapname")));

                BigScreenConfigDB bigScreenConfig = new BigScreenConfigDB();
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
                actionDB.setBigScreenConfig(bigScreenConfig);

                TouchScreenConfigDB touchScreenConfig = new TouchScreenConfigDB();
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

    /**
     * 查询单个导购点的所有信息
     * @param rootMapName 总图名字
     * @param name 导购点名字：自己后台拟定的
     */
    public static ShoppingActionDB SelectActionData(String rootMapName,String name,int type) {
        ShoppingActionDB listAction = new ShoppingActionDB();
        String sql = "SELECT * FROM shoppingactiondb actionpoint " +
                "LEFT JOIN bigscreenconfigdb bigscreen ON actionpoint.id = bigscreen.shoppingactiondb_id " +
                "LEFT JOIN touchscreenconfigdb touch ON actionpoint.id = touch.shoppingactiondb_id " +
                "WHERE actionpoint.rootmapname = ? AND actionpoint.pointName = ? AND actionpoint.actiontype = ?";
        Cursor cursor = LitePal.findBySQL(sql, rootMapName,name,type+"");
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

                BigScreenConfigDB bigScreenConfig = new BigScreenConfigDB();
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
                listAction.setBigScreenConfig(bigScreenConfig);

                TouchScreenConfigDB touchScreenConfig = new TouchScreenConfigDB();
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

    public static GuideConfigList selectGuideConfig(String rootMapName , String pointName){
        GuideConfigList guideList = new GuideConfigList() ;
        String sql = "SELECT * FROM guidepointpicdb WHERE guidepointpicdb.mapname = ? AND guidepointpicdb.pointname = ?";
        Cursor cursor = LitePal.findBySQL(sql , rootMapName,pointName);
        if (cursor!=null && cursor.moveToFirst()){
            do {
                guideList.setMapName(cursor.getString(cursor.getColumnIndex("mapname")));
                guideList.setMapTimeStamp(cursor.getLong(cursor.getColumnIndex("maptimestamp")));
                guideList.setPointName(cursor.getString(cursor.getColumnIndex("pointname")));
                guideList.setGuidePicUrl(cursor.getString(cursor.getColumnIndex("guidepicurl")));
                guideList.setPointTimeStamp(cursor.getLong(cursor.getColumnIndex("pointtimestamp")));

            }while (cursor.moveToNext());
            cursor.close();
        }
        return guideList;
    }

    public static List<GuideConfigList> selectGuideList(String rootMapName){
        List<GuideConfigList> guideList = new ArrayList<>() ;
        String sql = "SELECT * FROM guidepointpicdb WHERE guidepointpicdb.mapname = ? ";
        Cursor cursor = LitePal.findBySQL(sql , rootMapName);
        if (cursor!=null && cursor.moveToFirst()){
            do {
                GuideConfigList config = new GuideConfigList();
                config.setMapName(cursor.getString(cursor.getColumnIndex("mapname")));
                config.setMapTimeStamp(cursor.getLong(cursor.getColumnIndex("maptimestamp")));
                config.setPointName(cursor.getString(cursor.getColumnIndex("pointname")));
                config.setGuidePicUrl(cursor.getString(cursor.getColumnIndex("guidepicurl")));
                config.setPointTimeStamp(cursor.getLong(cursor.getColumnIndex("pointtimestamp")));
                guideList.add(config);
            }while (cursor.moveToNext());
            cursor.close();
        }
        return guideList;
    }


    public static ArrayList<SendShoppingActionModel> SelectAndSendActionTime() {
        ArrayList<SendShoppingActionModel> listAction = new ArrayList<>();
        String sql = "SELECT * FROM shoppingactiondb actionpoint " +
                "LEFT JOIN bigscreenconfigdb bigscreen ON actionpoint.id = bigscreen.shoppingactiondb_id " +
                "LEFT JOIN touchscreenconfigdb touch ON actionpoint.id = touch.shoppingactiondb_id ";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                SendShoppingActionModel sendShoppingActionModel = new SendShoppingActionModel();
                sendShoppingActionModel.setName(cursor.getString(cursor.getColumnIndex("name")));
                sendShoppingActionModel.setTimeStamp(cursor.getLong(cursor.getColumnIndex("timestamp")));
                listAction.add(sendShoppingActionModel);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return listAction;
    }

    public static ArrayList<GuideSendModel> sendGuideConfig(){
        ArrayList<GuideSendModel> sendList = new ArrayList<>();
        String sql = "SELECT distinct guidepointpicdb.mapname,guidepointpicdb.maptimestamp FROM guidepointpicdb";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            // 创建一个 MapConfig 列表
            List<MapConfig> mapConfigs = new ArrayList<>();
            do {
                // 从 cursor 中获取数据
                String mapName = cursor.getString(cursor.getColumnIndex("mapname"));
                long mapTimeStamp = cursor.getLong(cursor.getColumnIndex("maptimestamp"));

                // 创建 MapConfig 对象并添加到列表中
                MapConfig mapConfig = new MapConfig(mapName, mapTimeStamp);
                mapConfigs.add(mapConfig);
            } while (cursor.moveToNext());
            // 创建 GuideSendModel 对象并添加到 sendList 中
            GuideSendModel send = new GuideSendModel(mapConfigs);
            sendList.add(send);
            cursor.close();
        }
        return sendList;
    }

    public static GuideFoundationConfigDB selectGuideFouConfig(){
        GuideFoundationConfigDB ConfigList = new GuideFoundationConfigDB();
        String sql = "SELECT * FROM guidefoundationconfigdb foundation\n" +
                "    LEFT JOIN bigscreenconfigdb bigscreen ON foundation.id = bigscreen.guidefoundationconfigdb_id \n" +
                "    LEFT JOIN touchscreenconfigdb touch ON foundation.id = touch.guidefoundationconfigdb_id ";
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor != null && cursor.moveToFirst()) {
            do {

                ConfigList.setInterruptPrompt(cursor.getString(cursor.getColumnIndex("interruptprompt")));
                ConfigList.setArrivePrompt(cursor.getString(cursor.getColumnIndex("arriveprompt")));
                ConfigList.setFirstPrompt(cursor.getString(cursor.getColumnIndex("firstprompt")));
                ConfigList.setMovePrompt(cursor.getString(cursor.getColumnIndex("moveprompt")));
                ConfigList.setTimeStamp(cursor.getLong(cursor.getColumnIndex("timestamp")));

                BigScreenConfigDB bigScreenConfig = new BigScreenConfigDB();
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
                ConfigList.setBigScreenConfig(bigScreenConfig);

                TouchScreenConfigDB touchScreenConfig = new TouchScreenConfigDB();
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
            }while (cursor.moveToNext());
        }
        return ConfigList;
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
