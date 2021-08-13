package com.shiro.formhrddover.ui.formhirechecklist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shiro.formhrddover.database.entity.hirechecklist.MCategoryEntity
import com.shiro.formhrddover.database.entity.hirechecklist.TNewHireCheckListEntity
import com.shiro.formhrddover.databinding.ItemTrxHireChecklistBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FormHireChecklistAdapter(val buttonHireListChecklistListener: ButtonHireListChecklistListener) :
        RecyclerView.Adapter<FormHireChecklistAdapter.FormHireChecklistHolder>() {

    private val listHireChecklList = ArrayList<TNewHireCheckListEntity>()

    companion object{
        val listStatus = mapOf(1 to "Un-Sync", 2 to "Sync")
    }

    fun setDataInspection(data: List<TNewHireCheckListEntity>?) {
        if (data == null) return
        this.listHireChecklList.clear()
        data.let { this.listHireChecklList.addAll(it) }
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormHireChecklistHolder {
        val itemsTrxHireChecklistBinding = ItemTrxHireChecklistBinding.inflate(
                LayoutInflater.from(
                        parent.context
                ), parent, false
        )
        return FormHireChecklistHolder(itemsTrxHireChecklistBinding)
    }

    override fun onBindViewHolder(holder: FormHireChecklistHolder, position: Int) {
        val data = listHireChecklList[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int = listHireChecklList.size

    inner class FormHireChecklistHolder(private val binding: ItemTrxHireChecklistBinding) :
            RecyclerView.ViewHolder(
                    binding.root
            ) {
        fun bind(data: TNewHireCheckListEntity) {
            with(binding) {
                tvTrxIdHeaderHire.text = data.transactionno
                tvTrxJointDateHire.text = SimpleDateFormat("dd-MM-yyyy").format(dateToTimestamp(data.jointdate)?.let { timestampToDate(it) })
                tvNameEmployeeHire.text = data.employeename
                tvTrxNpkOrientation.text = data.employeeno.toString()
                for (status in listStatus) {
                    if (data.status == status.key) {
                        tvTrxStatusHire.text = status.value
                    }
                }

                itemView.setOnClickListener {
                    buttonHireListChecklistListener.btnViewListener(
                            data.transactionno
                    )
//                    (itemView.context as Activity).finish()
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

    interface ButtonHireListChecklistListener {
        fun btnViewListener(idtrx: String)
    }
}