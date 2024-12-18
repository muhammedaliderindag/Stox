package com.muhammedaliderindag.sarisayfalar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.muhammedaliderindag.sarisayfalar.adapter.KullaniciAdapter
import com.muhammedaliderindag.sarisayfalar.databinding.FragmentKullaniciListelemeBinding
import com.muhammedaliderindag.sarisayfalar.dataclass.KullaniciBilgi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class KullaniciListeleme : Fragment() {

    private lateinit var binding: FragmentKullaniciListelemeBinding
    private lateinit var adapter: KullaniciAdapter
    private lateinit var kullaniciListesi: MutableList<KullaniciBilgi>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentKullaniciListelemeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kullaniciListesi = mutableListOf()
        adapter = KullaniciAdapter(requireContext(), kullaniciListesi)

        binding.kullaniciListesi.layoutManager = LinearLayoutManager(requireContext())
        binding.kullaniciListesi.adapter = adapter

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Firebase veritabanındaki "Kullanicilar" düğümüne erişim sağla
            databaseReference = FirebaseDatabase.getInstance().reference.child("Kullanicilar")

            // Kullanıcıları dinle ve RecyclerView'e ekle
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    kullaniciListesi.clear()
                    for (userSnapshot in snapshot.children) {
                        val kullanici = userSnapshot.getValue(KullaniciBilgi::class.java)
                        kullanici?.let {
                            kullaniciListesi.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Hata durumunda yapılacak işlemler
                }
            })
        }
    }



}
