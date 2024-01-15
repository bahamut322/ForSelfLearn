package com.sendi.deliveredrobot.entity.entitySql;

import com.sendi.deliveredrobot.entity.Table_Big_Screen;
import com.sendi.deliveredrobot.entity.Table_Guide_Point_Pic;
import com.sendi.deliveredrobot.entity.Table_Shopping_Action;
import com.sendi.deliveredrobot.entity.Table_Touch_Screen;

import org.litepal.LitePal;

/**
 * @Author Swn
 * @Data 2023/11/8
 * @describe
 */
public class DeleteSql {

    /**
     * 删除ShoppingActionDB中的点
     * @param actionName 导购点名字（当传入数据为null的时候则会删除总图下所有的点）
     * @param rootMapName 总图名字
     */
    public static boolean deleteShoppingAction(String actionName, String rootMapName) {

        int rowsAffected = LitePal.deleteAll(Table_Shopping_Action.class, "name = ? and rootMapName = ?", actionName, rootMapName);
        return rowsAffected > 0;
    }
    public static void deleteBigPic(String fileName) {
         LitePal.deleteAll(Table_Big_Screen.class, "imagefile = ? ",fileName);
    }
    public static void deleteTouchPic(String fileName) {
        LitePal.deleteAll(Table_Touch_Screen.class, "touch_imagefile = ? ",fileName);
    }
    public static void deleteGuidePointConfig (String name,String mapName){
        LitePal.deleteAll(Table_Guide_Point_Pic.class, "pointname = ? and mapName = ?",name,mapName);
    }

}
