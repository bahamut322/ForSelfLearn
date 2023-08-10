package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.LineInfoModel
import com.sendi.deliveredrobot.model.PointCompat
import com.sendi.deliveredrobot.model.RectModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.entity.Point
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import geometry_msgs.Point32
import geometry_msgs.Pose2D
import java.util.Objects
import kotlin.math.cos
import kotlin.math.sin


/**
 *   @author: heky
 *   @date: 2021/9/6 16:44
 *   @describe: 激光点绘制View
 */
class LaserPointsView(context: Context?, attrs: AttributeSet?) : View(context, attrs),
    GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener,
    ScaleGestureDetector.OnScaleGestureListener{
    companion object {
        const val POINT_AXIS_SCALE_TIME = 30f
        const val MAX_SCALE_TIME = 7f
        const val MIN_SCALE_TIME = 1f
        const val MODE_TRANSLATE = 0
        const val MODE_ZOOM = 1
        private const val circleColor = Color.WHITE
        private val lineColor = Color.parseColor("#FF8282")
        private val staticPointColor = Color.parseColor("#808080")
        private const val targetPointColor = Color.RED
        private const val routePointColor = Color.RED
        private const val backGroundColor = Color.BLACK
        private val limitSpeedColor = Color.parseColor("#226DE8")
        private val pointColors = arrayOf(Color.parseColor("#00FF7F"), Color.RED)
        private val controlPointColor = Color.RED
        private val avoidPointColor = Color.GREEN
    }

    private var robotPose: Pose2D? = null //机器人位置
    private var floatArrayPoints: FloatArray = FloatArray(0)    //点
    private var floatArrayPoints2: FloatArray = FloatArray(0)    //点
    //    private var floatArrayPoints2: FloatArray = FloatArray(0)    //点
    private var staticMap: ArrayList<FloatArray> = ArrayList() //点集合
    private var updateMap: ArrayList<FloatArray> = ArrayList() //点集合
    private var routePoint: ArrayList<FloatArray> = ArrayList() //路径点集合
    private var routePoint2: ArrayList<FloatArray> = ArrayList() //实时路径点集合
    private var targetPoint: ArrayList<Point> = ArrayList() //目标点集合
    private var lineInfoList: ArrayList<LineInfoModel> = ArrayList() //限速区/虚拟墙点集合列表
    private var lineInfo: LineInfoModel? = null //限速区/虚拟墙点集合
    private var listIds: ArrayList<Point32> = ArrayList(0)       //激光图编号
    private var detector: GestureDetector = GestureDetector(context, this)
    private var detector2: ScaleGestureDetector = ScaleGestureDetector(context, this)
    private var pointColor: Int = pointColors[0]
    private val paint = Paint()
    private val scaleMatrix = Matrix()
    private var mode = MODE_TRANSLATE //手势模式
    private val path = Path()
    private val listRect = ArrayList<RectModel>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scaleMatrix.postScale(MIN_SCALE_TIME, MIN_SCALE_TIME)
        scaleMatrix.postTranslate(measuredWidth / 2f, measuredHeight / 2f)
    }

    /**
     * @author Sunzecong
     */
    fun setLaserPoints(data: IntArray) {
        val tempFloatArray = FloatArray(data.size)
        for (index in data.indices) {
            tempFloatArray[index] = data[index] / 100f
        }
//        if(tempFloatArray.size > 10000){
//            val index = when((tempFloatArray.size / 5) % 2 == 0) {
//                         true -> tempFloatArray.size / 5 - 1
//                         false -> tempFloatArray.size / 5
//                        }
//            floatArrayPoints = tempFloatArray.sliceArray(IntRange(0,index))
//            floatArrayPoints2 = tempFloatArray.sliceArray(IntRange(index + 1,tempFloatArray.size - 1))
//            LogUtil.i("/map/sub_map_info:dataSize1:${floatArrayPoints.size}")
//            LogUtil.i("/map/sub_map_info:dataSize2:${floatArrayPoints2.size}")
//        }else{
        floatArrayPoints = tempFloatArray
//        }
        invalidate()
    }

    /**
     * @description
     */
    fun setLaserPointsArray(data: List<IntArray>){
        if(data.size > 50){
            val tempList1 = data.subList(0,50)
            val tempList2 = data.subList(50, data.size - 1)
            val tempFloatArray1: ArrayList<Float> = ArrayList()
            val tempFloatArray2: ArrayList<Float> = ArrayList()
            tempList1.map { it ->
                it.map {
                    tempFloatArray1.add(it / 100f)
                }
            }
            tempList2.map { it ->
                it.map {
                    tempFloatArray2.add(it / 100f)
                }
            }
            floatArrayPoints = tempFloatArray1.toFloatArray()
            floatArrayPoints2 = tempFloatArray2.toFloatArray()
        }else{
            val tempFloatArray1: ArrayList<Float> = ArrayList()
            data.map { it ->
                it.map {
                    tempFloatArray1.add(it / 100f)
                }
            }
            floatArrayPoints = tempFloatArray1.toFloatArray()
        }
    }

    /**
     * @describe 设置机器人位置
     */
    fun setRobotPose(pose2D: Pose2D) {
        this.robotPose = pose2D
    }

    /**
     * @describe 设置激光状态
     */
    fun setStatus(status: Int) {
        pointColor = when (status) {
            1 -> pointColors[0]
            2 -> pointColors[1]
            else -> pointColors[0]
        }
    }

    /**
     * @describe 设置子图编号
     */
    fun setIdsInfo(idsInfo: List<Point32>) {
        this.listIds.clear()
        this.listIds.addAll(idsInfo)
    }

    /**
     * @describe 静态图
     */
