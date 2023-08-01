package com.shiro.formhrddover.ui.formorientationemployee

import android.R
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANRequest
import com.shiro.formhrddover.database.DateTypeConverter
import com.shiro.formhrddover.database.entity.hirechecklist.TNewHireCheckListEntity
import com.shiro.formhrddover.database.entity.orientation.TOrientationEntity
import com.shiro.formhrddover.databinding.ActivityFormListOrientationBinding
import com.shiro.formhrddover.helper.ViewModelFactory
import com.shiro.formhrddover.ui.formhirechecklist.FormHireChecklistViewModel
import com.shiro.formhrddover.ui.formorientationemployee.header.FormHeaderOrientationActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

class FormListOrientationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormListOrientationBinding
    private lateinit var viewModel: FormOrientationViewModel
    private lateinit var viewModelHire: FormHireChecklistViewModel
    private lateinit var dateTypeConverter: DateTypeConverter
    private lateinit var formAdapter: FormListOrientationAdapter
    private lateinit var localBroadcastManager: LocalBroadcastManager

    companion object{
        private var broadcastReceiver: BroadcastReceiver? = null
        private var broadcastReceiverRefresh: BroadcastReceiver? = null
        val DATA_SAVED_BROADCAST: String = "com.shiro.formhrddover.helloworld"
        val DATA_SAVED_BROADCAST_REFRESH: String = "com.shiro.formhrddover.cobadulu"
        private const val JOB_ID = 12
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormListOrientationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                this@FormListOrientationActivity.runOnUiThread {
                    loadRefreshData(true)
                }
            }
        }
        broadcastReceiverRefresh = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                searchTrxOrientation()
            }
        }

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Form List Orientation"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)

        dateTypeConverter = DateTypeConverter()

        val factory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, factory)[FormOrientationViewModel::class.java]
        viewModelHire = ViewModelProvider(this, factory)[FormHireChecklistViewModel::class.java]

        binding.tvStartCreatedDateOrientation.text = getLocalDateNow(true)
        binding.tvEndCreatedDateOrientation.text = getLocalDateNow(false)
        binding.btnStartCreatedDateOrientation.setOnClickListener { dialogDate("from") }
        binding.btnEndCreatedDateOrientation.setOnClickListener { dialogDate("to") }
        binding.tvSearchNameEmployeeOrientation.addTextChangedListener { searchTrxOrientation() }
        binding.tvSearchOfficerOrientation.addTextChangedListener { searchTrxOrientation() }

        binding.fabAddInspection.setOnClickListener {
//            if (isJobRunning(this, JOB_ID)) {
//                val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
//                scheduler.cancel(JOB_ID)
//                Toast.makeText(this, "Job Service canceled", Toast.LENGTH_SHORT).show()
//            }
            val intent = Intent(this, FormHeaderOrientationActivity::class.java)
            intent.putExtra(FormHeaderOrientationActivity.TYPE_INTENT, "INSERT")
            startActivity(intent)
        }

        formAdapter = FormListOrientationAdapter(object : FormListOrientationAdapter.ButtonListOrientationListener {
            override fun btnViewListener(idtrx: String, iscancel: Int) {
//                if (isJobRunning(this@FormListOrientationActivity, JOB_ID)) {
//                    val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
//                    scheduler.cancel(JOB_ID)
//                    Toast.makeText(this@FormListOrientationActivity, "Job Service canceled", Toast.LENGTH_SHORT).show()
//                }
                val intent = Intent(this@FormListOrientationActivity, FormHeaderOrientationActivity::class.java)
                intent.putExtra(FormHeaderOrientationActivity.TYPE_INTENT, "UPDATE")
                intent.putExtra(FormHeaderOrientationActivity.EXTRA_TRANSACTION_ID, idtrx)
                intent.putExtra(FormHeaderOrientationActivity.EXTRA_IS_CANCEL, iscancel)
                startActivity(intent)
            }
        })
    }

    private fun loadRefreshData(full : Boolean){
        viewModel.refresh(this, full)
    }

    private fun dialogDate(type : String) {
        val day: Int
        val month: Int
        val year: Int
        if(type == "to"){
            day = binding.tvEndCreatedDateOrientation.text.substring(0..1).toInt()
            month = binding.tvEndCreatedDateOrientation.text.substring(3..4).toInt() - 1
            year = binding.tvEndCreatedDateOrientation.text.substring(6..9).toInt()
        } else {
            day = binding.tvStartCreatedDateOrientation.text.substring(0..1).toInt()
            month = binding.tvStartCreatedDateOrientation.text.substring(3..4).toInt() - 1
            year = binding.tvStartCreatedDateOrientation.text.substring(6..9).toInt()
        }

        val dpd = DatePickerDialog(
                this,
                { view, year, monthOfYear, dayOfMonth ->
                    // Display Selected date in textbox
                    val month = monthOfYear + 1
                    var monthS = month.toString()
                    if (monthS.length == 1) {
                        monthS = "0$monthS"
                    }
                    var day = dayOfMonth.toString()
                    if (day.length == 1) {
                        day = "0$day"
                    }
                    if(type == "to"){
                        binding.tvEndCreatedDateOrientation.text = "$day-$monthS-$year"
                    } else {
                        binding.tvStartCreatedDateOrientation.text = "$day-$monthS-$year"
                    }
                    searchTrxOrientation()
                },
                year,
                month,
                day
        )
        dpd.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        // jobscheduler
//        startJobSyncHire()
        broadcastReceiver?.let {
            localBroadcastManager.registerReceiver(
                    it, IntentFilter(
                    DATA_SAVED_BROADCAST
            )
            )
        }
        broadcastReceiverRefresh?.let {
            localBroadcastManager.registerReceiver(
                    it, IntentFilter(DATA_SAVED_BROADCAST_REFRESH)
            )
        }

        GlobalScope.launch {
            // Baca data kosong jika tidak ada data transaksi sama sekali di device
            val listTNewHireCheckListEntity = GlobalScope.async { viewModel.getTOrientation() }.await()
            val listMan = arrayListOf<String>("Complete", "Cancel")
            this@FormListOrientationActivity.runOnUiThread {
                val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        this@FormListOrientationActivity,
                        R.layout.simple_spinner_item,
                        listMan
                )
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                binding.spinSearchStatus.adapter = adapter
                binding.spinSearchStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        searchTrxOrientation()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                if(listTNewHireCheckListEntity.isEmpty()){
                    loadRefreshData(true)
                } else {
                    if(isOnline(this@FormListOrientationActivity)){
                        loadRefreshData(false)
                    } else {
                        Toast.makeText(this@FormListOrientationActivity, "Anda sedang dalam jaringan offline", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun searchTrxOrientation(){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("loading")
        progressDialog.setCancelable(false)
        progressDialog.show()

        binding.rvTrxOrientation.adapter = null

        val sdate = dateTypeConverter.stringToTimestamp(binding.tvStartCreatedDateOrientation.text.toString())
        val edate = dateTypeConverter.stringToTimestamp(binding.tvEndCreatedDateOrientation.text.toString())
        val name = binding.tvSearchNameEmployeeOrientation.text.toString().toLowerCase()
        val officer = binding.tvSearchOfficerOrientation.text.toString().toLowerCase()
        val position = binding.spinSearchStatus.selectedItemPosition

        GlobalScope.launch {
            val listOrientation = GlobalScope.async { viewModel.getTOrientation(sdate, edate, name, officer) }.await()
            runOnUiThread {
            Log.d("JUJU", listOrientation.toString())
                if (!listOrientation.isNullOrEmpty()) {
                    val list = ArrayList<TOrientationEntity>()
                    for (j in listOrientation) {
                        if(position == 0) {
                            if (j.iscancel == 0) {
                                list.add(j)
                            }
                        } else {
                            if (j.iscancel == 1) {
                                list.add(j)
                            }
                        }
                    }
                    formAdapter.setDataOrientation(list)
                    formAdapter.notifyDataSetChanged()

                    with(binding.rvTrxOrientation) {
                        layoutManager = LinearLayoutManager(context)
                        setHasFixedSize(true)
                        adapter = formAdapter
                    }
                }
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLocalDateNow(first : Boolean): String {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val current = LocalDateTime.now()
        val firstDayOfMonth = LocalDate.parse(current.format(formatter), formatter).with(
                TemporalAdjusters.firstDayOfMonth()
        )
        return if(first){
            firstDayOfMonth.format(formatter)
        } else {
            current.format(formatter)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(com.shiro.formhrddover.R.menu.activity_list_plant_patrol, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.shiro.formhrddover.R.id.refresh -> {
                if (isOnline(this)) {
                    AlertDialog.Builder(this)
                            // Judul
                            .setTitle("MESSAGES")
                            // Pesan yang di tampilkan
                            .setMessage("Anda yakin ingin mensyncronisasikan dengan server?")
                            .setPositiveButton("Ya") { _, _ ->
                                GlobalScope.launch {
                                    val listOrientationUnSync = GlobalScope.async { viewModel.getTOrientation(1) }.await()
                                    this@FormListOrientationActivity.runOnUiThread {
                                        if (listOrientationUnSync.isNotEmpty()) {
                                            loadDataSync()
                                        } else {
                                            loadRefreshData(true)
                                        }
                                    }
                                }
                            }
                            .setNegativeButton("Tidak") { _, _ ->
                                Toast.makeText(this, "Anda pilih tidak", Toast.LENGTH_LONG).show()
                            }
                            .show()
                } else {
                    Toast.makeText(this, "Anda sedang offline", Toast.LENGTH_LONG).show()
                }
                true
            }
            R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun patternFormatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
    }

    data class ReturnFromServer(val trxno : String, val status : Boolean)

    override fun onStop() {
        broadcastReceiver?.let { localBroadcastManager.unregisterReceiver(it) }
        broadcastReceiverRefresh?.let { localBroadcastManager.unregisterReceiver(it) }
        super.onStop()
    }

    override fun onDestroy() {
        broadcastReceiver?.let { localBroadcastManager.unregisterReceiver(it) }
        broadcastReceiverRefresh?.let { localBroadcastManager.unregisterReceiver(it) }
        super.onDestroy()
    }

    override fun onPause() {
        broadcastReceiver?.let { localBroadcastManager.unregisterReceiver(it) }
        broadcastReceiverRefresh?.let { localBroadcastManager.unregisterReceiver(it) }
        super.onPause()
    }

//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun isJobRunning(context: Context, jobId: Int): Boolean {
//        var isScheduled = false
//
//        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
//
//        for (jobInfo in scheduler.allPendingJobs) {
//            if (jobInfo.id == jobId) {
//                isScheduled = true
//                break
//            }
//        }
//
//        return isScheduled
//    }

    inner class PushToServer(imageFile: File, format: JSONObject, mTrxIdOld: String) : AsyncTask<String, Void, ReturnFromServer>() {

        private val formata: JSONObject = format
        private val file: File = imageFile
        private val trxIdOld: String = mTrxIdOld
        private val spDataAPI = getSharedPreferences("DATAAPIHRD", MODE_PRIVATE)
        private lateinit var progressDialog: ProgressDialog

        override fun doInBackground(vararg params: String): ReturnFromServer {
            var returnFromServer = ReturnFromServer("", false)
            val api = spDataAPI.getString("APIGLOBAL", "http://192.168.5.254")
            val url = "$api/dovechem/dc_hra/Masters/API?token=ZG9fdXBsb2FkLDIwMjEwNTI0LUFQUDAwMQ"
            //    val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
            //    val url = "$api/dc_hrd/Masters/API?token=ZG9fdXBsb2FkLDIwMjEwNTI0LUFQUDAwMQ"
            AndroidNetworking.initialize(this@FormListOrientationActivity)
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
                    Log.d("KARINA", "SYNC GAGAL")
                }
            }
        }
    }


//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    fun startJobSyncHire() {
//        /*
//       Cek running job terlebih dahulu
//        */
//        if (isJobRunning(this, JOB_ID)) {
//            Toast.makeText(this, "Job Service is already scheduled", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val mServiceComponent = ComponentName(this, SyncOrientationJobScheduler::class.java)
//
//        val builder = JobInfo.Builder(JOB_ID, mServiceComponent)
//
//        /*
//        Kondisi network,
//        NETWORK_TYPE_ANY, berarti tidak ada ketentuan tertentu
//        NETWORK_TYPE_UNMETERED, adalah network yang tidak dibatasi misalnya wifi
//        */
//        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//
//        /*
//        Kondisi device, secara default sudah pada false
//        false, berarti device tidak perlu idle ketika job ke trigger
//        true, berarti device perlu dalam kondisi idle ketika job ke trigger
//        */
//        builder.setRequiresDeviceIdle(false)
//
//        /*
//        Kondisi charging
//        false, berarti device tidak perlu di charge
//        true, berarti device perlu dicharge
//        */
//        builder.setRequiresCharging(false)
//
//        /*
//        Periode interval sampai ke trigger
//        Dalam milisecond, 1000ms = 1detik
//        */
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            builder.setPeriodic(900000) //15 menit
//        } else {
//            builder.setPeriodic(180000) //3 menit
//        }
//
//        val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
//        scheduler.schedule(builder.build())
//        Toast.makeText(this, "Job Service started", Toast.LENGTH_SHORT).show()
//    }

    private fun loadDataSync() {
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
                    isSuccess = if(imageFile.exists()){
                        val res = PushToServer(imageFile, format, data.transactionno).execute().get()
                        res.status
                    } else {
                        false
                    }
                }
                this@FormListOrientationActivity.runOnUiThread {
                    if (isSuccess) {
                        loadRefreshData(true)
                    } else {
                        loadRefreshData(false)
                    }
                }
            }
        }
    }
}