package com.sendi.deliveredrobot.entity.entitySql;

import android.util.Log;

import com.sendi.deliveredrobot.entity.BigScreenConfigDB;
import com.sendi.deliveredrobot.entity.ShoppingActionDB;
import com.sendi.deliveredrobot.entity.TouchScreenConfigDB;

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

        int rowsAffected = LitePal.deleteAll(ShoppingActionDB.class, "name = ? and rootMapName = ?", actionName, rootMapName);
        return rowsAffected > 0;
    }
    public static void deleteBigPic(String fileName) {
         LitePal.deleteAll(BigScreenConfigDB.class, "imagefile = ? ",fileName);
    }
    public static void deleteTouchPic(String fileName) {
        LitePal.deleteAll(TouchScreenConfigDB.class, "touch_imagefile = ? ",fileName);
    }

}
