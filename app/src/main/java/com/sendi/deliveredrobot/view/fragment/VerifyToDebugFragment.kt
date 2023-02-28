package com.sendi.deliveredrobot.view.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentVerifyToDebugBinding
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.NavigationBarUtil
import com.sendi.deliveredrobot.utils.ToastUtil


/**
 * @author lsz
 * @desc 调试入口
 * @date 2021/9/2 14:31
 **/
class VerifyToDebugFragment : Fragment() {

    private var mView: View? = null
    private lateinit var binding: FragmentVerifyToDebugBinding
    var controller: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.d("DebuggingFragment onCreate")
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogUtil.d("DebuggingFragment onCreateView")
        mView = inflater.inflate(R.layout.fragment_verify_to_debug, container, false)
        binding = DataBindingUtil.bind(mView!!)!!
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(view)
        initView(view)
        initData()
    }


    fun initData() {
        if (BuildConfig.IS_DEBUG) {
            binding.edtAccount.setText("admin")
            binding.edtPwd.setText("sendirobot")
        }

    }

    fun initView(view: View){
        binding.btnLogin.apply {
            setOnClickListener {
                var accountStr = binding.edtAccount.getText().toString().trim { it <= ' ' }
                var passwordStr = binding.edtPwd.getText().toString().trim { it <= ' ' }
                if (accountStr == "" ||  passwordStr == "") {
                    ToastUtil.show(resources.getString(R.string.str_debug_login_input_tip))
                }else{
                    if (accountStr.equals("admin") && passwordStr.equals("sendirobot")){
                        findNavController().popBackStack()
                        findNavController().navigate(R.id.debuggingFragment)
                    }else{
                        ToastUtil.show(resources.getString(R.string.str_debug_login_error_tip))
                    }
                }
            }
        }
        binding.llReturn.apply {
            setOnClickListener {
                controller!!.navigate(R.id.action_verifyToDebugFragment_to_settingHomeFragment)
            }
        }


        binding.llayoutLogin.viewTreeObserver.addOnGlobalLayoutListener( object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                //监听到了就注销监听
//                binding.llayoutLogin.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                var rect:Rect = Rect()
                binding.llayoutLogin.getWindowVisibleDisplayFrame(rect)
                var  rootInvisibleHeight:Int = binding.llayoutLogin.getRootView().getHeight() - rect.bottom;
//                LogUtil.d( "getRootView().getHeight()=" + binding.llayoutLogin.getRootView().getHeight() + ",rect.bottom=" + rect.bottom + ",rootInvisibleHeight=" + rootInvisibleHeight);
                if (rootInvisibleHeight <= 100) {
                    //软键盘隐藏啦

                } else {
                    //软键盘弹出啦  隐藏状态栏
                    val window: Window = activity?.window!!
                    if(window != null){
                        NavigationBarUtil.hideNavigationBar(window)
                    }
                }
            }
        })

    }



}