package com.example.movicard.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseHelper {
    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("tarjetas")

    // Inicializa la colección con valores en 0 si no existen
    fun inicializarTarjetas() {
        val tarjetas = listOf("MOVI_10", "MOVI_MES", "MOVI_TRIMESTRAL")
        for (tarjeta in tarjetas) {
            collectionRef.document(tarjeta).get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    collectionRef.document(tarjeta).set(mapOf("cantidad" to 0))
                }
            }
        }
    }

    // Incrementa la cantidad de una tarjeta en Firestore
    fun incrementarTarjeta(nombreTarjeta: String) {
        val docRef = collectionRef.document(nombreTarjeta)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val cantidadActual = snapshot.getLong("cantidad") ?: 0
            transaction.update(docRef, "cantidad", cantidadActual + 1)
        }
    }

    // Obtiene las cantidades de cada tarjeta
    // Obtiene las cantidades de cada tarjeta en tiempo real
    fun obtenerDatosTarjetas(callback: (Map<String, Int>) -> Unit) {
        collectionRef.addSnapshotListener { result, e ->
            if (e != null) {
                Log.w("FirebaseHelper", "Error obteniendo datos.", e)
                return@addSnapshotListener
            }

            val datos = mutableMapOf<String, Int>()
            for (document in result!!) {
                datos[document.id] = document.getLong("cantidad")?.toInt() ?: 0
            }
            callback(datos)
        }
    }
}