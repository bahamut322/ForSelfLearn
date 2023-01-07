package com.sendi.deliveredrobot.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sendi.deliveredrobot.room.entity.MyRelationshipPoint;
import com.sendi.deliveredrobot.room.entity.Point;
import com.sendi.deliveredrobot.room.entity.PublicArea;
import com.sendi.deliveredrobot.room.entity.RelationshipLift;
import com.sendi.deliveredrobot.room.entity.RelationshipPoint;
import com.sendi.deliveredrobot.room.entity.RootMap;
import com.sendi.deliveredrobot.room.entity.RouteMap;
import com.sendi.deliveredrobot.room.entity.SubMap;
import com.sendi.deliveredrobot.room.entity.SubMapName;

import java.util.List;

@Dao
public interface DebugDao {

    @Query("select name from map_root")
    List<String> queryMapRootList();

    @Query("delete from map_root where id = :id")
    void delteMapRootByMapId(int id);

    @Insert(entity = RootMap.class)
    long insertMapRoot(RootMap rootMap);

    @Query("update map_root set name = :name where id = :id")
    void updateMapRoot(int id, String name);

    @Query("select * from map_root where name = :name")
    List<RootMap> searchMapRootName(String name);

    @Query("select * from map_sub where id = :id")
    SubMap querySubMap(int id);

    @Query("select name from map_sub")
    List<String> queryMapLaserList();

    /**
     * @describe 查询所有激光地图
     */
    @Query("select * from map_sub order by map_sub.id DESC")
    List<SubMap> queryMapSubList();

    @Query("select name from map_route order by map_route.id DESC")
    List<String> queryMapRouteList();

    @Query("select map_sub.name from map_point inner join map_sub on map_point.sub_map_id = map_sub.id group by sub_map_id")
    List<String> queryMapPointList();

    @Query("select * from map_route order by map_route.id DESC")
    List<RouteMap> queryMapRoute();

    @Query("select * from map_route where name = :name")
    List<RouteMap> searchRouteName(String name);

    @Query("select * from map_route where id = :id")
    RouteMap queryRouteId(int id);

    @Insert
    void insertMapRoute(RouteMap routeMap);

    @Query("select id from relationship_point where route_id = :route_id")
    List<Integer> cantDeleteRoute(int route_id);

    @Query("delete from map_route where id = :id")
    void deleteRoute(int id);

    // ===================== TargetPoint API ===================== //
    @Insert
    void insertTargetPoint(Point point);

    @Query("SELECT id FROM map_point ORDER BY id DESC LIMIT 1")
    int queryMaxId();

    @Query("SELECT * FROM map_point WHERE sub_map_id = :subMapId ORDER BY CAST(map_point.name as INTEGER)")
    List<Point> queryPointsBySubMapId(int subMapId);

    @Query("DELETE FROM map_point WHERE sub_map_id = :subMapId")
    void deletePointsBySubMapId(int subMapId);

    @Delete
    void deletePoint(Point point);

    @Delete
    void deleteType(PublicArea area);

    @Update
    void updatePoint(Point point);

    @Query("SELECT * FROM map_point WHERE id = :pointId")
    Point queryPointById(int pointId);

    @Insert
    void insertType(PublicArea area);

    @Query("SELECT * FROM public_area")
    List<PublicArea> queryTypeList();

    @Query("SElECT id FROM relationship_point WHERE point_id = :pointId")
    List<Integer> judgeDeletePoint(int pointId);

    @Query("SELECT DISTINCT " +
            "ms.*" +
            " FROM map_sub AS ms LEFT JOIN map_point as mp ON mp.sub_map_id = ms.id order by ms.id DESC")
    List<SubMap> queryTargetPointMap();

    @Query("SELECT DISTINCT " +
            "ms.*" +
            " FROM map_sub AS ms")
    List<SubMapName> queryTargetPointMapName();
    // =========================================================== //

    @Insert(entity = SubMap.class)
    long insertLaserMap(SubMap subMap);

    @Query("select map_sub.path from map_sub where name = :name")
    String selectLaserOriginalNameByName(String name);

    @Query("select distinct mr.name from relationship_point rp " +
            "left join map_sub ms on rp.sub_map_id = ms.id " +
            "left join map_root mr on rp.root_map_id = mr.id " +
            " where ms.id = :id")
    List<String> selectLaserMapCountInRelationShipTable(int id);

    @Query("delete from map_sub where id = :id")
    void deleteLaserMapById(int id);


    @Insert(entity = RelationshipPoint.class)
    long insertRelationshipPoint(RelationshipPoint relationshipPoint);

    @Query("select " +
            "relationship_point.id," +
            "relationship_point.root_map_id," +
            "relationship_point.sub_map_id ," +
            "relationship_point.route_id," +
            "relationship_point.point_id," +
            "map_root.name AS root_map_name," +
            "map_route.name AS route_name" +
            " from relationship_point " +
            "LEFT JOIN map_root ON relationship_point.root_map_id = map_root.id " +
            "LEFT JOIN map_route ON relationship_point.route_id = map_route.id " +
            "where root_map_id = :id")
    List<MyRelationshipPoint> selectRelationshipPointByMapId(int id);

    @Query("delete from relationship_point where root_map_id = :id")
    void delteRelationshipPointByMapId(int id);

    @Insert(entity = RelationshipLift.class)
    long insertRelationshipLift(RelationshipLift relationshipLift);

}
