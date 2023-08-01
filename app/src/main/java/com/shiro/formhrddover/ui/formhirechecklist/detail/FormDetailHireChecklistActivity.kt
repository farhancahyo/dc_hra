package com.shiro.formhrddover.ui.formhirechecklist.detail

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.lifecycle.ViewModelProvider
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.shiro.formhrddover.R
import com.shiro.formhrddover.database.DateTypeConverter
import com.shiro.formhrddover.database.entity.hirechecklist.MCategoryEntity
import com.shiro.formhrddover.database.entity.hirechecklist.MDepartementEntity
import com.shiro.formhrddover.database.entity.hirechecklist.TDetailNewHireCheckListEntity
import com.shiro.formhrddover.database.entity.hirechecklist.TNewHireCheckListEntity
import com.shiro.formhrddover.databinding.ActivityFormDetailHireChecklistBinding
import com.shiro.formhrddover.helper.ViewModelFactory
import com.shiro.formhrddover.ui.formhirechecklist.FormHireChecklistViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class FormDetailHireChecklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormDetailHireChecklistBinding
    private lateinit var viewModel: FormHireChecklistViewModel
    private lateinit var dateTypeConverter: DateTypeConverter
    private lateinit var spDataLogin: SharedPreferences
    private lateinit var listDepartement: List<MDepartementEntity>
    private lateinit var listCategory: List<MCategoryEntity>
    private lateinit var adapterCategory: ArrayAdapter<MCategoryEntity>
    private lateinit var tvImageCamera: TextView
    private lateinit var dataHeader: TNewHireCheckListEntity

    companion object {
        private const val JOB_ID = 10
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
        const val EXTRA_NEW_HIRE_CHECKLIST = "extra_new_hire_checklist"
        private var idTrx = "TRXXX-00001"
        private var netsuite: Int = 0
        private var username: String = "Name"
        private var categoryid: Int = 0
        var lastClickTime: Long = 0
        const val DOUBLE_CLICK_TIME_DELTA: Long = 100
        val listStatus = mapOf(1 to "Un-Sync", 2 to "Sync")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormDetailHireChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Form Detail New Hire Check List"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        dateTypeConverter = DateTypeConverter()

        // Data Login
        spDataLogin = getSharedPreferences(
                "DATALOGIN",
                MODE_PRIVATE
        )
        netsuite = spDataLogin.getInt("IDNETSUITE", 0)
        username = spDataLogin.getString("USERNAME", "").toString()
        idTrx = intent.getStringExtra(EXTRA_TRANSACTION_ID).toString()
        dataHeader = intent.getParcelableExtra(EXTRA_NEW_HIRE_CHECKLIST)!!
        val factory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, factory)[FormHireChecklistViewModel::class.java]

        GlobalScope.launch {
            listCategory = GlobalScope.async { viewModel.getMCategory() }.await()
//            adapterCategory = ArrayAdapter<MCategoryEntity>(this@FormDetailHireChecklistActivity, android.R.layout.simple_spinner_item, listCategory)
//            adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this@FormDetailHireChecklistActivity.runOnUiThread {
                binding.edtMemoNewHireChecklist.setText(dataHeader.memo)
//                binding.spinCategoryHire.adapter = adapterCategory
                loadViewItemCategory()
            }
//            // Proses get dari spiner
//            binding.spinCategoryHire.onItemSelectedListener =
//                    object : AdapterView.OnItemSelectedListener {
//                        override fun onItemSelected(
//                                parent: AdapterView<*>?,
//                                view: View?,
//                                position: Int,
//                                id: Long
//                        ) {
//                            val selectedItem = binding.spinCategoryHire.selectedItem.toString()
//                            val strArray = selectedItem.split("-").toTypedArray()
//                            categoryid = strArray[0].toInt()
//
//                        }
//
//                        override fun onNothingSelected(parent: AdapterView<*>?) {}
//                    }
        }
    }

