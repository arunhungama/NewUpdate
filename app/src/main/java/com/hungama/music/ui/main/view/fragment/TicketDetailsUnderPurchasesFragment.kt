package com.hungama.music.ui.main.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.model.OfflineEventRespModel
import com.hungama.myplay.activity.R
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.PurchasesEventsAdapter
import com.hungama.music.data.model.PurchasesEventsModel
import com.hungama.music.data.model.RentedMovieRespModel
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.ui.main.view.activity.CommonWebViewActivity
import com.hungama.music.ui.main.view.activity.PaytmInsiderWebviewActivity
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.utils.hide
import com.hungama.music.utils.show
import kotlinx.android.synthetic.main.fragment_ticket_details_under_purchases.*
import kotlinx.coroutines.launch


class TicketDetailsUnderPurchasesFragment : BaseFragment() {
    var userSubscriptionViewModel: UserSubscriptionViewModel? = null
    private lateinit var eventPurchasesAdapter : PurchasesEventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ticket_details_under_purchases, container, false)
    }

    override fun initializeComponent(view: View) {
        CommonUtils.applyButtonTheme(requireContext(), btnExplore)

        setUpViewModel()
        rvEventPurchases.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)


        btnExplore.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), btnExplore!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            displayEmpty(true)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun setUpViewModel() {
        userSubscriptionViewModel = ViewModelProvider(this).get(UserSubscriptionViewModel::class.java)

        getOfflineEventData()
    }

    fun getOfflineEventData(){
        baseMainScope.launch {
            if (ConnectionUtil(requireContext()).isOnline) {
                userSubscriptionViewModel?.getOfflineEventData(requireContext())?.observe(this@TicketDetailsUnderPurchasesFragment,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                pb_progress.hide()
                              //  setProgressBarVisible(false)
                                if (it?.data != null && it?.data?.success!!) {
                                    setLog(TAG, "isViewLoading $it")
                                    fillUI(it?.data)
                                }else{
                                    Utils.showSnakbar(requireContext(),
                                        requireView(),
                                        false,
                                        it?.message!!
                                    )
                                }
                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                                pb_progress.show()
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                pb_progress.hide()
                            }
                        }
                    })
            }else {
                val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true)
                CommonUtils.showToast(requireContext(), messageModel,"TicketDetailsUnderPurchasesFragment","getRentedMovieList")
            }
        }

    }

    private fun fillUI(data: OfflineEventRespModel) {
        if(data.data.offlineEvent.size>0){
            eventPurchasesAdapter = PurchasesEventsAdapter(requireContext(),object :PurchasesEventsAdapter.OnMovieItemClick{
                override fun onItemClick(shortdata: String,event_id: String,event_name: String) {

                    val i = Intent(requireContext(), PaytmInsiderWebviewActivity::class.java)
                    i.putExtra(EventConstant.EVENTID,event_id)
                    i.putExtra(EventConstant.EVENTNAME,event_name)
                    i.putExtra(Constant.EXTRA_URL,shortdata)
                    startActivity(i)
                }

            })
            rvEventPurchases.adapter = eventPurchasesAdapter

            eventPurchasesAdapter.setEventList(data.data.offlineEvent)
            displayEmpty(false)
        }else{
            displayEmpty(true)
        }


    }

    fun displayEmpty(status:Boolean){
        if(status){
            clExplore.visibility = View.VISIBLE
            clEventPurchases.visibility = View.GONE
        }else{
            clExplore.visibility = View.GONE
            clEventPurchases.visibility = View.VISIBLE
        }
    }
}
