package com.shiro.formhrddover.ui.formhirechecklist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.shiro.formhrddover.R
import com.shiro.formhrddover.database.entity.hirechecklist.MNotificationItem
import com.shiro.formhrddover.ui.formhirechecklist.detail.FormDetailHireChecklistActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SyncHireCheckListJobScheduler : JobService() {

    companion object {
        private const val TAG = "KARINA"
        private const val CHANNEL_NAME = "paraform_plant_shift"
        private const val GROUP_KEY_EMAILS = "group_key_emails"
        private const val NOTIFICATION_REQUEST_CODE = 200
        private const val MAX_NOTIFICATION = 2
        private const val CHANNEL_ID = "channel_01"
    }

    private lateinit var viewModel: FormHireChecklistViewModel
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private var idNotification = 0
    private val stackNotif = ArrayList<MNotificationItem>()

    override fun onStartJob(params: JobParameters?): Boolean {
        viewModel = FormHireChecklistViewModel(this.application)
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        Log.d(TAG, "onStartJob()")
        loadDataSync(params)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob()")
        return true
    }

    private fun loadDataSync(params: JobParameters?) {
        GlobalScope.launch {
            val listHireUnSync = GlobalScope.async { viewModel.getTNewHireChecklist(1) }.await()
            if (listHireUnSync.isNotEmpty()) {
                var isSuccess = false
                for(data in listHireUnSync) {
                    val dataHeader = GlobalScope.async { viewModel.getTNewHireChecklist(data.transactionno) }.await()
                    val header = JSONObject()
                    header.put("transaction_no", dataHeader.transactionno)
                    header.put("employee_name", dataHeader.employeename)
                    header.put("departement_id", dataHeader.departementid)
                    header.put("title_name", dataHeader.titlename)
                    header.put("joint_date", SimpleDateFormat("yyyy/MM/dd").format(dataHeader.jointdate))
                    header.put("employee_no", dataHeader.employeeno)
                    header.put("status", dataHeader.status)
                    header.put("createddate", patternFormatDate(dataHeader.createddate))
                    header.put("createdby", dataHeader.createdby)
                    header.put("createdname", dataHeader.createdname)
                    header.put(
                            "lastmodifieddate",
                            patternFormatDate(dataHeader.lastmodifieddate)
                    )
                    header.put("lastmodifiedby", dataHeader.lastmodifiedby)
                    header.put("lastmodifiedname", dataHeader.lastmodifiedname)

                    val detail = JSONArray()
                    val listDetail = GlobalScope.async { viewModel.getTDetailNewHireChecklist(data.transactionno) }.await()
                    for (data in listDetail) {
                        val obj = JSONObject()
                        obj.put("internal_id", data.internalid)
                        obj.put("transaction_no", data.transactionno)
                        obj.put("category_id", data.categoryid)
                        obj.put("mapping_id_category", data.mappingidcategory)
                        obj.put("value_check", data.valuecheck)
                        obj.put("netsuite_id_operator", data.netsuiteidoperator)
                        obj.put("operator_name", data.operatorname)
                        obj.put("status", data.status)
                        obj.put("createddate",
                                patternFormatDate(data.createddate)
                        )
                        obj.put("createdby", data.createdby)
                        obj.put("createdname", data.createdname)
                        obj.put(
                                "lastmodifieddate",
                                patternFormatDate(data.lastmodifieddate)
                        )
                        obj.put("lastmodifiedby", data.lastmodifiedby)
                        obj.put("lastmodifiedname", data.lastmodifiedname)
                        detail.put(obj)
                    }

                    val format = JSONObject()
                    format.put("header", header)
                    format.put("detail", detail)
                    Log.d("GSONSON2FORMAT", format.toString())

                    val res = SyncToHello(format, data.transactionno).execute().get()
                    if(res.status){
                        isSuccess = true
                        val msg = "Your previous transaction number is ${data.transactionno} changed to ${res.trxno}. According to the order from server"
                        stackNotif.add(MNotificationItem(idNotification, "HR New Hire Checklist", msg))
                        sendNotif()
                        idNotification++
                    }
                }
                if(isSuccess){
                    val intent = Intent(FormListHireChecklistActivity.DATA_SAVED_BROADCAST)
                    localBroadcastManager.sendBroadcast(intent)
                    jobFinished(params, false)
                } else {
                    jobFinished(params, true)
                }
            } else {
                jobFinished(params, false)
            }
        }
    }

    private fun patternFormatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
    }

    private fun sendNotif() {
        val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val largeIcon =
                BitmapFactory.decodeResource(resources, R.drawable.ic_baseline_notifications_48)
        val intent = Intent(this, FormListHireChecklistActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
                this,
                NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val mBuilder: NotificationCompat.Builder

        //Melakukan pengecekan jika idNotification lebih kecil dari Max Notif
        if (idNotification < MAX_NOTIFICATION) {
            mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(stackNotif[idNotification].sender)
                    .setContentText(stackNotif[idNotification].message)
                    .setSmallIcon(R.drawable.ic_baseline_sync_48)
                    .setLargeIcon(largeIcon)
                    .setGroup(GROUP_KEY_EMAILS)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
        } else {
            val inboxStyle = NotificationCompat.InboxStyle()
                    .addLine(stackNotif[idNotification].sender)
                    .addLine(stackNotif[idNotification - 1].sender)
                    .setBigContentTitle("$idNotification new sync transaksi")
//                .setSummaryText("mail@dicoding")
            mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("$idNotification new sync transaksi")
//                .setContentText("mail@dicoding.com")
                    .setSmallIcon(R.drawable.ic_baseline_clear_24)
                    .setGroup(GROUP_KEY_EMAILS)
                    .setGroupSummary(true)
                    .setContentIntent(pendingIntent)
                    .setStyle(inboxStyle)
                    .setAutoCancel(true)
        }
        /*
        Untuk android Oreo ke atas perlu menambahkan notification channel
        Materi ini akan dibahas lebih lanjut di modul extended
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create or update. */
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            )

            mBuilder.setChannelId(CHANNEL_ID)

            mNotificationManager.createNotificationChannel(channel)
        }

        val notification = mBuilder.build()

        mNotificationManager.notify(idNotification, notification)
    }

    inner class SyncToHello(format: JSONObject, mTrxIdOld: String) : AsyncTask<String, Void, ReturnFromServer>() {

        private val formata: JSONObject = format
        private val spDataAPI = getSharedPreferences("DATAAPIINSPECTION", MODE_PRIVATE)
        private val trxIdOld = mTrxIdOld

        override fun doInBackground(vararg params: String): ReturnFromServer {
            var returnFromServer = ReturnFromServer("", false, 0)
            val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
            val url = "$api/dc_hrd/Masters/API?token=c3luY0hpcmVDaGVja0xpc3QsMjAyMTAzMTgtQVBQMDAx"
            AndroidNetworking.initialize(this@SyncHireCheckListJobScheduler)

            val request = AndroidNetworking.post(url)
                    .addBodyParameter("postData", formata.toString())
                    .build()
            // Response yang diterima berupa JSON object
            val response = request.executeForJSONObject()
            if (response.isSuccess) {
                try {
                    // Assignment response menjadi sebuah json object
                    val jsonObject = response.result as JSONObject
                    // Jika string dari object key 1 maka assignment result barang
                    val trx = jsonObject.getString("transaction_number")
                    val status = jsonObject.getBoolean("success")
                    val internalserver = jsonObject.getString("internalserver").toInt()
                    returnFromServer = ReturnFromServer(trx, status, internalserver)
                } catch (ex: JSONException) {
                    Log.d("PEGGIESPATROLIERROR", ex.toString())
                }
            } else {
                val error: ANError = response.error
                Log.d("PEGGIESPATROLIERROR", error.message.toString())
                // Handle Error
            }
            return returnFromServer
        }

        override fun onPostExecute(returnFromServer: ReturnFromServer) {
            super.onPostExecute(returnFromServer)
            Log.d("PEGGIESRESULT", returnFromServer.toString())
            if (returnFromServer.status) {
                GlobalScope.launch {
                    // Update Trx Patroli
                    val header = GlobalScope.async { viewModel.getTNewHireChecklist(trxIdOld) }.await()
                    GlobalScope.async { viewModel.deleteTNewHireCheckList(header) }.await()
                    header.transactionno = returnFromServer.trxno
                    header.status = 2
                    GlobalScope.async { viewModel.insertTNewHireChecklist(header) }.await()

                    // Update Trx Detail Silo
                    val listDetail = GlobalScope.async { viewModel.getTDetailNewHireChecklist(trxIdOld) }.await()
                    if (!listDetail.isNullOrEmpty()) {
                        for (detail in listDetail) {
                            detail.transactionno = returnFromServer.trxno
                            detail.status = 2
                            GlobalScope.async { viewModel.updateTDetailNewHireChecklist(detail) }.await()
                        }
                    }
                }
            } else {
                Log.d(TAG, "SYNC GAGAL")
            }
        }
    }

    data class ReturnFromServer(val trxno : String, val status : Boolean, val idsrv : Int)
}