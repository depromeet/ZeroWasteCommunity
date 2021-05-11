package com.depromeet.zerowaste.feature.main.main_community

import android.view.View
import androidx.fragment.app.viewModels
import com.depromeet.zerowaste.R
import com.depromeet.zerowaste.comm.BaseFragment
import com.depromeet.zerowaste.comm.BaseRecycleAdapter
import com.depromeet.zerowaste.data.community.Post
import com.depromeet.zerowaste.data.community.Tag
import com.depromeet.zerowaste.databinding.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainCommunityFragment :
    BaseFragment<FragmentMainCommunityBinding>(R.layout.fragment_main_community) {

    private val viewModel: MainCommunityViewModel by viewModels()

    private val cardAdapter = BaseRecycleAdapter(R.layout.item_main_community_card) { item: Post, bind: ItemMainCommunityCardBinding, _: Int -> bind.item = item }
    private val listAdapter = MainCommunityListAdapter()

    override fun init() {
        binding.vm = viewModel
        binding.fragment = this
        initTagList()
        initPostList()
    }

    private fun initTagList() {
        viewModel.initTagList()
        val data = viewModel.tagList.value ?: return
        val adapter = BaseRecycleAdapter(R.layout.item_main_community_tag) { item: Tag, bind: ItemMainCommunityTagBinding, _: Int -> bind.item = item }
        adapter.setData(data)
        binding.mainCommunityTagList.adapter = adapter
    }

    private fun initPostList() {
        viewModel.getPostList.observe(this) { data ->
            cardAdapter.addData(data)
            listAdapter.addData(data)
        }
        val onNeedLoadMore = {
            viewModel.getNewPostList()
        }
        cardAdapter.needLoadMore = onNeedLoadMore
        listAdapter.needLoadMore = onNeedLoadMore

        binding.mainCommunityCardView.adapter = cardAdapter
        binding.mainCommunityListView.adapter = listAdapter

        viewModel.getNewPostList()
    }

    fun changeListType(isCard: Boolean) {
        if(isCard) {
            binding.mainCommunityCardView.visibility = View.VISIBLE
            binding.mainCommunityListView.visibility = View.GONE
        } else {
            binding.mainCommunityCardView.visibility = View.GONE
            binding.mainCommunityListView.visibility = View.VISIBLE
        }
    }

}