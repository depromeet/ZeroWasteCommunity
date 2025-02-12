package com.depromeet.zerowaste.feature.main.main_mission

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.depromeet.zerowaste.R
import com.depromeet.zerowaste.comm.*
import com.depromeet.zerowaste.data.Ordering
import com.depromeet.zerowaste.data.Place
import com.depromeet.zerowaste.data.Theme
import com.depromeet.zerowaste.data.mission.Mission
import com.depromeet.zerowaste.data.mission.MissionTag
import com.depromeet.zerowaste.data.mission.Rank
import com.depromeet.zerowaste.databinding.*
import com.depromeet.zerowaste.feature.main.MainFragmentDirections
import com.depromeet.zerowaste.feature.main.MainViewModel
import com.depromeet.zerowaste.feature.mission.detail.MissionDetailViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainMissionFragment :
    BaseFragment<FragmentMainMissionBinding>(R.layout.fragment_main_mission) {
    override var statusBarBackGroundColorRes = R.color.black
    override var isLightStatusBar = false

    private val viewModel: MainMissionViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val missionDetailViewModel: MissionDetailViewModel by activityViewModels()

    private val rankerAdapter = BaseRecycleAdapter(R.layout.item_main_mission_ranker)
    { item: Rank, bind: ItemMainMissionRankerBinding, _ ->
        bind.item = item
        bind.itemMainMissionRankUserTxt.text = SpanStrBuilder(requireContext())
            .add(item.rank.toString(), colorRes = R.color.main_enabled)
            .add(textId = R.string.rank, colorRes = R.color.main_enabled)
            .add(" ${item.name}")
            .build()
    }
    private val chipAdapter = BaseRecycleAdapter(R.layout.item_main_mission_chip)
    { item: MissionTag, bind: ItemMainMissionChipBinding, position ->
        bind.itemMainMissionChip.setOnClickListener { missionChipClick(position) }
        bind.item = item
    }
    private val missionAdapter = BaseRecycleAdapter(R.layout.item_main_mission_list)
    { item: Mission, bind: ItemMainMissionListBinding, position ->
        bind.item = item
        bind.root.setOnClickListener { missionCardClick(item) }
        bind.itemMainMissionListLike.setOnClickListener {
            viewModel.toggleLikeMission(item.id, item.isLiked)
            {
                if(it == 0) {
                    item.isLiked = !item.isLiked
                    this.changeData(item, position)
                }
            }
        }
        val tagAdapter = BaseRecycleAdapter(R.layout.item_mission_tag){ i: Theme, b: ItemMissionTagBinding, _ -> b.item = i }
        tagAdapter.setData(item.theme)
        bind.itemMainMissionListTags.layoutManager = genLayoutManager(requireContext(), isVertical = false)
        bind.itemMainMissionListTags.adapter = tagAdapter
    }
    private val iconAdapter = BaseRecycleAdapter(R.layout.item_mission_logo) { item: Place, bind: ItemMissionLogoBinding, _ ->
        when (item) {
            Place.ALL -> bind.itemMissionLogo.setImageResource(R.drawable.ic_mission_all_logo)
            Place.BATHROOM -> bind.itemMissionLogo.setImageResource(R.drawable.ic_mission_bathroom_logo)
            Place.CAFE -> bind.itemMissionLogo.setImageResource(R.drawable.ic_mission_cafe_logo)
            Place.ETC -> bind.itemMissionLogo.setImageResource(R.drawable.ic_mission_etc_logo)
            Place.KITCHEN -> bind.itemMissionLogo.setImageResource(R.drawable.ic_mission_kitchen_logo)
            Place.RESTAURANT -> bind.itemMissionLogo.setImageResource(R.drawable.ic_mission_restaurant_logo)
            Place.OUTSIDE -> bind.itemMissionLogo.setImageResource(R.drawable.ic_mission_outside_logo)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getMissionList()
    }

    override fun init() {
        viewModel.error.observe(this) {
            showToast(it.message.toString())
        }
        /*
        * 데이터 옵저버 동작, 뷰 이벤트 세팅
        * */
        initMissions()
        initRanker()
        initMissionTags()
        initSort()
        initPlaces()
    }

    /*
    * 초기화
    * */
    private fun initMissions() {
        binding.mainMissionList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(dy > 0 && binding.mainMissionMotion.progress != 1f) {
                    binding.mainMissionMotion.progress += Float.MIN_VALUE
                }
            }
        })
        viewModel.missionList.observe(this) {
            binding.mainMissionList.post {
                missionAdapter.setData(it)
            }
        }
