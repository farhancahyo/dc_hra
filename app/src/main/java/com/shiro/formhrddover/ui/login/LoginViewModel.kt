package com.shiro.formhrddover.ui.login

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.shiro.formhrddover.database.entity.user.DDIAuthPagesEntity
import com.shiro.formhrddover.database.entity.user.DDIAuthUserEntity
import com.shiro.formhrddover.database.entity.user.DDIRefMenuAdminEntity
import com.shiro.formhrddover.database.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel(application: Application) : ViewModel() {
    private val mUserRepository = UserRepository(application)

    fun loadData(mContext: Context) {
        GetDDIAuthUser(mContext).execute("")
    }

    suspend fun getDDIAuthUserLogin(username: String, userpass: String): List<DDIAuthUserEntity> =
        mUserRepository.getDDIAuthUserLogin(username, userpass)

    inner class GetDDIAuthUser(mContext: Context) : AsyncTask<String, Void, Boolean>() {

        private val spDataAPI =
            mContext.getSharedPreferences("DATAAPIINSPECTION", AppCompatActivity.MODE_PRIVATE)
        private val context = mContext
        private var isSuccess = false
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<DDIAuthUserEntity>()

        override fun doInBackground(vararg params: String): Boolean? {

            val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
            val url = "$api/dc_pfa/Masters/API?token=Z2V0QWxsVGFibGUsMjAyMTA1MjQtQVBQMDAx"
            AndroidNetworking.initialize(context)

            val request = AndroidNetworking.post(url).addBodyParameter("postData", "ddi_auth_user")
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

                            val id_auth_user: String = item.getString("id_auth_user")
                            val id_auth_user_grup: String = item.getString("id_auth_user_grup")
                            val netsuite_id_employee: Int = item.getInt("netsuite_id_employee")
                            val username: String = item.getString("username")
                            val userpass: String = item.getString("userpass")
                            val name: String = item.getString("name")
                            val pass: String = "pass"

                            dataResponse.add(
                                DDIAuthUserEntity(
                                    id_auth_user,
                                    id_auth_user_grup,
                                    netsuite_id_employee,
                                    username,
                                    userpass,
                                    name,
                                    pass
                                )
                            )
                        }
                    }
                    Log.d("PEGGIESTEST", dataResponse.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("JSONHELPER" + "_Exception", e.toString())
                }
            } else {
                val anError = response.error
                Log.d("ERX", anError.toString())
            }
            return isSuccess
        }

        override fun onPreExecute() {
            super.onPreExecute()

            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("loading")
            progressDialog.setCancelable(false)
            progressDialog.show()

        }

        override fun onPostExecute(hello: Boolean) {
            super.onPostExecute(hello)
            if (hello) {
                Log.d("KANO", "BERHASIL DI DDIAUTHUSER")
                viewModelScope.launch {
                    viewModelScope.async { mUserRepository.clearDDIAuthUser() }.await()
                    viewModelScope.async { mUserRepository.insertDDIAuthUser(dataResponse) }
                        .await()
                    GetDDIAuthPages(context).execute("")
                }
            } else {
                Log.d("KANO", "GAGAL DI DDIAUTHUSER")
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }

        fun alertDialog(context: Context) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            builder.setTitle("Alert Messages")
            builder.setMessage("Connection Timeout. Failed to contact the server. Please contact your administrator.")
            builder.setPositiveButton("Okay") { dialog, which ->
            }
            builder.show()
        }
    }

    inner class GetDDIAuthPages(mContext: Context) : AsyncTask<String, Void, Boolean>() {
        private val spDataAPI =
            mContext.getSharedPreferences("DATAAPIINSPECTION", AppCompatActivity.MODE_PRIVATE)
        private val context = mContext
        private var isSuccess = false
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<DDIAuthPagesEntity>()

        override fun doInBackground(vararg params: String): Boolean? {
            val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
            val url = "$api/dc_pfa/Masters/API?token=Z2V0QWxsVGFibGUsMjAyMTA1MjQtQVBQMDAx"
            AndroidNetworking.initialize(context)

            val request = AndroidNetworking.post(url).addBodyParameter("postData", "ddi_auth_pages")
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

                            val id_auth_pages: String = item.getString("id_auth_pages")
                            val id_auth_user_grup: String = item.getString("id_auth_user_grup")
                            val id_auth_user: String = item.getString("id_auth_user")
                            val id_ref_menu_admin: String = item.getString("id_ref_menu_admin")
                            val netsuite_id_employee: String = item.getString("netsuite_id_employee")

                            dataResponse.add(
                                DDIAuthPagesEntity(
                                    id_auth_pages,
                                    id_auth_user_grup,
                                    id_auth_user,
                                    id_ref_menu_admin,
                                    netsuite_id_employee
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("JSONHELPER" + "_Exception", e.toString())
                }
            } else {
                val anError = response.error
                Log.d("ERX", anError.toString())
            }
            return isSuccess
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("loading")
            progressDialog.setCancelable(false)
            progressDialog.show()

        }

        override fun onPostExecute(hello: Boolean) {
            super.onPostExecute(hello)
            if (hello) {
                Log.d("KANO", "BERHASIL DI DDIAUTHPAGES")
                viewModelScope.launch {
                    viewModelScope.async { mUserRepository.clearDDIAuthPages() }.await()
                    viewModelScope.async { mUserRepository.insertDDIAuthPages(dataResponse) }
                        .await()
                    GetDDIRefMenuAdmin(context).execute("")
                }
            } else {
                Log.d("KANO", "GAGAL DI DDIAUTHPAGES")
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }

        }

        fun alertDialog(context: Context) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            builder.setTitle("Alert Messages")
            builder.setMessage("Connection Timeout. Failed to contact the server. Please contact your administrator.")
            builder.setPositiveButton("Okay") { dialog, which ->
            }
            builder.show()
        }
    }

    inner class GetDDIRefMenuAdmin(mContext: Context) : AsyncTask<String, Void, Boolean>() {
        private val spDataAPI =
            mContext.getSharedPreferences("DATAAPIINSPECTION", AppCompatActivity.MODE_PRIVATE)
        private val context = mContext
        private var isSuccess = false
        private lateinit var progressDialog: ProgressDialog
        private val dataResponse = ArrayList<DDIRefMenuAdminEntity>()

        override fun doInBackground(vararg params: String): Boolean? {
            val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
            val url = "$api/dc_pfa/Masters/API?token=Z2V0QWxsVGFibGUsMjAyMTA1MjQtQVBQMDAx"
            AndroidNetworking.initialize(context)

            val request =
                AndroidNetworking.post(url).addBodyParameter("postData", "ddi_ref_menu_admin")
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

                            val id_ref_menu_admin: String = item.getString("id_ref_menu_admin")
                            val id_parents_menu_admin: String =
                                item.getString("id_parents_menu_admin")
                            val menu: String = item.getString("menu")
                            val file: String = item.getString("file")
                            val androidpath: String = item.getString("androidpath")
                            val urut: Int = item.getString("urut").toInt()
                            val status: Int = item.getString("status").toInt()
                            val icon: String = item.getString("icon")

                            dataResponse.add(
                                DDIRefMenuAdminEntity(
                                    id_ref_menu_admin,
                                    id_parents_menu_admin,
                                    menu,
                                    file,
                                    androidpath,
                                    urut,
                                    status,
                                    icon
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("JSONHELPER" + "_Exception", e.toString())
                }
            } else {
                val anError = response.error
                Log.d("ERX", anError.toString())

            }
            return isSuccess
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("loading")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun onPostExecute(hello: Boolean) {
            super.onPostExecute(hello)
            if (hello) {
                Log.d("KANO", "Berhasil DI DDIREFMENU")
                viewModelScope.launch {
                    viewModelScope.async { mUserRepository.clearDDIRefMenuAdmin() }.await()
                    viewModelScope.async { mUserRepository.insertDDIRefMenuAdmin(dataResponse) }
                        .await()
                }
//                GetMShiftStatus(context).execute()
            } else {
                Log.d("KANO", "GAGAL DI DDIREFMENU")
                alertDialog(context)
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }

        }

        fun alertDialog(context: Context) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            builder.setTitle("Alert Messages")
            builder.setMessage("Connection Timeout. Failed to contact the server. Please contact your administrator.")
            builder.setPositiveButton("Okay") { dialog, which ->
            }
            builder.show()
        }
    }

