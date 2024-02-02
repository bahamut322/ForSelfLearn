package com.sendi.deliveredrobot.view.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentAppManagerBinding
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlin.math.abs


class AppManagerFragment : Fragment() {
    companion object{
        const val URL = "app_manage_url"
        const val NAME = "app_manage_name"
        const val TYPE = "app_manage_type"
        const val RICH_TEXT = "app_manage_rich_text"
    }


    private lateinit var binding: FragmentAppManagerBinding
    private var controller: NavController? = null
    private lateinit var gestureDetector: GestureDetector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_app_manager, container, false)
        binding = DataBindingUtil.bind(view)!!
        return view
    }


    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    private fun initWebView() {
        // 设置 WebView 的基本属性
        val webSettings: WebSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        // 设置 WebViewClient，处理页面导航
        binding.webView.webViewClient = WebViewClient()
        // 设置 WebChromeClient，处理页面加载进度等
        binding.webView.webChromeClient = WebChromeClient()
        // 添加触摸事件监听器
        binding.webView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    // 左滑
                    if (diffX > 0) {
                        animateWebViewTranslation()
                    }
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }

    private fun animateWebViewTranslation() {

        // 计算 WebView 的宽度
        val webViewWidth = binding.webView.width.toFloat()

        // 创建平移动画，向左移动 WebView 的宽度
        val translationX = ObjectAnimator.ofFloat(binding.webView, View.TRANSLATION_X, webViewWidth)
        translationX.duration = 500 // 设置动画持续时间，单位毫秒
        translationX.start()
        // 在动画结束时执行回退操作或其他逻辑
        translationX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                goBackInWebView()
                // 将 WebView 的平移重置为0
                binding.webView.translationX = 0f
            }
        })
    }

    private fun goBackInWebView() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            // 如果 WebView 不能返回，可以执行其他操作，例如关闭 Fragment
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        controller = Navigation.findNavController(requireView())
        gestureDetector = GestureDetector(requireContext(), MyGestureListener())
        // 初始化 WebView
        initWebView()
        //网页
        arguments?.let {
            val name = it.getString(NAME)
            when (it.getInt(TYPE)) {
                AppContentFragment.APPLET_TYPE_URL -> {
                    val url = it.getString(URL)?:""
                    binding.webView.loadUrl(url)
                }
                AppContentFragment.APPLET_TYPE_APK -> {}
                AppContentFragment.APPLET_TYPE_RICH_TEXT -> {
                    val richText = it.getString(RICH_TEXT)?:""
                    binding.webView.loadDataWithBaseURL(null, getHtmlData(richText), "text/html", "utf-8", null)
                }
            }
            binding.tvAppTitle.text  = name
        }
        binding.returnBlack.setOnClickListener {
            goBackInWebView()
        }
        //网页监听
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                // 默认情况下，继续在 WebView 内加载新的链接
                view.loadUrl(url)
                return true
            }
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                binding.progress.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                binding.progress.visibility = View.GONE
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                binding.progress.visibility = View.GONE
                ToastUtil.show("网页请求出错：$error")
            }
        }

    }

    private fun getHtmlData(bodyHTML: String): String {
        val head = ("<head>"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> "
                + "<style>img{max-width: 100%; width:100%; height:auto;}*{margin:0px;}</style>"
                + "</head>")
        return "<html>$head<body>$bodyHTML</body></html>"
    }
}