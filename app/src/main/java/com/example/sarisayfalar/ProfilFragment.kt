package com.muhammedaliderindag.sarisayfalar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.sarisayfalar.dataclass.CurrencyResponse
import com.example.sarisayfalar.dataclass.WeatherResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import android.app.AlertDialog
import android.widget.EditText
import android.text.InputType
import com.google.firebase.auth.EmailAuthProvider

class ProfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Kullanıcıyı ve veritabanı referansını başlat
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        databaseReference =
            FirebaseDatabase.getInstance().reference.child("Kullanicilar").child(user.uid)

        // Fragment'in layout dosyasını inflate et
        val view = inflater.inflate(R.layout.fragment_profil, container, false)

        // Kullanıcı ad ve soyadını çek ve textView'lara set et
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    val adSoyad = snapshot.child("ad ve soyad").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)

                    val textViewAdSoyad: TextView = view.findViewById(R.id.adSoyadTextView)
                    val textViewEmail: TextView = view.findViewById(R.id.emailBilgiTextView)

                    textViewAdSoyad.text = adSoyad
                    textViewEmail.text = email
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfilFragment", "Veri okuma işlemi iptal edildi.", error.toException())
            }
        })



        val client = OkHttpClient()
        val temperaturea: TextView = view.findViewById(R.id.temperatureText)
        val aciklama: TextView = view.findViewById(R.id.weatherDescription)
        val sehir: TextView = view.findViewById(R.id.locationText)
        val weatherIcon = view.findViewById<ImageView>(R.id.weatherIcon)
        fun fetchApiData() {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url("https://api.collectapi.com/weather/getWeather?data.lang=tr&data.city=kocaeli")
                        .header("authorization", "apikey 3YPgVwNuX5Ot9wsjZetVdX:2eE954GoV76B7aVqlYsEjk")
                        .header("content-type", "application/json")
                        .build()

                    val response = client.newCall(request).execute()
                    response.body?.string()?.let { responseData ->
                        val gson = Gson()
                        val weatherResponse = gson.fromJson(responseData, WeatherResponse::class.java)
                        val weatherList = weatherResponse.result

                        // Bugünün hava durumu bilgisini alalım (listedeki ilk eleman)
                        val todayWeather = weatherList.firstOrNull()

                        withContext(Dispatchers.Main) {
                            todayWeather?.let { weather ->
                                // UI güncellemelerini burada yapabilirsiniz
                                temperaturea.text = "${weather.degree}°C"
                                aciklama.text = (weather.description).uppercase()
                                sehir.text = "Kocaeli"

                                // İkonu Picasso veya Glide ile yükleyebilirsiniz
                                Glide.with(requireContext())
                                    .load(weather.icon)
                                    .into(weatherIcon)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Hata: ${e.message}")
                }
            }
        }
        fetchApiData()


        // AdminPanelView simgesine tıklanma işlemi
        val adminPanelView: Button = view.findViewById(R.id.adminPanelView)

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
                                adminPanelView.visibility = View.VISIBLE
                            } else {
                                adminPanelView.visibility = View.GONE
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
        adminPanelView.setOnClickListener {
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
                                val navController =
                                    NavHostFragment.findNavController(this@ProfilFragment)
                                navController.navigate(R.id.action_profilFragment_to_kullaniciListeleme)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Bu işlemi gerçekleştirmek için yetkiniz yok.",
                                    Toast.LENGTH_SHORT
                                ).show()
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


        // Exit butonunu bul ve click listener ekle
        val exitButon: Button = view.findViewById(R.id.exitButon)
        exitButon.setOnClickListener {
            exit()
        }

        // Diğer butonu bul ve click listener ekle
        val changeMail: Button = view.findViewById(R.id.email_guncelle)
        changeMail.setOnClickListener {
            ayarlarSayfaGec()
        }
        val changePass: Button = view.findViewById(R.id.sifreDegistir)
        changePass.setOnClickListener {
            ayarlarSayfaGec()
        }
        val hesapSilButon: Button = view.findViewById(R.id.hesapSilButon)
        hesapSilButon.setOnClickListener {
            hesapSil()
        }

        return view
    }

    private fun exit() {
        Log.d("ProfilFragment", "Exit fonksiyonu çağrıldı.")
        auth.signOut()
        val intent = Intent(requireContext(), GirisActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun ayarlarSayfaGec() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigate(R.id.action_profilFragment_to_ayarlarFragment)
    }

    private fun kullaniciListelemeSayfaGec() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigate(R.id.action_profilFragment_to_kullaniciListeleme)
    }

    private fun hesapSil() {
        val user = auth.currentUser

        // Kullanıcının email'ini al
        val userEmail = user?.email

        // Şifre girişi için dialog göster
        val builder = AlertDialog.Builder(requireContext())
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "Şifrenizi girin"

        builder.setTitle("Hesap Silme")
            .setMessage("Hesabınızı silmek için şifrenizi girin:")
            .setView(input)
            .setPositiveButton("Sil") { dialog, _ ->
                val password = input.text.toString()

                if (password.isNotEmpty() && userEmail != null) {
                    // Kullanıcıyı yeniden doğrula
                    val credential = EmailAuthProvider.getCredential(userEmail, password)

                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            // Yeniden doğrulama başarılı, şimdi hesabı silebiliriz

                            // Önce Realtime Database'den kullanıcı verilerini sil
                            val userRef = FirebaseDatabase.getInstance().reference
                                .child("Kullanicilar")
                                .child(user.uid)

                            userRef.removeValue()
                                .addOnSuccessListener {
                                    // Şimdi Authentication'dan hesabı sil
                                    user.delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(),
                                                "Hesabınız başarıyla silindi",
                                                Toast.LENGTH_LONG).show()

                                            // Giriş sayfasına yönlendir
                                            auth.signOut()
                                            val intent = Intent(requireContext(), GirisActivity::class.java)
                                            startActivity(intent)
                                            requireActivity().finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(requireContext(),
                                                "Hesap silme hatası: ${e.message}",
                                                Toast.LENGTH_LONG).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(),
                                        "Veritabanı silme hatası: ${e.message}",
                                        Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(),
                                "Kimlik doğrulama hatası: ${e.message}",
                                Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(requireContext(),
                        "Lütfen şifrenizi girin",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}