//    inner class GetMShiftStatus(mContext: Context) : AsyncTask<String, Void, Boolean>() {
//
//        private val context = mContext
//        private lateinit var progressDialog: ProgressDialog
//        private val dataResponse = ArrayList<MStatusShiftEntity>()
//
//        @RequiresApi(Build.VERSION_CODES.O)
//        override fun doInBackground(vararg params: String): Boolean {
//
//            val status1 = MStatusShiftEntity(1, "Un-Sync")
//            val status2 = MStatusShiftEntity(2, "Sync")
//
//            dataResponse.add(status1)
//            dataResponse.add(status2)
//
//            return true
//        }
//
//        override fun onPreExecute() {
//            super.onPreExecute()
//            progressDialog = ProgressDialog(context)
//            progressDialog.setMessage("loading")
//            progressDialog.setCancelable(false)
//            progressDialog.show()
//
//        }
//
//        override fun onPostExecute(hello: Boolean) {
//            super.onPostExecute(hello)
//            if (hello) {
//                Log.d("KANO2", "BERHASIL DI MSTATUS")
//                viewModelScope.launch {
//                    viewModelScope.async { mUserRepository.clearMShiftStatus() }.await()
//                    viewModelScope.async { mUserRepository.insertMShiftStatus(dataResponse) }
//                        .await()
//                }
//                GetMstStatus(context).execute("")
//            }
//            if (progressDialog.isShowing) {
//                progressDialog.dismiss()
//            }
//        }
//    }

