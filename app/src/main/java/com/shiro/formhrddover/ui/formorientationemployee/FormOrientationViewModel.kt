package com.shiro.formhrddover.ui.formorientationemployee

import android.app.Activity
import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.shiro.formhrddover.database.entity.hirechecklist.MDepartementEntity
import com.shiro.formhrddover.database.entity.hirechecklist.MItemEntity
import com.shiro.formhrddover.database.entity.hirechecklist.TDetailNewHireCheckListEntity
import com.shiro.formhrddover.database.entity.hirechecklist.TNewHireCheckListEntity
import com.shiro.formhrddover.database.entity.orientation.MEmployeeEntity
import com.shiro.formhrddover.database.entity.orientation.MUraianEntity
import com.shiro.formhrddover.database.entity.orientation.TDetailOrientationEntity
import com.shiro.formhrddover.database.entity.orientation.TOrientationEntity
import com.shiro.formhrddover.database.repository.HireChecklistRepository
import com.shiro.formhrddover.database.repository.OrientationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class FormOrientationViewModel (application: Application) : ViewModel() {
    private val mOrientationRepository = OrientationRepository(application)
    private val mHireChecklistRepository = HireChecklistRepository(application)

    private val spDataAPI = application.getSharedPreferences("DATAAPIHRD", AppCompatActivity.MODE_PRIVATE)
//    private val api = spDataAPI.getString("APIGLOBAL", "http://192.168.5.254")
//    private val url = "$api/dovechem/dc_hra/Masters/API?token=Z2V0QWxsVGFibGUsMjAyMTAzMTgtQVBQMDAx"
    val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
    val url = "$api/dc_hrd/Masters/API?token=Z2V0QWxsVGFibGUsMjAyMTAzMTgtQVBQMDAx"

    suspend fun getTOrientation() = mOrientationRepository.getTOrientation()
    suspend fun getTOrientation(sdate : Long, edate : Long, name : String, no : String) = mOrientationRepository.getTOrientation(sdate, edate, name, no)
    suspend fun getTOrientation(idTrx : String) = mOrientationRepository.getTOrientation(idTrx)
    suspend fun getTOrientation(status : Int) = mOrientationRepository.getTOrientation(status)
    suspend fun getTOrientationEmployee(employeeno: Int) = mOrientationRepository.getTOrientationEmployee(employeeno)
    suspend fun getTOrientationOffline() = mOrientationRepository.getTOrientationOffline()
    suspend fun getMUraian() = mOrientationRepository.getMUraian()
    suspend fun getMEmployee(keyword : String) = mOrientationRepository.getMEmployee(keyword)
    suspend fun getTDetailOrientation(idTrx: String) = mOrientationRepository.getTDetailOrientation(idTrx)
    suspend fun getTDetailOrientation(idTrx: String, uraianid : Int) = mOrientationRepository.getTDetailOrientation(idTrx, uraianid)
    suspend fun getNewHireChecklistSearch(keyword: String) = mHireChecklistRepository.getTNewHireCheckListSearch(keyword)

    suspend fun insertTOrientation(data : TOrientationEntity) = mOrientationRepository.insertTOrientation(data)
    suspend fun insertTDetailOrientation(data : TDetailOrientationEntity) = mOrientationRepository.insertTDetailOrientation(data)

    suspend fun updateTOrientation(data : TOrientationEntity) = mOrientationRepository.updateTOrientation(data)
    suspend fun updateTDetailOrientation(data : TDetailOrientationEntity) = mOrientationRepository.updateTDetailOrientation(data)

    suspend fun deleteTDetailOrientation(data : TDetailOrientationEntity) = mOrientationRepository.deleteTDetailOrientation(data)
    suspend fun deleteTOrientation(data : TOrientationEntity) = mOrientationRepository.deleteTOrientation(data)

    fun alertDialog(context: Context) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Alert Messages")
        builder.setMessage("Connection Timeout. Failed to contact the server. Please contact your administrator.")
        builder.setPositiveButton("Okay") { _, _ -> }
        builder.show()
    }

    fun stringToTimestamp(string: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val parsedDate: Date = dateFormat.parse(string)
        val timestamp = Timestamp(parsedDate.time)
        return timestamp.time
    }

    fun khususonTodDate(string: String) : Long{
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val parsedDate: Date = dateFormat.parse(string)
        val timestamp = Timestamp(parsedDate.time)
        return timestamp.time
    }

    fun timestampToDate(value: Long): Date? {
        return if (value == null) null else Date(value)
    }

    fun refresh(activity: Activity, full: Boolean){
        GetMstUraian(activity, full).execute()
    }

    inner class GetMstUraian(mContext: Activity, full: Boolean) : AsyncTask<String, Void, Boolean>() {
        private val context = mContext
        private val mFull = full
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<MUraianEntity>()
        private var isSuccess = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String): Boolean? {
            AndroidNetworking.initialize(context)
            val request = AndroidNetworking.post(url).addBodyParameter("postData", "m_uraian")
                    .setPriority(Priority.LOW).build()
            val response = request.executeForJSONObject()
            if (response.isSuccess) {
                try {
                    val res = response.result as JSONObject

                    isSuccess = res.getBoolean("success")
                    if (res.getBoolean("success")) {
                        val items = res.getJSONArray("response")
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)

                            val internalid: Int = item.getString("internal_id").toString().toInt()

                            val itemname: String = item.getString("uraian_text").toString()

                            val status: Int = item.getString("status").toString().toInt()

                            val createddate: Date? = timestampToDate(
                                    stringToTimestamp(
                                            item.getString("createddate").toString()
                                    )
                            )

                            val createdby: Int = item.getString("createdby").toString().toInt()

                            val createdname: String = item.getString("createdname").toString()

                            val lastmodifieddate: Date?
                            val lastmodifiedby: Int
                            val lastmodifiedname: String
                            if (item.getString("lastmodifieddate").toString() != "null") {
                                lastmodifieddate = timestampToDate(
                                        stringToTimestamp(
                                                item.getString("lastmodifieddate").toString()
                                        )
                                )
                                lastmodifiedby = item.getString("lastmodifiedby").toString().toInt()
                                lastmodifiedname = item.getString("lastmodifiedname").toString()
                            } else {
                                lastmodifieddate = createddate
                                lastmodifiedby = createdby
                                lastmodifiedname = createdname
                            }

                            if (createddate != null && lastmodifieddate != null) {
                                val mDataResponse = MUraianEntity(
                                        internalid,
                                        itemname,
                                        status,
                                        createddate,
                                        createdby,
                                        createdname,
                                        lastmodifieddate,
                                        lastmodifiedby,
                                        lastmodifiedname
                                )
                                dataResponse.add(mDataResponse)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("JSONHELPER" + "_Exception", e.toString())
                }
            } else {
                val anError = response.error
                Log.d("ERX_Condition", anError.toString())
            }
            return isSuccess
        }

        override fun onPreExecute() {
            super.onPreExecute()
            context.run {
                progressDialog = ProgressDialog(context)
                progressDialog.setMessage("loading")
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
        }

        override fun onPostExecute(hello: Boolean) {
            super.onPostExecute(hello)
            if (hello) {
                Log.d("KANO2", "BERHASIL DI MEquipment")
                viewModelScope.launch {
                    viewModelScope.async { mOrientationRepository.clearMUraian() }.await()
                    viewModelScope.async { mOrientationRepository.insertMUraian(dataResponse) }.await()
                }
                GetMstDepartement(context, mFull).execute()
            } else {
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    inner class GetMstDepartement(mContext: Activity, full: Boolean) : AsyncTask<String, Void, Boolean>() {
        private val mFull = full
        private val context = mContext
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<MDepartementEntity>()
        private var isSuccess = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String): Boolean? {
            val url = "http://115.85.65.42:8000/dc_api/WebAPI?token=Z2V0RGVwYXJ0bWVudCwyMDIxMDUyNC1BUFAwMDE="
            AndroidNetworking.initialize(context)
            val request = AndroidNetworking.post(url).addBodyParameter("postData", "m_departement")
                    .setPriority(Priority.LOW).build()
            val response = request.executeForJSONObject()
            if (response.isSuccess) {
                try {
                    val res = response.result as JSONObject

                    isSuccess = res.getBoolean("success")
                    if (res.getBoolean("success")) {
                        val items = res.getJSONArray("response")
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)

                            val internalid: Int = item.getString("internal_id").toString().toInt()
                            val netsuiteiddepartement: Int = item.getString("netsuite_id").toString().toInt()
                            val departementname: String = item.getString("name").toString()
                            val status: Int = item.getString("status").toString().toInt()

                            val mDataResponse = MDepartementEntity(
                                    internalid,
                                    netsuiteiddepartement,
                                    departementname,
                                    status,
                                    Date()
                            )
                            dataResponse.add(mDataResponse)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("JSONHELPER" + "_Exception", e.toString())
                }
            } else {
                val anError = response.error
                Log.d("ERX_Condition", anError.toString())
            }
            return isSuccess
        }

        override fun onPreExecute() {
            super.onPreExecute()
            context.runOnUiThread{
                progressDialog = ProgressDialog(context)
                progressDialog.setMessage("loading")
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
        }

        override fun onPostExecute(hello: Boolean) {
            super.onPostExecute(hello)
            if (hello) {
                Log.d("KANO2", "BERHASIL DI MEquipment")
                viewModelScope.launch {
                    viewModelScope.async { mHireChecklistRepository.clearMDepartement() }.await()
                    viewModelScope.async { mHireChecklistRepository.insertMDepartement(dataResponse) }.await()
                }
                GetMstEmployee(context, mFull).execute()
            } else {
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    inner class GetMstEmployee(mContext: Activity, full: Boolean) : AsyncTask<String, Void, Boolean>() {
        private val context = mContext
        private val mfull = full
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<MEmployeeEntity>()
        private var isSuccess = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String): Boolean? {
            AndroidNetworking.initialize(context)
            val request = AndroidNetworking.post(url).addBodyParameter("postData", "m_employee")
                    .setPriority(Priority.LOW).build()
            val response = request.executeForJSONObject()
            if (response.isSuccess) {
                try {
                    val res = response.result as JSONObject
                    isSuccess = res.getBoolean("success")
                    if (res.getBoolean("success")) {
                        val items = res.getJSONArray("response")
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)

                            val internalid: Int = item.getString("internal_id").toString().toInt()

                            val employeename: String = item.getString("employee_name").toString()

                            val status: Int = item.getString("status").toString().toInt()

                            val createddate: Date? = timestampToDate(
                                    stringToTimestamp(
                                            item.getString("createddate").toString()
                                    )
                            )

                            val createdby: Int = item.getString("createdby").toString().toInt()

                            val createdname: String = item.getString("createdname").toString()

                            if (createddate != null) {
                                val mDataResponse = MEmployeeEntity(
                                        internalid,
                                        employeename,
                                        status,
                                        createddate,
                                        createdby,
                                        createdname
                                )
                                dataResponse.add(mDataResponse)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("JSONHELPER" + "_Exception", e.toString())
                }
            } else {
                val anError = response.error
                Log.d("ERX_Condition", anError.toString())
            }
            return isSuccess
        }

        override fun onPreExecute() {
            super.onPreExecute()
            Handler(Looper.getMainLooper()).post {
                progressDialog = ProgressDialog(context)
                progressDialog.setMessage("loading")
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
        }

        override fun onPostExecute(hello: Boolean) {
            super.onPostExecute(hello)
            if (hello) {
                Log.d("KANO2", "BERHASIL DI MEquipment")
                viewModelScope.launch {
                    viewModelScope.async { mOrientationRepository.clearMEmployee() }.await()
                    viewModelScope.async { mOrientationRepository.insertMEmployee(dataResponse) }.await()
                }
                if(mfull){
                    GetTrxOrientation(context).execute()
                }
            } else {
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    inner class GetTrxOrientation(mContext: Activity) : AsyncTask<String, Void, Boolean>() {
        private val context = mContext
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<TOrientationEntity>()
        private var isSuccess = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String): Boolean? {
            AndroidNetworking.initialize(context)
            val request = AndroidNetworking.post(url).addBodyParameter("postData", "t_orientation")
                    .setPriority(Priority.LOW).build()
            val response = request.executeForJSONObject()
            if (response.isSuccess) {
                try {
                    val res = response.result as JSONObject

                    isSuccess = res.getBoolean("success")
                    if (res.getBoolean("success")) {
                        val items = res.getJSONArray("response")
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)

                            val transactionno: String = item.getString("transaction_no").toString()

                            val employeename: String = item.getString("employee_name").toString()

                            val employeeno: Int = item.getString("employee_no").toString().toInt()

                            val departementid: Int = item.getString("departement_id").toString().toInt()

                            val titlename: String = item.getString("title_name").toString()

                            val starteddate: Date? = timestampToDate(khususonTodDate(item.getString("started_date").toString()))

                            val localsignfilename: String = item.getString("localsignfilename").toString()

                            val serversignfilename: String = item.getString("serversignfilename").toString()

                            val status: Int = item.getString("status").toString().toInt()

                            val iscancel: Int = item.getString("is_cancel").toString().toIntOrNull() ?: 0

                            val createddate: Date? = timestampToDate(
                                    stringToTimestamp(
                                            item.getString("createddate").toString()
                                    )
                            )

                            val createdby: Int = item.getString("createdby").toString().toInt()

                            val createdname: String = item.getString("createdname").toString()

                            val lastmodifieddate: Date?
                            val lastmodifiedby: Int
                            val lastmodifiedname: String
                            if (item.getString("lastmodifieddate").toString() != "null") {
                                lastmodifieddate = timestampToDate(
                                        stringToTimestamp(
                                                item.getString("lastmodifieddate").toString()
                                        )
                                )
                                lastmodifiedby = item.getString("lastmodifiedby").toString().toInt()
                                lastmodifiedname = item.getString("lastmodifiedname").toString()
                            } else {
                                lastmodifieddate = createddate
                                lastmodifiedby = createdby
                                lastmodifiedname = createdname
                            }

                            if (createddate != null && lastmodifieddate != null && starteddate != null) {
                                val mDataResponse = TOrientationEntity(
                                        transactionno,
                                        employeename,
                                        employeeno,
                                        departementid,
                                        titlename,
                                        starteddate,
                                        localsignfilename,
                                        serversignfilename,
                                        status,
                                        iscancel,
                                        createddate,
                                        createdby,
                                        createdname,
                                        lastmodifieddate,
                                        lastmodifiedby,
                                        lastmodifiedname
                                )
                                dataResponse.add(mDataResponse)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("JSONHELPER" + "_Exception", e.toString())
                }
            } else {
                val anError = response.error
                Log.d("ERX_Condition", anError.toString())
            }
            return isSuccess
        }

        override fun onPreExecute() {
            super.onPreExecute()
            Handler(Looper.getMainLooper()).post {
                progressDialog = ProgressDialog(context)
                progressDialog.setMessage("loading")
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
        }

        override fun onPostExecute(hello: Boolean) {
            super.onPostExecute(hello)
            if (hello) {
                Log.d("KANO2", "BERHASIL DI MEquipment")
                viewModelScope.launch {
                    viewModelScope.async { mOrientationRepository.clearTOrientation() }.await()
                    viewModelScope.async { mOrientationRepository.insertTOrientation(dataResponse) }.await()
                }
                GetTrxDetailOrientation(context).execute()
            } else {
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    inner class GetTrxDetailOrientation(mContext: Activity) : AsyncTask<String, Void, Boolean>() {
        val localBroadcastManager = LocalBroadcastManager.getInstance(mContext)
        private val context = mContext
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<TDetailOrientationEntity>()
        private var isSuccess = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String): Boolean? {
            AndroidNetworking.initialize(context)
            val request = AndroidNetworking.post(url).addBodyParameter("postData", "t_detail_orientation")
                    .setPriority(Priority.LOW).build()
            val response = request.executeForJSONObject()
            if (response.isSuccess) {
                try {
                    val res = response.result as JSONObject

                    isSuccess = res.getBoolean("success")
                    if (res.getBoolean("success")) {
                        val items = res.getJSONArray("response")
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)

                            val internalid: Int = item.getString("internal_id").toInt()

                            val transactionno: String = item.getString("transaction_no")

                            val uraianid: Int = item.getString("uraian_id").toInt()

                            val valuecheck: Int = item.getString("value_check").toInt()

                            val netsuiteidoperator: Int = item.getString("netsuite_id_operator").toInt()

                            val operatorname: String = item.getString("operator_name")

                            val status: Int = item.getString("status").toString().toInt()

                            val createddate: Date? = timestampToDate(
                                    stringToTimestamp(
                                            item.getString("createddate").toString()
                                    )
                            )

                            val createdby: Int = item.getString("createdby").toString().toInt()

                            val createdname: String = item.getString("createdname").toString()

                            val lastmodifieddate: Date?
                            val lastmodifiedby: Int
                            val lastmodifiedname: String
                            if (item.getString("lastmodifieddate").toString() != "null") {
                                lastmodifieddate = timestampToDate(
                                        stringToTimestamp(
                                                item.getString("lastmodifieddate").toString()
                                        )
                                )
                                lastmodifiedby = item.getString("lastmodifiedby").toString().toInt()
                                lastmodifiedname = item.getString("lastmodifiedname").toString()
                            } else {
                                lastmodifieddate = createddate
                                lastmodifiedby = createdby
                                lastmodifiedname = createdname
                            }

                            if (createddate != null && lastmodifieddate != null) {
                                val mDataResponse = TDetailOrientationEntity(
                                        internalid,
                                        transactionno,
                                        uraianid,
                                        valuecheck,
                                        netsuiteidoperator,
                                        operatorname,
                                        status,
                                        createddate,
                                        createdby,
                                        createdname,
                                        lastmodifieddate,
                                        lastmodifiedby,
                                        lastmodifiedname
                                )
                                dataResponse.add(mDataResponse)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("JSONHELPER" + "_Exception", e.toString())
                }
            } else {
                val anError = response.error
                Log.d("ERX_Condition", anError.toString())
            }
            return isSuccess
        }

        override fun onPreExecute() {
            super.onPreExecute()
            Handler(Looper.getMainLooper()).post {
                progressDialog = ProgressDialog(context)
                progressDialog.setMessage("loading")
                progressDialog.setCancelable(false)
                progressDialog.show()
            }
        }

        override fun onPostExecute(hello: Boolean) {
            super.onPostExecute(hello)
            if (hello) {
                Log.d("KANO2", "BERHASIL DI MEquipment")
                viewModelScope.launch {
                    viewModelScope.async { mOrientationRepository.clearTDetailOrientation() }.await()
                    viewModelScope.async { mOrientationRepository.resetTDetailOrientation() }.await()
                    viewModelScope.async { mOrientationRepository.insertTDetailOrientation(dataResponse) }.await()
                }
                // refresh lewat broadcast
                val intent = Intent(FormListOrientationActivity.DATA_SAVED_BROADCAST_REFRESH)
                localBroadcastManager.sendBroadcast(intent)

            } else {
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }
}