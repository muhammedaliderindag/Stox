package com.muhammedaliderindag.sarisayfalar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.sarisayfalar.dataclass.UpdateTotalEvent
import com.muhammedaliderindag.sarisayfalar.R
import com.muhammedaliderindag.sarisayfalar.SepetFragment
import com.muhammedaliderindag.sarisayfalar.dataclass.UrunDataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus

class SepetAdapter(private val context: Context, private val sepetList: List<UrunDataClass>) :
    RecyclerView.Adapter<SepetAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urunIsmi: TextView = itemView.findViewById(R.id.urunIsmi)
        val urunFiyat: TextView = itemView.findViewById(R.id.urunFiyat)
        val imgItem: ImageView = itemView.findViewById(R.id.imgItem)
        val btnArttir: Button = itemView.findViewById(R.id.btnArttir)
        val btnAzalt: Button = itemView.findViewById(R.id.btnAzalt)
        val txtMiktar: TextView = itemView.findViewById(R.id.txtMiktar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_sepet_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = sepetList[position]
        holder.urunIsmi.text = currentItem.urunIsim
        holder.urunFiyat.text = currentItem.urunFiyat.toString()
        holder.txtMiktar.text = currentItem.urunAdet.toString()
        Picasso.get().load(currentItem.newimageurl).into(holder.imgItem)

        holder.btnArttir.setOnClickListener {
            // UrunAdet 1'den büyükse azaltma işlemi yapılabilir
            currentItem.urunAdet?.let { adet ->
                    currentItem.urunId?.let { urunId ->
                        // Firebase'deki "urunAdet" değerini azalt
                        updateFirebaseUrunAdet(urunId, currentItem.urunAdet!! + 1)

                        // Adapter'deki mevcut ürünü güncelle
                        currentItem.urunAdet = adet + 1
                        // EventBus ile güncelleme eventi yayınla
                        EventBus.getDefault().post(UpdateTotalEvent(currentItem.urunAdet!!))
                        // RecyclerView'deki öğeyi güncelle
                        notifyItemChanged(holder.adapterPosition)
                    }
            }
        }

        holder.btnAzalt.setOnClickListener {
            // UrunAdet 1'den büyükse azaltma işlemi yapılabilir
            currentItem.urunAdet?.let { adet ->
                if (adet > 1) {
                    currentItem.urunId?.let { urunId ->
                        // Firebase'deki "urunAdet" değerini azalt
                        updateFirebaseUrunAdet(urunId, currentItem.urunAdet!! - 1)

                        // Adapter'deki mevcut ürünü güncelle
                        currentItem.urunAdet = adet - 1
                        // EventBus ile güncelleme eventi yayınla
                        EventBus.getDefault().post(UpdateTotalEvent(currentItem.urunAdet!!))
                        // RecyclerView'deki öğeyi güncelle
                        notifyItemChanged(holder.adapterPosition)
                    }
                } else {
                    // Eğer adet 1'den küçükse, kullanıcıya bilgi ver
                    Toast.makeText(context, "Adet 1'den az olamaz.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return sepetList.size
    }

    private fun updateFirebaseUrunAdet(urunId: String, yeniMiktar: Int) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        currentUser?.uid?.let { userId ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("Kullanicilar")
                .child(userId)
                .child("Sepet")
                .child(urunId)
            val urunlerReference = FirebaseDatabase.getInstance().getReference("Urunler")
                .child(urunId)

            databaseReference.child("urunAdet").setValue(yeniMiktar)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Firebase işlemleri tamamlandıktan sonra toplamFiyat'ı güncelle
                        (context as? SepetFragment)?.verileriAl()
                        // "Urunler" düğümünden urunStok değerini azalt
                        urunlerReference.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val urunStok = snapshot.child("urunStok").getValue(Int::class.java) ?: 0
                                if (urunStok > 0) {
                                    urunlerReference.child("urunStok").setValue(urunStok)
                                } else {
                                    Toast.makeText(context, "Stokta yeterli ürün yok.", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Hata durumu
                            }
                        })
                    } else {
                        // Hata durumu
                    }
                }
        }
    }
}
