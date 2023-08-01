package com.shiro.formhrddover.ui.formorientationemployee.header

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
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
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANRequest
import com.bumptech.glide.Glide
import com.shiro.formhrddover.R
import com.shiro.formhrddover.database.DateTypeConverter
import com.shiro.formhrddover.database.entity.hirechecklist.MDepartementEntity
import com.shiro.formhrddover.database.entity.orientation.TDetailOrientationEntity
import com.shiro.formhrddover.database.entity.orientation.TOrientationEntity
import com.shiro.formhrddover.databinding.ActivityFormHeaderOrientationBinding
import com.shiro.formhrddover.helper.ViewModelFactory
import com.shiro.formhrddover.ui.formhirechecklist.FormHireChecklistViewModel
import com.shiro.formhrddover.ui.formorientationemployee.FormOrientationViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FormHeaderOrientationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormHeaderOrientationBinding
    private lateinit var viewModel: FormOrientationViewModel
    private lateinit var viewModelHire: FormHireChecklistViewModel
    private lateinit var dateTypeConverter: DateTypeConverter
    private lateinit var spDataLogin: SharedPreferences
    private lateinit var listDepartement: List<MDepartementEntity>
    private lateinit var adapterDepartement: ArrayAdapter<MDepartementEntity>

    companion object {
        private const val JOB_ID = 12
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
        const val EXTRA_IS_CANCEL = "extra_is_cancel"
        private var idTrx = "TRXXX-00001"
        const val TYPE_INTENT = "type_intent"
        private var netsuite: Int = 0
        private var username: String = "Name"
        private var pathSignature = ""
        private var pathSignatureServer = ""
        var lastClickTime: Long = 0
        const val DOUBLE_CLICK_TIME_DELTA: Long = 100
        val listStatus = mapOf(0 to "Completed", 1 to "Cancel")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormHeaderOrientationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Form Detail Orientation"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)

        // Data Login
        spDataLogin = getSharedPreferences("DATALOGIN", MODE_PRIVATE)
        netsuite = spDataLogin.getInt("IDNETSUITE", 0)
        username = spDataLogin.getString("USERNAME", "").toString()
        idTrx = intent.getStringExtra(EXTRA_TRANSACTION_ID).toString()

        val factory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, factory)[FormOrientationViewModel::class.java]
        viewModelHire = ViewModelProvider(this, factory)[FormHireChecklistViewModel::class.java]

        dateTypeConverter = DateTypeConverter()

        // Spinner Product
        GlobalScope.launch {
            listDepartement = GlobalScope.async { viewModelHire.getMDepartement() }.await()

            this@FormHeaderOrientationActivity.runOnUiThread {
                adapterDepartement = ArrayAdapter<MDepartementEntity>(this@FormHeaderOrientationActivity, android.R.layout.simple_spinner_item, listDepartement)
                adapterDepartement.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinDivisionOrientation.adapter = adapterDepartement
                loadViewItemCategory()
            }

            if (intent.getStringExtra(TYPE_INTENT) == "UPDATE") {
                val idTrx = intent.getStringExtra(EXTRA_TRANSACTION_ID)

                if (idTrx != null) {
                    this@FormHeaderOrientationActivity.runOnUiThread {
                        getTOrientationById(idTrx)
                    }
                }
            } else {
                this@FormHeaderOrientationActivity.runOnUiThread {
                    binding.ll1.visibility = View.GONE
                    binding.llOperatorOrientation.visibility = View.GONE
                    binding.tvCreatedDateOrientation.text = getLocalDateNow()
                    binding.tvStartedDateOrientationInput.text = getLocalDateNow()
                }

                GlobalScope.launch {
                    val listTrx = GlobalScope.async { viewModel.getTOrientationOffline() }.await()
                    if (listTrx.isNotEmpty()) {
                        val lasttrx = listTrx[0].transactionno.substring(6, 11).toInt() + 1
                        binding.tvIdTrxHeaderHire.text = String.format("YTRX1-%05d", lasttrx)
                    } else {
                        binding.tvIdTrxHeaderHire.text = String.format("YTRX1-%05d", 1)
                    }
                }
            }
        }

