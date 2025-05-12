package com.example.movicard.helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.movicard.model.Invoice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Nombre y versión de la base de datos
private const val DATABASE_NAME = "InvoicesDB"
private const val DATABASE_VERSION = 2

// Nombre de la tabla y columnas
private const val TABLE_INVOICES = "invoices"
private const val COLUMN_ID = "id"
private const val COLUMN_NAME = "name"
private const val COLUMN_DATE = "date"
private const val COLUMN_AMOUNT = "amount"
private const val COLUMN_URL = "url"
private const val COLUMN_PAYMENT_INTENT_ID = "payment_intent_id"


/*
 * Se usará para guardar las facturas creadas de Stripe, con el monto, URL, nombre, fecha de compra, etc.
 * Con todos los registros, llenaré el recyclerView que tengo en mi activity Invoices
 */
class InvoiceDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
        CREATE TABLE $TABLE_INVOICES (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT,
            $COLUMN_DATE TEXT,
            $COLUMN_AMOUNT REAL,
            $COLUMN_URL TEXT,
            $COLUMN_PAYMENT_INTENT_ID TEXT
        )
    """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) { // Suponiendo que la versión inicial es 1 y esta es la 2
            db.execSQL("ALTER TABLE $TABLE_INVOICES ADD COLUMN $COLUMN_PAYMENT_INTENT_ID TEXT")
        }
    }

    // Obtener todas las facturas
    fun getAllInvoices(): List<Invoice> {
        val invoiceList = mutableListOf<Invoice>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_INVOICES", null)

        if (cursor.count == 0) {  // Si no hay datos, retorna lista vacía
            cursor.close()
            db.close()
            return emptyList()
        }

        if (cursor.moveToFirst()) {
            do {
                val invoice = Invoice(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)) ?: "Sin nombre",
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)) ?: "Fecha desconocida",
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)) ?: "Sin URL",
                    paymentIntentId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_INTENT_ID)) ?: "Sin ID"
                )
                invoiceList.add(invoice)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return invoiceList
    }

    fun insertInvoice(name: String, amount: String, paymentIntentId: String, url: String?): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_DATE, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            put(COLUMN_PAYMENT_INTENT_ID, paymentIntentId) // Guardo la ID del pago
            put(COLUMN_URL, url ?: "Sin URL") // Guardo la URL para luego descargar la factura
        }

        val result = db.insert(TABLE_INVOICES, null, values)
        db.close()
        return result != -1L
    }

    fun deleteAllInvoices(): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_INVOICES, null, null)
        db.close()
        return result > 0
    }
}