package com.muhammedaliderindag.sarisayfalar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhammedaliderindag.sarisayfalar.adapter.SiparisAdapter
import com.muhammedaliderindag.sarisayfalar.dataclass.SiparisDataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminAnaSayfaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var siparisAdapter: SiparisAdapter
    private val siparisList = mutableListOf<SiparisDataClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_ana_sayfa, container, false)

        // RecyclerView ve LayoutManager'ı tanımla
        recyclerView = view.findViewById(R.id.rvSiparisler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // SiparisAdapter oluştur ve RecyclerView'a bağla
        siparisAdapter = SiparisAdapter(requireContext(), siparisList)
        recyclerView.adapter = siparisAdapter

        // Firebase veritabanı referansını tanımla
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance().reference.child("Siparisler").child(uid ?: "")

        // ValueEventListener ile verileri dinle
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Veritabanındaki her çocuk için döngü
                siparisList.clear()
                for (dataSnapshot in snapshot.children) {
                    // SiparisDataClass nesnesini al ve listeye ekle
                    val siparis = dataSnapshot.getValue(SiparisDataClass::class.java)
                    siparis?.let {
                        siparisList.add(it)
                    }
                }

                // Adapter'a değişiklikleri bildir
                siparisAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunu ele al
                // (İstediğiniz gibi işlemler ekleyebilirsiniz)
            }
        })

        return view

    }
    private fun onaylaSiparisler() {
        // Firebase işlemleri için gerekli referansları al
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // Siparisler düğümüne erişim sağla
        val siparislerRef = uid?.let { firebaseDatabase.reference.child("Siparisler").child(it) }

        // OnaylıSiparisler düğümüne erişim sağla
        val onayliSiparislerRef = uid?.let { firebaseDatabase.reference.child("OnayliSiparisler").child(it) }

        // Siparisler düğümündeki verileri OnayliSiparisler düğümüne taşı ve Siparisler düğümünü temizle
        siparislerRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Tüm siparişleri OnayliSiparisler düğümüne taşı
                    for (siparisSnapshot in snapshot.children) {
                        val siparisId = siparisSnapshot.key
                        val siparisData = siparisSnapshot.getValue(SiparisDataClass::class.java)

                        // OnayliSiparisler düğümüne ekle
                        siparisId?.let { onayliSiparislerRef?.child(it)?.setValue(siparisData) }
                    }

                    // Siparisler düğümünü temizle
                    siparislerRef.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumu
                Log.d("OnaylaSiparisler", "Siparisler düğümünden veri çekme hatası: ${error.message}")
            }
        })
    }
}