//        binding.edtNameEmployeeOrientation.addTextChangedListener {
//            if(it.isNullOrEmpty()){
//                binding.edtNumberEmployeeOrientation.setText("")
////                binding.lstNameEmployeeOrientation.adapter = null
////                binding.lstNameEmployeeOrientation.visibility = View.GONE
//            } else {
//                GlobalScope.launch {
//                    val employeName = GlobalScope.async { viewModel.getNewHireChecklistSearch(it.toString()) }.await()
//                    runOnUiThread {
//                        if (employeName.isNotEmpty()) {
//                            if(employeName[0].employeename == binding.edtNameEmployeeOrientation.text.toString()){
//                                goneListviewName()
//                            } else {
////                                val adapter = ArrayAdapter(this@FormHeaderOrientationActivity, android.R.layout.simple_list_item_1, employeName)
////                                binding.lstNameEmployeeOrientation.adapter = adapter
////                                binding.lstNameEmployeeOrientation.visibility = View.VISIBLE
////                                binding.lstNameEmployeeOrientation.setOnItemClickListener { parent, _, position, _ ->
////                                    val s: String = parent.getItemAtPosition(position).toString()
////                                    if(s == ""){
////                                        Toast.makeText(this@FormHeaderOrientationActivity, "Teks kosong", Toast.LENGTH_SHORT).show()
////                                    } else {
////                                        binding.edtNumberEmployeeOrientation.setText(s.split("-").toTypedArray()[0])
////                                        binding.edtNameEmployeeOrientation.setText(s.split("-").toTypedArray()[1])
////                                        if (s.split("-").toTypedArray()[2].toIntOrNull() != null) {
////                                            for (l in listDepartement) {
////                                                if (s.split("-").toTypedArray()[2].toInt() == l.internalid) {
////                                                    val position = adapterDepartement.getPosition(l)
////                                                    binding.spinDivisionOrientation.setSelection(position)
////                                                }
////                                            }
////                                        }
////                                    }
//                                    goneListviewName()
//                                }
//                            }
//
//                        } else {
//                            goneListviewName()
//                        }
//                    }
//                }
//            }
//        }

        binding.btnStartedDateOrientationInput.setOnClickListener { dialogDate() }
        binding.imgviewSignatureOrientation.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Choose your want")
            val image = arrayOf("View Signature", "Create Signature")
            builder.setItems(image) { dialog, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(
                                this,
                                ImageZoomActivity::class.java
                        )
                        val imageFile = File(pathSignature)
                        if(imageFile.exists()){
                            intent.putExtra(
                                    ImageZoomActivity.IMAGE_PATH,
                                    pathSignature
                            )
                        } else {
                            intent.putExtra(
                                    ImageZoomActivity.IMAGE_PATH,
                                    pathSignatureServer
                            )
                        }
                        startActivity(intent)
                    }
                    1 -> {
                        startActivityForResult(Intent(this, SignatureViewActivity::class.java), 101);
                    }
                }
            }
            val dialog = builder.create()
            dialog.show()

        }

        binding.btnSaveOrientation.setOnClickListener {
            AlertDialog.Builder(this)
                    // Judul
                    .setTitle("ALERT DIALOG")
                    .setCancelable(false)
                    // Pesan yang di tampilkan
                    .setMessage(
                            Html.fromHtml(
                                    "<b>${username}</b> , anda yakin ingin simpan ini. sudahkah cek dengan seksama?",
                                    Html.FROM_HTML_MODE_LEGACY
                            )
                    )
                    .setPositiveButton("Iya") { _, _ ->
                        this.runOnUiThread {
                            postData()
                        }
                    }
                    .setNegativeButton("Tidak") { _, _ ->
                        Toast.makeText(this, "Memilih tidak", Toast.LENGTH_SHORT).show()
                    }
                    .show()
        }
    }

