package com.shiro.formhrddover.ui.formhirechecklist.header

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.shiro.formhrddover.database.DateTypeConverter
import com.shiro.formhrddover.database.entity.hirechecklist.MDepartementEntity
import com.shiro.formhrddover.database.entity.hirechecklist.TNewHireCheckListEntity
import com.shiro.formhrddover.databinding.ActivityFormHeaderHireChecklistBinding
import com.shiro.formhrddover.helper.ViewModelFactory
import com.shiro.formhrddover.ui.formhirechecklist.FormHireChecklistViewModel
import com.shiro.formhrddover.ui.formhirechecklist.detail.FormDetailHireChecklistActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FormHeaderHireChecklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormHeaderHireChecklistBinding
    private lateinit var viewModel: FormHireChecklistViewModel
    private lateinit var dateTypeConverter: DateTypeConverter
    private lateinit var spDataLogin: SharedPreferences
    private lateinit var listDepartement: List<MDepartementEntity>
    private lateinit var adapterDepartement: ArrayAdapter<MDepartementEntity>

    companion object {
        private const val JOB_ID = 10
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
        const val EXTRA_IS_CANCEL = "extra_is_cancel"
        const val TYPE_INTENT = "type_intent"
        private var netsuite: Int = 0
        private var username: String = "Name"
        var lastClickTime: Long = 0
        const val DOUBLE_CLICK_TIME_DELTA: Long = 100
        val listStatus = mapOf(0 to "Complete", 1 to "Cancel")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormHeaderHireChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Form Header New Hire Check List"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)

        // Data Login
        spDataLogin = getSharedPreferences(
                "DATALOGIN",
                AppCompatActivity.MODE_PRIVATE
        )
        netsuite = spDataLogin.getInt("IDNETSUITE", 0)
        username = spDataLogin.getString("USERNAME", "").toString()

        val factory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, factory)[FormHireChecklistViewModel::class.java]

        dateTypeConverter = DateTypeConverter()

        // Spinner Product
        GlobalScope.launch {
            listDepartement = GlobalScope.async { viewModel.getMDepartement() }.await()

            adapterDepartement = ArrayAdapter<MDepartementEntity>(this@FormHeaderHireChecklistActivity, android.R.layout.simple_spinner_item, listDepartement)
            adapterDepartement.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this@FormHeaderHireChecklistActivity.runOnUiThread {
                binding.spinDepartementHire.adapter = adapterDepartement

            if (intent.getStringExtra(TYPE_INTENT) == "UPDATE") {
                val idTrx = intent.getStringExtra(EXTRA_TRANSACTION_ID)

                if (idTrx != null) {
                    this@FormHeaderHireChecklistActivity.runOnUiThread {
                        getTNewHireById(idTrx)
                    }
                }
            } else {
                binding.ll1.visibility = View.GONE
                binding.llOperator.visibility = View.GONE
                binding.llDibuat.visibility = View.GONE
                binding.tvJointDateHire.text = getLocalDateNow()

                GlobalScope.launch {
                    val listTrx = GlobalScope.async { viewModel.getTNewHireChecklistOffline() }.await()
                    if (listTrx.isNotEmpty()) {
                        val lasttrx = listTrx[0].transactionno.substring(6, 11).toInt() + 1
                        binding.tvIdTrxHeaderHire.text = String.format("ZTRX1-%05d", lasttrx)
                    } else {
                        binding.tvIdTrxHeaderHire.text = String.format("ZTRX1-%05d", 1)
                    }
                }
            }
            }
        }

        binding.btnJointDateHire.setOnClickListener { dialogDate() }
        binding.btnNextHire.setOnClickListener { postData() }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun postData() {
        if (!isDoubleClick()) {
            if (isJobRunning(this, JOB_ID)) {
                val scheduler = this.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                scheduler.cancel(JOB_ID)
                Toast.makeText(
                        this,
                        "Job Service canceled",
                        Toast.LENGTH_SHORT
                ).show()
            }

            // Id Transaksi
            val idTrx = binding.tvIdTrxHeaderHire.text.toString()
            // Spinner Departement
            val departementid = binding.spinDepartementHire.selectedItem.toString().split("-").toTypedArray()[0].toInt()
            // Joint Date
            val jointDate = dateTypeConverter.fromTimestamp(dateTypeConverter.stringToTimestamp(binding.tvJointDateHire.text.toString()))
            if(binding.edtNameEmployeeHire.text.toString().isEmpty()){
                binding.edtNameEmployeeHire.error = "Fill empty"
                return
            }
            if(binding.edtTitleEmployeeHire.text.toString().isEmpty()){
                binding.edtTitleEmployeeHire.error = "Fill empty"
                return
            }
            if(binding.edtNpkEmployeeHire.text.toString().isEmpty()){
                binding.edtNpkEmployeeHire.error = "Fill empty"
                return
            }

            if (binding.btnNextHire.text == "UPDATE") {
                GlobalScope.launch {
                    val data = GlobalScope.async { viewModel.getTNewHireChecklist(idTrx) }.await()
                    if (jointDate != null) {
                        data.employeename = binding.edtNameEmployeeHire.text.toString()
                        data.titlename = binding.edtTitleEmployeeHire.text.toString()
                        data.employeeno = binding.edtNpkEmployeeHire.text.toString().toInt()
                        data.departementid = departementid
                        data.status = 1
                        data.iscancel = 0
                        data.jointdate = jointDate
                        data.lastmodifieddate = Date()
                        data.lastmodifiedby = netsuite
                        data.lastmodifiedname = username
                    }
//                    GlobalScope.async { viewModel.updateTNewHireChecklist(data) }.await()
                    this@FormHeaderHireChecklistActivity.runOnUiThread {
                        val intent = Intent(
                                this@FormHeaderHireChecklistActivity, FormDetailHireChecklistActivity::class.java
                        )
                        intent.putExtra(FormDetailHireChecklistActivity.EXTRA_TRANSACTION_ID, idTrx)
                        intent.putExtra(FormDetailHireChecklistActivity.EXTRA_NEW_HIRE_CHECKLIST, data)
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                GlobalScope.launch {
                    if (jointDate != null) {
                        val data = TNewHireCheckListEntity(
                                idTrx,
                                binding.edtNameEmployeeHire.text.toString(),
                                departementid,
                                binding.edtTitleEmployeeHire.text.toString(),
                                jointDate,
                                binding.edtNpkEmployeeHire.text.toString().toInt(),
                                "",
                                1,
                                0,
                                Timestamp(System.currentTimeMillis()),
                                netsuite,
                                username,
                                Timestamp(System.currentTimeMillis()),
                                netsuite,
                                username
                        )
//                        GlobalScope.async { viewModel.insertTNewHireChecklist(data) }.await()
                        this@FormHeaderHireChecklistActivity.runOnUiThread {
                            Toast.makeText(this@FormHeaderHireChecklistActivity, "Transaksi berhasil dibuat", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@FormHeaderHireChecklistActivity, FormDetailHireChecklistActivity::class.java)
                            intent.putExtra(FormDetailHireChecklistActivity.EXTRA_TRANSACTION_ID, idTrx)
                            intent.putExtra(FormDetailHireChecklistActivity.EXTRA_NEW_HIRE_CHECKLIST, data)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun getTNewHireById(idTrx: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("loading")
        progressDialog.setCancelable(false)
        progressDialog.show()

        GlobalScope.launch {
            val data = GlobalScope.async { viewModel.getTNewHireChecklist(idTrx) }.await()
            if (data != null) {
                this@FormHeaderHireChecklistActivity.runOnUiThread {
                    binding.tvIdTrxHeaderHire.text = data.transactionno
                    binding.edtNameEmployeeHire.setText(data.employeename)
                    binding.edtNpkEmployeeHire.setText(data.employeeno.toString())
                    binding.edtTitleEmployeeHire.setText(data.titlename)
                    binding.btnNextHire.text = "UPDATE"
                    binding.tvJointDateHire.text = SimpleDateFormat("dd-MM-yyyy").format(data.jointdate).toString()
                    binding.tvOperatorNameHire.text = data.lastmodifiedname
                    binding.tvCreatedByHire.text = data.createdname
                    if (data.departementid != 0) {
                        for (l in listDepartement) {
                            if (data.departementid == l.internalid) {
                                val position = adapterDepartement.getPosition(l)
                                binding.spinDepartementHire.setSelection(position)
                            }
                        }
                    }
                    for (status in listStatus) {
                        if (data.iscancel == status.key) {
                            binding.tvStatusHeaderHire.text = status.value
                        }
                    }
                    if (data.iscancel == 1){
                        binding.btnNextHire.isEnabled = false
                    }
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun dialogDate() {
        val day: Int = binding.tvJointDateHire.text.substring(0..1).toInt()
        val month: Int = binding.tvJointDateHire.text.substring(3..4).toInt() - 1
        val year: Int = binding.tvJointDateHire.text.substring(6..9).toInt()

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

                    binding.tvJointDateHire.text = "$dayX-$monthS-$yearJ"

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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun isJobRunning(context: Context, jobId: Int): Boolean {
        var isScheduled = false

        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == jobId) {
                isScheduled = true
                break
            }
        }

        return isScheduled

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (intent.getStringExtra(TYPE_INTENT) == "UPDATE") {
            val idTrx = intent.getStringExtra(EXTRA_TRANSACTION_ID)
            val isCancel = intent.getIntExtra(EXTRA_IS_CANCEL, 0)
            if (idTrx != null) {
                if(isCancel == 1){
                    menu?.add("Completed")?.setOnMenuItemClickListener {
                        AlertDialog.Builder(this)
                                // Judul
                                .setTitle("Apa anda sudah yakin?")
                                // Pesan yang di tampilkan
                                .setMessage("Anda yakin ingin completed transaksi ini. sudahkah cek dengan seksama?")
                                .setPositiveButton("Iya") { _, _ ->
                                    GlobalScope.launch {
                                        val tHeader = GlobalScope.async { viewModel.getTNewHireChecklist(idTrx) }.await()
                                        tHeader.status = 1
                                        tHeader.iscancel = 0
                                        GlobalScope.async { viewModel.updateTNewHireChecklist(tHeader) }.await()
                                        this@FormHeaderHireChecklistActivity.runOnUiThread {
                                            binding.btnNextHire.isEnabled = true
                                            binding.tvStatusHeaderHire.text = "Completed"
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
                        AlertDialog.Builder(this)
                                // Judul
                                .setTitle("Apa anda sudah yakin?")
                                // Pesan yang di tampilkan
                                .setMessage("Anda yakin ingin cancel transaksi ini. sudahkah cek dengan seksama?")
                                .setPositiveButton("Iya") { _, _ ->
                                    GlobalScope.launch {
                                        val tHeader = GlobalScope.async { viewModel.getTNewHireChecklist(idTrx) }.await()
                                        tHeader.status = 1
                                        tHeader.iscancel = 1
                                        GlobalScope.async { viewModel.updateTNewHireChecklist(tHeader) }.await()
                                        this@FormHeaderHireChecklistActivity.runOnUiThread {
                                            binding.btnNextHire.isEnabled = false
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}