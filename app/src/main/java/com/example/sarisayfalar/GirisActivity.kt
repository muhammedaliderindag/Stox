package com.muhammedaliderindag.sarisayfalar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.muhammedaliderindag.sarisayfalar.databinding.ActivityGirisBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GirisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGirisBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen()
        binding = ActivityGirisBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.uyeGiris.setOnClickListener {
            girisYap()
        }
        binding.yeniUye.setOnClickListener {
            val intent = Intent(this, UyeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val user = firebaseAuth.currentUser
        if (user != null) {
            // Kullanıcı oturum açmışsa, anasayfaya yönlendir
            val intent = Intent(this@GirisActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun girisYap() {
        val email = binding.girisEmail.text.toString()
        val sifre = binding.girisSifreInput.text.toString()

        if (email.isNotEmpty() && sifre.isNotEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, sifre).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        // Kullanıcı giriş yaptı, kullanıcı türünü kontrol et
                        val uid = user.uid
                        kontrolKullaniciTuru(uid)
                    }
                } else {
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun kontrolKullaniciTuru(userId: String) {
        // Firebase veritabanında kullanıcı türünü kontrol et
        val databaseReference =
            FirebaseDatabase.getInstance().reference.child("Kullanicilar").child(userId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val kullaniciTuru = snapshot.child("kullaniciTuru").getValue(String::class.java)
                    if (kullaniciTuru == "Admin") {
                        // Admin ise Admin sayfasına yönlendir
                        val intent = Intent(this@GirisActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Diğer durumlarda (User) normal ana sayfaya yönlendir
                        val intent = Intent(this@GirisActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    // Veritabanında kullanıcı bilgisi bulunamazsa hata durumu
                    Toast.makeText(
                        this@GirisActivity,
                        "Kullanıcı bilgisi bulunamadı.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Veritabanı hatası durumu
                Toast.makeText(
                    this@GirisActivity,
                    "Veritabanı hatası: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
