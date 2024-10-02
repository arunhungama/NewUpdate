package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.model.*
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.DetailTVCastShowAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.myplay.activity.R
import kotlinx.android.synthetic.main.fragment_tvcast.rvPodcastMain


/**
 * A simple [Fragment] subclass.
 * Use the [DynamicFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TVCastingShowFragment : BaseFragment(), OnParentItemClickListener, TracksContract.View{

    var v: View? = null
    var data: Int = 0
    var info: PlaylistModel.Data.Body.Row.Info?=null
    var tvShowDetailRespModel: PlaylistDynamicModel? = null
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playerType:String? = null
    var detailTVShowAdapter: DetailTVCastShowAdapter? = null
    var seasonList = ArrayList<PlaylistModel.Data.Body.Row.Info.Cast>()


    companion object {

        fun addfrag(
            tabIndex: Int,
            info: PlaylistModel.Data.Body.Row.Info?,
            tvShowDetailRespModel: PlaylistDynamicModel?
        ): TVCastingShowFragment {
            val fragment = TVCastingShowFragment()
            val args = Bundle()
            args.putInt("tabIndex", tabIndex)
            args.putParcelable("info", info)
            args.putParcelable("detail", tvShowDetailRespModel)
            fragment.arguments = args
            return fragment
        }

        var tvShowEpisode: PlaylistModel.Data.Body.Row.Info.Cast.CastData?=null
        var childposition=0
    }

    override fun initializeComponent(view: View) {
        //data = requireArguments().getInt("tabIndex", 0)
        //setLog("tabIndex", data.toString())
        //tvIndex.text = data.toString()
        Constant.screen_name ="Cast"
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)
        playerType = "96"
        if(requireArguments()!=null&&requireArguments().containsKey("info")){
            info =requireArguments().getParcelable("info")
        }
        if (requireArguments()!=null&&requireArguments().containsKey("detail")){
            tvShowDetailRespModel = requireArguments().getParcelable("detail")!!
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setLog("tabIndex2", "OnCreateView")
        return inflater.inflate(R.layout.fragment_tvcast, container, false)
    }

    fun setTVCastShowDetailsListData(info: List<PlaylistModel.Data.Body.Row.Info.Cast>) {

        if (info != null && info != null) {
            seasonList = ArrayList()
            seasonList = info as ArrayList<PlaylistModel.Data.Body.Row.Info.Cast>



            setTvShowAdapter()

            rvPodcastMain.layoutManager = GridLayoutManager(context,3)


        } else {

        }
    }

    private fun setTvShowAdapter(){
        Constant.screen_name ="TvShow Screen"
        detailTVShowAdapter = DetailTVCastShowAdapter(requireContext(),seasonList,
            object : DetailTVCastShowAdapter.OnChildItemClick {
                override fun onUserClick(childPosition: Int, lin: LinearLayoutCompat) {
                    tvShowEpisode = seasonList?.get(childPosition)?.data
                    childposition = childPosition
                        if (isOnClick()) {
                            val bundle = Bundle()
                            bundle.putString(Constant.defaultContentImage, tvShowEpisode?.image)
                            bundle.putString(Constant.defaultContentId, tvShowEpisode?.id)
                            bundle.putString(Constant.defaultContentPlayerType, tvShowEpisode?.type.toString())
                            var varient = 1
                            if (!TextUtils.isEmpty(tvShowEpisode?.variantDetails)) {
                                if (tvShowEpisode?.variantDetails.equals("v2", true)) {
                                    bundle.putBoolean(Constant.defaultContentVarient, true)
                                } else {
                                    bundle.putBoolean(Constant.defaultContentVarient, false)
                                }
                            } else {
                                bundle.putBoolean(Constant.defaultContentVarient, false)
                            }
                            val artistDetailsFragment = ArtistDetailsFragment()
                            artistDetailsFragment.arguments = bundle
                            addFragment(R.id.fl_container, this@TVCastingShowFragment, artistDetailsFragment, false)

                    }

                }
            })
        rvPodcastMain.adapter = detailTVShowAdapter
    }

    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }


    override fun onDestroy() {
        (requireActivity() as MainActivity).removeVideoDownloadListener()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
        super.onDestroy()
        tracksViewModel.onCleanup()
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {

    }


    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }


    override fun onResume() {
        super.onResume()
        info?.let { setTVCastShowDetailsListData(it.cast) }
    }

}

