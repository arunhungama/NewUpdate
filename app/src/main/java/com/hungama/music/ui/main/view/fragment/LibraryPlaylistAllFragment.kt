package com.hungama.music.ui.main.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.util.Util
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.hungama.fetch2.Download
import com.hungama.fetch2core.Reason
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.database.oldappdata.DBOHandler
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.CreatedPlaylistEvent
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.LibraryMusicAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.utils.customview.downloadmanager.model.DownloadedAudio
import com.hungama.music.utils.hide
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.myplay.activity.R
import kotlinx.android.synthetic.main.dialog_create_playlist.switchicon
import kotlinx.android.synthetic.main.fr_library_playlist.*
import kotlinx.android.synthetic.main.fragment_library_music_all.*
import kotlinx.android.synthetic.main.fragment_library_music_all.btnExplore
import kotlinx.android.synthetic.main.fragment_library_music_all.clExplore
import kotlinx.android.synthetic.main.fragment_library_music_all.createPlaylist
import kotlinx.android.synthetic.main.fragment_library_music_all.rvMusicPlaylist
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap
import java.util.stream.Collectors
import kotlin.collections.ArrayList


class LibraryPlaylistAllFragment() : BaseFragment(),
    CreatePlaylistDialog.createPlayListListener,
    BaseActivity.OnDownloadQueueItemChanged, TracksContract.View,
    BaseActivity.OnLocalBroadcastEventCallBack {

    companion object {
        val CREATE_PLAYLIST_ID = 5001
    }


    private lateinit var rvMusicLibrary: RecyclerView
    private lateinit var musicLibarayAdapter: LibraryMusicAdapter
    private var musicplayList = ArrayList<LibraryMusicModel>()
    var userViewModel: UserViewModel? = null

    var downloadedSongSubtitle = ""
    var downloadedPodcastSubtitle = ""
    var downloadingSubtitle = ""
    var favorited_songs_subtitle = ""

    var isDownloadinProgressItemShow = true

    var userPlaylist: ArrayList<PlaylistRespModel.Data> = ArrayList()

    private lateinit var tracksViewModel: TracksContract.Presenter
    var musicData: LibraryMusicModel? = null
    var playableContentViewModel: PlayableContentViewModel? = null
    var defaultPlaylistName = ""
    var libraryAllModel: ArrayList<LibraryAllRespModel.Row>? = null
    lateinit var playlistListener: CreatePlaylistDialog.createPlayListListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library_music_all, container, false)
    }

    override fun initializeComponent(view: View) {
        CommonUtils.applyButtonTheme(requireContext(), btnExplore)
        defaultPlaylistName = getString(R.string.hungama_playlist) + " 1"
        favorited_songs_subtitle = "0 " + getString(R.string.library_playlist_str_8)
        rvMusicLibrary = view.findViewById(R.id.rvMusicPlaylist)

        playlistListener = this
        ivMyDevice?.hide()
        tvTitleMyDevice?.hide()

        rvMusicLibrary.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        btnExplore?.setOnClickListener {
//            createPlaylist()
            (activity as MainActivity).applyScreen(1)

        }

        createPlaylist?.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnExplore!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            createPlaylist()
        }

        musicLibarayAdapter = LibraryMusicAdapter(requireContext(), musicplayList, object :
            LibraryMusicAdapter.PlayListItemClick {
            override fun libraryItemOnClick(musicData: LibraryMusicModel) {
                setLog(
                    TAG,
                    "libraryItemOnClick musicData id:${musicData.id} musicData containId:${musicData.containId}"
                )

                if (musicData.id.equals("" + CREATE_PLAYLIST_ID)) {
                    createPlaylist()

                    CommonUtils.PageViewEvent(
                        "", "", "", "",
                        MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_All",
                        "popup_create playlist", ""
                    )

                } else if (musicData.id.equals("" + 0)) {
                    val bundle = Bundle()
                    bundle.putString("image", musicData.image)
                    bundle.putString("id", musicData.containId)
                    bundle.putString("playerType", "" + musicData.id)
                    bundle.putBoolean("varient", false)
                    val artistDetailsFragment = ArtistDetailsFragment()
                    artistDetailsFragment.arguments = bundle
                    addFragment(
                        R.id.fl_container,
                        this@LibraryPlaylistAllFragment,
                        artistDetailsFragment,
                        false
                    )

                } else if (musicData.id.equals("" + 55555)) {
                    val bundle = Bundle()
                    bundle.putString("image", musicData?.image)
                    bundle.putString("id", musicData?.containId)
                    bundle.putString("playerType", "" + musicData?.id)
                    var varient = 1

                    val playlistDetailFragment = PlaylistDetailFragmentDynamic.newInstance(varient)
                    playlistDetailFragment.arguments = bundle

                    addFragment(
                        R.id.fl_container,
                        this@LibraryPlaylistAllFragment,
                        playlistDetailFragment,
                        false
                    )

                }else if (musicData?.containId != null) {
                    val varient = 1
                    val playlistDetailFragment = MyPlaylistDetailFragment(varient,
                        object : MyPlaylistDetailFragment.onBackPreesHendel {
                            override fun backPressItem(status: Boolean) {
                            }

                        })
                    val bundle = Bundle()
                    bundle.putString("image", musicData?.image)
                    bundle.putString("id", musicData?.containId)
                    bundle.putString("source", "101")
                    bundle.putString("playerType", Constant.MUSIC_PLAYER)
                    playlistDetailFragment.arguments = bundle
                    setLocalBroadcast()
                    addFragment(
                        R.id.fl_container,
                        this@LibraryPlaylistAllFragment,
                        playlistDetailFragment,
                        false
                    )

                    /*                    CommonUtils.PageViewEvent("","","","",
                                            MainActivity.lastItemClicked + "_" +MainActivity.headerItemName + "_All",
                                            MainActivity.lastItemClicked + "_" +MainActivity.headerItemName + "_All_"+ "Add to Playlist","")*/
                }
            }

        })
        rvMusicLibrary.adapter = musicLibarayAdapter
        musicLibarayAdapter.notifyDataSetChanged()

        val fragment = LibraryMainTabFragment()
        fragment?.addReloadListener(object : LibraryMainTabFragment.onReloadListener {
            override fun onRefresh() {
                if (isAdded) {
                    if (requireView() != null) {
                        setLog(TAG, "onHiddenChanged: initializeComponent called")
                        initializeComponent(requireView())
                    }
                }
            }

        })


        baseIOScope.launch {
            firstTimeAddData(null)
        }

        setupUserViewModel()

        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)

        CommonUtils.setPageBottomSpacing(
            rvMusicPlaylist,
            requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_150),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            0
        )
    }

    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null) {
            setLog("PlayableItem", playableContentModel?.data?.head?.headData?.id.toString())
            songDataList = arrayListOf()


            if (playableContentModel?.data?.head?.headData?.id == musicData?.containId) {
                setPlayableDataList(
                    playableContentModel,
                    musicData!!
                )
            }
            BaseActivity.setTrackListData(songDataList)
            tracksViewModel.prepareTrackPlayback(0)


        }
    }

    var songDataList: ArrayList<Track> = arrayListOf()
    fun setPlayableDataList(
        playableContentModel: PlayableContentModel?,
        playableItem: LibraryMusicModel
    ) {
        val track: Track = Track()

        if (!TextUtils.isEmpty(playableItem?.containId)) {
            track.id = playableItem?.id!!.toLong()
        } else {
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.Title)) {
            track.title = playableItem?.Title
        } else {
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.SubTitle)) {
            track.subTitle = playableItem?.SubTitle
        } else {
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)) {
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        } else {
            track.url = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            track.drmlicence =
                playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        } else {
            track.drmlicence = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.type.toString())) {
            track.playerType = playableContentModel?.data?.head?.headData?.type.toString()
        } else {
            track.playerType = ""
        }
        if (!TextUtils.isEmpty(playableItem.Title)) {
            track.heading = playableItem.Title
        } else {
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.image.toString())) {
            track.image = playableContentModel?.data?.head?.headData?.image
        } else if (!TextUtils.isEmpty(playableItem?.image)) {
            track.image = playableItem?.image
        } else {
            track.image = ""
        }

        if (playableContentModel?.data?.head?.headData?.misc?.explicit != null) {
            track.explicit = playableContentModel?.data?.head?.headData?.misc?.explicit!!
        }
        if (playableContentModel?.data?.head?.headData?.misc?.restricted_download != null) {
            track.restrictedDownload =
                playableContentModel?.data?.head?.headData?.misc?.restricted_download!!
        }
        if (playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating != null) {
            track.attributeCensorRating =
                playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating.toString()
        }

        if (playableContentModel != null) {
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)

        CommonUtils
    }


