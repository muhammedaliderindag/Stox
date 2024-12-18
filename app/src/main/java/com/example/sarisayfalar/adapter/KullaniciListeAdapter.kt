package com.muhammedaliderindag.sarisayfalar.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.muhammedaliderindag.sarisayfalar.databinding.RvUserBinding
import com.muhammedaliderindag.sarisayfalar.dataclass.KullaniciBilgi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class KullaniciAdapter(
    private val context: Context,
    private val kullaniciListesi: List<KullaniciBilgi>
) : RecyclerView.Adapter<KullaniciAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: RvUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(kullanici: KullaniciBilgi) {
            // Email'i göster
            binding.KullaniciEmail.text = kullanici.email

            binding.userSil.setOnClickListener {
                silmeIsleminiBaslat(kullanici)
            }
        }

        private fun silmeIsleminiBaslat(silinecekKullanici: KullaniciBilgi) {
            val auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()

            val currentUserUid = auth.currentUser?.uid
            if (currentUserUid == null) {
                Toast.makeText(context, "Oturum hatası!", Toast.LENGTH_SHORT).show()
                return
            }

            val currentUserRef = database.getReference("Kullanicilar").child(currentUserUid)

            currentUserRef.get().addOnSuccessListener { adminSnapshot ->
                // Admin kontrolü
                if (adminSnapshot.child("kullaniciTuru").value.toString() != "Admin") {
                    Toast.makeText(context, "Bu işlem için admin yetkisine sahip değilsiniz!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Silinecek kullanıcının admin olup olmadığını kontrol et
                database.getReference("Kullanicilar")
                    .orderByChild("email")
                    .equalTo(silinecekKullanici.email)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val userSnapshot = snapshot.children.first()
                            val kullaniciTuru = userSnapshot.child("kullaniciTuru").value.toString()

                            // Eğer silinecek kullanıcı admin ise engelle
                            if (kullaniciTuru == "Admin") {
                                Toast.makeText(context, "Admin hesabı silinemez!", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

                            // Admin değilse silme işlemine devam et
                            val userUid = userSnapshot.key
                            if (userUid != null) {
                                database.getReference("Kullanicilar")
                                    .child(userUid)
                                    .removeValue()
                                    .addOnSuccessListener {
                                        val position = kullaniciListesi.indexOf(silinecekKullanici)
                                        if (position != -1) {
                                            notifyItemRemoved(position)
                                        }
                                        Toast.makeText(context, "Kullanıcı başarıyla silindi", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Silme işlemi başarısız: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Kullanıcı bulunamadı!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Kullanıcı arama hatası: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Yetki kontrolü hatası: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(kullaniciListesi[position])
    }

    override fun getItemCount(): Int = kullaniciListesi.size
}
