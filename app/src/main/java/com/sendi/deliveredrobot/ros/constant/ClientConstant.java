package com.sendi.deliveredrobot.ros.constant;

/**
 * ros 服务调用/消息订阅 url常量
 *
 * @author eden
 */
public class ClientConstant {
    /**
     * Client Heart beat
     * 用作心跳检测消息，回复不处理，保持ws连接
     */
    public static final String HEART_BEAT = "/rosapi/get_time";

    /**
     * self checking
     */
    // 红外摄像头自检查询
    public static final String INFRARED_MANAGE = "/infrared_manage";
    // 急停按钮自检查询
    public static final String CHASSIC_STATE = "/chassis_state";
    /**
     * robot
     */
    // 低电量关机下发
    public static final String LOW_POWER_SHUTDOWN = "/stm32_command";
    // 时间同步
    public static final String TIME_UPDATE = "/chassis/time_update";
    // 恢复出厂 cmd 1：重置所有/2：重置地图文件/3：重置日志文件/4：重置BAG文件
    public static final String CHASSIS_RESET = "/chassis/chassis_reset";
    // 获取版本 type 1：底盘版本/2：stm32版本
    public static final String VERSION_GET = "/chassis/version_get";
    /**
     * ros api param and field
     */
    // 设置参数
    public static final String SET_PARAM = "/rosapi/set_param";
    // 是否存在参数（是否未创建参数）
    public static final String HAS_PARAM = "/rosapi/has_param";
    // 获取参数
    public static final String GET_PARAM = "/rosapi/get_param";
    // 参数名-调度时使用
    public static final String RESUME_OLD_GOAL_FLAG = "/navigation_base/resume_old_goal_flag";
    // 参数名-巡航是轮询还是单次
    public static final String CRUISE_TYPE = "/intelligent_cruise/cruise_type";
    // 参数名-运动速度
    public static final String SET_SPEED = "/set-speed";
    // 参数名-调度任务是否处理完成
    public static final String CHANGE_COAL_RESPONSE = "/navigation_base/change_goal_respl";
    // 参数名-调度任务是否处理完成
    public static final String SERIAL_NUMBER = "/serial_number";
    // 参数名-底盘启动标识 1-success
    public static final String NAVIGATION_VALID = "/navigation_base/vaild";


    /**
     * 导航
     */
    // 移动到目标点
    public static final String MOVE_TO = "/navigation_base/move_to";
    // 设置导航状态
    public static final String MANAGE = "/navigation_base/manage";
    // 巡航移动
    public static final String CRUISE_MOVE_TO = "/navigation_base/cruise_move_to";
    // 获取路径目标点列表
    public static final String SHOW_PATH_MAP = "/map/show_path_map";
    // 最近点排序（规划路径）
    public static final String PATH_SORTING = "/navigation_base/path_sorting";
    // 下发调度点
    public static final String SCH_CHANGE_GOAL = "/navigation_base/sch_change_goal";
    // 到达调度点后发给导航 - 服务参数
    public static final String SCH_RESUME_OLD_GOAL = "/navigation_base/sch_resume_old_goal";
    // 设置导航地图
    public static final String SET_NAVIGATION_MAP = "/navigation_base/set_navigation_map";
    // 发送巡航命令
    public static final String CRUISE = "/navigation_base/cruise";
    // 发送巡航命令
    public static final String RECV_LIFT_INDOOR = "/navigation_base/recv_lift_indoor";