//    override fun onHiddenChanged(hidden: Boolean) {
//        super.onHiddenChanged(hidden)
//        if (!hidden) {
//            firstTimeAddData()
//        }
//    }

    private fun firstTimeAddData(rows: ArrayList<LibraryAllRespModel.Row>?) {
        try {
            if (isAdded && context != null) {
                downloadingSubtitle = ""
                downloadedSongSubtitle = ""
                downloadedPodcastSubtitle = ""

                musicplayList = ArrayList<LibraryMusicModel>()

                musicplayList.add(
                    LibraryMusicModel(
                        "" + CREATE_PLAYLIST_ID,
                        getString(R.string.library_playlist_str_5),
                        "", "", ""
                    )
                )



                val downloadingSongTotal = AppDatabase?.getInstance()?.downloadQueue()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)
                setLog(TAG, "firstTimeAddData: downloadingSongTotal " + downloadingSongTotal)
                val downloadingPodcastTotal = AppDatabase?.getInstance()?.downloadQueue()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.PODCAST.value)


                var totalSongs = 0
                var totalPadcast = 0

                if (downloadingSongTotal != null && downloadingSongTotal.isNotEmpty()) {
                    totalSongs = downloadingSongTotal.size
                    setLog(TAG, "firstTimeAddData: " + totalSongs)
                    downloadingSubtitle += totalSongs.toString() + " " + getString(R.string.library_playlist_str_8)

                    val userDataMap = java.util.HashMap<String, String>()
                    userDataMap.put(EventConstant.NUMBER_OF_DOWNLOADED_SONGS, "" + totalSongs)
                    EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
                }

                if (downloadingPodcastTotal != null && downloadingPodcastTotal.isNotEmpty()) {
                    totalPadcast = downloadingPodcastTotal.size
                    if (totalSongs > 0) {
                        downloadingSubtitle += ", " + totalPadcast.toString() + " " + getString(R.string.podcast_str_9)
                    } else {
                        downloadingSubtitle += totalPadcast.toString() + " " + getString(R.string.podcast_str_9)
                    }
                }


                val downloadedSongTotal = AppDatabase?.getInstance()?.downloadedAudio()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)

                setLog(TAG, "downloadedSongTotal: ${downloadedSongTotal?.size}")


                val downloadedPodcastTotal = AppDatabase?.getInstance()?.downloadedAudio()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.PODCAST.value)

                var totalDownloadedPodcast = 0
                if (downloadedPodcastTotal != null && downloadedPodcastTotal.isNotEmpty()) {
                    totalDownloadedPodcast = downloadedPodcastTotal.size
                    downloadedPodcastSubtitle += totalDownloadedPodcast.toString() + " " + getString(
                        R.string.podcast_str_9
                    )
                }


                val playListoffline = AppDatabase.getInstance()?.downloadedAudio()
                    ?.getPlayList() as ArrayList<DownloadedAudio>
                if (!playListoffline.isNullOrEmpty()) {
                    playListoffline.forEach {
                        setLog(TAG, "firstTimeAddData: $it")
                        musicplayList.add(
                            LibraryMusicModel(
                                "55555",
                                it.pName!!,
                                "" + getString(R.string.library_music_str_9),
                                it.parentThumbnailPath.toString(),
                                it.parentId!!,
                            )
                        )

                    }
                }

                val userPlayListOffline = AppDatabase.getInstance()?.downloadedAudio()
                    ?.getUserPlayList() as ArrayList<DownloadedAudio>
                if (!userPlayListOffline.isNullOrEmpty()) {
                    userPlayListOffline.forEach {
                        var isUserPlaylistAvailable = false
                        if (!rows.isNullOrEmpty()) {
                            for (item in rows) {
                                if (item.data.id.equals(it.parentId)) {
                                    isUserPlaylistAvailable = true
                                }
                            }
                        }
                        if (!isUserPlaylistAvailable) {
                            setLog(TAG, "firstTimeAddData: $it")
                            musicplayList.add(
                                LibraryMusicModel(
                                    "99999",
                                    it.pName!!,
                                    "" + getString(R.string.library_music_str_9),
                                    it.parentThumbnailPath.toString(),
                                    it.parentId!!,
                                )
                            )
                        }
                    }
                }

                val albumoffline = AppDatabase.getInstance()?.downloadedAudio()
                    ?.getAlbumList() as ArrayList<DownloadedAudio>
                if (!albumoffline.isNullOrEmpty()) {
                    albumoffline.forEach {
                        setLog(TAG, "firstTimeAddData: $it")
                        var songsCount = 0
                        if (downloadedSongTotal != null) {
                            it.parentId?.let { it1 ->songsCount = getAlbumSongs(downloadedSongTotal, it1) }
                        }
                        musicplayList.add(
                            LibraryMusicModel(
                                "1",
                                it?.pName!!,
                                "" + songsCount + " " + getString(R.string.discover_str_24),
                                it?.pImage!!,
                                it?.parentId!!,
                            )
                        )

                    }
                }
                setLog(TAG, "downloadedSongTotal3: ${musicplayList?.size}")
                if (musicplayList.size > 1) {
                    setUpUi(true)
                } else {
                    setUpUi(false)
                }


            }
        } catch (e: Exception) {

        }


    }

    fun getAlbumSongs(offlineSongs:List<DownloadedAudio>, parentId:String):Int{
        var songsCount = 0
        for(song in offlineSongs)
        {
            if (song.parentId.equals(parentId))
                songsCount +=1
        }
            return songsCount
    }

    private fun setupUserViewModelAgain() {
        baseMainScope.launch {
            userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

            if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {

                userViewModel?.getLibraryAllDataNew(requireContext())?.observe(requireActivity(),
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                setLog(TAG, "setupUserViewModel: it?.data " + it?.data)

                                if (it?.data != null) {
                                    libraryAllModel = it.data?.rows
                                    userViewModel?.setLibraryAllRespModelData(it.data)

                                }
                            }
                            Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(
                                    requireContext(),
                                    requireView(),
                                    true,
                                    it.message!!
                                )
                            }
                        }
                    })
            } else {
                setEmptyVisible(false)
                setProgressBarVisible(false)
                val messageModel = MessageModel(
                    getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true
                )
                CommonUtils.showToast(requireContext(), messageModel)

                if (SharedPrefHelper.getInstance()
                        .getPlayList("PlayList") != null && SharedPrefHelper.getInstance()
                        .getPlayList("PlayList")!!.isNotEmpty()
                ) {
                    libraryAllModel = SharedPrefHelper.getInstance().getPlayList("PlayList")!!
//                    setupAllList(libraryAllModel)
                }
//                else{
                val downloadedSongTotal = AppDatabase?.getInstance()?.downloadedAudio()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)
                val dbDataold = DBOHandler.getAllCachedTracks(requireContext())

                Log.e("alkghlah", Gson().toJson(dbDataold))

                var count = 1
                if (downloadedSongTotal!!.isNotEmpty() && dbDataold.isNotEmpty()) {

                    for (item in downloadedSongTotal) {
                        for (i in dbDataold) {
                            if (item.contentId == i.trackId) {
                                count += 1
                                val jsonObject = JSONObject(i.jsonDetails)

                                val misc = LibraryAllRespModel.Row.Data.Misc()
//                                    misc.artist = stringToWords(item.artist!!) as ArrayList<String>
                                if (item.attribute_censor_rating!!.isNotEmpty())
                                    misc.attributeCensorRating =
                                        stringToWords(item.attribute_censor_rating!!) as ArrayList<String>
                                misc.description = item.description!!
                                misc.favCount = item.f_fav_count
                                if (item.language!!.isNotEmpty())
                                    if (item.movierights!!.isNotEmpty())
                                        misc.movierights =
                                            stringToWords(item.movierights.toString()) as ArrayList<String>
                                misc.nudity = item.nudity!!
                                misc.playcount = item.playcount.toString()
                                misc.ratingCritic = if (item.criticRating.toString()
                                        .isNotEmpty()
                                ) item.criticRating.toString().toInt() else 0
                                if (item.artist!!.isNotEmpty())
//                                        misc.sArtist = stringToWords(item.artist.toString()) as ArrayList<String>
                                    misc.synopsis = item.synopsis!!

                                val data = LibraryAllRespModel.Row.Data()

                                setLog("alhgolahoighoa", " " + Gson().toJson(jsonObject))

                                data.duration = 0
                                data.id =
                                    if (jsonObject.has("myPlaylistID")) jsonObject.getString("myPlaylistID") else ""
                                data.image = item.thumbnailPath.toString()
                                data.misc = misc
                                data.releasedate = item.releaseDate!!
                                data.subtitle = item.subTitle!!
                                data.title =
                                    if (jsonObject.has("playlistname")) jsonObject.getString("playlistname") else "Offline_playlist$count"
//                                    data.type = DOWNLOADED_SONGS
                                data.type = 99999

                                setLog("playlistData", Gson().toJson(data))

                                val rows = LibraryAllRespModel.Row()
                                rows.data = data
                                rows.itype = 2
                                rows.public = true

                                var count = 0
                                if (libraryAllModel?.size!! > 0) {

                                    for (libray in libraryAllModel!!) {
                                        if (libray.data.id == data.id) {
                                            count += 1
                                        }
                                    }
                                }

                                if (count == 0)
                                    libraryAllModel?.add(rows)
                            }
                        }
                    }

                    var libraryAllModel1: ArrayList<LibraryAllRespModel.Row> = ArrayList()
                    for (item in libraryAllModel!!) {
                        if (item.data.id.isNotEmpty()) {
                            libraryAllModel1.add(item)
                        }
                    }

                    libraryAllModel1 = libraryAllModel1.stream().distinct()
                        .collect(Collectors.toList()) as ArrayList<LibraryAllRespModel.Row>
                    setLog("playlistData", Gson().toJson(libraryAllModel1))
                    setupAllList(libraryAllModel1)
                }
