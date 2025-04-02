package com.example.movicard.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movicard.R

class InvoiceAdapter(private val invoiceList: List<Invoice>, private val listener: InvoiceClickListener) :
    RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder>() {

    interface InvoiceClickListener {
        fun onViewInvoice(invoice: Invoice)
        fun onDownloadInvoice(invoice: Invoice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invoice, parent, false)
        return InvoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val invoice = invoiceList[position]
        holder.bind(invoice, listener)
    }

    override fun getItemCount(): Int = invoiceList.size

    class InvoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textInvoiceName)
        private val textDate: TextView = itemView.findViewById(R.id.textInvoiceDate)
        private val textAmount: TextView = itemView.findViewById(R.id.textInvoiceAmount)
        private val btnView: Button = itemView.findViewById(R.id.btnViewInvoice)
        private val btnDownload: Button = itemView.findViewById(R.id.btnDownloadInvoice)

        fun bind(invoice: Invoice, listener: InvoiceClickListener) {
            textName.text = invoice.name
            textDate.text = invoice.date
            textAmount.text = "$${invoice.amount}"

            btnView.setOnClickListener { listener.onViewInvoice(invoice) }
            btnDownload.setOnClickListener { listener.onDownloadInvoice(invoice) }
        }
    }
}