//    private fun loadViewItemCategory() {
//        val progressDialog = ProgressDialog(this@FormDetailHireChecklistActivity)
//        progressDialog.setMessage("LOADING")
//        progressDialog.setCancelable(false)
//        progressDialog.show()
//        binding.llayoutItemHire.removeAllViews()
//        GlobalScope.launch {
//            val listMItemUraianCheckList = GlobalScope.async { viewModel.getItemUraianChecklist(categoryid) }.await()
//            val listdetail = GlobalScope.async { viewModel.getTDetailNewHireChecklist(idTrx, categoryid) }.await()
//            if (listMItemUraianCheckList.isNotEmpty()) {
//                for (item in listMItemUraianCheckList) {
//                    this@FormDetailHireChecklistActivity.runOnUiThread {
//                        // Conf Layout
//                        val row = LinearLayout(this@FormDetailHireChecklistActivity)
//                        row.id = item.mappingid
//                        row.orientation = LinearLayout.VERTICAL
//                        val viewGroup = LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                LinearLayout.LayoutParams.WRAP_CONTENT
//                        )
//                        viewGroup.setMargins(0, 0, 0, 32)
//                        row.layoutParams = viewGroup
//                        row.setPadding(16, 16, 16, 16)
//                        row.background = ContextCompat.getDrawable(
//                                this@FormDetailHireChecklistActivity,
//                                R.drawable.rect
//                        )
//
//                        //
//                        val chkList = CheckBox(this@FormDetailHireChecklistActivity)
//                        chkList.layoutParams = LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                1.0f
//                        )
//                        chkList.isChecked = false
//                        for (itemlow in listdetail) {
//                            if (itemlow.mappingidcategory == item.mappingid) {
//                                if (itemlow.valuecheck == 1) {
//                                    chkList.isChecked = true
//                                }
//                            }
//                        }
//                        chkList.textSize = 24.0f
//                        chkList.layoutDirection = View.LAYOUT_DIRECTION_RTL
//                        chkList.text = item.itemname
//                        chkList.gravity = Gravity.CENTER_VERTICAL
//
//                        row.addView(chkList)
//
//                        binding.llayoutItemHire.addView(row)
//                    }
//                }
//            }
//
//            this@FormDetailHireChecklistActivity.runOnUiThread {
//                if (progressDialog.isShowing) {
//                    progressDialog.dismiss()
//                }
//            }
//        }
//    }

    private fun loadViewItemCategory() {
        val progressDialog = ProgressDialog(this@FormDetailHireChecklistActivity)
        progressDialog.setMessage("LOADING")
        progressDialog.setCancelable(false)
        progressDialog.show()
        binding.llayoutItemHire.removeAllViews()
        GlobalScope.launch {
            if (listCategory.isNotEmpty()) {
                for (item in listCategory) {
//                    this@FormDetailHireChecklistActivity.runOnUiThread {
                    // Conf Layout
                    val row = LinearLayout(this@FormDetailHireChecklistActivity)
                    row.id = item.internalid
                    row.orientation = LinearLayout.VERTICAL
                    val viewGroup = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    viewGroup.setMargins(0, 0, 0, 16)
                    row.layoutParams = viewGroup
                    row.setPadding(16, 16, 16, 16)

                    val linevg = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            3
                    )
                    linevg.setMargins(0, 8, 0, 0)
                    val lineview = View(this@FormDetailHireChecklistActivity)
                    lineview.layoutParams = linevg
                    lineview.setBackgroundColor(Color.parseColor("#000000"))

                    // Conf Layout
                    val rowUraian = LinearLayout(this@FormDetailHireChecklistActivity)
                    rowUraian.orientation = LinearLayout.VERTICAL
                    rowUraian.layoutParams = viewGroup
                    rowUraian.setPadding(16, 16, 16, 16)

                    //
                    val txtTitle = TextView(this@FormDetailHireChecklistActivity)
                    txtTitle.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1.0f
                    )
                    txtTitle.text = item.categoryname
                    txtTitle.textSize = 24.0f
                    txtTitle.typeface = Typeface.DEFAULT_BOLD;
                    txtTitle.gravity = Gravity.CENTER_VERTICAL
                    Log.d("MEN", item.internalid.toString())
//                        GlobalScope.launch {
                    val listMItemUraianCheckList = GlobalScope.async { viewModel.getItemUraianChecklist(item.internalid) }.await()
                    val listdetail = GlobalScope.async { viewModel.getTDetailNewHireChecklist(idTrx, item.internalid) }.await()
                    if (listMItemUraianCheckList.isNotEmpty()) {
                        for (data in listMItemUraianCheckList) {
                            this@FormDetailHireChecklistActivity.runOnUiThread {
                                // Conf Layout
                                val rowDetail = LinearLayout(this@FormDetailHireChecklistActivity)
                                rowDetail.id = data.mappingid
                                rowDetail.orientation = LinearLayout.VERTICAL
                                rowDetail.layoutParams = viewGroup
                                rowDetail.setPadding(16, 16, 16, 16)
                                rowDetail.background = ContextCompat.getDrawable(
                                        this@FormDetailHireChecklistActivity,
                                        R.drawable.rect
                                )

                                val chkList = CheckBox(this@FormDetailHireChecklistActivity)
                                chkList.layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        1.0f
                                )
                                chkList.isChecked = false
                                for (itemlow in listdetail) {
                                    if (itemlow.mappingidcategory == data.mappingid) {
                                        if (itemlow.valuecheck == 1) {
                                            chkList.isChecked = true
                                        }
                                    }
                                }
                                chkList.textSize = 24.0f
                                chkList.layoutDirection = View.LAYOUT_DIRECTION_RTL
                                chkList.text = data.itemname
                                chkList.gravity = Gravity.CENTER_VERTICAL

                                rowDetail.addView(chkList)
                                rowUraian.addView(rowDetail)
                            }
                        }
                    }
                    this@FormDetailHireChecklistActivity.runOnUiThread {
                        row.addView(txtTitle)
                        row.addView(lineview)
                        row.addView(rowUraian)
                        binding.llayoutItemHire.addView(row)
                    }
//                        }
//                    }
                }
            }
            this@FormDetailHireChecklistActivity.runOnUiThread {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_detail_plant_patrol, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.check -> {
                AlertDialog.Builder(this)
                        // Judul
                        .setTitle("ALERT DIALOG")
                        .setCancelable(false)
                        // Pesan yang di tampilkan
                        .setMessage(
                                Html.fromHtml(
                                        "<b>$username</b> , anda yakin ingin simpan ini. sudahkah cek dengan seksama?",
                                        Html.FROM_HTML_MODE_LEGACY
                                )
                        )
                        .setPositiveButton("Iya") { _, _ ->
                            postData()
                        }
                        .setNegativeButton("Tidak") { _, _ ->
                            toastCancel()
                        }
                        .show()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toastCancel() {
        Toast.makeText(application, "Anda memilih tombol tidak", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun postData() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("LOADING")
        progressDialog.setCancelable(false)
        progressDialog.show()
        GlobalScope.launch {
            dataHeader.memo = this@FormDetailHireChecklistActivity.binding.edtMemoNewHireChecklist.text.toString()

            val listNewHire = GlobalScope.async { viewModel.getTNewHireChecklist(dataHeader.transactionno) }.await()
            if (listNewHire == null) {
                val listNewHireEmployee = GlobalScope.async { viewModel.getTNewHireChecklistEmployee(dataHeader.employeeno) }.await()
                if (listNewHireEmployee.isNotEmpty()) {
                    runOnUiThread {
                        if(progressDialog.isShowing){
                            progressDialog.dismiss()
                        }
                        Toast.makeText(this@FormDetailHireChecklistActivity, "Employee number is already", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                GlobalScope.async { viewModel.insertTNewHireChecklist(dataHeader) }.await()
            } else {
                GlobalScope.async { viewModel.updateTNewHireChecklist(dataHeader) }.await()
            }

            val listdetail = GlobalScope.async { viewModel.getTDetailNewHireChecklist(idTrx) }.await()
            if (listdetail.isNotEmpty()) {
                for (detail in listdetail) {
                    GlobalScope.async { viewModel.deleteTDetailNewHireChecklist(detail) }.await()
                }
            }
            binding.llayoutItemHire.forEach { row ->
                val rowFull = row as LinearLayout
                val idCategory = rowFull.id

                val rowUraian = rowFull.getChildAt(2) as LinearLayout
                rowUraian.forEach { rowDetail ->
                    val rowD = rowDetail as LinearLayout

                    val mappingId = rowD.id
                    val chkBox = rowD.getChildAt(0) as CheckBox
                    var valueCheck = 0
                    if (chkBox.isChecked) {
                        valueCheck = 1
                    }
                    val data = TDetailNewHireCheckListEntity(
                            0,
                            idTrx,
                            idCategory,
                            mappingId,
                            valueCheck,
                            netsuite,
                            username,
                            1,
                            Date(),
                            netsuite,
                            username,
                            Date(),
                            netsuite,
                            username
                    )
                    GlobalScope.async { viewModel.insertTDetailNewHireChecklist(data) }.await()
                }
//                val mappingId = rowFull.id
//                val chkBox = rowFull.getChildAt(0) as CheckBox
//                var valuecheck = 0
//                if (chkBox.isChecked) {
//                    valuecheck = 1
//                }
//                val data = TDetailNewHireCheckListEntity(
//                        0,
//                        idTrx,
//                        categoryid,
//                        mappingId,
//                        valuecheck,
//                        netsuite,
//                        username,
//                        1,
//                        Date(),
//                        netsuite,
//                        username,
//                        Date(),
//                        netsuite,
//                        username
//                )
//                GlobalScope.async { viewModel.insertTDetailNewHireChecklist(data) }.await()
            }
            this@FormDetailHireChecklistActivity.runOnUiThread {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
                finish()
            }
//            if(isOnline(this@FormDetailHireChecklistActivity)){
//                GlobalScope.launch {
//                    val dataHeader = GlobalScope.async { viewModel.getTNewHireChecklist(idTrx) }.await()
//                    val header = JSONObject()
//                    header.put("transaction_no", dataHeader.transactionno)
//                    header.put("employee_name", dataHeader.employeename)
//                    header.put("departement_id", dataHeader.departementid)
//                    header.put("title_name", dataHeader.titlename)
//                    header.put("joint_date", SimpleDateFormat("yyyy/MM/dd").format(dataHeader.jointdate))
//                    header.put("employee_no", dataHeader.employeeno)
//                    header.put("status", dataHeader.status)
//                    header.put("createddate", patternFormatDate(dataHeader.createddate))
//                    header.put("createdby", dataHeader.createdby)
//                    header.put("createdname", dataHeader.createdname)
//                    header.put(
//                            "lastmodifieddate",
//                            patternFormatDate(dataHeader.lastmodifieddate)
//                    )
//                    header.put("lastmodifiedby", dataHeader.lastmodifiedby)
//                    header.put("lastmodifiedname", dataHeader.lastmodifiedname)
//
//                    val detail = JSONArray()
//                    val listDetail = GlobalScope.async { viewModel.getTDetailNewHireChecklist(idTrx) }.await()
//                    for (data in listDetail) {
//                        val obj = JSONObject()
//                        obj.put("internal_id", data.internalid)
//                        obj.put("transaction_no", data.transactionno)
//                        obj.put("category_id", data.categoryid)
//                        obj.put("mapping_id_category", data.mappingidcategory)
//                        obj.put("value_check", data.valuecheck)
//                        obj.put("netsuite_id_operator", data.netsuiteidoperator)
//                        obj.put("operator_name", data.operatorname)
//                        obj.put("status", data.status)
//                        obj.put("createddate",
//                                patternFormatDate(data.createddate)
//                        )
//                        obj.put("createdby", data.createdby)
//                        obj.put("createdname", data.createdname)
//                        obj.put(
//                                "lastmodifieddate",
//                                patternFormatDate(data.lastmodifieddate)
//                        )
//                        obj.put("lastmodifiedby", data.lastmodifiedby)
//                        obj.put("lastmodifiedname", data.lastmodifiedname)
//                        detail.put(obj)
//                    }
//
//                    val format = JSONObject()
//                    format.put("header", header)
//                    format.put("detail", detail)
//                    Log.d("GSONSON2FORMAT", format.toString())
//
//                    SyncToServer(format, dataHeader.transactionno).execute()
//                }
//            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
        return false
    }

    fun patternFormatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
    }

    data class ReturnFromServer(val trxno: String, val status: Boolean, val idsrv: Int)

    inner class SyncToServer(format: JSONObject, mTrxIdOld: String) : AsyncTask<String, Void, ReturnFromServer>() {

        private val formata: JSONObject = format
        private val spDataAPI = getSharedPreferences("DATAAPIHRD", MODE_PRIVATE)
        private val trxIdOld = mTrxIdOld
        private lateinit var progressDialog: ProgressDialog

        override fun doInBackground(vararg params: String): ReturnFromServer {
            var returnFromServer = ReturnFromServer("", false, 0)
            val api = spDataAPI.getString("APIGLOBAL", "http://192.168.5.254")
            val url = "$api/dovechem/dc_hra/Masters/API?token=c3luY0hpcmVDaGVja0xpc3QsMjAyMTA1MjQtQVBQMDAx"
            AndroidNetworking.initialize(this@FormDetailHireChecklistActivity)

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
                    Handler(Looper.getMainLooper()).post {
                        alertDialog(this@FormDetailHireChecklistActivity)
                    }
                }
            } else {
                val error: ANError = response.error
                Log.d("PEGGIESPATROLIERROR", error.message.toString())
                // Handle Error
                Handler(Looper.getMainLooper()).post {
                    alertDialog(this@FormDetailHireChecklistActivity)
                }
            }
            return returnFromServer
        }

        override fun onPreExecute() {
            super.onPreExecute()
            Handler(Looper.getMainLooper()).post {
                progressDialog = ProgressDialog(this@FormDetailHireChecklistActivity)
                progressDialog.setMessage("loading")
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
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

                    this@FormDetailHireChecklistActivity.runOnUiThread {
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                        idTrx = returnFromServer.trxno
                        loadViewItemCategory()
                        val builder = AlertDialog.Builder(this@FormDetailHireChecklistActivity)
                        builder.setTitle("Success in push")
                        builder.setMessage("Your previous transaction number is $trxIdOld changed to ${returnFromServer.trxno} .According to the order from server")
                        builder.setPositiveButton("Okay") { dialog, which -> }
                        builder.show()
                    }
                }
            } else {
                alertDialog(this@FormDetailHireChecklistActivity)
                this@FormDetailHireChecklistActivity.runOnUiThread {
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                }
            }
        }

        fun alertDialog(context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Alert Messages")
            builder.setMessage("Connection Timeout. Failed to contact the server. Please contact your administrator.")
            builder.setPositiveButton("Okay") { dialog, which ->
            }
            builder.show()
        }
    }
}