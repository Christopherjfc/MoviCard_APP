package com.example.movicard.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movicard.R
import com.example.movicard.model.PasoEstaciones
import com.example.movicard.model.PasoTransbordo

class RutaAdapter(private val pasos: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PASO_ESTACION = 0
        private const val TYPE_TRANSBORDO = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (pasos[position]) {
            is PasoEstaciones -> TYPE_PASO_ESTACION
            is PasoTransbordo -> TYPE_TRANSBORDO
            else -> throw IllegalArgumentException("Tipo de paso desconocido en la lista")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_PASO_ESTACION -> {
                val view = inflater.inflate(R.layout.item_paso_estacion, parent, false)
                PasoEstacionViewHolder(view)
            }
            TYPE_TRANSBORDO -> {
                val view = inflater.inflate(R.layout.item_transbordo, parent, false)
                PasoTransbordoViewHolder(view)
            }
            else -> throw IllegalArgumentException("ViewType no soportado")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PasoEstacionViewHolder -> holder.bind(pasos[position] as PasoEstaciones)
            is PasoTransbordoViewHolder -> holder.bind(pasos[position] as PasoTransbordo)
        }
    }

    override fun getItemCount() = pasos.size

    // ViewHolder para pasos de estaciones
    class PasoEstacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombreEstacion = itemView.findViewById<TextView>(R.id.tvNombreEstacion)
        private val tvTiempoSiguiente = itemView.findViewById<TextView>(R.id.tvTiempoSiguiente)

        fun bind(paso: PasoEstaciones) {
            val estacion = paso.estacion
            tvNombreEstacion.text = "→ ${estacion.nombre} (${estacion.linea.nombre})"
            tvTiempoSiguiente.text = "🕒 Tiempo hasta siguiente: ${paso.tiempoHastaSiguiente} min"
        }
    }

    // ViewHolder para transbordos
    class PasoTransbordoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMensajeTransbordo = itemView.findViewById<TextView>(R.id.tvMensajeTransbordo)
        private val tvTiempoEsperaTransbordo = itemView.findViewById<TextView>(R.id.tvTiempoEsperaTransbordo)

        fun bind(paso: PasoTransbordo) {
            val estacion = paso.estacionTransbordo
            val nuevaLinea = paso.nuevaLinea
            tvMensajeTransbordo.text = "🔄 Transbordo en ${estacion.nombre}: cambia a ${nuevaLinea.nombre}"
            tvTiempoEsperaTransbordo.text = "⏳ Espera de ${paso.tiempoDeEspera} min"
        }
    }
}

