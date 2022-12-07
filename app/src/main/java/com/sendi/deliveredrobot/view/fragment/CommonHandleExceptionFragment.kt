package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.CommonHandleExceptionListAdapter
import com.sendi.deliveredrobot.databinding.FragmentCommonHandleExceptionBinding
import com.sendi.deliveredrobot.viewmodel.CommonHandleExceptionViewModel

/**
 * @author heky
 * @date 2022-08-01
 * @description 通用处理异常页面
 */
class CommonHandleExceptionFragment : Fragment() {
    private lateinit var binding: FragmentCommonHandleExceptionBinding
    val viewModel = CommonHandleExceptionViewModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_common_handle_exception, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        val fragments = ArrayList<Fragment>()
        fragments.add(HandleExceptionControlDoorFragment())
        fragments.add(HandleExceptionRelocationFragment())
        binding.recyclerViewCommonHandleException.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CommonHandleExceptionListAdapter(viewModel)
        }
        viewModel.currentPosition.observe(viewLifecycleOwner) {
            val bt = parentFragmentManager.beginTransaction()
            bt.replace(R.id.frameLayoutContainer, fragments[it])
            bt.commit()
        }
    }
}