package com.sendi.deliveredrobot.entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.sendi.deliveredrobot.MyApplication;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;
import org.litepal.LitePalDB;
import org.litepal.crud.LitePalSupport;
import org.litepal.exceptions.DataSupportException;
import org.litepal.tablemanager.Connector;

/**
 * @author swn
 * @describe 更新数据
 */
public class UpDataSQL {
    /**
     * 根据条件更新指定表中的数据
     *
     * @param tableName   数据库表名
     * @param values      更新的字段值
     * @param whereClause 更新的条件
     * @param whereArgs   更新条件的参数值
     * @return 更新的行数
     */
    public static int update(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = LitePal.getDatabase();
        int rowsAffected = db.update(tableName, values, whereClause, whereArgs);
        return rowsAffected;
    }
//  val values = ContentValues()
//        values.put("speechspeed", 500)
//        UpDataSQL.update("basicsetting", values, "id = ?", arrayOf(QuerySql.basicId().toString()))
//        LogUtil.d("ddddd+"+basicSetting[0].speechSpeed)
}
