package com.muhammedaliderindag.sarisayfalar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.muhammedaliderindag.sarisayfalar.databinding.ActivityUyeBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UyeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUyeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUyeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Kullanicilar")


        binding.uyeKaydet.setOnClickListener {
            uyeOl()

        }

    }

    private fun uyeOl() {
        val adSoyad = binding.uyeAd.text.toString()
        val email = binding.uyeEmail.text.toString()
        val sifre = binding.sifreInput.text.toString()

        if (adSoyad.isNotEmpty() && email.isNotEmpty() && sifre.isNotEmpty()) {
            firebaseAuth.createUserWithEmailAndPassword(email, sifre)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = firebaseAuth.currentUser?.uid
                        if (userId != null) {
                            // Kullanıcı başarıyla oluşturuldu, bilgileri veritabanına kaydet
                            kaydetFirebaseDatabase(userId, adSoyad, email)

                            // Üye olma başarılıysa bir aktiviteye yönlendirme yapabilirsiniz
                            val intent = Intent(this, GirisActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this, "task.exception?.message", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
        }
    }

    private fun kaydetFirebaseDatabase(userId: String, adSoyad: String, email: String) {
        val kullaniciMap = HashMap<String, Any>()

        kullaniciMap["ad ve soyad"] = adSoyad
        kullaniciMap["email"] = email
        kullaniciMap["kullaniciTuru"] =
            "User" // Varsayılan olarak kullanıcı türünü "User" olarak ayarladık

        databaseReference.child(userId).setValue(kullaniciMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Kullanıcı bilgileri kaydedildi", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        this,
                        "Veritabanına kayıt sırasında bir hata oluştu",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}