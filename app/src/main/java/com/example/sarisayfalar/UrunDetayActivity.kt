package com.muhammedaliderindag.sarisayfalar

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.muhammedaliderindag.sarisayfalar.databinding.ActivityUrunDetayBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso


class UrunDetayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUrunDetayBinding
    private val auth = FirebaseAuth.getInstance()
    private lateinit var urunId: String // urunId'yi sınıf özelliği olarak taşıyın

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUrunDetayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent'ten veri al
        urunId = intent.getStringExtra("urunId") ?: ""

        // Diğer verileri al ve UI elemanlarına set et
        val urunIsim = intent.getStringExtra("urunIsim") ?: ""
        val urunAciklama = intent.getStringExtra("urunAciklama") ?: ""
        val urunFiyat = intent.getIntExtra("urunFiyat", 0)
        val urunStok = intent.getIntExtra("urunStok", 0)


        binding.urunDetayIsim.text = urunIsim
        binding.urunDetayAciklama.text = urunAciklama
        binding.urunDetayFiyat.text = "$urunFiyat TL"
        binding.urunDetayStok.text = "Stok $urunStok"

        // Picasso, Glide veya diğer bir kütüphane ile resmi yükleyin

        val urunResmiUrl = intent.getStringExtra("newimageurl")
     //   Picasso.get().load(urunResmiUrl).into(binding.imgItem)
        Glide.with(this)
            .load(urunResmiUrl)
            .into(binding.urunDetayImage)



        fun kontrol(){
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            if (user != null) {
                val databaseReference =
                    FirebaseDatabase.getInstance().reference.child("Kullanicilar").child(user.uid)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val kullaniciTuru =
                                snapshot.child("kullaniciTuru").getValue(String::class.java)

                            if (kullaniciTuru == "Admin") {
                                binding.adminUrunSil.visibility = View.VISIBLE
                                binding.adminUrunSil.visibility = View.VISIBLE
                            } else {
                                binding.adminUrunSil.visibility = View.GONE
                                binding.adminUrunSil.visibility = View.GONE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Veritabanı hatası durumunda
                        Log.d("ProfilFragment", "Veritabanı hatası: ${error.message}")
                    }
                })
            }
        }
        kontrol()

        binding.adminUrunSil.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            if (user != null) {
                val databaseReference =
                    FirebaseDatabase.getInstance().reference.child("Kullanicilar").child(user.uid)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val kullaniciTuru =
                                snapshot.child("kullaniciTuru").getValue(String::class.java)

                            if (kullaniciTuru == "Admin") {
                                urunuSil(urunId)
                            } else {
                                Toast.makeText(
                                    this@UrunDetayActivity,
                                    "Bu işlemi sadece Admin kullanıcıları gerçekleştirebilir.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Veritabanı hatası durumunda
                        Log.d("UrunDetayActivity", "Veritabanı hatası: ${error.message}")
                    }
                })
            }
        }

        // Sepete ekle butonuna tıklanma dinleyicisi ekle
        binding.sepeteEkleButon.setOnClickListener {
            urunuSepeteEkle(urunId)
            Toast.makeText(this@UrunDetayActivity, "Sepete Eklendi!", Toast.LENGTH_SHORT).show()
        }

        // Ana sayfaya dön butonuna tıklanma dinleyicisi ekle
        binding.anaSayfaDonButon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun urunuSepeteEkle(urunId: String) {
        val kullaniciId = auth.currentUser?.uid
        if (kullaniciId != null) {
            val sepetRef =
                FirebaseDatabase.getInstance().getReference("Kullanicilar/$kullaniciId/Sepet")
            val urunRef = FirebaseDatabase.getInstance().getReference("Urunler/$urunId")

            // Ürün bilgilerini al
            urunRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val urunIsmi = snapshot.child("urunIsim").getValue(String::class.java)
                    val urunFiyat = snapshot.child("urunFiyat").getValue(Int::class.java)
                    val urunAciklama = snapshot.child("urunAciklama").getValue(String::class.java)
                    val urunResmiUrl = snapshot.child("newimageurl").getValue(String::class.java)
                    val urunStok = snapshot.child("urunStok").getValue(Int::class.java)

                    // Sepete eklenecek ürün bilgilerini oluştur
                    val sepetUrunMap = HashMap<String, Any>()
                    sepetUrunMap["urunId"] = urunId
                    sepetUrunMap["urunIsim"] = urunIsmi ?: ""
                    sepetUrunMap["urunFiyat"] = urunFiyat ?: 0
                    sepetUrunMap["urunAciklama"] = urunAciklama ?: ""
                    sepetUrunMap["newimageurl"] = urunResmiUrl ?: ""
                    sepetUrunMap["urunAdet"] = 1 // Her ürün eklediğinizde başlangıçta 1 adet olacak

                    // Sepet düğümüne ürünü ekle
                    val yeniUrunRef = sepetRef.child(urunId)
                    yeniUrunRef.updateChildren(sepetUrunMap)
                        .addOnSuccessListener {
                            // Sepete ekleme başarılı, ürünün stok sayısını azalt
                            /*urunStok?.let {
                                if (it > 0) {
                                    urunRef.child("urunStok").setValue(it - 1)
                                }
                            }*/

                            // Diğer işlemler (örneğin, SepetActivity'ye yönlendirme)
                        }
                        .addOnFailureListener {
                            // Sepete ekleme başarısız
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Hata durumu
                }
            })
        }
    }

    private fun urunuSil(urunId: String) {
        // Kullanıcı bilgilerini çek
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val databaseReference =
                FirebaseDatabase.getInstance().reference.child("Kullanicilar").child(user.uid)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val kullaniciTuru =
                            snapshot.child("kullaniciTuru").getValue(String::class.java)

                        if (kullaniciTuru == "Admin") {
                            val urunRef =
                                FirebaseDatabase.getInstance().getReference("Urunler").child(urunId)
                            urunRef.removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@UrunDetayActivity,
                                        "Ürün başarıyla silindi",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish() // Aktiviteyi kapat
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this@UrunDetayActivity,
                                        "Ürün silinirken bir hata oluştu",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Toast.makeText(
                                this@UrunDetayActivity,
                                "Yetkisiz kullanıcı: Ürünü silme yetkiniz yok",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Hata durumu
                    Toast.makeText(
                        this@UrunDetayActivity,
                        "Veri tabanı hatası: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

    }
}