//    private fun goneListviewName(){
//        binding.lstNameEmployeeOrientation.adapter = null
//        binding.lstNameEmployeeOrientation.visibility = View.GONE
//    }

    private fun getTOrientationById(idTrx: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("loading")
        progressDialog.setCancelable(false)
        progressDialog.show()

        GlobalScope.launch {
            val data = GlobalScope.async { viewModel.getTOrientation(idTrx) }.await()
            if (data != null) {
                this@FormHeaderOrientationActivity.runOnUiThread {
                    binding.tvIdTrxHeaderHire.text = data.transactionno
                    binding.edtNumberEmployeeOrientation.setText("${data.employeeno}")
                    binding.edtNameEmployeeOrientation.setText(data.employeename)
                    binding.edtTitleEmployeeOrientation.setText(data.titlename)
                    binding.tvStartedDateOrientationInput.text = SimpleDateFormat("dd-MM-yyyy").format(data.starteddate).toString()
                    binding.tvCreatedByOrientation.text = data.lastmodifiedname
                    binding.tvCreatedDateOrientation.text = SimpleDateFormat("dd-MM-yyyy").format(data.createddate).toString()
                    if (data.departementid != 0) {
                        for (l in listDepartement) {
                            if (data.departementid == l.internalid) {
                                val position = adapterDepartement.getPosition(l)
                                binding.spinDivisionOrientation.setSelection(position)
                            }
                        }
                    }
                    for (status in listStatus) {
                        if (data.iscancel == status.key) {
                            binding.tvStatusHeaderHire.text = status.value
                        }
                    }
                    val takenImage = BitmapFactory.decodeFile(data.localsignfilename)
                    if (takenImage != null) {
                        binding.imgviewSignatureOrientation.setImageBitmap(takenImage)
                        pathSignature = data.localsignfilename
                    } else {
                        val strArray = data.localsignfilename.split("/").toTypedArray()
                        val lastNo = strArray.size - 1
                        val deleteLast = strArray.dropLast(1)
                        val path = deleteLast.joinToString("/")
                        val imageName = strArray[lastNo]
//                                    val imagePath = no.localfilename
                        val imageUrl = data.serversignfilename
                        DownloadFromServerImage(imageUrl, imageName, path).execute()

                        // Get Image From url
                        Glide.with(this@FormHeaderOrientationActivity)
                                .load(data.serversignfilename)
                                .placeholder(R.drawable.ic_baseline_broken_image_24)
                                .into(binding.imgviewSignatureOrientation)
                        pathSignatureServer = data.serversignfilename
                    }
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun loadViewItemCategory() {

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("LOADING")
        progressDialog.setCancelable(false)
        progressDialog.show()
        binding.llayoutUraianOrientation.removeAllViews()
        GlobalScope.launch {
            val listMUraian = GlobalScope.async { viewModel.getMUraian() }.await()
            val listDetail = GlobalScope.async { viewModel.getTDetailOrientation(idTrx) }.await()
            if (listMUraian.isNotEmpty()) {
                for (item in listMUraian) {
                    // Conf Layout
                    val row = LinearLayout(this@FormHeaderOrientationActivity)
                    row.id = item.internalid
                    row.orientation = LinearLayout.VERTICAL
                    val viewGroup = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    viewGroup.setMargins(0, 0, 0, 32)
                    row.layoutParams = viewGroup
                    row.setPadding(16, 16, 16, 16)
                    row.background = ContextCompat.getDrawable(
                            this@FormHeaderOrientationActivity,
                            R.drawable.rect
                    )

                    //
                    val chkList = CheckBox(this@FormHeaderOrientationActivity)
                    chkList.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1.0f
                    )
                    chkList.isChecked = false
                    for (itemlow in listDetail) {
                        if (itemlow.uraianid == item.internalid) {
                            if (itemlow.valuecheck == 1) {
                                chkList.isChecked = true
                            }
                        }
                    }
                    chkList.textSize = 16.0f
//                    chkList.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    chkList.text = item.uraiantext
                    chkList.gravity = Gravity.CENTER_VERTICAL

                    row.addView(chkList)

                    this@FormHeaderOrientationActivity.runOnUiThread {
                        binding.llayoutUraianOrientation.addView(row)
                    }
                }
            }

            this@FormHeaderOrientationActivity.runOnUiThread {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun dialogDate() {
        val day: Int = binding.tvStartedDateOrientationInput.text.substring(0..1).toInt()
        val month: Int = binding.tvStartedDateOrientationInput.text.substring(3..4).toInt() - 1
        val year: Int = binding.tvStartedDateOrientationInput.text.substring(6..9).toInt()

        val dpd = DatePickerDialog(
                this,
                { _, yearJ, monthOfYear, dayOfMonth ->
                    // Display Selected date in textbox
                    val monthX = monthOfYear + 1
                    var monthS = monthX.toString()
                    if (monthS.length == 1) {
                        monthS = "0$monthS";
                    }
                    var dayX = dayOfMonth.toString()
                    if (dayX.length == 1) {
                        dayX = "0$dayX";
                    }

                    binding.tvStartedDateOrientationInput.text = "$dayX-$monthS-$yearJ"

                },
                year,
                month,
                day
        )
        dpd.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLocalDateNow(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return current.format(formatter)
    }

    private fun isDoubleClick(): Boolean {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            lastClickTime = clickTime
            return true
        }
        lastClickTime = clickTime
        return false
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
//            R.id.check -> {
//                AlertDialog.Builder(this)
//                        // Judul
//                        .setTitle("ALERT DIALOG")
//                        .setCancelable(false)
//                        // Pesan yang di tampilkan
//                        .setMessage(
//                                Html.fromHtml(
//                                        "<b>${username}</b> , anda yakin ingin simpan ini. sudahkah cek dengan seksama?",
//                                        Html.FROM_HTML_MODE_LEGACY
//                                )
//                        )
//                        .setPositiveButton("Iya") { _, _ ->
//                            this.runOnUiThread {
//                                postData()
//                            }
//                        }
//                        .setNegativeButton("Tidak") { _, _ ->
//                            Toast.makeText(this, "Memilih tidak", Toast.LENGTH_SHORT).show()
//                        }
//                        .show()
//                true
//            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                val path: String? = data?.getStringExtra("pathsignature")
                if (path != null) {
                    pathSignature = path
                    val selectedImage = BitmapFactory.decodeFile(path)
                    binding.imgviewSignatureOrientation.setImageBitmap(selectedImage)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun postData() {
        val progressDialog = ProgressDialog(this@FormHeaderOrientationActivity)
        progressDialog.setMessage("LOADING")
        progressDialog.setCancelable(false)
        progressDialog.show()
        GlobalScope.launch {
            if(pathSignature == ""){
                runOnUiThread {
                    Toast.makeText(this@FormHeaderOrientationActivity, "Tanda tangan terlebih dahulu", Toast.LENGTH_SHORT).show()
                    if(progressDialog.isShowing){
                        progressDialog.dismiss()
                    }
                }
            } else {
                // Header
                // Id Transaksi
                val idTrx = binding.tvIdTrxHeaderHire.text.toString()
                // Spinner Departement
                if(binding.spinDivisionOrientation.adapter.isEmpty){
                    runOnUiThread {
                        Toast.makeText(this@FormHeaderOrientationActivity, "Departement not found", Toast.LENGTH_SHORT).show()
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    }
                    return@launch
                }
                val departementid = binding.spinDivisionOrientation.selectedItem.toString().split("-").toTypedArray()[0].toInt()

                // Joint Date
                val startedDate = dateTypeConverter.fromTimestamp(dateTypeConverter.stringToTimestamp(binding.tvStartedDateOrientationInput.text.toString()))
                // Employee No
                val employeeNo = binding.edtNumberEmployeeOrientation.text.toString().toIntOrNull()
                if (employeeNo == null) {
                    runOnUiThread {
                        Toast.makeText(this@FormHeaderOrientationActivity, "Proses simpan gagal", Toast.LENGTH_SHORT).show()
                        binding.edtNumberEmployeeOrientation.setText("")
                        binding.edtNumberEmployeeOrientation.error = "Employee number not found"
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    }
                    return@launch
                }

                val listEmployee = GlobalScope.async { viewModel.getTOrientationEmployee(employeeNo) }.await()
                if(listEmployee.isNotEmpty()){
                    runOnUiThread {
                        Toast.makeText(this@FormHeaderOrientationActivity, "Proses simpan gagal", Toast.LENGTH_SHORT).show()
                        binding.edtNumberEmployeeOrientation.setText("")
                        binding.edtNumberEmployeeOrientation.error = "Employee number already yet"
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    }
                    return@launch
                }

                // Employee Name
                val employeeName = binding.edtNameEmployeeOrientation.text.toString()
                if (employeeName.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this@FormHeaderOrientationActivity, "Proses simpan gagal", Toast.LENGTH_SHORT).show()
                        binding.edtNameEmployeeOrientation.error = "Employee name not found"
                        binding.edtNameEmployeeOrientation.setText("")
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    }
                    return@launch
                }
                // TitleName
                val titleName = binding.edtTitleEmployeeOrientation.text.toString()
                if (titleName.isEmpty()) {
                    this@FormHeaderOrientationActivity.runOnUiThread {
                        Toast.makeText(this@FormHeaderOrientationActivity, "Proses simpan gagal", Toast.LENGTH_SHORT).show()
                        binding.edtTitleEmployeeOrientation.error = "Tidak Boleh Kosong"
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    }
                    return@launch
                }

                val data = GlobalScope.async { viewModel.getTOrientation(idTrx) }.await()
                if (data != null) {
                    if (startedDate != null) {
                        data.employeename = employeeName
                        data.employeeno = employeeNo
                        data.titlename = titleName
                        data.departementid = departementid
                        data.localsignfilename = pathSignature
                        data.serversignfilename = ""
                        data.status = 1
                        data.iscancel = 0
                        data.starteddate = startedDate
                        data.lastmodifieddate = Date()
                        data.lastmodifiedby = netsuite
                        data.lastmodifiedname = username
                        GlobalScope.async { viewModel.updateTOrientation(data) }.await()
                    }
                } else {
                    if (startedDate != null) {
                        val datax = TOrientationEntity(
                                idTrx,
                                employeeName,
                                employeeNo,
                                departementid,
                                titleName,
                                startedDate,
                                pathSignature,
                                "",
                                1,
                                0,
                                Date(),
                                netsuite,
                                username,
                                Date(),
                                netsuite,
                                username
                        )
                        GlobalScope.async { viewModel.insertTOrientation(datax) }.await()
                    }
                }

                // Detail
                val listdetail = GlobalScope.async { viewModel.getTDetailOrientation(idTrx) }.await()
                if (listdetail.isNotEmpty()) {
                    for (detail in listdetail) {
                        GlobalScope.async { viewModel.deleteTDetailOrientation(detail) }.await()
                    }
                }
                binding.llayoutUraianOrientation.forEach { row ->
                    val rowFull = row as LinearLayout
                    val uraianid = rowFull.id
                    val chkBox = rowFull.getChildAt(0) as CheckBox
                    var valuecheck = 0
                    if (chkBox.isChecked) {
                        valuecheck = 1
                    }

                    val data = TDetailOrientationEntity(
                            0,
                            idTrx,
                            uraianid,
                            valuecheck,
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
                    GlobalScope.async { viewModel.insertTDetailOrientation(data) }.await()
                }
                if (isOnline(this@FormHeaderOrientationActivity)) {
                    val dataHeader = GlobalScope.async { viewModel.getTOrientation(idTrx) }.await()
                    val header = JSONObject()
                    header.put("transaction_no", dataHeader.transactionno)
                    header.put("employee_name", dataHeader.employeename)
                    header.put("employee_no", dataHeader.employeeno)
                    header.put("departement_id", dataHeader.departementid)
                    header.put("title_name", dataHeader.titlename)
                    header.put("started_date", SimpleDateFormat("yyyy/MM/dd").format(dataHeader.starteddate))
                    header.put("localsignfilename", dataHeader.localsignfilename)
                    header.put("serversignfilename", dataHeader.serversignfilename)
                    header.put("status", dataHeader.status)
                    header.put("is_cancel", dataHeader.iscancel)
                    header.put("createddate", patternFormatDate(dataHeader.createddate))
                    header.put("createdby", dataHeader.createdby)
                    header.put("createdname", dataHeader.createdname)
                    header.put("lastmodifieddate", patternFormatDate(dataHeader.lastmodifieddate))
                    header.put("lastmodifiedby", dataHeader.lastmodifiedby)
                    header.put("lastmodifiedname", dataHeader.lastmodifiedname)

                    val detail = JSONArray()
                    val listdetailmetal = GlobalScope.async { viewModel.getTDetailOrientation(idTrx) }.await()
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

                    val imageFile = File(dataHeader.localsignfilename)
                    if (imageFile.exists()) {
                        this@FormHeaderOrientationActivity.runOnUiThread {
                            if (progressDialog.isShowing) {
                                progressDialog.dismiss()
                            }
                        }
                        PushToServer(imageFile, format, idTrx).execute()
                    } else {
                        this@FormHeaderOrientationActivity.runOnUiThread {
                            if (progressDialog.isShowing) {
                                progressDialog.dismiss()
                            }
                            finish()
                        }
                    }
                } else {
                    runOnUiThread {
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                        finish()
                    }
                }
            }
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

    private fun patternFormatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
    }

    inner class PushToServer(
            imageFile: File,
            format: JSONObject,
            mTrxIdOld: String
    ) : AsyncTask<String, Void, ReturnFromServer>() {

        private val formata: JSONObject = format
        private val file: File = imageFile
        private val trxIdOld: String = mTrxIdOld
        private val spDataAPI = getSharedPreferences("DATAAPIHRD", MODE_PRIVATE)
        private lateinit var progressDialog: ProgressDialog

        override fun doInBackground(vararg params: String): ReturnFromServer {
            var returnFromServer = ReturnFromServer("", false)
//            val api = spDataAPI.getString("APIGLOBAL", "http://192.168.5.254")
//            val url = "$api/dovechem/dc_hra/Masters/API?token=ZG9fdXBsb2FkLDIwMjEwNTI0LUFQUDAwMQ"
                val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
                val url = "$api/dc_hrd/Masters/API?token=ZG9fdXBsb2FkLDIwMjEwNTI0LUFQUDAwMQ"
            AndroidNetworking.initialize(this@FormHeaderOrientationActivity)
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

        override fun onPreExecute() {
            super.onPreExecute()
            this@FormHeaderOrientationActivity.runOnUiThread{
                progressDialog = ProgressDialog(this@FormHeaderOrientationActivity)
                progressDialog.setMessage("loading")
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
        }

        override fun onPostExecute(hello: ReturnFromServer) {
            super.onPostExecute(hello)

            this@FormHeaderOrientationActivity.runOnUiThread {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
            }

            if (hello.success) {
                GlobalScope.launch {
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
                }
                Toast.makeText(this@FormHeaderOrientationActivity, "Data saved", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog(this@FormHeaderOrientationActivity)
            }
            finish()
        }

        private fun alertDialog(context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Alert Messages")
            builder.setMessage("Connection Timeout. Failed to contact the server. Please contact your administrator.")
            builder.setPositiveButton("Okay") { dialog, which ->
            }
            builder.show()
        }
    }

    data class ReturnFromServer(val trxno: String, val success: Boolean)

    inner class DownloadFromServerImage(
            mImageUrl: String,
            mImageName: String,
            mImagePath: String
    ) : AsyncTask<String, Void, Boolean>() {

        private val imageUrl = mImageUrl
        private val imageName = mImageName
        private val imagePath = mImagePath
        var isSuccess = false
        private lateinit var progressDialog: ProgressDialog

        override fun doInBackground(vararg params: String): Boolean? {
            AndroidNetworking.initialize(this@FormHeaderOrientationActivity)

            val request: ANRequest<*> = AndroidNetworking.download(imageUrl, imagePath, imageName)
                    .build()
                    .setDownloadProgressListener { bytesUploaded, totalBytes ->

                    }
            val response = request.executeForJSONObject()
            if (response.isSuccess) {
                val jsonObject = response.result
                Log.d("PEGGIESDLIMAGES", jsonObject.toString())
                isSuccess = true
            } else {
                val error = response.error
                isSuccess = false
            }
            return true
        }

        override fun onPreExecute() {
            super.onPreExecute()
            this@FormHeaderOrientationActivity.runOnUiThread {
                progressDialog = ProgressDialog(this@FormHeaderOrientationActivity)
                progressDialog.setMessage("loading")
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
        }

        override fun onPostExecute(hello: Boolean) {
            super.onPostExecute(hello)
            Log.d("PEGGIESDLRES", hello.toString())

            Log.d("PEGGUESPATHRAW", imagePath)

            if (hello) {
                Toast.makeText(this@FormHeaderOrientationActivity, "Proses Download Gambar Berhasil", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@FormHeaderOrientationActivity, "Proses Download Gambar Gagal", Toast.LENGTH_LONG).show()
                alertDialog(this@FormHeaderOrientationActivity)
            }
            progressDialog.dismiss()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (intent.getStringExtra(TYPE_INTENT) == "UPDATE") {
            val idTrx = intent.getStringExtra(EXTRA_TRANSACTION_ID)
            val iscancel = intent.getIntExtra(EXTRA_IS_CANCEL, 0)
            if (idTrx != null) {
                if(iscancel == 1){
                    menu?.add("Completed")?.setOnMenuItemClickListener {
                        android.app.AlertDialog.Builder(this)
                                // Judul
                                .setTitle("Apa anda sudah yakin?")
                                // Pesan yang di tampilkan
                                .setMessage("Anda yakin ingin completed transaksi ini. sudahkah cek dengan seksama?")
                                .setPositiveButton("Iya") { _, _ ->
                                    GlobalScope.launch {
                                        val tHeader = GlobalScope.async { viewModel.getTOrientation(idTrx) }.await()
                                        tHeader.status = 1
                                        tHeader.iscancel = 0
                                        GlobalScope.async { viewModel.updateTOrientation(tHeader) }.await()
                                        this@FormHeaderOrientationActivity.runOnUiThread {
                                            binding.btnSaveOrientation.isEnabled = true
                                            binding.tvStatusHeaderHire.text = "Sync"
                                            finish()
                                        }
                                    }
                                }
                                .setNegativeButton("Tidak") { _, _ ->
                                    Toast.makeText(this, "Anda memilih tombol tidak", Toast.LENGTH_LONG).show()
                                }
                                .show()
                        true
                    }
                } else {
                    menu?.add("Cancel")?.setOnMenuItemClickListener {
                        android.app.AlertDialog.Builder(this)
                                // Judul
                                .setTitle("Apa anda sudah yakin?")
                                // Pesan yang di tampilkan
                                .setMessage("Anda yakin ingin cancel transaksi ini. sudahkah cek dengan seksama?")
                                .setPositiveButton("Iya") { _, _ ->
                                    GlobalScope.launch {
                                        val tHeader = GlobalScope.async { viewModel.getTOrientation(idTrx) }.await()
                                        tHeader.status = 1
                                        tHeader.iscancel = 1
                                        GlobalScope.async { viewModel.updateTOrientation(tHeader) }.await()
                                        this@FormHeaderOrientationActivity.runOnUiThread {
                                            binding.btnSaveOrientation.isEnabled = false
                                            binding.tvStatusHeaderHire.text = "Cancel"
                                            finish()
                                        }
                                    }
                                }
                                .setNegativeButton("Tidak") { _, _ ->
                                    Toast.makeText(this, "Anda memilih tombol tidak", Toast.LENGTH_LONG).show()
                                }
                                .show()
                        true
                    }
                }
            }
        }
        return super.onCreateOptionsMenu(menu)
    }
}