//    inner class GetMstStatus(mContext: Context) : AsyncTask<String, Void, Boolean>() {
//
//        private val context = mContext
//        private lateinit var progressDialog: ProgressDialog
//        private val dataResponse = ArrayList<MStatusEntity>()
//
//        @RequiresApi(Build.VERSION_CODES.O)
//        override fun doInBackground(vararg params: String): Boolean? {
//
//            val status1 = MStatusEntity(1, "On Progress")
//            val status2 = MStatusEntity(2, "Completed")
//            val status3 = MStatusEntity(3, "Cancel")
//            val status4 = MStatusEntity(4, "Sync")
//
//            dataResponse.add(status1)
//            dataResponse.add(status2)
//            dataResponse.add(status3)
//            dataResponse.add(status4)
//
//            return true
//        }
//
//        override fun onPreExecute() {
//            super.onPreExecute()
//            progressDialog = ProgressDialog(context)
//            progressDialog.setMessage("loading")
//            progressDialog.setCancelable(false)
//            progressDialog.show()
//
//        }
//
//        override fun onPostExecute(hello: Boolean) {
//            super.onPostExecute(hello)
//            if (hello) {
//                Log.d("KANO2", "BERHASIL DI MSTATUS")
//                viewModelScope.launch {
//                    viewModelScope.async { mUserRepository.clearMStatus() }.await()
//                    viewModelScope.async { mUserRepository.insertMStatus(dataResponse) }.await()
//                }
//                alertDialogComplete(context)
//            }
//            if (progressDialog.isShowing) {
//                progressDialog.dismiss()
//            }
//        }
//
//        private fun alertDialogComplete(context: Context) {
//            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
//            builder.setTitle("Messages")
//            builder.setMessage("Get Data From Server Complete")
//            builder.setPositiveButton("Okay") { _, _ ->
//            }
//            builder.show()
//        }
//    }
}