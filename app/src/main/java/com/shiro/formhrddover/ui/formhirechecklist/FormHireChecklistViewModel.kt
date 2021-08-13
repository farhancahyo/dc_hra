package com.shiro.formhrddover.ui.formhirechecklist

import android.app.*
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.shiro.formhrddover.database.entity.hirechecklist.*
import com.shiro.formhrddover.database.repository.HireChecklistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FormHireChecklistViewModel(application: Application) : ViewModel() {
    private val mHireChecklistRepository = HireChecklistRepository(application)

    suspend fun getTNewHireChecklist() = mHireChecklistRepository.getTNewHireCheckList()
    suspend fun getTNewHireChecklist(sdate : Long, edate : Long, name : String, no : String) = mHireChecklistRepository.getTNewHireCheckList(sdate, edate, name, no)
    suspend fun getTNewHireChecklist(idTrx : String) = mHireChecklistRepository.getTNewHireCheckList(idTrx)
    suspend fun getTNewHireChecklist(status : Int) = mHireChecklistRepository.getTNewHireCheckList(status)
    suspend fun getTNewHireChecklistOffline() = mHireChecklistRepository.getTNewHireCheckListOffline()
    suspend fun getMDepartement() = mHireChecklistRepository.getMDepartement()
    suspend fun getMCategory() = mHireChecklistRepository.getMCategory()
    suspend fun getTDetailNewHireChecklist(idTrx: String) = mHireChecklistRepository.getTDetailNewHireCheckList(idTrx)
    suspend fun getTDetailNewHireChecklist(idTrx: String, categoryid : Int) = mHireChecklistRepository.getTDetailNewHireCheckList(idTrx, categoryid)
    suspend fun getItemUraianChecklist(categoryid: Int) = mHireChecklistRepository.getItemUraianChecklist(categoryid)

    suspend fun insertTNewHireChecklist(data : TNewHireCheckListEntity) = mHireChecklistRepository.insertTNewHireCheckList(data)
    suspend fun insertTDetailNewHireChecklist(data : TDetailNewHireCheckListEntity) = mHireChecklistRepository.insertTDetailNewHireCheckList(data)

    suspend fun updateTNewHireChecklist(data : TNewHireCheckListEntity) = mHireChecklistRepository.updateTNewHireCheckList(data)
    suspend fun updateTDetailNewHireChecklist(data : TDetailNewHireCheckListEntity) = mHireChecklistRepository.updateTDetailNewHireCheckList(data)

    suspend fun deleteTDetailNewHireChecklist(data : TDetailNewHireCheckListEntity) = mHireChecklistRepository.deleteTDetailNewHireCheckList(data)
    suspend fun deleteTNewHireCheckList(data : TNewHireCheckListEntity) = mHireChecklistRepository.deleteTNewHireCheckList(data)

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

    inner class GetMstCategory(mContext: Activity) : AsyncTask<String, Void, Boolean>() {
        private val spDataAPI =
                mContext.getSharedPreferences("DATAAPIPARAFORM", AppCompatActivity.MODE_PRIVATE)
        private val context = mContext
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<MCategoryEntity>()
        private var isSuccess = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String): Boolean? {
            val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
//            val api = spDataAPI.getString("APIGLOBAL", "http://192.168.100.61")
            val url = "$api/dc_hrd/Masters/API?token=Z2V0QWxsVGFibGUsMjAyMTAzMTgtQVBQMDAx"
            AndroidNetworking.initialize(context)
            val request = AndroidNetworking.post(url).addBodyParameter("postData", "m_category")
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

                            val categoryname: String = item.getString("category_name").toString()

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
                                val mDataResponse = MCategoryEntity(
                                        internalid,
                                        categoryname,
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
                isSuccess = false
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
                    viewModelScope.async { mHireChecklistRepository.clearMCategory() }.await()
                    viewModelScope.async { mHireChecklistRepository.insertMCategory(dataResponse) }.await()
                }
//                GetMstParameter(context).execute("")
            } else {
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    inner class GetMstItem(mContext: Activity) : AsyncTask<String, Void, Boolean>() {
        private val spDataAPI =
                mContext.getSharedPreferences("DATAAPIPARAFORM", AppCompatActivity.MODE_PRIVATE)
        private val context = mContext
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<MItemEntity>()
        private var isSuccess = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String): Boolean? {
            val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
//            val api = spDataAPI.getString("APIGLOBAL", "http://192.168.100.61")
            val url = "$api/dc_hrd/Masters/API?token=Z2V0QWxsVGFibGUsMjAyMTAzMTgtQVBQMDAx"
            AndroidNetworking.initialize(context)
            val request = AndroidNetworking.post(url).addBodyParameter("postData", "m_item")
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

                            val itemname: String = item.getString("item_name").toString()

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
                                val mDataResponse = MItemEntity(
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
                    viewModelScope.async { mHireChecklistRepository.clearMItem() }.await()
                    viewModelScope.async { mHireChecklistRepository.insertMItem(dataResponse) }.await()
                }
//                GetMstParameter(context).execute("")
            } else {
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    inner class GetMstDepartement(mContext: Activity) : AsyncTask<String, Void, Boolean>() {
        private val spDataAPI =
                mContext.getSharedPreferences("DATAAPIPARAFORM", AppCompatActivity.MODE_PRIVATE)
        private val context = mContext
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<MDepartementEntity>()
        private var isSuccess = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String): Boolean? {
            val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
//            val api = spDataAPI.getString("APIGLOBAL", "http://192.168.100.61")
            val url = "$api/dc_hrd/Masters/API?token=Z2V0QWxsVGFibGUsMjAyMTAzMTgtQVBQMDAx"
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
                            val netsuiteiddepartement: Int = item.getString("netsuite_id_departement").toString().toInt()
                            val departementname: String = item.getString("departement_name").toString()
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
                                val mDataResponse = MDepartementEntity(
                                        internalid,
                                        netsuiteiddepartement,
                                        departementname,
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
                    viewModelScope.async { mHireChecklistRepository.clearMDepartement() }.await()
                    viewModelScope.async { mHireChecklistRepository.insertMDepartement(dataResponse) }.await()
                }
//                GetMstParameter(context).execute("")
            } else {
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    inner class GetMstMappingCategory(mContext: Activity) : AsyncTask<String, Void, Boolean>() {
        private val spDataAPI =
                mContext.getSharedPreferences("DATAAPIPARAFORM", AppCompatActivity.MODE_PRIVATE)
        private val context = mContext
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<MMappingCategoryEntity>()
        private var isSuccess = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String): Boolean? {
            val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
//            val api = spDataAPI.getString("APIGLOBAL", "http://192.168.100.61")
            val url = "$api/dc_hrd/Masters/API?token=Z2V0QWxsVGFibGUsMjAyMTAzMTgtQVBQMDAx"
            AndroidNetworking.initialize(context)
            val request = AndroidNetworking.post(url).addBodyParameter("postData", "m_mapping_category")
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

                            val categoryid: Int = item.getString("category_id").toString().toInt()

                            val itemid: Int = item.getString("item_id").toString().toInt()

                            val status: Int = item.getString("status").toString().toInt()

                            val mDataResponse = MMappingCategoryEntity(
                                    internalid,
                                    categoryid,
                                    itemid,
                                    status
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
                    viewModelScope.async { mHireChecklistRepository.clearMMappingCategory() }.await()
                    viewModelScope.async { mHireChecklistRepository.insertMMappingCategory(dataResponse) }.await()
                }
//                GetMstParameter(context).execute("")
            } else {
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    inner class GetTrxNewHireChecklist(mContext: Activity) : AsyncTask<String, Void, Boolean>() {
        private val spDataAPI =
                mContext.getSharedPreferences("DATAAPIPARAFORM", AppCompatActivity.MODE_PRIVATE)
        private val context = mContext
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<TNewHireCheckListEntity>()
        private var isSuccess = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String): Boolean? {
            val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
//            val api = spDataAPI.getString("APIGLOBAL", "http://192.168.100.61")
            val url = "$api/dc_hrd/Masters/API?token=Z2V0QWxsVGFibGUsMjAyMTAzMTgtQVBQMDAx"
            AndroidNetworking.initialize(context)
            val request = AndroidNetworking.post(url).addBodyParameter("postData", "t_new_hire_checklist")
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

                            val departementid: Int = item.getString("departement_id").toString().toInt()

                            val titlename: String = item.getString("title_name").toString()

                            val jointdate: Date? = timestampToDate(khususonTodDate(item.getString("joint_date").toString()))

                            val employeeno: Int = item.getString("employee_no").toString().toInt()

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

                            if (createddate != null && lastmodifieddate != null && jointdate != null) {
                                val mDataResponse = TNewHireCheckListEntity(
                                        transactionno,
                                        employeename,
                                        departementid,
                                        titlename,
                                        jointdate,
                                        employeeno,
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
                    viewModelScope.async { mHireChecklistRepository.clearTNewHireCheckList() }.await()
                    viewModelScope.async { mHireChecklistRepository.insertTNewHireCheckList(dataResponse) }.await()
                }
//                GetMstParameter(context).execute("")
            } else {
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    inner class GetTrxDetailNewHireChecklist(mContext: Activity) : AsyncTask<String, Void, Boolean>() {
        private val spDataAPI = mContext.getSharedPreferences("DATAAPIPARAFORM", AppCompatActivity.MODE_PRIVATE)
        private val context = mContext
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<TDetailNewHireCheckListEntity>()
        private var isSuccess = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: String): Boolean? {
            val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
//            val api = spDataAPI.getString("APIGLOBAL", "http://192.168.100.61")
            val url = "$api/dc_hrd/Masters/API?token=Z2V0QWxsVGFibGUsMjAyMTAzMTgtQVBQMDAx"
            AndroidNetworking.initialize(context)
            val request = AndroidNetworking.post(url).addBodyParameter("postData", "t_detail_new_hire_checklist")
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

                            val categoryid: Int = item.getString("category_id").toInt()

                            val mappingidcategory: Int = item.getString("mapping_id_category").toInt()

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
                                val mDataResponse = TDetailNewHireCheckListEntity(
                                        internalid,
                                        transactionno,
                                        categoryid,
                                        mappingidcategory,
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
                    viewModelScope.async { mHireChecklistRepository.clearTDetailNewHireCheckList() }.await()
                    viewModelScope.async { mHireChecklistRepository.insertTDetailNewHireCheckList(dataResponse) }.await()
                }
            } else {
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }
}