package com.example.movicard.DbHelper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.movicard.data.Invoice

// Nombre y versión de la base de datos
private const val DATABASE_NAME = "InvoicesDB"
private const val DATABASE_VERSION = 1

// Nombre de la tabla y columnas
private const val TABLE_INVOICES = "invoices"
private const val COLUMN_ID = "id"
private const val COLUMN_NAME = "name"
private const val COLUMN_DATE = "date"
private const val COLUMN_AMOUNT = "amount"
private const val COLUMN_URL = "url"
private const val COLUMN_FILE_PATH = "file_path"

class InvoiceDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_INVOICES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_AMOUNT REAL,
                $COLUMN_URL TEXT,
                $COLUMN_FILE_PATH TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INVOICES")
        onCreate(db)
    }

    // Insertar una nueva factura
    fun addInvoice(name: String, date: String, amount: Double, url: String, filePath: String?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_DATE, date)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_URL, url)
            put(COLUMN_FILE_PATH, filePath)
        }
        db.insert(TABLE_INVOICES, null, values)
        db.close()
    }

    // Obtener todas las facturas
    fun getAllInvoices(): List<Invoice> {
        val invoiceList = mutableListOf<Invoice>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_INVOICES", null)

        if (cursor.moveToFirst()) {
            do {
                val invoice = Invoice(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)),
                    filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH))
                )
                invoiceList.add(invoice)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return invoiceList
    }
}