//    fun setStaticPoints(data: List<Point32>) {
//        val tempFloatArray = FloatArray(data.size shl 1)
//        LogUtil.d("staticMap:${tempFloatArray.size}----data:${data.size}")
//        for (index in data.indices) {
//            tempFloatArray[index shl 1] = data[index].x
//            tempFloatArray[(index shl 1) + 1] = data[index].y
//        }
//        floatArrayPoints2 = tempFloatArray
//        LogUtil.d(tempFloatArray.contentToString())
//    }

    /**
     * @describe 静态图
     */
    fun setStaticPoints(data: ArrayList<FloatArray>) {
        this.staticMap.clear()
        this.staticMap.addAll(data)
    }

    /**
     * @describe 动态图
     */
    fun setUpdatePoints(data: ArrayList<FloatArray>) {
        this.updateMap.clear()
        this.updateMap.addAll(data)
        invalidate()
    }

    /**
     * @describe 设置路径点
     */
    fun setRoutePoints(data: ArrayList<FloatArray>) {
        this.routePoint.clear()
        this.routePoint.addAll(data)
        invalidate()
    }

    /**
     * @describe 添加路径点
     */
    fun setCurrentRoutePoints(data: Pose2D) {
        this.routePoint2.add(floatArrayOf(data.x.toFloat(), data.y.toFloat(), data.theta.toFloat()))
        invalidate()
    }

    /**
     * @describe 路径点
     */
    fun setTargetPoints(data: ArrayList<Point>) {
        this.targetPoint.clear()
//        for (item in data) {
////            this.targetPoint.add(floatArrayOf(item.x!!.toFloat(),item.y!!.toFloat()))
//            //汉烜要求显示的时候要把x和y坐标反转显示
//            this.targetPoint.add(floatArrayOf(item.y!!.toFloat(), item.x!!.toFloat()))
//            LogUtil.d("x=" + item.x!!.toFloat() + ";y=" + item.y!!.toFloat())
//        }
        this.targetPoint.addAll(data)
        invalidate()
    }

    /**
     * @describe 限速区/虚拟墙列表
     */
    fun setLineInfoModelList(list: List<LineInfoModel>){
        this.lineInfoList.clear()
        this.lineInfoList.addAll(list)
        invalidate()
    }

    /**
     * @describe 单条限速区/虚拟墙
     */
    fun setLineInfo(lineInfoModel: LineInfoModel){
        this.lineInfo = null
        this.lineInfo = lineInfoModel
        invalidate()
    }

    /**
     * @description 清除单条限速区/虚拟墙
     */
    fun clearLineInfo(){
        this.lineInfo = null
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawColor(backGroundColor)
        canvas?.concat(scaleMatrix)
        //实时地图 较旧部分
        if(floatArrayPoints2.isNotEmpty()){
            paint.apply {
                color = staticPointColor
                strokeWidth = 1f
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = false
            }
            canvas?.drawPoints(floatArrayPoints2.map {
                it * POINT_AXIS_SCALE_TIME
            }.toFloatArray(), paint)
        }
        //实时地图 较新部分
        if (floatArrayPoints.isNotEmpty()) {
            paint.apply {
                color = pointColor
                strokeWidth = 1f
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = false
            }
            canvas?.drawPoints(floatArrayPoints.map {
                it * POINT_AXIS_SCALE_TIME
            }.toFloatArray(), paint)
        }
        //静态图
        if (staticMap.isNotEmpty()) {
            paint.apply {
                color = staticPointColor
                strokeWidth = 1f
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = false
            }
            for (floats in staticMap) {
                canvas?.drawPoints(floats.map {
                    it * POINT_AXIS_SCALE_TIME
                }.toFloatArray(), paint)
            }
        }
        //动态图
        if (updateMap.isNotEmpty()) {
            paint.apply {
                color = pointColor
                strokeWidth = 2f
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = false
            }
            for (floats in updateMap) {
                canvas?.drawPoints(floats.map {
                    it * POINT_AXIS_SCALE_TIME
                }.toFloatArray(), paint)
            }
        }
        //路径点
        if (routePoint.isNotEmpty()) {
            paint.apply {
                color = routePointColor
                strokeWidth = 1f
                strokeCap = Paint.Cap.ROUND
                style = Paint.Style.STROKE
                isAntiAlias = false
            }
//            for (floats in routePoint) {
//                canvas?.drawPoints(floats.map {
//                    it * POINT_AXIS_SCALE_TIME
//                }.toFloatArray(), paint)
//            }
            when(RobotStatus.chassisVersionName < "3.8.0") {
                true -> {
                    path.reset()
                    routePoint.forEachIndexed { index, floats ->
                        when (index) {
                            routePoint.size - 1 -> {
                                if (routePoint.size - 1 == 0) {
                                    path.moveTo(
                                        floats[0] * POINT_AXIS_SCALE_TIME,
                                        floats[1] * POINT_AXIS_SCALE_TIME
                                    )
                                } else {
                                    path.lineTo(
                                        floats[0] * POINT_AXIS_SCALE_TIME,
                                        floats[1] * POINT_AXIS_SCALE_TIME
                                    )
                                }
//                                if (floats.size == 3) {
//                                    paint.apply {
//                                        color = circleColor
//                                        strokeWidth = 1f
//                                        style = Paint.Style.STROKE
//                                        isAntiAlias = false
//                                    }
//                                    canvas?.drawCircle(
//                                        floats[0] * POINT_AXIS_SCALE_TIME,
//                                        floats[1] * POINT_AXIS_SCALE_TIME,
//                                        8f,
//                                        paint
//                                    )
//                                    paint.apply {
//                                        color = lineColor
//                                        strokeWidth = 1f
//                                        isAntiAlias = false
//                                    }
//                                    canvas?.drawLine(
//                                        floats[0] * POINT_AXIS_SCALE_TIME,
//                                        floats[1] * POINT_AXIS_SCALE_TIME,
//                                        (floats[0] + cos(CommonHelper.adjustAngel(floats[2] + Math.PI)) * 0.8).toFloat() * POINT_AXIS_SCALE_TIME,
//                                        (floats[1] + sin(CommonHelper.adjustAngel(floats[2] + Math.PI)) * 0.8).toFloat() * POINT_AXIS_SCALE_TIME,
//                                        paint.apply {
//                                            color = Color.BLUE
//                                        }
//                                    )
//                                }
                            }
                            0 -> {
                                path.moveTo(
                                    floats[0] * POINT_AXIS_SCALE_TIME,
                                    floats[1] * POINT_AXIS_SCALE_TIME
                                )
//                        canvas?.drawPoint(floats[0] * POINT_AXIS_SCALE_TIME, floats[1] * POINT_AXIS_SCALE_TIME, paint)
                            }
                            else -> {
                                path.lineTo(
                                    floats[0] * POINT_AXIS_SCALE_TIME,
                                    floats[1] * POINT_AXIS_SCALE_TIME
                                )
//                        canvas?.drawPoint(floats[0] * POINT_AXIS_SCALE_TIME, floats[1] * POINT_AXIS_SCALE_TIME, paint)
                            }
                        }
                    }
                    canvas?.drawPath(path, paint.apply {
                        strokeWidth = 5f
                        style = Paint.Style.STROKE
                        color = Color.RED
                        isAntiAlias = true
                    })
                }
                false -> {
                    routePoint.forEachIndexed { index, floats ->
                        when{
                            index % 2 == 1 -> {
                                val prevPoint = routePoint[index - 1]
                                canvas?.drawLine(
                                    prevPoint[0] * POINT_AXIS_SCALE_TIME,
                                    prevPoint[1] * POINT_AXIS_SCALE_TIME,
                                    floats[0] * POINT_AXIS_SCALE_TIME,
                                    floats[1] * POINT_AXIS_SCALE_TIME,
                                    paint.apply {
                                        strokeWidth = 5f
                                        style = Paint.Style.STROKE
                                        color = Color.RED
                                        isAntiAlias = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        if (routePoint2.isNotEmpty()) {
            paint.apply {
                color = routePointColor
                strokeWidth = 1f
                strokeCap = Paint.Cap.ROUND
                style = Paint.Style.STROKE
                isAntiAlias = false
            }
            path.reset()
            routePoint2.forEachIndexed { index, floats ->
                when (index) {
                    routePoint2.size - 1 -> {
                        if (routePoint2.size - 1 == 0) {
                            path.moveTo(
                                floats[0] * POINT_AXIS_SCALE_TIME,
                                floats[1] * POINT_AXIS_SCALE_TIME
                            )
                        } else {
                            path.lineTo(
                                floats[0] * POINT_AXIS_SCALE_TIME,
                                floats[1] * POINT_AXIS_SCALE_TIME
                            )
                        }
                        if (floats.size == 3) {
                            paint.apply {
                                color = circleColor
                                strokeWidth = 1f
                                style = Paint.Style.STROKE
                                isAntiAlias = false
                            }
                            canvas?.drawCircle(
                                floats[0] * POINT_AXIS_SCALE_TIME,
                                floats[1] * POINT_AXIS_SCALE_TIME,
                                8f,
                                paint
                            )
                            paint.apply {
                                color = lineColor
                                strokeWidth = 1f
                                isAntiAlias = false
                            }
                            canvas?.drawLine(
                                floats[0] * POINT_AXIS_SCALE_TIME,
                                floats[1] * POINT_AXIS_SCALE_TIME,
                                (floats[0] + cos(CommonHelper.adjustAngel(floats[2] + Math.PI)) * 0.8).toFloat() * POINT_AXIS_SCALE_TIME,
                                (floats[1] + sin(CommonHelper.adjustAngel(floats[2] + Math.PI)) * 0.8).toFloat() * POINT_AXIS_SCALE_TIME,
                                paint.apply {
                                    color = Color.BLUE
                                }
                            )
                        }
                    }
                    0 -> {
                        path.moveTo(
                            floats[0] * POINT_AXIS_SCALE_TIME,
                            floats[1] * POINT_AXIS_SCALE_TIME
                        )
//                        canvas?.drawPoint(floats[0] * POINT_AXIS_SCALE_TIME, floats[1] * POINT_AXIS_SCALE_TIME, paint)
                    }
                    else -> {
                        path.lineTo(
                            floats[0] * POINT_AXIS_SCALE_TIME,
                            floats[1] * POINT_AXIS_SCALE_TIME
                        )
//                        canvas?.drawPoint(floats[0] * POINT_AXIS_SCALE_TIME, floats[1] * POINT_AXIS_SCALE_TIME, paint)
                    }
                }
            }
            canvas?.drawPath(path, paint.apply {
                strokeWidth = 5f
                style = Paint.Style.STROKE
                color = Color.RED
                isAntiAlias = true
            })
        }
        //目标点
        if (targetPoint.isNotEmpty()) {
            for (point in targetPoint) {
                paint.apply {
                    color = targetPointColor
                    strokeWidth = 3f
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = false
                }
                canvas?.drawPoint(
                    (point.y ?: 0f) * POINT_AXIS_SCALE_TIME,
                    when (RobotStatus.chassisVersionName < "3.8.0") {
                        true -> (point.x ?: 0f) * POINT_AXIS_SCALE_TIME
                        false -> -(point.x ?: 0f) * POINT_AXIS_SCALE_TIME
                    },
                    paint
                )
                paint.apply {
                    textSize = 10f
                    color = targetPointColor
                    style = Paint.Style.FILL
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                canvas?.drawText(
                    point.name ?: "",
                    (point.y ?: 0f) * POINT_AXIS_SCALE_TIME,
                    when (RobotStatus.chassisVersionName < "3.8.0") {
                        true -> (point.x ?: 0f) * POINT_AXIS_SCALE_TIME - 3f
                        false -> -(point.x ?: 0f) * POINT_AXIS_SCALE_TIME - 3f
                    },
                    paint
                )
            }
        }

        //限速区路径列表
        if (lineInfoList.isNotEmpty()) {
            for (lineInfoModel in lineInfoList) {
                if (lineInfoModel.pose != null && lineInfoModel.pose!!.isNotEmpty()) {
                    lineInfoModel.pose!!.forEachIndexed { _, pointCompat ->
                        paint.apply {
                            color = limitSpeedColor
                            strokeWidth = 1f
                            strokeCap = Paint.Cap.ROUND
                            style = Paint.Style.STROKE
                            isAntiAlias = false
                        }
                        canvas?.drawPoint(pointCompat.x.toFloat() * POINT_AXIS_SCALE_TIME, pointCompat.y.toFloat() * POINT_AXIS_SCALE_TIME,paint)
                    }
                    paint.apply {
                        textSize = 10f
                        color = limitSpeedColor
                        style = Paint.Style.FILL
                        typeface = Typeface.MONOSPACE
                        isAntiAlias = true
                    }
                    //绘制LineName
                    val midEntity = lineInfoModel.pose!![lineInfoModel.pose!!.size / 2]
                    canvas?.drawText(lineInfoModel.name, midEntity.x.toFloat() * POINT_AXIS_SCALE_TIME, midEntity.y.toFloat() * POINT_AXIS_SCALE_TIME, paint)
                }
                if(lineInfoModel.pose1 != null){
                    paint.apply {
                        color = controlPointColor
                        strokeWidth = 5f
                        strokeCap = Paint.Cap.ROUND
                        style = Paint.Style.STROKE
                        isAntiAlias = false
                    }
                    val pose1 = lineInfoModel.pose1
                    canvas?.drawPoint(
                        pose1?.x?.toFloat()?.times(POINT_AXIS_SCALE_TIME) ?: 0f,
                        pose1?.y?.toFloat()?.times(POINT_AXIS_SCALE_TIME) ?: 0f,paint)
                    paint.apply {
                        textSize = 10f
                        color = controlPointColor
                        style = Paint.Style.FILL
                        typeface = Typeface.MONOSPACE
                        isAntiAlias = true
                    }
                    canvas?.drawText(
                        "${lineInfoModel.name}-控制点",
                        (pose1?.x?.times(POINT_AXIS_SCALE_TIME))?.toFloat()?:0f,
                        (pose1?.y?.times(POINT_AXIS_SCALE_TIME))?.toFloat()?:0f,
                        paint
                    )
                }
                if(lineInfoModel.pose2 != null){
                    paint.apply {
                        color = avoidPointColor
                        strokeWidth = 5f
                        strokeCap = Paint.Cap.ROUND
                        style = Paint.Style.STROKE
                        isAntiAlias = false
                    }
                    val pose2 = lineInfoModel.pose2
                    canvas?.drawPoint(
                        pose2?.x?.toFloat()?.times(POINT_AXIS_SCALE_TIME) ?: 0f,
                        pose2?.y?.toFloat()?.times(POINT_AXIS_SCALE_TIME) ?: 0f,paint)
                    paint.apply {
                        textSize = 10f
                        color = avoidPointColor
                        style = Paint.Style.FILL
                        typeface = Typeface.MONOSPACE
                        isAntiAlias = true
                    }
                    canvas?.drawText(
                        "${lineInfoModel.name}-避让点",
                        (pose2?.x?.times(POINT_AXIS_SCALE_TIME))?.toFloat()?:0f,
                        (pose2?.y?.times(POINT_AXIS_SCALE_TIME))?.toFloat()?:0f,
                        paint
                    )
                }
            }
        }

        //单条限速区路径
        if (Objects.nonNull(lineInfo)) {
            paint.apply {
                color = limitSpeedColor
                strokeWidth = 5f
                strokeCap = Paint.Cap.ROUND
                style = Paint.Style.STROKE
                isAntiAlias = false
            }
            if (lineInfo?.pose != null) {
                val pose = lineInfo?.pose
                for (index in pose?.indices!!) {
                    //因为底盘原因，这里x，y需要调换
                    canvas?.drawPoint(pose[index].x.toFloat() * POINT_AXIS_SCALE_TIME ,pose[index].y.toFloat() * POINT_AXIS_SCALE_TIME,paint)
                    // 机器人当前位置
                    if (index == pose.size - 1) {
                        paint.apply {
                            color = circleColor
                            strokeWidth = 1f
                            style = Paint.Style.STROKE
                            isAntiAlias = false
                        }
                        canvas?.drawCircle((pose[index].x * POINT_AXIS_SCALE_TIME).toFloat(),
                            (pose[index].y * POINT_AXIS_SCALE_TIME).toFloat(), 8f, paint)
                        paint.apply {
                            color = lineColor
                            strokeWidth = 1f
                            isAntiAlias = false
                        }
                        canvas?.drawLine(
                            (pose[index].x * POINT_AXIS_SCALE_TIME).toFloat(),
                            (pose[index].y * POINT_AXIS_SCALE_TIME).toFloat(),
                            (pose[index].x + cos(CommonHelper.adjustAngel(pose[index].z + Math.PI)) * 0.8).toFloat() * POINT_AXIS_SCALE_TIME,
                            (pose[index].y + sin(CommonHelper.adjustAngel(pose[index].z + Math.PI)) * 0.8).toFloat() * POINT_AXIS_SCALE_TIME,
                            paint
                        )
                    }
                }
            }
            if(lineInfo?.pose1 != null){
                paint.apply {
                    color = controlPointColor
                    strokeWidth = 5f
                    strokeCap = Paint.Cap.ROUND
                    style = Paint.Style.STROKE
                    isAntiAlias = false
                }
                val pose1 = lineInfo?.pose1
                canvas?.drawPoint(
                    pose1?.x?.toFloat()?.times(POINT_AXIS_SCALE_TIME) ?: 0f,
                    pose1?.y?.toFloat()?.times(POINT_AXIS_SCALE_TIME) ?: 0f,paint)
                paint.apply {
                    textSize = 10f
                    color = controlPointColor
                    style = Paint.Style.FILL
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                canvas?.drawText(
                    "${lineInfo?.name?:""}-控制点",
                    (pose1?.x?.times(POINT_AXIS_SCALE_TIME))?.toFloat()?:0f,
                    (pose1?.y?.times(POINT_AXIS_SCALE_TIME))?.toFloat()?:0f,
                    paint
                )
            }
            if(lineInfo?.pose2 != null){
                paint.apply {
                    color = avoidPointColor
                    strokeWidth = 5f
                    strokeCap = Paint.Cap.ROUND
                    style = Paint.Style.STROKE
                    isAntiAlias = false
                }
                val pose2 = lineInfo?.pose2
                canvas?.drawPoint(
                    pose2?.x?.toFloat()?.times(POINT_AXIS_SCALE_TIME) ?: 0f,
                    pose2?.y?.toFloat()?.times(POINT_AXIS_SCALE_TIME) ?: 0f,paint)
                paint.apply {
                    textSize = 10f
                    color = avoidPointColor
                    style = Paint.Style.FILL
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                canvas?.drawText(
                    "${lineInfo?.name?:""}-避让点",
                    (pose2?.x?.times(POINT_AXIS_SCALE_TIME))?.toFloat()?:0f,
                    (pose2?.y?.times(POINT_AXIS_SCALE_TIME))?.toFloat()?:0f,
                    paint
                )
            }
        }

        //机器人位置
        if (robotPose != null) {
            paint.apply {
                color = circleColor
                strokeWidth = 1f
                style = Paint.Style.STROKE
                isAntiAlias = false
            }
            canvas?.drawCircle(robotPose!!.x.toFloat() * POINT_AXIS_SCALE_TIME, robotPose!!.y.toFloat() * POINT_AXIS_SCALE_TIME, 3f, paint)
            paint.apply {
                color = lineColor
                strokeWidth = 1f
                isAntiAlias = false
            }
            canvas?.drawLine(
                robotPose!!.x.toFloat() * POINT_AXIS_SCALE_TIME,
                robotPose!!.y.toFloat() * POINT_AXIS_SCALE_TIME,
                (cos(robotPose!!.theta) * 0.8 + robotPose!!.x).toFloat() * POINT_AXIS_SCALE_TIME,
                (sin(robotPose!!.theta) * 0.8 + robotPose!!.y).toFloat() * POINT_AXIS_SCALE_TIME,
                paint
            )
        }
        //子图编号
        if (listIds.isNotEmpty()) {
            paint.apply {
                textSize = 10f
                color = pointColor
                style = Paint.Style.FILL
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            for (listId in listIds) {
                if(listRect.size < listIds.size) {
                    val rect = Rect(
                        (listId.x * POINT_AXIS_SCALE_TIME - 10).toInt(),
                        (listId.y * POINT_AXIS_SCALE_TIME - 10).toInt(),
                        (listId.x * POINT_AXIS_SCALE_TIME + 10).toInt(),
                        (listId.y * POINT_AXIS_SCALE_TIME + 10).toInt()
                    )
                    listRect.add(RectModel(rect, "${listId.z.toInt()}"))
                }
                canvas?.drawText("${listId.z.toInt()}", listId.x * POINT_AXIS_SCALE_TIME, listId.y * POINT_AXIS_SCALE_TIME, paint)
            }
        }
    }

    override fun onDown(event: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
//        LogUtil.i("onScroll")
//        LogUtil.i(
//            " \r\ne1.x = ${e1?.x} \ne1.y = ${e1?.y}\ne2.x = ${e2?.x} \n" +
//                    "e2.y = ${e2?.y}\ndistanceX = $distanceX\ndistanceY = $distanceY"
//        )
        if ((e1?.pointerCount ?: 0) > 1 || (e2?.pointerCount ?: 0) > 1) {
            return false
        }
        scaleMatrix.postTranslate(-distanceX, -distanceY)
        invalidate()
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        resetView()
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mode = MODE_TRANSLATE
            }
            MotionEvent.ACTION_UP -> {
                var x = event.x
                var y = event.y
                val floats = getMatrixFloats()
                x = (x - floats[Matrix.MTRANS_X]) / floats[Matrix.MSCALE_X]
                y = (y - floats[Matrix.MTRANS_Y]) / floats[Matrix.MSCALE_Y]
                for (rect in listRect) {
                    if (rect.rect.contains(x.toInt(), y.toInt())) {
                        ROSHelper.highLightSubMap(rect.id)
                        ToastUtil.show(rect.id)
                        try {
                            //val updateMap = mapResult.data["updateMap"] as List<Point32>
                            val updateMap = RosPointArrUtil.updateMap
                            //setLaserPoints(updateMap)
                            setUpdatePoints(updateMap)
                        } catch (e: Exception) {
                            LogUtil.e(e.toString())
                        }
                    }
                }
                mode = MODE_TRANSLATE
            }
            MotionEvent.ACTION_POINTER_2_DOWN -> {
                mode = MODE_ZOOM
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == MODE_TRANSLATE){
                    return detector.onTouchEvent(event)
                }else if(mode == MODE_ZOOM){
                    return detector2.onTouchEvent(event)
                }
            }
        }
        return detector.onTouchEvent(event)
    }

    /**
     * @describe 重置View
     */
    private fun resetView() {
        scaleMatrix.apply {
            reset()
            scaleMatrix.postScale(MIN_SCALE_TIME, MIN_SCALE_TIME)
            scaleMatrix.postTranslate(measuredWidth / 2f, measuredHeight / 2f)
        }
        invalidate()
    }

    /**
     * @describe 获取当前缩放倍数
     */
    private fun getScale(): Float {
        val floats = FloatArray(9)
        scaleMatrix.getValues(floats)
        return floats[Matrix.MSCALE_X]
    }

    private fun getMatrixFloats(): FloatArray{
        val floats = FloatArray(9)
        scaleMatrix.getValues(floats)
        return floats
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        val scale = getScale()
        var scaleFactor = detector?.scaleFactor ?: 1f
        if (scale * scaleFactor < MIN_SCALE_TIME) scaleFactor = MIN_SCALE_TIME / scale
        else if (scale * scaleFactor > MAX_SCALE_TIME) scaleFactor = MAX_SCALE_TIME / scale
        scaleMatrix.postScale(
            scaleFactor,
            scaleFactor,
            detector?.focusX ?: 0f,
            detector?.focusY ?: 0f
        )
        invalidate()
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
    }
}