//        missionAdapter.loadAnimation = recycleAnimation {
//            val animator = ObjectAnimator.ofFloat(it, "alpha", 0f, 1f).apply {
//                duration = 300L
//                interpolator = LinearInterpolator()
//            }
//            AnimatorSet().apply {
//                play(animator)
//            }
//        }
        binding.mainMissionList.adapter = missionAdapter
    }

    private fun initRanker() {
        viewModel.rankerList.observe(this) {
            rankerAdapter.setData(it)
        }
        val rankers = ArrayList<Rank>()
        rankers.add(Rank(2, "",""))
        rankers.add(Rank(1, "",""))
        rankers.add(Rank(3, "",""))
        rankerAdapter.setData(rankers)
        binding.mainMissionRanker.adapter = rankerAdapter
    }

    private fun initMissionTags() {
        viewModel.missionTagList.observe(this) {
            binding.mainMissionChips.post {
                chipAdapter.setData(it)
            }
        }
        binding.mainMissionChips.adapter = chipAdapter
        viewModel.initTagList()
    }

    private fun initPlaces() {
        viewModel.placeList.observe(this) { list ->
            list.forEach { place ->
                iconAdapter.addData(place)
            }
        }
        binding.mainMissionIcon.isUserInputEnabled = false
        binding.mainMissionIcon.adapter = iconAdapter
        binding.mainMissionTab.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { tabSelected(tab.position) }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        TabLayoutMediator(binding.mainMissionTab, binding.mainMissionIcon) { tab, position ->
            tab.text = resources.getString(iconAdapter.getItem(position)?.textId ?: Place.ALL.textId)
        }.attach()
        viewModel.initPlaceList()
        tabSelected(0)
    }

    private fun initSort() {
        val sortKinds = ArrayList<Pair<Ordering, String>>()
        Ordering.values().forEach {
            sortKinds.add(Pair(it, resources.getString(it.textId)))
        }
        binding.mainMissionSort.setText(viewModel.selectedOrder.value?.textId ?: Ordering.RECENT.textId)
        viewModel.selectedOrder.observe(this) { order ->
            binding.mainMissionSort.setText(order.textId)
        }
        val sortClick: (View) -> Unit = {
            bottomSheet(resources.getString(R.string.sort),
                sortKinds,
                viewModel.selectedOrder.value
            ) {
                viewModel.changeOrder(it)
                if(binding.mainMissionMotion.progress != 1f) binding.mainMissionMotion.transitionToEnd()
            }
        }
        binding.mainMissionSort.setOnClickListener(sortClick)
        binding.mainMissionSortDownArrow.setOnClickListener(sortClick)
    }

    /*
    * 이벤트 세팅
    * */
    private fun missionCardClick(mission: Mission) {
        missionDetailViewModel.setMission(mission)
        mainViewModel.navigate(MainFragmentDirections.actionMainFragmentToMissionDetailFragment())
    }

    private fun missionChipClick(position: Int) {
        val refreshFinish = {
            chipAdapter.notifyDataSetChanged()
            if(binding.mainMissionMotion.progress != 1f) binding.mainMissionMotion.transitionToEnd()
        }
        chipAdapter.getItems().forEachIndexed { i, item ->
            item.selected = if(position == i) {
                viewModel.changeTheme(if(item.selected) null else item.theme, refreshFinish)
                !item.selected
            } else false
        }
    }

    private fun tabSelected(position: Int) {
        val place = viewModel.placeList.value?.get(position) ?: Place.ALL
        viewModel.resetOrder()
        binding.mainMissionSuggestTxt.text = SpanStrBuilder(requireContext())
            .add(textId = place.textId)
            .add(textId = R.string.main_mission_suggest)
            .build()
        chipAdapter.getItems().forEach { item -> item.selected = false }
        chipAdapter.notifyDataSetChanged()
        if(binding.mainMissionMotion.progress != 0f) binding.mainMissionMotion.transitionToStart()
        binding.mainMissionList.scrollTo(0, 0)
        viewModel.changePlace(place) {
            viewModel.refreshRankerList()
        }
    }


}