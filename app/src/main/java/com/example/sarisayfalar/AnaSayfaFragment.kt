package com.muhammedaliderindag.sarisayfalar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sarisayfalar.dataclass.CurrencyResponse
import com.muhammedaliderindag.sarisayfalar.adapter.UrunAdapter
import com.muhammedaliderindag.sarisayfalar.dataclass.UrunDataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import okhttp3.*
import okio.IOException
import com.google.gson.Gson

class AnaSayfaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var urunAdapter: UrunAdapter
    private val urunList = mutableListOf<UrunDataClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_ana_sayfa, container, false)

        // RecyclerView ve LayoutManager'ı tanımla
        recyclerView = view.findViewById(R.id.urunListe)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // com.muhammedaliderindag.sarisayfalar.adapter.UrunAdapter oluştur ve RecyclerView'a bağla
        urunAdapter = UrunAdapter(requireContext(), urunList)
        recyclerView.adapter = urunAdapter

        // Firebase veritabanı referansını tanımla
        val dbRef = FirebaseDatabase.getInstance().getReference("Urunler")

        // ValueEventListener ile verileri dinle
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                urunList.clear()
                // Veritabanındaki her çocuk için döngü
                for (dataSnapshot in snapshot.children) {
                    // UrunDataClass nesnesini al ve listeye ekle
                    val urun = dataSnapshot.getValue(UrunDataClass::class.java)
                    urun?.let {
                        urunList.add(it)
                    }
                }

                // Adapter'a değişiklikleri bildir
                urunAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunu ele al
                // (İstediğiniz gibi işlemler ekleyebilirsiniz)
            }
        })
        // AdminPanelView simgesine tıklanma işlemi
        val adminPanelView: ImageView = view.findViewById(R.id.homeAdminPanel)
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
                                    NavHostFragment.findNavController(this@AnaSayfaFragment)
                                navController.navigate(R.id.action_anaSayfaFragment_to_adminAnaSayfaFragment)
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
        val adminUrunYukleme: ImageView = view.findViewById(R.id.urunUpload)

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
                                adminUrunYukleme.visibility = View.VISIBLE
                                adminPanelView.visibility = View.VISIBLE
                            } else {
                                adminUrunYukleme.visibility = View.GONE
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


        adminUrunYukleme.setOnClickListener {
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
                                val intent = Intent(context, UrunYuklemeActivity::class.java)

                                context?.startActivity(intent)
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

        return view
    }
}
