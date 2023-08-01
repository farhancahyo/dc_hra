package com.shiro.formhrddover.ui.formorientationemployee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shiro.formhrddover.database.entity.hirechecklist.MCategoryEntity
import com.shiro.formhrddover.database.entity.hirechecklist.TNewHireCheckListEntity
import com.shiro.formhrddover.database.entity.orientation.TOrientationEntity
import com.shiro.formhrddover.databinding.ItemTrxHireChecklistBinding
import com.shiro.formhrddover.databinding.ItemTrxOrientationBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FormListOrientationAdapter(val buttonHireListChecklistListener: ButtonListOrientationListener) :
        RecyclerView.Adapter<FormListOrientationAdapter.FormListOrientationHolder>() {

    private val listOrientation = ArrayList<TOrientationEntity>()

    companion object{
        val listStatus = mapOf(1 to "Un-Sync", 2 to "Sync")
    }

    fun setDataOrientation(data: List<TOrientationEntity>?) {
        if (data == null) return
        this.listOrientation.clear()
        data.let { this.listOrientation.addAll(it) }
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormListOrientationHolder {
        val itemsTrxOrientationBinding = ItemTrxOrientationBinding.inflate(
                LayoutInflater.from(
                        parent.context
                ), parent, false
        )
        return FormListOrientationHolder(itemsTrxOrientationBinding)
    }

    override fun onBindViewHolder(holder: FormListOrientationHolder, position: Int) {
        val data = listOrientation[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int = listOrientation.size

    inner class FormListOrientationHolder(private val binding: ItemTrxOrientationBinding) :
            RecyclerView.ViewHolder(
                    binding.root
            ) {
        fun bind(data: TOrientationEntity) {
            with(binding) {
                tvTrxIdOrientation.text = data.transactionno
                tvCreatedDateOrientation.text = SimpleDateFormat("dd-MM-yyyy").format(dateToTimestamp(data.createddate)?.let { timestampToDate(it) })
                tvStartedDateOrientation.text = SimpleDateFormat("dd-MM-yyyy").format(dateToTimestamp(data.starteddate)?.let { timestampToDate(it) })
                tvNameEmployeeOrientation.text = data.employeename
                tvOperatorNameOrientation.text = data.lastmodifiedname
                for (status in listStatus) {
                    if (data.status == status.key) {
                        tvTrxStatusOrientation.text = status.value
                    }
                }

                itemView.setOnClickListener {
                    buttonHireListChecklistListener.btnViewListener(
                            data.transactionno, data.iscancel
                    )
                }
            }
        }
    }

    fun timestampToDate(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    interface ButtonListOrientationListener {
        fun btnViewListener(idtrx: String, iscancel: Int)
    }
}