package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.SendPlaceBin2ListAdapter
import com.sendi.deliveredrobot.databinding.FragmentPlaceListBinding
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel

class SendPlaceBin2ListFragment(position: Int) : Fragment() {
    private lateinit var binding: FragmentPlaceListBinding
    private val viewModel by viewModels<SendPlaceBin2ViewModel>({ requireActivity() })
    private val localPosition = position
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_place_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        with(binding.rvContainer) {
            layoutManager = GridLayoutManager(context, 5)
            adapter = SendPlaceBin2ListAdapter(viewModel, localPosition)
        }
        viewModel.place.observe(viewLifecycleOwner) {
            if (TextUtils.isEmpty(it)) {
//                viewModel.clearSelected()
                with(binding.rvContainer) { adapter!!.notifyDataSetChanged() }
            }
        }
    }
}