    /**
     * 地图
     */
    // 删除地图
    public static final String MAP_DELETE = "/map/delete";
    // 清除地图信息--退出调试
    public static final String CANCEL_RAW_MAP = "/laser_map/cancle_raw_map";
    // 保存地图
    public static final String CRUSE_CONTROL_POINTS = "/navigation_base/cruse_control_points";
    // 路径绘制初始化
    public static final String ROUTE_MAP_INIT = "/route_map/init";
    // 关闭路径绘制
    public static final String ROUTE_MAP_END = "/route_map/end";
    // 获取实景图
    public static final String INFRARED_IMAGE_RAW = "/infrared_image_raw";
    // 开启调试日志
    public static final String ROSBAG_RECORD = "/rosbag_record";
    // 设置路径文件地图
    public static final String SET_LOCATION_MAP = "/map/set_location_map";
    // 显示标签点地图
    public static final String SHOW_LOCATION_MAP = "/map/show_location_map";
    // 标签地图定位状态
    public static final String LOCATION_STATE = "/get_location_state";
    // 移动到修复点
    public static final String ADVAN_MOVE_TO = "/navigation_base/advance_move_to";
    /**
     * label - 纯标签建图
     */
    // 创建标签点图
    public static final String LABEL_INIT = "/label/init";
    // 创建结束
    public static final String LABEL_END = "/label/end";
    // 补点
    public static final String LABEL_REPAIR = "/label/repair";
    // 补点结束
    public static final String LABEL_REPAIR_END = "/label/repair_end";
    // 获取标签总图
    public static final String GET_RUNNING_LOCATION_MAP = "/label/get_running_location_map";
    // 清除标签点
    public static final String CLEAR_RUNTIME_MAP = "/label/clear_runtime_map";
    // 获取机器人当前位置 - 用作目标点打点
    public static final String TARGET_POSE = "/label/target_pose";
    // 打电梯内点服务
    public static final String GET_LIFT_POINT = "/label/get_lift_point";
    // 提前家在激光地图 参数：source_name
    public static final String PRE_LOAD_MAP = "/laser_location/pre_load_map";
    /**
     * label - 混合导航标签建图
     */
    // 结束扫描 0-退出不保存 1-退出保存
    public static final String LABEL_BASE_LASER_END = "/laser_based_label/end";
    // 开始扫描
    public static final String LABEL_BASE_LASER_INIT = "/laser_based_label/init";
    // 捕获标签坐标
    public static final String CHECK_RECORD = "/laser_based_label/check_record";
    // 添加标签图
    public static final String LABEL_BASE_LASER_REPAIR = "/laser_based_label/repair";
    // 总览图
    public static final String LABEL_BASE_LASER_GET_RUNNING_LOCATION_MAP = "/laser_based_label/get_running_location_map";
    // 清空标签图
    public static final String LABEL_BASE_LASER_CLEAR_RUNTIME_MAP = "/laser_based_label/clear_runtime_map";
    /**
     * 虚拟墙
     */
    public static final String CREATE_VIRTUAL_WALL = "/create_virtual_wall";
    /**
     * 单行道
     */
    public static final String CREATE_SINGLE_LANE = "/create_one_way";
    /**
     * 限速区域
     */
    public static final String CREATE_SPEED_LIMIT_AREA = "/create_slow_area";
    public static final String GET_DISTANCE = "/move_control/obstacle_message";
    /**
     * auto door
     */
    public static final String DOOR_CONTROL_SERVICE = "/commuication/peripherals_ctrl";
    /**
     * subscribe topics
     */
    // 导航状态 1,终止（到达）  2,暂停  3,继续
    public static final String NAVIGATION_STATE_TOPIC = "/navigation_base/state";
    // 巡航圈数上报
    public static final String FINISH_ONCE_CRUISE = "/navigation_base/finish_once_cruse";
    // 调度-调度页面
    public static final String SCHEDULING_PAGE = "/navigation_base/sch_change_page";
    // 调度-调度点接收处理下发
    public static final String SCHEDULING_CHANGE_GOAL = "/navigation_base/change_goal";
    // 自动感应门
    public static final String DOOR_CONTROL = "/navigation_base/auto_door";
    // 机器人位置
    public static final String ROBOT_POSE = "/route_map/robot_pose";
    // 标签点位置
    public static final String LABEL_LIST = "/label/label_list";
    // 机器人阻塞状态
    public static final String VOICE_PROMPT_TOPIC = "/commands/voice_prompt";
    // 虚拟墙/单行道/限速区 路径点显示
    public static final String TEMP_OBSTACLE = "/temp_obstacle";
    // 急停按钮状态
    public static final String SAFE_STATE_TOPIC = "/chassis/safe_state";
    // 电池信息上报
    public static final String BATTERY_STATE = "/chassis/battery_state";
    // 激光雷达数据
    public static final String LASER_SCAN = "/scan";
    // 机器人靠近电梯内外点
    public static final String NEAR_INDOOR_LIFT = "/navigation_base/near_lift_goal";
    // 底盘错误日志
    public static final String CHASSIS_MSGS_TOPIC = "/chassis_msgs";
    // 建图实时子图数据 - 激光建图
    public static final String SUB_MAP_INFO = "/map/sub_map_info";
    // 当前待确认子图信息 - 激光建图
    public static final String PAUSE_CHECK = "/map/pause_check";
    // 重定位子图信息 - 激光建图
    public static final String GLOBAL_LASER = "/map/current_global_laser";
    // 获取lora接收到的透传信息
    public static final String LORA_RECEIVE = "/commuication/lora_recv";
    // 机器人里程数
    public static final String ROBOT_MILEAGE = "/robot_mileage";
    // 重定位时的位置以及朝向
    public static final String MAPPING_POSE = "/map/mapping_pose";

