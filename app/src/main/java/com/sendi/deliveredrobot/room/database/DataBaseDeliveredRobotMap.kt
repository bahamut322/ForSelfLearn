package com.sendi.deliveredrobot.room.database

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.entity.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.DecimalFormat

/**
 * @describe 送物机器人数据库
 */
@Database(
    entities = [
        Point::class,
        PublicArea::class,
        RelationshipArea::class,
//        RelationshipLiftPoint::class,
        RelationshipPoint::class,
        RootMap::class,
        RouteMap::class,
        SubMap::class,
        MapConfig::class,
        BasicConfig::class,
//        RelationshipChargePoint::class,
        RelationshipLift::class,
//        RelationshipBindingPoints::class,
        FileInfo::class
    ],
    version = 9,
    exportSchema = false
)
abstract class DataBaseDeliveredRobotMap : RoomDatabase() {
    abstract fun getDao(): DeliveredRobotDao
    abstract fun getDebug(): DebugDao

    companion object {
        val path = "${Environment.getExternalStorageDirectory()}/database/delivered_robot_map.db"
        val file = File(path)
        private val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE basic_config ADD COLUMN wifi_open INTEGER")
            }
        }
        private val migration_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE map_sub ADD COLUMN limit_speed INTEGER")
                database.execSQL("ALTER TABLE map_sub ADD COLUMN virtual_wall INTEGER")
                database.execSQL("ALTER TABLE map_sub ADD COLUMN one_way INTEGER")
            }
        }
        private val migration_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE basic_config ADD COLUMN send_volume_2 INTEGER NOT NULL DEFAULT 60")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN send_volume_3 INTEGER NOT NULL DEFAULT 60")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN guide_volume_2 INTEGER NOT NULL DEFAULT 60")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN guide_volume_3 INTEGER NOT NULL DEFAULT 60")
            }
        }
        private val migration_4_5 = object : Migration(4, 5){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE basic_config ADD COLUMN send_mode_open INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN send_mode_verify_password INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN guide_mode_open INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN guide_mode_verify_password INTEGER NOT NULL DEFAULT 1")
            }
        }
        private val migration_5_6 = object : Migration(5, 6){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE relationship_binding_points")
                database.execSQL("ALTER TABLE map_point ADD COLUMN elevator TEXT DEFAULT NULL")
                database.execSQL("CREATE TABLE relationship_lift_backup (id INTEGER PRIMARY KEY NOT NULL, sub_map_id INTEGER, floor_name TEXT)")
                database.execSQL("INSERT INTO relationship_lift_backup SELECT id, sub_map_id, floor_name FROM relationship_lift")
                database.execSQL("DROP TABLE relationship_lift")
                database.execSQL("ALTER TABLE relationship_lift_backup RENAME to relationship_lift")
            }
        }
        private val migration_6_7 = object : Migration(6, 7){
            override fun migrate(database: SupportSQLiteDatabase) {
//                writeDefaultUsherFile()
                database.execSQL("ALTER TABLE map_config ADD COLUMN ready_point_id INTEGER DEFAULT NULL")
                database.execSQL("UPDATE map_config SET ready_point_id = charge_point_id WHERE map_config.id = 1")
                database.execSQL("INSERT INTO public_area (id,name,type) VALUES (4,'待命点',0)")
                database.execSQL("INSERT INTO public_area (id,name,type) VALUES (5,'迎宾点',0)")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN usher_volume INTEGER NOT NULL DEFAULT 60")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN usher_mode_open INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN usher_mode_verify_password INTEGER NOT NULL DEFAULT 1")
                database.execSQL("CREATE TABLE file_info (id INTEGER PRIMARY KEY NOT NULL, file_id INTEGER, file_name TEXT, file_path TEXT, file_type INTEGER, file_suffix TEXT )")
                database.execSQL("INSERT INTO file_info (id, file_id, file_name, file_path, file_type, file_suffix) VALUES (1, -1, '欢迎光临', '/-1/欢迎光临.png', 2, '.png')")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN usher_id INTEGER DEFAULT -1")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN usher_file_info_id INTEGER DEFAULT -1")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN usher_content TEXT DEFAULT '欢迎光临'")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN usher_timing INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN usher_duration REAL NOT NULL DEFAULT 1.0")
                database.execSQL("ALTER TABLE basic_config ADD COLUMN usher_speed REAL NOT NULL DEFAULT 0.6")
            }
        }
        private val migration_7_8 = object : Migration(7, 8){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("UPDATE public_area SET id = id + 2000")
                database.execSQL("UPDATE map_point SET type = type + 2000")
                database.execSQL("""UPDATE map_point SET x = round(x,2)""")
                database.execSQL("""UPDATE map_point SET y = round(y,2)""")
                database.execSQL("""UPDATE map_point SET w = round(w,2)""")
            }
        }

        private val migration_8_9 = object : Migration(8, 9){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE basic_config ADD COLUMN room_number_length INTEGER NOT NULL DEFAULT 4")
                database.execSQL("INSERT INTO public_area (id,name,type) VALUES (${PointType.LIFT_AXIS},'电梯锚点',0)")
                database.execSQL("""
                    INSERT INTO map_point (name,direction,x,y,w,sub_map_id,type,elevator)
                    SELECT '锚点'||name,direction,x,y,w,sub_map_id,${PointType.LIFT_AXIS},elevator FROM map_point WHERE map_point.type = ${PointType.LIFT_INSIDE}
                """.trimIndent())
            }
        }

        init {
            MediaScannerConnection.scanFile(MyApplication.instance!!, arrayOf(file.absolutePath), arrayOf("text/plain")
            ) { _, _ -> }
        }

        @Volatile
        private var INSTANCE: DataBaseDeliveredRobotMap? = null

        fun getDatabase(context: Context): DataBaseDeliveredRobotMap {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
//                val path = "${Environment.getExternalStorageDirectory()}/database/delivered_robot_map.db"
//                val file = File(path)
                if (!file.exists()) {
                    file.parentFile?.mkdirs()
                    file.createNewFile()
                    val inputStream = context.resources.assets.open("database/delivered_robot_map.db")
                    val outputStream = file.outputStream()
                    val byteArray = ByteArray(1024)
                    var result: Int
                    do{
                        result = inputStream.read(byteArray,0,byteArray.size)
                        if(result > 0){
                            outputStream.write(byteArray,0,result)
                        }
                    }while(result != -1)
                    inputStream.close()
                    outputStream.close()
//                    MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("text/plain")
//                    ) { _, _ -> }
                }
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataBaseDeliveredRobotMap::class.java,
//                    "delivered_robot_map.db"
                    path
                )
//                    .createFromAsset("database/delivered_robot_map.db")
                    .addMigrations(migration_1_2)
                    .addMigrations(migration_2_3)
                    .addMigrations(migration_3_4)
                    .addMigrations(migration_4_5)
                    .addMigrations(migration_5_6)
                    .addMigrations(migration_6_7)
                    .addMigrations(migration_7_8)
                    .addMigrations(migration_8_9)
                    .setJournalMode(JournalMode.TRUNCATE)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        private fun writeDefaultUsherFile(){
            val defaultFilePath = "${Environment.getExternalStorageDirectory()}/-1/欢迎光临.png"
            val defaultFile = File(defaultFilePath)
            if (!defaultFile.exists()) {
                defaultFile.parentFile?.mkdirs()
                defaultFile.createNewFile()
            }
            val inputStream: InputStream = MyApplication.instance!!.assets.open("欢迎光临.png")
            val outputStream: OutputStream = FileOutputStream(defaultFile)
            val byteArray = ByteArray(1024)
            var result: Int
            do {
                result = inputStream.read(byteArray, 0, byteArray.size)
                if (result > 0) {
                    outputStream.write(byteArray, 0, result)
                }
            } while (result != -1)
            inputStream.close()
            outputStream.close()
        }
    }
}