package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.DecelerateInterpolator
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentStartExplantionBinding
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.MyResultModel
import com.sendi.deliveredrobot.model.SecondModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.ConsumptionTask
import com.sendi.deliveredrobot.navigationtask.LineUpTaskHelp
import com.sendi.deliveredrobot.navigationtask.LineUpTaskHelp.OnTaskListener
import com.sendi.deliveredrobot.navigationtask.LineUpTaskHelp.getInstance
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.CenterItemUtils
import com.sendi.deliveredrobot.utils.CenterItemUtils.CenterViewItem
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.UiUtils
import com.sendi.deliveredrobot.viewmodel.StartExplanViewModel
import kotlinx.coroutines.launch

/**
 * @author swn
 * @describe 开始智能讲解
 */
class StartExplantionFragment : Fragment() {
    var binding: FragmentStartExplantionBinding? = null
    var controller: NavController? = null
    private var mAdapter: MAdapter?  = MAdapter()
    private var centerToTopDistance //RecyclerView高度的一半 ,也就是控件中间位置到顶部的距离 ，
            = 0
    private val childViewHalfCount = 0 //当前RecyclerView一半最多可以存在几个Item
    private var viewModel: StartExplanViewModel? = null
    private var lineUpTaskHelp: LineUpTaskHelp<ConsumptionTask> = getInstance()
    var index = 0

    override fun onStop() {
        super.onStop()
        viewModel!!.cancelMainScope()
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lineUpTaskHelp = getInstance()
        controller = Navigation.findNavController(view)
        viewModel = ViewModelProvider(this).get(StartExplanViewModel::class.java)
        viewModel!!.inForListData()
        //数组赋值
        RobotStatus.speakContinue!!.observe(viewLifecycleOwner) {
            if (RobotStatus.speakContinue!!.value == 1) {
                if (lineUpTaskHelp .checkTask()) {
                    lineUpTaskHelp .deletePlanNoAll("speakNumber")
                }
                getLength(RobotStatus.speakNumber.value)
                RobotStatus.speakContinue!!.postValue(0)
            }
        }
        init()
        viewModel!!.mainScope()
        binding!!.finishBtn.setOnClickListener { v ->
            //返回
            viewModel!!.finish()
            BaiduTTSHelper.getInstance().stop()
            //删除讲解队列
            if (lineUpTaskHelp .checkTask()) {
                lineUpTaskHelp.deletePlanNoAll("speakNumber")
            }
        }
        binding!!.nextTaskBtn.setOnClickListener { viewModel!!.nextTask() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_start_explantion, container, false)
        binding = DataBindingUtil.bind(view)
        return view
    }