    /**
     * LASER 激光建图
     */
    // 雷达图初始化
    public static final String NEW_EMPTY_MAP = "/map/new_empty_map";
    // 雷达-开始建图
    public static final String START_BUILD_MAP = "/map/start_build_map";
    // 确认子图
    public static final String MAP_CHECK = "/map/check";
    // 设置回环子图对
    public static final String LOOP_PAIR = "/map/loop_pair";
    // 手动匹配子图
    public static final String MOVE_SUB_MAP = "/map/move_sub_map";
    // 确认当前回环
    public static final String CHECK_LOOP = "/map/check_loop";
    // 更新地图
    public static final String MAP_UPDATE = "/map/update";
    // 修改地图
    public static final String MAP_REBASE = "/map/rebase";
    // 初始化起始位置
    public static final String INIT_LOCATION = "/map/init_location";
    // 获取全局图
    public static final String GET_GLOBAL_MAP = "/map/get_global_map";
    // 导出导入地图
    public static final String OUT_IN_MAP = "/map/out_in";
    // 锚点切换状态
    public static final String GET_POSE = "/laser_location/get_pose";
    // 实时激光点图
    public static final String GET_NOW_LASER = "/navigation_base/get_now_laser";
    // 高亮部分子图
    public static final String HIGH_LIGHT_SUB_MAP = "/map/high_ligh_sub_map";


    // 自动充电
    public static final String DOCK_COMMAND = "/dock_command";
    public static final String DOCK_STATE = "/dock_state";

    // 开关仓门
    public static final String DOOR_COMMAND = "/door_command";
    public static final String DOOR_STATE = "/chassis/door_state";

    // 导航的时候切换楼层
    public static final String SET_MULTI_AXIS = "/laser_location/set_multi_axis";

    // 开机的时候评分
    public static final String CHECK_POSE = "/laser_location/check_pose";

    // 开机的时候设置位置
    public static final String SET_POSE = "/laser_location/set_pose";

    // 进电梯
    public static final String GO_TO_LIFT = "/navigation_base/go_to_lift";

    // 出电梯
    public static final String OUT_OF_LIFT = "/navigation_base/out_of_lift";

    // 判断是否能进出电梯
    public static final String CHECK_LIFT_VAILD = "/laser_location/check_lift_vaild";

    // 通过lora往外发送透传信息
    public static final String LORA_SEND = "/commuication/lora_send";
    // 控制多机调度信息发送开关
    public static final String LORA_SWITCH_CONTROL = "/commuication/lora_schemsg_switch_w";
    // 读取多机调度信息开关状态
    public static final String LORA_SWITCH_STATUS = "/commuication/lora_schemsg_switch_r";

    // 判断是否在充点电附近
    public static final String CHECK_POINT = "/laser_location/check_near_charge";
    // 判断是否出梯
    public static final String CHECK_OUT_OF_LIFT = "/navigation_base/check_out_of_lift";

    // 判断导航前需不要弹出提示框的服务
    public static final String CALCULATE_CHARGE_POSE = "/navigation_base/calculate_charge_pose";
    // 获取机器人里程计的位置
    public static final String GET_ODOM_POSE = "/navigation_base/get_odom_pose";
    // 在充电桩检测到充电信号后的机器人位置设置
    public static final String SET_CALCULATE_CHARGE_POSE = "/laser_location/set_calculate_charge_pose";


}
