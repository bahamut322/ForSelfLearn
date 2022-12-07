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
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.entity.*
import java.io.File

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
//        RelationshipBindingPoints::class
    ],
    version = 6,
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
                    .setJournalMode(JournalMode.TRUNCATE)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}