    private fun init() {
        binding!!.pointList.layoutManager = LinearLayoutManager(context)
        binding!!.pointList.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding!!.pointList.viewTreeObserver.removeOnGlobalLayoutListener(this)
                centerToTopDistance = binding!!.pointList.height / 2
//                val childViewHeight = UiUtils.dip2px(context, 72f) //72是当前已知的 Item的高度
                //                childViewHalfCount = (recyclerView.getHeight() / childViewHeight + 1) / 2;
                initData()
                findView()
            }
        })
        binding!!.pointList.postDelayed({ scrollToCenter(0) }, 100L)
    }

    private fun initData() {
        viewModel!!.inForListData()
        for (j in 0..childViewHalfCount) { //头部的空布局
            viewModel!!.mDatas!!.add(0, null)
        }
        for (k in 0..childViewHalfCount) {  //尾部的空布局
            viewModel!!.mDatas!!.add(null)
        }
    }

    private var isTouch = false
    private val centerViewItems: MutableList<CenterViewItem> = ArrayList()

    @SuppressLint("ClickableViewAccessibility")
    private fun findView() {
        mAdapter = MAdapter()
        binding!!.pointList.adapter = mAdapter
        binding!!.pointList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                    val fi = linearLayoutManager!!.findFirstVisibleItemPosition()
                    val la = linearLayoutManager.findLastVisibleItemPosition()
                    Log.i("ccb", "onScrollStateChanged:首个item: $fi  末尾item:$la")
                    if (isTouch) {
                        isTouch = false
                        //获取最中间的Item View
                        val centerPositionDiffer = (la - fi) / 2
                        var centerChildViewPosition = fi + centerPositionDiffer //获取当前所有条目中中间的一个条目索引
                        centerViewItems.clear()
                        //遍历循环，获取到和中线相差最小的条目索引(精准查找最居中的条目)
                        if (centerChildViewPosition != 0) {
                            for (i in centerChildViewPosition - 1 until centerChildViewPosition + 2) {
                                val cView = recyclerView.layoutManager!!.findViewByPosition(i)
                                val viewTop = cView!!.top + cView.height / 2
                                centerViewItems.add(
                                    CenterViewItem(
                                        i,
                                        Math.abs(centerToTopDistance - viewTop)
                                    )
                                )
                            }
                            val centerViewItem = CenterItemUtils.getMinDifferItem(centerViewItems)
                            centerChildViewPosition = centerViewItem.position
                        }
                        scrollToCenter(centerChildViewPosition)
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                for (i in 0 until recyclerView.childCount) {
                    recyclerView.getChildAt(i).invalidate()
                }
            }
        })
        binding!!.pointList.setOnTouchListener { view, motionEvent ->
            isTouch = true
            false
        }
    }

    private val decelerateInterpolator = DecelerateInterpolator()

    /**
     * 移动指定索引到中心处 ， 只可以移动可见区域的内容
     */
    private fun scrollToCenter(position: Int) {
        var position = position
        position = if (position < childViewHalfCount) childViewHalfCount else position
        position =
            if (position < mAdapter!!.itemCount - childViewHalfCount - 1) position else mAdapter!!.itemCount - childViewHalfCount - 1
        val linearLayoutManager = binding!!.pointList.layoutManager as LinearLayoutManager?
        val childView = linearLayoutManager!!.findViewByPosition(position)
        Log.i("ccb", "滑动后中间View的索引: $position")
        //把当前View移动到居中位置
        if (childView == null) return
        val childVhalf = childView.height / 2
        val childViewTop = childView.top
        val viewCTop = centerToTopDistance
        val smoothDistance = childViewTop - viewCTop + childVhalf
        Log.i(
            "ccb", """
     居中位置距离顶部距离: $viewCTop
     当前居中控件距离顶部距离: $childViewTop
     当前居中控件的一半高度: $childVhalf
     滑动后再次移动距离: $smoothDistance
     """.trimIndent()
        )
        binding!!.pointList.smoothScrollBy(0, smoothDistance, decelerateInterpolator)
        mAdapter!!.setSelectPosition(position)
    }

    internal inner class MAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            viewModel!!.inForListData()
            return VH(
                LayoutInflater.from(
                    context
                ).inflate(R.layout.item_start_explanation, parent, false)
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val vh = holder as VH
            if (position == 0) {
                vh.tvTopLine.visibility = View.INVISIBLE
            }
            if (selectPosition == position) {
                //当前点显示的文字&图片
                vh.tv.setTextColor(resources.getColor(R.color.color_49DCFA))
                vh.tvDot.setBackgroundResource(R.drawable.lline_dot_normal)
                binding!!.pointName.text = viewModel!!.mDatas!![position]!!.name
                Glide.with(context!!).load(viewModel!!.mDatas!![position]!!.touch_imagefile).into(
                    binding!!.pointImage
                )
                binding!!.acceptstationTv.text = viewModel!!.mDatas!![position]!!.explanationtext
                binding!!.nowExplanation.text = "当前讲解：" + viewModel!!.mDatas!![position]!!.name
                //帧动画
                if (position != 0) {
                    vh.tvTopLine.setImageResource(R.drawable.anim_login_start_loading)
                    val animationDrawable = vh.tvTopLine.drawable as AnimationDrawable
                    animationDrawable.start()
                }

                //副屏更新
//                    RobotStatus.SecondModel?.value =
//                        viewModel!!.secondScreenModel(position, viewModel!!.mDatas!!)
//                    RobotStatus.sdScreenStatus!!.postValue(3)
                //如果有讲解内容
                if (viewModel!!.mDatas!![position]!!.walktext != null) {
                    //截取文字加入队列
                    getLength(viewModel!!.mDatas!![position]!!.walktext)
                    RobotStatus.speakNumber.postValue(viewModel!!.mDatas!![position]!!.walktext)
                    //观察讲解是否结束
                    RobotStatus.speakContinue!!.observe(viewLifecycleOwner) { integer: Int? ->
                        RobotStatus.ready.observe(viewLifecycleOwner) { integer: Int? ->
                            viewModel!!.mainScope.launch {
                                if (RobotStatus.ready.value == 1 && RobotStatus.speakContinue!!.value == 3) {
                                    arrayToDo(viewModel!!.mDatas, position)
                                }
                            }
                        }
                    }
                }
                //如果有mp3音屏
                if (viewModel!!.mDatas!![position]!!.walkvoice != null) {
                    MediaPlayerHelper.play(viewModel!!.mDatas!![position]!!.walkvoice)
                    //mp3播放监听
                    MediaPlayerHelper.setOnProgressListener { currentPosition: Int, totalDuration: Int ->
                        LogUtil.i("currentPosition : $currentPosition totalDuration : $totalDuration")
                        viewModel!!.mainScope.launch {
                            RobotStatus.ready.observe(viewLifecycleOwner) { integer: Int? ->
                                viewModel!!.mainScope.launch {
                                    if (currentPosition >= totalDuration && integer == 1) {
                                        LogUtil.i("到点并且播放完成")
                                        arrayToDo(viewModel!!.mDatas, position)
                                        // mediaplayer went away with unhandled events
//                                    ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                vh.tv.setTextColor(resources.getColor(R.color.white))
                vh.tvDot.setBackgroundResource(R.drawable.lline_dot_first)
            }
            vh.itemView.setOnClickListener { v: View? ->
                scrollToCenter(position)
                Toast.makeText(
                    context,
                    "点击" + viewModel!!.mDatas!![position],
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private var selectPosition = -1

        @SuppressLint("NotifyDataSetChanged")
        fun setSelectPosition(cposition: Int) {
            selectPosition = cposition
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return viewModel!!.mDatas!!.size
        }

        internal inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tv: TextView
            var tvDot: TextView
            var tvTopLine: ImageView
            init {
                // 第一行头的竖线不显示
                tvTopLine = itemView.findViewById(R.id.tvTopLine)
                tv = itemView.findViewById(R.id.tv)
                tvDot = itemView.findViewById(R.id.tvDot)
            }
        }
    }

    fun arrayToDo(mDatas: ArrayList<MyResultModel?>?, position: Int) {
            if (mDatas!![position]!!.explanationtext != "") {
//                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
                getLength(mDatas[position]!!.explanationtext)
                if (RobotStatus.speakContinue!!.value == 3) {
                    BillManager.currentBill()?.executeNextTask()
                    mAdapter!!.setSelectPosition(position+1)
            } else if (mDatas[position]?.explanationvoice != "") {
//                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
                MediaPlayerHelper.play(mDatas[position]!!.explanationvoice)
                MediaPlayerHelper.setOnProgressListener { currentPosition: Int, totalDuration: Int ->
                    viewModel!!.mainScope.launch {
                        if (currentPosition != totalDuration) {
                            BillManager.currentBill()?.executeNextTask()
                            mAdapter!!.setSelectPosition(position+1)
//                            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
                        }
                    }
                }
            }else if(mDatas[position]?.explanationvoice == "" && mDatas[position]!!.explanationtext != ""){
                BillManager.currentBill()?.executeNextTask()
                mAdapter!!.setSelectPosition(position+1)
            }
        }
        RobotStatus.ready.postValue(0)
    }

    //根据字符串长度添加换行符
    fun getLength(string: String?) {
        //记录一共有多少位字符
        var valueLength = 0
        //定义一个StringBuffer存储数据
        var stringBuffer = StringBuilder()
        var remainNum = 0
        var remainText: String? = null
        //剩余字数
        val remainSize = string!!.length - string.length % 45
        if (string.length % 45 == 0) {
            LogUtil.d("讲解字数为45倍数不需要过多操作")
        } else {
            remainText = string.substring(remainSize) //截取
        }
        for (i in 0 until string.length) {
            // 获取一个字符
            val temp = string.substring(i, i + 1)
            // 判断是否为中文字符
            valueLength += 1
            remainNum += 1
            //每个数据放入StringBuffer中
            stringBuffer.append(temp)
            //如果长度为45，开始换行
            if (valueLength >= 45) {
                val task = ConsumptionTask()
                task.explantinSpeaking = stringBuffer.toString() // 队列数据
                task.planNo = "speakNumber" // 将数据分组，可以不进行设置
                lineUpTaskHelp.addTask(task) // 添加到排队列表中去， 如果还有任务没完成，
                stringBuffer = StringBuilder()
                //清空valueLength
                valueLength = 0
            }
            //将剩余的字添加到队列中
            if (remainNum == remainSize) {
                val task = ConsumptionTask()
                task.explantinSpeaking = remainText // 队列数据
                task.planNo = "speakNumber" // 将数据分组，可以不进行设置
                lineUpTaskHelp.addTask(task) // 添加到排队列表中去， 如果还有任务没完成，
                remainNum = 0
            }
            initListener()
        }
    }

    private fun initListener() {
        lineUpTaskHelp.setOnTaskListener(object : OnTaskListener<ConsumptionTask?> {
            override fun exNextTask(task: ConsumptionTask?) {
                if (task != null) {
                    exTask(task)
                }
            }

            override fun noTask() {
                Log.e("Post", "所有任务执行完成")
            }
        })
    }

    fun exTask(task: ConsumptionTask) {
        object : Thread() {
            override fun run() {
                super.run()
                //子线程执行队列任务
                BaiduTTSHelper.getInstance().speaks(task.explantinSpeaking, "explantion")
                Log.e("Post", "开始执行任务" + task.explantinSpeaking)
                // 检查列队
                lineUpTaskHelp.exOk(task)
            }
        }.start()
    }
}