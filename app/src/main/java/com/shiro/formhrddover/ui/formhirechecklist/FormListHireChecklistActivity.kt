package com.shiro.formhrddover.ui.formhirechecklist

import android.R
import android.app.*
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobService
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
import com.androidnetworking.error.ANError
import com.shiro.formhrddover.database.DateTypeConverter
import com.shiro.formhrddover.database.entity.hirechecklist.TNewHireCheckListEntity
import com.shiro.formhrddover.databinding.ActivityFormListHireChecklistBinding
import com.shiro.formhrddover.helper.ViewModelFactory
import com.shiro.formhrddover.ui.formhirechecklist.header.FormHeaderHireChecklistActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

class FormListHireChecklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormListHireChecklistBinding
    private lateinit var viewModel: FormHireChecklistViewModel
    private lateinit var dateTypeConverter: DateTypeConverter
    private lateinit var formAdapter: FormHireChecklistAdapter
    private lateinit var localBroadcastManager: LocalBroadcastManager

    companion object{
        private var broadcastReceiver: BroadcastReceiver? = null
        private var broadcastReceiverRefresh: BroadcastReceiver? = null
        val DATA_SAVED_BROADCAST: String = "com.example.formparaformdover.RAFATHAR"
        val DATA_SAVED_BROADCAST_REFRESH: String = "com.example.formparaformdover.TOKYO"
        private const val JOB_ID = 10
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                this@FormListHireChecklistActivity.runOnUiThread {
                    loadRefreshData(true)
                }
            }
        }
        broadcastReceiverRefresh = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                this@FormListHireChecklistActivity.runOnUiThread {
                    searchTrxHireChecklist()
                }
            }
        }

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Form List New Hire Check List"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)

        dateTypeConverter = DateTypeConverter()

        binding = ActivityFormListHireChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, factory)[FormHireChecklistViewModel::class.java]

        binding.tvFromDateHire.text = getLocalDateNow(true)
        binding.tvToDateHire.text = getLocalDateNow(false)
        binding.btnFormDateHire.setOnClickListener { dialogDate("from") }
        binding.btnToDateHire.setOnClickListener { dialogDate("to") }

        binding.fabAddInspection.setOnClickListener {
//            if (isJobRunning(this, JOB_ID)) {
//                val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
//                scheduler.cancel(JOB_ID)
//                Toast.makeText(this, "Job Service canceled", Toast.LENGTH_SHORT).show()
//            }
            val intent = Intent(this, FormHeaderHireChecklistActivity::class.java)
            intent.putExtra(FormHeaderHireChecklistActivity.TYPE_INTENT, "INSERT")
            startActivity(intent)
        }

        formAdapter = FormHireChecklistAdapter(object : FormHireChecklistAdapter.ButtonHireListChecklistListener {
            override fun btnViewListener(idtrx: String, iscancel: Int) {
//                if (isJobRunning(this@FormListHireChecklistActivity, JOB_ID)) {
//                    val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
//                    scheduler.cancel(JOB_ID)
//                    Toast.makeText(this@FormListHireChecklistActivity, "Job Service canceled", Toast.LENGTH_SHORT).show()
//                }
                val intent = Intent(this@FormListHireChecklistActivity, FormHeaderHireChecklistActivity::class.java)
                intent.putExtra(FormHeaderHireChecklistActivity.TYPE_INTENT, "UPDATE")
                intent.putExtra(FormHeaderHireChecklistActivity.EXTRA_TRANSACTION_ID, idtrx)
                intent.putExtra(FormHeaderHireChecklistActivity.EXTRA_IS_CANCEL, iscancel)
                startActivity(intent)
            }
        })

        binding.tvSearchNameEmployeeHire.addTextChangedListener{ searchTrxHireChecklist() }
        binding.tvSearchNpkEmployeeHire.addTextChangedListener{ searchTrxHireChecklist() }
    }

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

    private fun loadRefreshData(full : Boolean){
        viewModel.refreshCuy(this, full)
    }

    private fun dialogDate(type : String) {
        val day: Int
        val month: Int
        val year: Int
        if(type == "to"){
            day = binding.tvToDateHire.text.substring(0..1).toInt()
            month = binding.tvToDateHire.text.substring(3..4).toInt() - 1
            year = binding.tvToDateHire.text.substring(6..9).toInt()
        } else {
            day = binding.tvFromDateHire.text.substring(0..1).toInt()
            month = binding.tvFromDateHire.text.substring(3..4).toInt() - 1
            year = binding.tvFromDateHire.text.substring(6..9).toInt()
        }

        val dpd = DatePickerDialog(
                this,
                { view, year, monthOfYear, dayOfMonth ->
                    // Display Selected date in textbox
                    val month = monthOfYear + 1
                    var monthS = month.toString()
                    if (monthS.length == 1) {
                        monthS = "0$monthS";
                    }
                    var day = dayOfMonth.toString()
                    if (day.length == 1) {
                        day = "0$day";
                    }
                    if(type == "to"){
                        binding.tvToDateHire.text = "$day-$monthS-$year"
                    } else {
                        binding.tvFromDateHire.text = "$day-$monthS-$year"
                    }
                    searchTrxHireChecklist()
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
                    it, IntentFilter(
                    DATA_SAVED_BROADCAST_REFRESH
            )
            )
        }

        GlobalScope.launch {
            // Baca data kosong jika tidak ada data transaksi sama sekali di device
            val listTNewHireCheckListEntity = GlobalScope.async { viewModel.getTNewHireChecklist() }.await()
            val listMan = arrayListOf<String>("Complete", "Cancel")
            this@FormListHireChecklistActivity.runOnUiThread {
                val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        this@FormListHireChecklistActivity,
                        R.layout.simple_spinner_item,
                        listMan
                )
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                binding.spinSearchStatus.adapter = adapter
                binding.spinSearchStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        searchTrxHireChecklist()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                if(listTNewHireCheckListEntity.isEmpty()){
                    loadRefreshData(true)
                } else {
                    if(isOnline(this@FormListHireChecklistActivity)){
                        loadRefreshData(false)
                    } else {
                        Toast.makeText(this@FormListHireChecklistActivity, "Anda sedang dalam jaringan offline", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun searchTrxHireChecklist(){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("loading")
        progressDialog.setCancelable(false)
        progressDialog.show()

        binding.rvTrxInspection.adapter = null

        val sdate = dateTypeConverter.stringToTimestamp(binding.tvFromDateHire.text.toString())
        val edate = dateTypeConverter.stringToTimestamp(binding.tvToDateHire.text.toString())
        val name = binding.tvSearchNameEmployeeHire.text.toString()
        val no = binding.tvSearchNpkEmployeeHire.text.toString()
        val position = binding.spinSearchStatus.selectedItemPosition

        GlobalScope.launch {
            val listHire = GlobalScope.async { viewModel.getTNewHireChecklist(sdate, edate, name, no) }.await()
            runOnUiThread {
                if (!listHire.isNullOrEmpty()) {
                    val list = ArrayList<TNewHireCheckListEntity>()
                    for (j in listHire) {
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

                    formAdapter.setDataInspection(list)
                    formAdapter.notifyDataSetChanged()

                    with(binding.rvTrxInspection) {
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
//        val mServiceComponent = ComponentName(this, SyncHireCheckListJobScheduler::class.java)
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
                                    val listHireUnSync = GlobalScope.async { viewModel.getTNewHireChecklist(1) }.await()
                                    this@FormListHireChecklistActivity.runOnUiThread {
                                        if (listHireUnSync.isNotEmpty()) {
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

    private fun loadDataSync() {
        GlobalScope.launch {
            val listHireUnSync = GlobalScope.async { viewModel.getTNewHireChecklist(1) }.await()
            if (listHireUnSync.isNotEmpty()) {
                var isSuccess = false
                for (data in listHireUnSync) {
                    val dataHeader = GlobalScope.async { viewModel.getTNewHireChecklist(data.transactionno) }.await()
                    val header = JSONObject()
                    header.put("transaction_no", dataHeader.transactionno)
                    header.put("employee_name", dataHeader.employeename)
                    header.put("departement_id", dataHeader.departementid)
                    header.put("title_name", dataHeader.titlename)
                    header.put("joint_date", SimpleDateFormat("yyyy/MM/dd").format(dataHeader.jointdate))
                    header.put("employee_no", dataHeader.employeeno)
                    header.put("memo", dataHeader.memo)
                    header.put("status", dataHeader.status)
                    header.put("is_cancel", dataHeader.iscancel)
                    header.put("createddate", patternFormatDate(dataHeader.createddate))
                    header.put("createdby", dataHeader.createdby)
                    header.put("createdname", dataHeader.createdname)
                    header.put("lastmodifieddate", patternFormatDate(dataHeader.lastmodifieddate))
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

                    val name = JSONObject()
                    name.put("employee_name", dataHeader.employeename)
                    name.put("status", dataHeader.status)
                    name.put("createddate", patternFormatDate(dataHeader.createddate))
                    name.put("createdby", dataHeader.createdby)
                    name.put("createdname", dataHeader.createdname)

                    val format = JSONObject()
                    format.put("header", header)
                    format.put("detail", detail)
                    format.put("name", name)
                    Log.d("GSONSON2FORMAT", format.toString())

                    val res = SyncToHello(format, data.transactionno).execute().get()
                    if (res.status) {
                        isSuccess = true
                    }
                }
                this@FormListHireChecklistActivity.runOnUiThread {
                    if (isSuccess) {
                        loadRefreshData(true)
                    } else {
                        loadRefreshData(false)
                    }
                }
            }
        }
    }

    private fun patternFormatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
    }

    inner class SyncToHello(format: JSONObject, mTrxIdOld: String) : AsyncTask<String, Void, ReturnFromServer>() {

        private val formata: JSONObject = format
        private val spDataAPI = getSharedPreferences("DATAAPIHRD", JobService.MODE_PRIVATE)
        private val trxIdOld = mTrxIdOld

        override fun doInBackground(vararg params: String): ReturnFromServer {
            var returnFromServer = ReturnFromServer("", false, 0)
//            val api = spDataAPI.getString("APIGLOBAL", "http://192.168.5.254")
//            val url = "$api/dovechem/dc_hra/Masters/API?token=c3luY0hpcmVDaGVja0xpc3QsMjAyMTAzMTgtQVBQMDAx"
            val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
            val url = "$api/dc_hrd/Masters/API?token=c3luY0hpcmVDaGVja0xpc3QsMjAyMTAzMTgtQVBQMDAx"
            AndroidNetworking.initialize(this@FormListHireChecklistActivity)

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
                Log.d("KARINA", "SYNC GAGAL")
            }
        }
    }

    data class ReturnFromServer(val trxno : String, val status : Boolean, val idsrv : Int)
}