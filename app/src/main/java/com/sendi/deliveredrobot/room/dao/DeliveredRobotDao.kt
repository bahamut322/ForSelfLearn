package com.sendi.deliveredrobot.room.dao

import androidx.room.*
import com.sendi.deliveredrobot.LIMIT_SPEED_AREA_TRUE
import com.sendi.deliveredrobot.VIRTUAL_WALL_TRUE
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.room.entity.*

@Dao
interface DeliveredRobotDao {
    /**
     * @describe 通过name查询目标点
     */
    @Query(
        """
SELECT
	relationship_point.root_map_id,
	map_point.id AS point_id,
	map_sub.id AS sub_map_id,
	map_route.id AS route_id,
	map_route.path AS route_path,
	map_sub.path AS sub_path,
	map_point.x AS x,
	map_point.y AS y,
	map_point.w AS w,
	map_point.name AS point_name,
	map_point.direction AS point_direction,
    relationship_lift.floor_name,
    map_point.elevator AS elevator
FROM
	relationship_point
	INNER JOIN map_sub ON relationship_point.sub_map_id = map_sub.id
	INNER JOIN map_point ON relationship_point.point_id = map_point.id
	INNER JOIN map_route ON relationship_point.route_id = map_route.id
    INNER JOIN relationship_lift ON relationship_lift.sub_map_id = relationship_point.sub_map_id
WHERE
	relationship_point.root_map_id = (SELECT root_map_id FROM map_config)   
	AND map_point.NAME = :name
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryPoint(name: String): QueryPointEntity?

    /**
     * @describe insert map_point
     */
    @Insert
    fun insertPoint(point: Point)

    /**
     * @describe insert relationship_point
     */
    @Insert
    fun insertRelationshipPoint(relationshipPoint: RelationshipPoint)

    /**
     * @describe 查询root_map下的所有公共区域的所有点的集合
     */
    @Query(
        """
SELECT
	public_area.id AS public_area_id,
	relationship_point.root_map_id AS root_map_id,
	public_area.name AS public_area_name,
	map_point.id AS point_id,
	map_sub.id AS sub_map_id,
	map_route.id AS route_id,
	map_route.path AS route_path,
	map_sub.path AS sub_path,
	map_point.x AS x,
	map_point.y AS y,
	map_point.w AS w,
	map_point.name AS point_name,
	map_point.direction AS point_direction,
    relationship_lift.floor_name,
    map_point.elevator AS elevator
FROM
	relationship_point
	INNER JOIN public_area ON public_area.id = map_point.type
	INNER JOIN map_sub ON relationship_point.sub_map_id = map_sub.id
	INNER JOIN map_point ON relationship_point.point_id = map_point.id
	INNER JOIN map_route ON relationship_point.route_id = map_route.id
    INNER JOIN relationship_lift ON relationship_lift.sub_map_id = relationship_point.sub_map_id
WHERE
	public_area.id > 100 AND relationship_point.root_map_id = (SELECT root_map_id FROM map_config)
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryPublicAreaPoints(): List<QueryPointEntity>

    /**
     * @describe 查询基本配置
     */
    @Query("SELECT * FROM basic_config")
    fun queryBasicConfig(): BasicConfig

    /**
     * @describe 更新基础设置
     */
    @Update
    fun updateBasicConfig(basicConfig: BasicConfig)

    /**
     * @describe 以subMapId查询电梯点
     */
    @Query(
        """
SELECT 
	relationship_point.root_map_id,
	map_point.id AS point_id,
	map_sub.id AS sub_map_id,
	map_route.id AS route_id,
	map_route.path AS route_path,
	map_sub.path AS sub_path,
	map_point.x AS x,
	map_point.y AS y,
	map_point.w AS w,
	map_point.name AS point_name,
	map_point.direction AS point_direction,
    relationship_lift.floor_name,
    map_point.elevator AS elevator
FROM 
	relationship_point
INNER JOIN map_point ON relationship_point.point_id = map_point.id
INNER JOIN map_sub ON relationship_point.sub_map_id = map_sub.id
INNER JOIN map_route ON relationship_point.route_id = map_route.id
INNER JOIN relationship_lift ON relationship_lift.sub_map_id = relationship_point.sub_map_id
WHERE
	relationship_point.sub_map_id = :subMapId 
    AND map_point.type = :type
    AND map_point.elevator = :elevator
    AND relationship_point.root_map_id = (SELECT root_map_id FROM map_config)
        """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryLiftPoint(subMapId: Int, type: Int, elevator: String): QueryPointEntity?


    /**
     * @describe 查询主地图列表
     */
    @Query("SELECT * FROM map_root ORDER BY map_root.id DESC")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryRootMap(): List<MyRootMap>?

    /**
     * @describe 查询当前主地图
     */
    @Query("SELECT root_map_id FROM map_config")
    fun queryMapConfig(): Int?

    /**
     * @describe 设置当前主地图
     */
    @Update
    fun updateMapConfig(mapConfig: MapConfig)

    /**
     * @describe 查询当前地图名字
     */
    @Query(
        """
SELECT 
    name 
FROM 
    map_root
INNER JOIN map_config ON map_config.root_map_id = map_root.id
"""
    )
    fun queryCurrentMapName(): String?

    /**
     * @describe 查询当前充电点
     */
    @Query(
        """
SELECT
	map_point.id AS point_id,
	map_sub.id AS sub_map_id,
	map_route.id AS route_id,
	map_route.path AS route_path,
	map_sub.path AS sub_path,
	map_point.x AS x,
	map_point.y AS y,
	map_point.w AS w,
	map_point.name AS point_name,
	map_point.direction AS point_direction,
	floor_name,
    map_point.elevator AS elevator
FROM
	relationship_point
	INNER JOIN map_sub ON relationship_point.sub_map_id = map_sub.id
	INNER JOIN map_point ON relationship_point.point_id = map_point.id
	INNER JOIN map_route ON relationship_point.route_id = map_route.id
	INNER JOIN relationship_lift ON relationship_point.sub_map_id = relationship_lift.sub_map_id
WHERE
	relationship_point.point_id = (SELECT charge_point_id FROM map_config)
AND
	relationship_point.root_map_id = (SELECT root_map_id FROM map_config)
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryChargePoint(): QueryPointEntity?


    /**
     * @describe 查询充电点列表
     */
    @Query(
        """
SELECT
    relationship_point.root_map_id as root_map_id,
	map_point.id AS point_id,
	map_sub.id AS sub_map_id,
	map_route.id AS route_id,
	map_route.path AS route_path,
	map_sub.path AS sub_path,
	map_point.x AS x,
	map_point.y AS y,
	map_point.w AS w,
	map_point.name AS point_name,
	map_point.direction AS point_direction
FROM
	relationship_point
	INNER JOIN map_sub ON relationship_point.sub_map_id = map_sub.id
	INNER JOIN map_point ON relationship_point.point_id = map_point.id
	INNER JOIN map_route ON relationship_point.route_id = map_route.id
WHERE
	relationship_point.root_map_id = (SELECT root_map_id FROM map_config) AND map_point.type = ${PointType.CHARGE_POINT}
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryChargePointList(): List<QueryPointEntity>?

    /**
     * @describe 根据楼层编码查询电梯点
     */
    @Query(
        """
SELECT 
	map_route.path AS route_path,
	map_sub.path AS sub_path,
    map_sub.id AS sub_map_id,
	map_point.x AS x,
	map_point.y AS y,
	map_point.w AS w,
	floor_name,
    map_point.elevator AS elevator
FROM 
	relationship_point
INNER JOIN relationship_lift ON relationship_lift.sub_map_id = relationship_point.sub_map_id
INNER JOIN map_sub ON relationship_lift.sub_map_id = map_sub.id
INNER JOIN map_point ON relationship_point.point_id = map_point.id
INNER JOIN map_route ON relationship_point.route_id = map_route.id
WHERE
	relationship_lift.floor_name = :floorName 
    AND map_point.type = ${PointType.LIFT_OUTSIDE}
    AND map_point.elevator = :elevator
    AND relationship_point.root_map_id = (SELECT root_map_id FROM map_config)
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryLiftPointByFloorName(floorName: String, elevator: String): QueryPointEntity

    /**
     * @describe 根据subMapId查询楼层编码
     */
    @Query(
        """
SELECT 
	*
FROM 
	relationship_lift
WHERE
	relationship_lift.sub_map_id = :subMapId
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryFloorBySubMapId(subMapId: Int): RelationshipLift?

    /**
     * @describe 查询root_map下的所有楼层的所有普通点的集合
     */
    @Query(
        """
SELECT
	point_name,
	floor_name,
	point_id,
	root_map_id,
	sub_map_id,
	route_id,
	route_path,
	sub_path,
	x,
	y,
	w,
	point_direction
FROM
(
SELECT
	map_point.name AS point_name,
	relationship_lift.floor_name AS floor_name,
	map_point.id AS point_id,
	relationship_point.root_map_id AS root_map_id,
	map_sub.id AS sub_map_id,
	map_route.id AS route_id,
	map_route.path AS route_path,
	map_sub.path AS sub_path,
	map_point.x AS x,
	map_point.y AS y,
	map_point.w AS w,
	map_point.direction AS point_direction
FROM
	relationship_point
  INNER JOIN relationship_lift ON relationship_point.sub_map_id = relationship_lift.sub_map_id
	INNER JOIN map_sub ON relationship_point.sub_map_id = map_sub.id
	INNER JOIN map_point ON relationship_point.point_id = map_point.id
	INNER JOIN map_route ON relationship_point.route_id = map_route.id
WHERE
map_point.type = 100 AND relationship_point.root_map_id = (SELECT root_map_id FROM map_config)
ORDER BY CAST(map_point.name as INTEGER)
)
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryFloorPoints(): List<QueryPointEntity>

    /**
     * @describe 查询所有激光地图
     */
    @Query(
        """
SELECT 
    map_sub.id as id,
    map_sub.name as name,
    map_sub.path as path,
    relationship_lift.id as relationship_lift_id,
    relationship_lift.floor_name as floor_name
FROM map_sub 
INNER JOIN relationship_lift ON map_sub.id = relationship_lift.sub_map_id
ORDER BY map_sub.id DESC
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun querySubMaps(): List<QuerySubMapEntity>

    /**
     * @describe 查询限速区列表
     */
    @Query(
        """
        SELECT 
    map_sub.id as id,
    map_sub.name as name,
    map_sub.path as path,
    map_sub.limit_speed as limit_speed,
    map_sub.virtual_wall as virtual_wall,
    map_sub.one_way as one_way
FROM map_sub 
WHERE map_sub.limit_speed = $LIMIT_SPEED_AREA_TRUE
ORDER BY map_sub.id DESC
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryLimitSpeedList(): List<QuerySubMapEntity>

    /**
     * @describe 更新激光地图状态
     */
    @Update
    fun updateSubMap(subMap: SubMap)

    /**
     * @describe 查询虚拟墙列表
     */
    @Query(
        """
        SELECT 
    map_sub.id as id,
    map_sub.name as name,
    map_sub.path as path,
    map_sub.limit_speed as limit_speed,
    map_sub.virtual_wall as virtual_wall,
    map_sub.one_way as one_way
FROM map_sub 
WHERE map_sub.virtual_wall = $VIRTUAL_WALL_TRUE
ORDER BY map_sub.id DESC
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryVirtualWallList(): List<QuerySubMapEntity>

    /**
     * @describe 查询激光图名是否存在
     */
    @Query(
        """
        SELECT
        map_sub.id as id
        FROM map_sub
        WHERE map_sub.name = :name
    """
    )
    fun queryLaserNameId(name: String): Long

    /**
     * @describe 查询目标点名字是否存在
     */
    @Query(
        """
        SELECT map_point.id 
        FROM map_point
        WHERE map_point.sub_map_id = :subMapId
        AND map_point.name = :pointName
    """
    )
    fun queryTargetNameExist(subMapId: Int, pointName: String): Long

    /**
     * @describe 查询路径图名是否存在
     */
    @Query(
        """
        SELECT
        map_route.id as id
        FROM map_route
        WHERE map_route.name = :routeMapName
    """
    )
    fun queryRouteMapExist(routeMapName: String): Long

    /**
     * @describe 删除子图
     */
    @Query(
        """
        DELETE FROM map_sub;
        """
    )
    fun deleteAllSubMap()

    /**
     * @describe 删除路径图
     */
    @Query(
        """
        DELETE FROM map_route;
        """
    )
    fun deleteAllRouteMap()

    /**
     * @describe 删除目标点
     */
    @Query(
        """
        DELETE FROM map_point;
        """
    )
    fun deleteAllPointMap()

    /**
     * @describe 删除总图
     */
    @Query(
        """
        DELETE FROM map_root;
        """
    )
    fun deleteAllRootMap()

    /**
     * @describe 删除点关系
     */
    @Query(
        """
        DELETE FROM relationship_point;
        """
    )
    fun deleteAllRelationshipPoint()

    /**
     * @describe 删除楼层关系
     */
    @Query(
        """
        DELETE FROM relationship_lift;
        """
    )
    fun deleteAllRelationshipLift()

    /**
     * @describe 删除区域关系
     */
    @Query(
        """
        DELETE FROM relationship_area;
        """
    )
    fun deleteAllRelationshipArea()

    /**
     * @describe 删除区域
     */
    @Query(
        """
        DELETE FROM public_area WHERE public_area.type = 1
        """
    )
    fun deleteAllPublicArea()

    /**
     * @describe 重置所有数据库数据
     */
    fun deleteAllData(): BasicConfig {
        deleteAllRootMap()
        deleteAllSubMap()
        deleteAllRouteMap()
        deleteAllPointMap()
        deleteAllRelationshipPoint()
        deleteAllRelationshipLift()
        deleteAllRelationshipArea()
        deleteAllPublicArea()
        updateMapConfig(MapConfig(1, 0, 0))
        val basicConfig = BasicConfig(
            appVersion = "",
            brightness = 40,
            sendSpeed = 0.7f,
            sendVolume = 50,
            sendPutObjectTime = 90,
            sendWaitTakeObjectTime = 60,
            sendTakeObjectTime = 180,
            needTakeObjectPassword = 0,
            guideSpeed = 0.7f,
            guideVolume = 50,
            guideWalkPauseTime = 30,
            robotUseDeadLine = "",
            verifyPassword = "00000",
            wifiOpen = 0,
            guideVolumeLobby = 60,
            guideVolumeLift = 60,
            sendVolumeLobby = 60,
            sendVolumeLift = 60,
            sendModeOpen = 1,
            sendModeVerifyPassword = 1,
            guideModeOpen = 1,
            guideModeVerifyPassword = 1
        )
        updateBasicConfig(basicConfig)
        return basicConfig
    }

    /**
     * @describe 查询当前总图下所有子图绝对路径
     */

    @Query(
        """
SELECT
distinct map_sub.path as sub_path,
map_route.path as route_path
FROM
map_sub
INNER JOIN map_route on map_sub.id = map_route.sub_map_id
WHERE 
map_sub.id in (
SELECT
distinct relationship_point.sub_map_id as id
FROM
relationship_point
WHERE
relationship_point.root_map_id = (SELECT map_config.root_map_id FROM map_config)
)
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryCurrentSubMapPaths(): List<QueryPointEntity>

    /**
     * @describe 查询root_map下的所有楼层的所有普通点的集合
     */
    @Query(
        """
SELECT
    name,
	point_name,
	floor_name,
	point_id,
	root_map_id,
	sub_map_id,
	route_id,
	route_path,
	sub_path,
	x,
	y,
	w,
	point_direction
FROM
(
SELECT
    map_root.name AS name ,
	map_point.name AS point_name,
	relationship_lift.floor_name AS floor_name,
	map_point.id AS point_id,
	relationship_point.root_map_id AS root_map_id,
	map_sub.id AS sub_map_id,
	map_route.id AS route_id,
	map_route.path AS route_path,
	map_sub.path AS sub_path,
	map_point.x AS x,
	map_point.y AS y,
	map_point.w AS w,
	map_point.direction AS point_direction
FROM
	relationship_point
  INNER JOIN relationship_lift ON relationship_point.sub_map_id = relationship_lift.sub_map_id
	INNER JOIN map_sub ON relationship_point.sub_map_id = map_sub.id
	INNER JOIN map_point ON relationship_point.point_id = map_point.id
	INNER JOIN map_route ON relationship_point.route_id = map_route.id
	inner join map_root on map_root.id=  relationship_point.root_map_id

WHERE
map_point.type >= 100 AND relationship_point.root_map_id = (SELECT root_map_id FROM map_config)
ORDER BY CAST(map_point.name as INTEGER)
)
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryAllPoints(): List<QueryPointEntity>

    @Query(
        """
SELECT
	point_name,
	floor_name,
	point_id,
	root_map_id,
	sub_map_id,
	route_id,
	route_path,
	sub_path,
	x,
	y,
	w,
	point_direction,
    type
FROM
(
SELECT
	map_point.name AS point_name,
	relationship_lift.floor_name AS floor_name,
	map_point.id AS point_id,
	relationship_point.root_map_id AS root_map_id,
	map_sub.id AS sub_map_id,
	map_route.id AS route_id,
	map_route.path AS route_path,
	map_sub.path AS sub_path,
	map_point.x AS x,
	map_point.y AS y,
	map_point.w AS w,
	map_point.direction AS point_direction,
    map_point.type AS type
FROM
	relationship_point
  INNER JOIN relationship_lift ON relationship_point.sub_map_id = relationship_lift.sub_map_id
	INNER JOIN map_sub ON relationship_point.sub_map_id = map_sub.id
	INNER JOIN map_point ON relationship_point.point_id = map_point.id
	INNER JOIN map_route ON relationship_point.route_id = map_route.id
WHERE
relationship_point.root_map_id = :rootMapId
ORDER BY CAST(map_point.name as INTEGER)
)
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryAllPoint(rootMapId: Int): List<QueryPointEntity>

    /**
     * @describe 查询root_map下的所有楼层的所有普通点的集合
     */
    @Query(
        """
SELECT
    name,
	point_name,
	floor_name,
	point_id,
	root_map_id,
	sub_map_id,
	route_id,
	route_path,
	sub_path,
	x,
	y,
	w,
	point_direction
FROM
(
SELECT
   map_root.name AS name ,
	map_point.name AS point_name,
	relationship_lift.floor_name AS floor_name,
	map_point.id AS point_id,
	relationship_point.root_map_id AS root_map_id,
	map_sub.id AS sub_map_id,
	map_route.id AS route_id,
	map_route.path AS route_path,
	map_sub.path AS sub_path,
	map_point.x AS x,
	map_point.y AS y,
	map_point.w AS w,
	map_point.direction AS point_direction
FROM
	relationship_point
  INNER JOIN relationship_lift ON relationship_point.sub_map_id = relationship_lift.sub_map_id
	INNER JOIN map_sub ON relationship_point.sub_map_id = map_sub.id
	INNER JOIN map_point ON relationship_point.point_id = map_point.id
	INNER JOIN map_route ON relationship_point.route_id = map_route.id
	inner join map_root on map_root.id=  relationship_point.root_map_id
WHERE
map_point.type >= 100 AND relationship_point.root_map_id = :id
ORDER BY CAST(map_point.name as INTEGER)
)
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryAllMapPoints(id: Int): List<QueryPointEntity>


    /**
     * @describe 查询root_map下的所有楼层的所有普通点的集合
     */
    @Query(
        """
SELECT
    name,
	point_name,
	floor_name,
	x,
	y,
	w
FROM
(
SELECT
    map_root.name AS name ,
	relationship_lift.floor_name AS floor_name,
    map_point.name AS point_name,
	map_point.x AS x,
	map_point.y AS y,
	map_point.w AS w

FROM
	relationship_point
    INNER JOIN relationship_lift ON relationship_point.sub_map_id = relationship_lift.sub_map_id
	INNER JOIN map_sub ON relationship_point.sub_map_id = map_sub.id
	INNER JOIN map_point ON relationship_point.point_id = map_point.id
	inner join map_root on map_root.id=  relationship_point.root_map_id
WHERE
map_point.type >= 100 
ORDER BY relationship_point.root_map_id,relationship_lift.floor_name
)
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryAllMapsPoints(): List<QueryAllPointEntity>

    /**
     * @description 查询所有公共区域区域
     */
    @Query(
        """
SELECT
	public_area.id AS public_area_id,
-- 	public_area.name AS public_area_name,
 	map_point.name AS point_name
--	 map_root.name AS name
FROM
	relationship_point
	INNER JOIN public_area ON public_area.id = map_point.type
	INNER JOIN map_sub ON relationship_point.sub_map_id = map_sub.id
	INNER JOIN map_point ON relationship_point.point_id = map_point.id
	INNER JOIN map_route ON relationship_point.route_id = map_route.id
  INNER JOIN relationship_lift ON relationship_lift.sub_map_id = relationship_point.sub_map_id
--	inner join map_root on map_root.id=  relationship_point.root_map_id
WHERE
	public_area.id > 100
    """
    )
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun queryAreaMapPoint(): List<QueryAreaPointEntity>

    /**
     * @description 查询区域
     */
    @Query(
        """
SELECT id, name, type 
 from public_area
 ORDER BY
 id != 100,
 id ASC
    """
    )
    fun queryPublicArea(): List<PublicArea>
}