//                }
            }
        }

    }


    private fun setUpUi(status: Boolean) {
        if (isAdded()) {
            baseMainScope.launch {
                setLog(TAG, "downloadedSongTotal4: $status")
                if (status) {
                    rvMusicPlaylist.visibility = View.VISIBLE
                    clExplore.visibility = View.GONE
                    //musicLibarayAdapter?.notifyDataSetChanged()
                    musicLibarayAdapter?.refreshData(musicplayList)
                } else {
                    clExplore.visibility = View.VISIBLE
                    rvMusicPlaylist.visibility = View.GONE

                }
            }
        }


    }

    private fun fetchDataFromDB(isUpdate: Boolean) {
        try {
            if (isAdded()) {
                downloadingSubtitle = ""
                downloadedSongSubtitle = ""
                downloadedPodcastSubtitle = ""

                val downloadingSongTotal = AppDatabase?.getInstance()?.downloadQueue()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)
                val downloadingPodcastTotal = AppDatabase?.getInstance()?.downloadQueue()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.PODCAST.value)


                var totalSongs = 0
                var totalPadcast = 0



                if (downloadingSongTotal != null && downloadingSongTotal.isNotEmpty()) {
                    totalSongs = downloadingSongTotal.size
                    setLog(TAG, "fetchDataFromDB: " + totalSongs)
                    downloadingSubtitle += totalSongs.toString() + " " + getString(R.string.library_playlist_str_8)
                }

                if (downloadingPodcastTotal != null && downloadingPodcastTotal.isNotEmpty()) {
                    totalPadcast = downloadingPodcastTotal.size
                    if (totalSongs > 0) {
                        downloadingSubtitle += ", " + totalPadcast.toString() + " " + getString(R.string.podcast_str_9)
                    } else {
                        downloadingSubtitle += totalPadcast.toString() + " " + getString(R.string.podcast_str_9)
                    }
                }


                val downloadedSongTotal = AppDatabase?.getInstance()?.downloadedAudio()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)

                var totalDownloadedSong = 0
                if (downloadedSongTotal != null && downloadedSongTotal.isNotEmpty()) {
                    totalDownloadedSong = downloadedSongTotal.size
                    setLog(TAG, "fetchDataFromDB: " + totalDownloadedSong)
                    downloadedSongSubtitle += totalDownloadedSong.toString() + " " + getString(R.string.library_playlist_str_8)
                }

                if (totalDownloadedSong != 0 && musicplayList.size > 1) {
                    var DOWNLOADED_SONGS_POSITION = 2
                    if (!isDownloadinProgressItemShow) {
                        DOWNLOADED_SONGS_POSITION = DOWNLOADED_SONGS_POSITION - 1
                    }

                    musicLibarayAdapter?.notifyItemChanged(DOWNLOADED_SONGS_POSITION)

                }

                val downloadedPodcastTotal = AppDatabase?.getInstance()?.downloadedAudio()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.PODCAST.value)

                var totalDownloadedPodcast = 0
                if (downloadedPodcastTotal != null && downloadedPodcastTotal.isNotEmpty()) {
                    totalDownloadedPodcast = downloadedPodcastTotal.size
                    downloadedPodcastSubtitle += totalDownloadedPodcast.toString() + " " + getString(
                        R.string.podcast_str_9
                    )
                }

                if (totalDownloadedPodcast != 0) {
                    var DOWNLOADED_PODCAST_POSITION = 3
                    if (!isDownloadinProgressItemShow) {
                        DOWNLOADED_PODCAST_POSITION = DOWNLOADED_PODCAST_POSITION - 1
                    }

                    musicLibarayAdapter?.notifyItemChanged(DOWNLOADED_PODCAST_POSITION)

                }


                setLog(TAG, "fetchDataFromDB: musicplayList size:${musicplayList.size}")

                if (musicplayList.size > 1) {
                    setUpUi(true)
                } else {
                    setUpUi(false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun playListCreatedSuccessfull(id: String) {
        var varient = 1

        val playlistDetailFragment =
            MyPlaylistDetailFragment(varient, object : MyPlaylistDetailFragment.onBackPreesHendel {
                override fun backPressItem(status: Boolean) {
                    setupUserViewModelAgain()
                }

            })
        val bundle = Bundle()
        bundle.putString("id", id)
        bundle.putString("playerType", Constant.MUSIC_PLAYER)
        bundle.putString("source", "101")
        playlistDetailFragment.arguments = bundle
        setLocalBroadcast()
        addFragment(R.id.fl_container, this@LibraryPlaylistAllFragment, playlistDetailFragment, false)
//        firstTimeAddData()
    }

    private fun createPlaylist(isDeepLink: Boolean = false) {
        var lastNum = 1
        if (!musicplayList.isNullOrEmpty()) {
            musicplayList.forEachIndexed { index, item ->
                if (item.id.equals("99999", true)) {
                    if (item.Title.contains("Hungama Playlist ")) {
                        var count = item.Title.filter { it.isDigit() }
                        if (!TextUtils.isEmpty(count)) {
                            try {
                                val num = count.toInt() + 1
                                if (lastNum < num) {
                                    lastNum = num
                                    defaultPlaylistName =
                                        getString(R.string.hungama_playlist) + " " + lastNum
                                }
                            } catch (e: Exception) {

                            }
                        }
                    }
                }
            }
        }

        if (isDeepLink) {
            callCreatePlaylist(defaultPlaylistName)
        } else {
            val createPlaylistDialog =
                CreatePlaylistDialog(this@LibraryPlaylistAllFragment, defaultPlaylistName)
            createPlaylistDialog.show(
                activity?.supportFragmentManager!!,
                "open createplaylist dialog"
            )
        }
    }
    private fun callCreatePlaylist(playListName: String) {
        try {
            val mainJson = JSONObject()

            mainJson.put("name", playListName)
            mainJson.put("public", switchicon?.isChecked == true)

            userViewModel?.createPlayList(requireContext(), mainJson)?.observe(this,
                Observer {
                    when (it.status) {
                        com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (it?.data != null && !TextUtils.isEmpty(it?.data?.data?.id)) {

                                setLog("TAG", "callCreatePlaylist: it?.data:${it?.data}")

                                CoroutineScope(Dispatchers.IO).launch {
                                    val hashMap = HashMap<String, String>()
                                    hashMap[EventConstant.CONTENTTYPE_EPROPERTY] = "Audio"
                                    hashMap[EventConstant.PLAYLISTNAME_EPROPERTY] =
                                        it.data.data.title
                                    if (MainActivity.lastBottomItemPosClicked == 4) {
                                        hashMap[EventConstant.SOURCE_NAME_EPROPERTY] = "Library"
                                    } else {
                                        hashMap[EventConstant.SOURCE_NAME_EPROPERTY] =
                                            "Player_3 Dot Menu"
                                    }

                                    EventManager.getInstance()
                                        .sendEvent(CreatedPlaylistEvent(hashMap))
                                }


                                if (playlistListener != null) {
                                    playlistListener.playListCreatedSuccessfull(it.data.data.id)

                                }
                            } else if (!TextUtils.isEmpty(it?.data?.message)) {
                                val messageModel = MessageModel(
                                    it?.data?.message.toString(),
                                    MessageType.NEUTRAL,
                                    true
                                )
                                CommonUtils.showToast(requireContext(), messageModel)
                            }
                        }

                        com.hungama.music.data.webservice.utils.Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        com.hungama.music.data.webservice.utils.Status.ERROR -> {
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(), requireView(), true, it.message!!)
                        }
                    }
                })

        } catch (e: Exception) {
            e.printStackTrace()
            Utils.showSnakbar(
                requireContext(),
                requireView(),
                false,
                getString(R.string.discover_str_2)
            )
        }
    }

    private fun setupUserViewModel() {
        baseMainScope.launch {
            userViewModel = ViewModelProvider(
                requireActivity()
            ).get(UserViewModel::class.java)
            setLog(
                "isGotoDownloadClicked",
                "LibraryMusicAllFragment-setupUserViewModel-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
            )
            if (ConnectionUtil(requireContext()).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
                val json = JSONObject()
                json.put("playlist", true)
                json.put("favourite", true)
                json.put("follow", true)

                val favArrayType = JSONArray()
                favArrayType.put("55555")
                favArrayType.put("1")
                favArrayType.put("34")
                favArrayType.put("77777")
                favArrayType.put("21")
                json.put("favouriteType", favArrayType)

                val followType = JSONArray()
                followType.put("0")
                followType.put("109")
                json.put("followType", followType)

                userViewModel?.getLibraryAllRespModelData()?.observe(this@LibraryPlaylistAllFragment, Observer {
                                setProgressBarVisible(false)
                                setLog(TAG, "setupUserViewModel: it?.data " + it)

                                if (it != null) {
                                    libraryAllModel = it?.rows
                                    var rowsData: ArrayList<LibraryAllRespModel.Row> = ArrayList()
//                                    rowsData.addAll(libraryAllModel)
                                    for (itemOf in libraryAllModel!!) {
                                        val playListoffline =
                                            AppDatabase.getInstance()?.downloadedAudio()
                                                ?.getUserPlayList(itemOf.data.id) as ArrayList<DownloadedAudio>


                                        for (playL in playListoffline) {
                                            if (itemOf.data.id == playL.parentId) {
                                                rowsData.add(itemOf)
                                            }
                                        }
                                    }

                                    SharedPrefHelper.getInstance().savePlayList("PlayList", rowsData)


                                    if ((it != null && it.rows.isNotEmpty())) {
                                        var i = 0
                                        for (playL in it.rows) {
                                            i++
                                            if (playL.data.type == 21 && playL.data.type == 1 ) {

                                            }else{
                                                if (playL.data.type == 55555 || playL.data.type == 99999) {
                                                    rowsData.add(playL)
                                                }
                                            }
                                        }

                                        for (rowIndex in rowsData.indices){
                                            if (rowIndex >= rowsData.size)
                                                break
                                            for (columnIndex in rowIndex until rowsData.size){
                                                if (columnIndex >= rowsData.size)
                                                    break
                                                if (columnIndex == rowIndex && rowsData.get(columnIndex).data.id == rowsData.get(rowIndex).data.id)
                                                {
                                                    // Empty
                                                }
                                                else
                                                {
                                                    if (rowsData.get(columnIndex).data.id == rowsData.get(rowIndex).data.id)
                                                        rowsData.removeAt(rowIndex)
                                                }
                                            }
                                        }

                                        if (!libraryAllModel.isNullOrEmpty()) {
                                            setLog(
                                                "lahlghla",
                                                "865" + Gson().toJson(libraryAllModel)
                                            )

                                            setupAllList(rowsData)
                                        }
                                    } else {
                                        setLog("lahlghla", "869" + Gson().toJson(libraryAllModel))

                                        setupAllList(rowsData)
                                    }

                                }
                    })
            }
            else {
                setEmptyVisible(false)
                setProgressBarVisible(false)
                val messageModel = MessageModel(
                    getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true
                )
                CommonUtils.showToast(requireContext(), messageModel)

                if (SharedPrefHelper.getInstance()
                        .getPlayList("PlayList") != null && SharedPrefHelper.getInstance()
                        .getPlayList("PlayList")!!.isNotEmpty()
                ) {
                    libraryAllModel = SharedPrefHelper.getInstance().getPlayList("PlayList")!!
//                    setupAllList(libraryAllModel)
                }
//                else{
                val downloadedSongTotal = AppDatabase?.getInstance()?.downloadedAudio()
                    ?.getDownloadQueueItemsByContentType(ContentTypes.AUDIO.value)
                val dbDataold = DBOHandler.getAllCachedTracks(requireContext())

//                    libraryAllModel.clear()

//                    Log.e("alkghlah", Gson().toJson(downloadedSongTotal) + "\n")
                Log.e("alkghlah", Gson().toJson(dbDataold))

                var count = 1
                if (downloadedSongTotal!!.isNotEmpty() && dbDataold.isNotEmpty()) {

                    for (item in downloadedSongTotal) {
                        for (i in dbDataold) {
                            if (item.contentId == i.trackId) {
                                count += 1
                                val jsonObject = JSONObject(i.jsonDetails)

                                val misc = LibraryAllRespModel.Row.Data.Misc()
//                                    misc.artist = stringToWords(item.artist!!) as ArrayList<String>
                                if (item.attribute_censor_rating!!.isNotEmpty())
                                    misc.attributeCensorRating =
                                        stringToWords(item.attribute_censor_rating!!) as ArrayList<String>
                                misc.description = item.description!!
                                misc.favCount = item.f_fav_count
                                if (item.language!!.isNotEmpty())
                                    if (item.movierights!!.isNotEmpty())
                                        misc.movierights =
                                            stringToWords(item.movierights.toString()) as ArrayList<String>
                                misc.nudity = item.nudity!!
                                misc.playcount = item.playcount.toString()
                                misc.ratingCritic = if (item.criticRating.toString()
                                        .isNotEmpty()
                                ) item.criticRating.toString().toInt() else 0
                                if (item.artist!!.isNotEmpty())
//                                        misc.sArtist = stringToWords(item.artist.toString()) as ArrayList<String>
                                    misc.synopsis = item.synopsis!!

                                val data = LibraryAllRespModel.Row.Data()

                                setLog("alhgolahoighoa", " " + Gson().toJson(jsonObject))

                                data.duration = 0
                                data.id =
                                    if (jsonObject.has("myPlaylistID")) jsonObject.getString("myPlaylistID") else ""
                                data.image = item.thumbnailPath.toString()
                                data.misc = misc
                                data.releasedate = item.releaseDate!!
                                data.subtitle = item.subTitle!!
                                data.title =
                                    if (jsonObject.has("playlistname")) jsonObject.getString("playlistname") else "Offline_playlist$count"
//                                    data.type = DOWNLOADED_SONGS
                                data.type = 99999

                                setLog("playlistData", Gson().toJson(data))

                                val rows = LibraryAllRespModel.Row()
                                rows.data = data
                                rows.itype = 2
                                rows.public = true

                                var count = 0
                                if (libraryAllModel?.size!! > 0) {

                                    for (libray in libraryAllModel!!) {
                                        if (libray.data.id == data.id) {
                                            count += 1
                                        }
                                    }
                                }

                                if (count == 0)
                                    libraryAllModel?.add(rows)
                            }
                        }
                    }

                    var libraryAllModel1: ArrayList<LibraryAllRespModel.Row> = ArrayList()
                    for (item in libraryAllModel!!) {
                        if (item.data.id.isNotEmpty()) {
                            libraryAllModel1.add(item)
                        }
                    }

                    libraryAllModel1 = libraryAllModel1.stream().distinct()
                        .collect(Collectors.toList()) as ArrayList<LibraryAllRespModel.Row>
                    setLog("playlistData", Gson().toJson(libraryAllModel1))
                    setupAllList(libraryAllModel1)
                }
//                }
            }
        }

    }

    fun stringToWords(s: String) = s.trim().splitToSequence(' ')
        .filter { it.isNotEmpty() } // or: .filter { it.isNotBlank() }
        .toList()

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
            ArrayList(),
            this,
            null,
            true,
            false
        )

        setLog(TAG, "onResume:............................")
    }

    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {
        baseIOScope?.launch {
            if (isAdded()) {
                setLog("DWProgrss-onChangedid", data.id.toString())
                setLog("DWProgrss-onChanged", reason.toString())

                when (reason) {
                    Reason.DOWNLOAD_ADDED -> {
                        setLog("DWProgrss-ADDED", data.id.toString())
                    }

                    Reason.DOWNLOAD_QUEUED -> {
                        setLog("DWProgrss-QUEUED", data.id.toString())

                    }

                    Reason.DOWNLOAD_STARTED -> {
                        setLog("DWProgrss-STARTED", data.id.toString())

                    }

                    Reason.DOWNLOAD_PROGRESS_CHANGED -> {
                        setLog("DWProgrss-CHANGED", data.id.toString())
                    }

                    Reason.DOWNLOAD_RESUMED -> {
                        setLog("DWProgrss-RESUMED", data.id.toString())
                    }

                    Reason.DOWNLOAD_PAUSED -> {
                        setLog("DWProgrss-PAUSED", data.id.toString())
                    }

                    Reason.DOWNLOAD_COMPLETED -> {
                        setLog("DWProgrss-COMPLETED", data.id.toString())
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(2000)
                            fetchDataFromDB(true)
                        }
                    }

                    Reason.DOWNLOAD_CANCELLED -> {
                        setLog("DWProgrss-CANCELLED", data.id.toString())
                    }

                    Reason.DOWNLOAD_REMOVED -> {
                        setLog("DWProgrss-REMOVED", data.id.toString())
                    }

                    Reason.DOWNLOAD_DELETED -> {
                        setLog("DWProgrss-DELETED", data.id.toString())
                    }

                    Reason.DOWNLOAD_ERROR -> {
                        setLog("DWProgrss-ERROR", data.id.toString())
                    }

                    Reason.DOWNLOAD_BLOCK_UPDATED -> {
                        setLog("DWProgrss-UPDATED", data.id.toString())
                    }

                    Reason.DOWNLOAD_WAITING_ON_NETWORK -> {
                        setLog("DWProgrss-NETWORK", data.id.toString())
                    }

                    else -> {}
                }
            }
        }


    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
        if (trackPlayStartPosition > 0) {
            intent.putExtra(Constant.SELECTED_TRACK_PLAY_START_POSITION, trackPlayStartPosition)
        }
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
        Util.startForegroundService(getViewActivity(), intent)
        (activity as MainActivity).reBindService()

    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    private fun setLocalBroadcast() {
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(mMessageReceiverLibrary)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiverLibrary, IntentFilter(Constant.AUDIO_PLAYER_EVENT))
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiverLibrary, IntentFilter(Constant.MYPLAYLIST_EVENT))
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(
                mMessageReceiverLibrary,
                IntentFilter(Constant.DOWNLOADED_CONTENT_EVENT)
            )
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(
                mMessageReceiverLibrary,
                IntentFilter(Constant.FAVORITE_CONTENT_EVENT)
            )
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiverLibrary, IntentFilter(Constant.LIBRARY_CONTENT_EVENT))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            mMessageReceiverLibrary,
            IntentFilter(Constant.STICKY_ADS_VISIBILITY_CHANGE_EVENT)
        )
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded) {
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.MYPLAYLIST_RESULT_CODE || event == Constant.DOWNLOADED_CONTENT_RESULT_CODE
                || event == Constant.FAVORITE_CONTENT_RESULT_CODE || event == Constant.LIBRARY_CONTENT_RESULT_CODE
            ) {
//                firstTimeAddData()
                setupUserViewModelAgain()
            }
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(
                    rvMusicPlaylist,
                    requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_150),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    0
                )
            } else if (context != null && event == Constant.STICKY_ADS_VISIBILITY_CHANGE_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(
                    rvMusicPlaylist,
                    requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_150),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    0
                )
            }
        }
    }

    private fun setupAllList(rows: ArrayList<LibraryAllRespModel.Row>) {
        baseMainScope?.launch {
            firstTimeAddData(rows)
            rows?.forEachIndexed { index, libraryAllRespModelItem ->
                var subtitle = ""
                if (libraryAllRespModelItem?.data?.type == 99999) {
                    //subtitle = getString(R.string.library_music_str_9)
                } else if (libraryAllRespModelItem?.data?.type == 55555) {
                    subtitle = getString(R.string.library_music_str_9)
                } else {
                    setLog("alhglhal", "OfflinePlayList")
                }
                if (!TextUtils.isEmpty(libraryAllRespModelItem?.data?.subtitle)) {
                    if (!TextUtils.isEmpty(subtitle)) {
                        subtitle += " • " + libraryAllRespModelItem?.data?.subtitle
                    } else {
                        subtitle += libraryAllRespModelItem?.data?.subtitle
                    }
                }
                musicplayList.add(
                    LibraryMusicModel(
                        "" +
                                libraryAllRespModelItem.data.type,
                        libraryAllRespModelItem.data.title,
                        subtitle,
                        libraryAllRespModelItem.data.image,
                        libraryAllRespModelItem.data.id,
                    )
                )
                setLog(
                    TAG,
                    "libraryMusicAllRespObserver musicData title:${libraryAllRespModelItem?.data?.title} musicData id:${libraryAllRespModelItem?.data?.type} musicData containId:${libraryAllRespModelItem?.data?.id}"
                )
            }

            for (rowIndex in musicplayList.indices){
                if (rowIndex >= musicplayList.size)
                    break
                for (columnIndex in rowIndex until musicplayList.size){
                    if (columnIndex >= musicplayList.size || columnIndex == 0)
                        break
                    if (columnIndex == rowIndex && musicplayList.get(columnIndex).Title == musicplayList.get(rowIndex).Title)
                    {
                        //Empty
                    }
                    else
                    {
                        if (musicplayList.get(columnIndex).Title == musicplayList.get(rowIndex).Title)
                            musicplayList.removeAt(rowIndex)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                if (musicplayList.size > 1) {
                    setUpUi(true)
                } else {
                    setUpUi(false)
                }

                setLog(TAG, "libraryMusic " + Gson().toJson(musicplayList))

                musicLibarayAdapter?.refreshData(musicplayList)

            }

        }

    }

    private val mMessageReceiverLibrary: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            CommonUtils.setLog(
                "BroadcastReceiver-1",
                "LibraryMusicAllFragment-mMessageReceiver-" + intent
            )
            if (intent != null) {
                if (intent.hasExtra("EVENT")) {
                    CommonUtils.setLog(
                        "BroadcastReceiver-1",
                        "LibraryMusicAllFragment-mMessageReceiver-" + intent.getIntExtra("EVENT", 0)
                    )
                    onLocalBroadcastEventCallBack(context, intent)
                }
            }
        }
    }


}