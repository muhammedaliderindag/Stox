package com.muhammedaliderindag.sarisayfalar.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.muhammedaliderindag.sarisayfalar.R
import com.muhammedaliderindag.sarisayfalar.dataclass.SiparisDataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SiparisAdapter(private val context: Context, private val siparisList: List<SiparisDataClass>) :
    RecyclerView.Adapter<SiparisAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val onaylaButon: Button = itemView.findViewById(R.id.onaylaButon)
        val txtUrunAdi: TextView = itemView.findViewById(R.id.txtUrunAdi)
        val txtUrunFiyati: TextView = itemView.findViewById(R.id.txtUrunFiyati)
        val txtAdet: TextView = itemView.findViewById(R.id.txtAdet)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_siparis_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = siparisList[position]

        holder.txtUrunAdi.text = currentItem.urunIsim
        holder.txtUrunFiyati.text = "${currentItem.urunFiyat?.toString()} TL"
        holder.txtAdet.text = "Adet: ${currentItem.urunAdet?.toString()}"
        holder.onaylaButon.setOnClickListener {
            // Siparişleri onayla
            onaylaSiparisler()

            // Kullanıcıya bilgi mesajı göster
            Toast.makeText(context,"Siparişler Onaylandı",Toast.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int {
        return siparisList.size
    }
    private fun onaylaSiparisler() {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val siparislerRef = uid?.let { firebaseDatabase.reference.child("Siparisler").child(it) }
        val onayliSiparislerRef = uid?.let { firebaseDatabase.reference.child("OnayliSiparisler").child(it) }

        siparislerRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (siparisSnapshot in snapshot.children) {
                        val siparisId = siparisSnapshot.key
                        val siparisData = siparisSnapshot.getValue(SiparisDataClass::class.java)
                        siparisId?.let { onayliSiparislerRef?.child(it)?.setValue(siparisData) }
                    }
                    siparislerRef.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("OnaylaSiparisler", "Siparisler düğümünden veri çekme hatası: ${error.message}")
            }
        })
    }
}
