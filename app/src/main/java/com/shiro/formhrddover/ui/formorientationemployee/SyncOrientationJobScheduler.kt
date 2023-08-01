package com.shiro.formhrddover.ui.formorientationemployee

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
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANRequest
import com.androidnetworking.error.ANError
import com.shiro.formhrddover.R
import com.shiro.formhrddover.database.entity.hirechecklist.MNotificationItem
import com.shiro.formhrddover.ui.formhirechecklist.FormListHireChecklistActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SyncOrientationJobScheduler : JobService() {

    companion object {
        private const val TAG = "KARINA"
        private const val CHANNEL_NAME = "hrd_orientation"
        private const val GROUP_KEY_EMAILS = "group_key_emails"
        private const val NOTIFICATION_REQUEST_CODE = 211
        private const val MAX_NOTIFICATION = 3
        private const val CHANNEL_ID = "channel_02"
    }

    private lateinit var viewModel: FormOrientationViewModel
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private var idNotification = 0
    private val stackNotif = ArrayList<MNotificationItem>()

    override fun onStartJob(params: JobParameters?): Boolean {
        viewModel = FormOrientationViewModel(this.application)
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        loadDataSync(params)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob()")
        return true
    }

    private fun loadDataSync(params: JobParameters?) {
        GlobalScope.launch {
            val listOrientationUnSync = GlobalScope.async { viewModel.getTOrientation(1) }.await()
            if (listOrientationUnSync.isNotEmpty()) {
                var isSuccess = false
                for(data in listOrientationUnSync) {
                    val header = JSONObject()
                    header.put("transaction_no", data.transactionno)
                    header.put("employee_name", data.employeename)
                    header.put("employee_no", data.employeeno)
                    header.put("departement_id", data.departementid)
                    header.put("title_name", data.titlename)
                    header.put("started_date", SimpleDateFormat("yyyy/MM/dd").format(data.starteddate))
                    header.put("localsignfilename", data.localsignfilename)
                    header.put("serversignfilename", data.serversignfilename)
                    header.put("status", data.status)
                    header.put("is_cancel", data.iscancel)
                    header.put("createddate", patternFormatDate(data.createddate))
                    header.put("createdby", data.createdby)
                    header.put("createdname", data.createdname)
                    header.put("lastmodifieddate", patternFormatDate(data.lastmodifieddate))
                    header.put("lastmodifiedby", data.lastmodifiedby)
                    header.put("lastmodifiedname", data.lastmodifiedname)

                    val detail = JSONArray()
                    val listdetailmetal = GlobalScope.async { viewModel.getTDetailOrientation(data.transactionno) }.await()
                    for (dataDtl in listdetailmetal) {
                        val obj = JSONObject()
                        obj.put("internal_id", dataDtl.internalid)
                        obj.put("transaction_no", dataDtl.transactionno)
                        obj.put("uraian_id", dataDtl.uraianid)
                        obj.put("value_check", dataDtl.valuecheck)
                        obj.put("netsuite_id_operator", dataDtl.netsuiteidoperator)
                        obj.put("operator_name", dataDtl.operatorname)
                        obj.put("status", dataDtl.status)
                        obj.put("createddate", patternFormatDate(dataDtl.createddate))
                        obj.put("createdby", dataDtl.createdby)
                        obj.put("createdname", dataDtl.createdname)
                        obj.put("lastmodifieddate", patternFormatDate(dataDtl.lastmodifieddate))
                        obj.put("lastmodifiedby", dataDtl.lastmodifiedby)
                        obj.put("lastmodifiedname", dataDtl.lastmodifiedname)
                        detail.put(obj)
                    }

                    val format = JSONObject()
                    format.put("header", header)
                    format.put("detail", detail)
                    Log.d("GSONSON2FORMAT", format.toString())

                    val imageFile = File(data.localsignfilename)
                    if(imageFile.exists()){
                        val res = PushToServer(imageFile, format, data.transactionno).execute().get()
                        if(res.status){
                            isSuccess = true
                            val msg = "Your previous transaction number is ${data.transactionno} changed to ${res.trxno}. According to the order from server"
                            stackNotif.add(MNotificationItem(idNotification, "HR New Hire Checklist", msg))
                            sendNotif()
                            idNotification++
                        } else {
                            isSuccess = false
                        }
                    } else {
                        isSuccess = false
                    }
                }
                if(isSuccess){
                    val intent = Intent(FormListOrientationActivity.DATA_SAVED_BROADCAST)
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

    inner class PushToServer(imageFile: File, format: JSONObject, mTrxIdOld: String) : AsyncTask<String, Void, ReturnFromServer>() {

        private val formata: JSONObject = format
        private val file: File = imageFile
        private val trxIdOld: String = mTrxIdOld
        private val spDataAPI = getSharedPreferences("DATAAPIHRD", MODE_PRIVATE)

        override fun doInBackground(vararg params: String): ReturnFromServer {
            var returnFromServer = ReturnFromServer("", false)
//            val api = spDataAPI.getString("APIGLOBAL", "http://192.168.5.254")
//            val url = "$api/dovechem/dc_hra/Masters/API?token=ZG9fdXBsb2FkLDIwMjEwNTI0LUFQUDAwMQ"
                val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
                val url = "$api/dc_hrd/Masters/API?token=ZG9fdXBsb2FkLDIwMjEwNTI0LUFQUDAwMQ"
            AndroidNetworking.initialize(this@SyncOrientationJobScheduler)
            val request: ANRequest<*> = AndroidNetworking.upload(url)
                    .addMultipartFile("files", file)
                    .addMultipartParameter("postData", formata.toString())
                    .build()
                    .setUploadProgressListener { _, _ -> }
            val response = request.executeForJSONObject()
            if (response.isSuccess) {
                val jsonObject = response.result as JSONObject
                val trx = jsonObject.getString("transaction_number")
                val success = jsonObject.getBoolean("success")
                returnFromServer = ReturnFromServer(trx, success)
            }
            return returnFromServer
        }

        override fun onPostExecute(hello: ReturnFromServer) {
            super.onPostExecute(hello)

            GlobalScope.launch {
                if (hello.status) {
                    // Update Header
                    val header = GlobalScope.async { viewModel.getTOrientation(trxIdOld) }.await()
                    GlobalScope.async { viewModel.deleteTOrientation(header) }.await()
                    header.transactionno = hello.trxno
                    header.status = 2
                    GlobalScope.async { viewModel.insertTOrientation(header) }.await()

                    // Update Detail
                    val listDetail = GlobalScope.async { viewModel.getTDetailOrientation(trxIdOld) }.await()
                    if (!listDetail.isNullOrEmpty()) {
                        for (detail in listDetail) {
                            detail.transactionno = hello.trxno
                            detail.status = 2
                            GlobalScope.async { viewModel.updateTDetailOrientation(detail) }.await()
                        }
                    }
                } else {
                    Log.d(TAG, "SYNC GAGAL")
                }
            }
        }
    }

    data class ReturnFromServer(val trxno : String, val status : Boolean)
}