package com.muhammedaliderindag.sarisayfalar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sarisayfalar.dataclass.CurrencyResponse
import com.example.sarisayfalar.dataclass.UpdateTotalEvent
import com.muhammedaliderindag.sarisayfalar.adapter.SepetAdapter
import com.muhammedaliderindag.sarisayfalar.databinding.FragmentSepetBinding
import com.muhammedaliderindag.sarisayfalar.dataclass.UrunDataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.NumberFormat
import java.util.Locale

class SepetFragment : Fragment() {
    private lateinit var binding: FragmentSepetBinding
    private lateinit var sepetAdapter: SepetAdapter
    private val sepetUrunList = mutableListOf<UrunDataClass>()
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSepetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sepetAdapter = SepetAdapter(requireContext(), sepetUrunList)
        binding.sepetList.adapter = sepetAdapter
        binding.sepetList.layoutManager = LinearLayoutManager(requireContext())


        val client = OkHttpClient()
        val kur = binding.dovizkuru
        var trys:Double?
        fun fetchApiData() {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url("https://api.freecurrencyapi.com/v1/latest?apikey=fca_live_3nvCLvywlBr9VYzlwQO3U1AypK8A82CD90VUWjcS&currencies=TRY")
                        .header("content-type", "application/json")
                        .build()

                    val response = client.newCall(request).execute()
                    response.body?.string()?.let { responseData ->
                        val gson = Gson()
                        val result = gson.fromJson(responseData, CurrencyResponse::class.java)
                        val currencies = result.data
                        trys = currencies["TRY"]

                        withContext(Dispatchers.Main) {
                            kur.setText(getString(R.string.currency_format, trys))
                        }
                    }
                } catch (e: Exception) {
                    println("Hata: ${e.message}")
                }
            }
        }
        fetchApiData()


        // Sepet verilerini Firebase'den al
        verileriAl()

        // Sepeti temizle butonuna tıklanıldığında
        binding.btnSepetiTemizle.setOnClickListener {
            sepetiTemizle()
        }
        binding.btnSiparisVer.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (uid != null) {
                val sepetRef = FirebaseDatabase.getInstance().reference
                    .child("Kullanicilar")
                    .child(uid)
                    .child("Sepet")

                val siparislerRef = FirebaseDatabase.getInstance().reference
                    .child("Siparisler")

                sepetRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // Önce tüm ürünlerin stoklarını güncelle
                            snapshot.children.forEach { urunSnapshot ->
                                val urunId = urunSnapshot.child("urunId").getValue(String::class.java)
                                val siparisAdedi = urunSnapshot.child("urunAdet").getValue(Int::class.java)

                                if (urunId != null && siparisAdedi != null) {
                                    val urunlerRef = FirebaseDatabase.getInstance().reference
                                        .child("Urunler")
                                        .child(urunId)

                                    // Mevcut stok miktarını al ve güncelle
                                    urunlerRef.child("urunStok").get().addOnSuccessListener { dataSnapshot ->
                                        val mevcutStok = dataSnapshot.getValue(Int::class.java) ?: 0
                                        val yeniStok = mevcutStok - siparisAdedi
                                        if (yeniStok >= 0) {
                                            urunlerRef.child("urunStok").setValue(yeniStok)
                                        }
                                    }
                                }
                            }

                            // Sepet verilerini siparişlere ekle
                            siparislerRef.child(uid).setValue(snapshot.value) { databaseError, _ ->
                                if (databaseError == null) {
                                    // Sepeti temizle
                                    sepetRef.removeValue().addOnSuccessListener {
                                        sepetUrunList.clear()
                                        sepetAdapter.notifyDataSetChanged()
                                        guncelToplamFiyatGoster(0)
                                        Toast.makeText(context, "Sipariş verildi.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context,
                                        "Sipariş verilemedi: ${databaseError.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(context,
                                "Sepetiniz boş. Önce ürün ekleyin.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context,
                            "Veritabanı hatası: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }


    }
    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateTotal(event: UpdateTotalEvent) {
        // Toplam fiyatı güncelle
        verileriAl()
    }
    fun verileriAl() {
        val kullaniciId = auth.currentUser?.uid
        if (kullaniciId != null) {
            val sepetRef =
                FirebaseDatabase.getInstance().getReference("Kullanicilar/$kullaniciId/Sepet")

            // Sepet verilerini al ve RecyclerView'e ekle
            sepetRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    sepetUrunList.clear()
                    var toplamFiyat = 0

                    for (sepetSnapshot in snapshot.children) {
                        val urunIsim = sepetSnapshot.child("urunIsim").getValue(String::class.java)
                        val urunId = sepetSnapshot.child("urunId").getValue(String::class.java)
                        val urunAciklama =
                            sepetSnapshot.child("urunAciklama").getValue(String::class.java)
                        val newimageurl =
                            sepetSnapshot.child("newimageurl").getValue(String::class.java)
                        val urunStok =
                            sepetSnapshot.child("urunStok").getValue(Int::class.java)
                        val urunAdet = sepetSnapshot.child("urunAdet").getValue(Int::class.java)
                        val urunFiyat = sepetSnapshot.child("urunFiyat").getValue(Int::class.java)

                        if (urunIsim != null && urunFiyat != null && urunAdet != null) {
                            val sepetUrun = UrunDataClass(
                                urunId,
                                urunIsim,
                                newimageurl!!,
                                urunAciklama,
                                urunFiyat,
                                urunStok,
                                urunAdet
                            )
                            sepetUrunList.add(sepetUrun)
                            toplamFiyat += urunFiyat * urunAdet
                        }
                    }

                    guncelToplamFiyatGoster(toplamFiyat)
                    sepetAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Hata durumu
                }
            })
        }
    }

    private fun guncelToplamFiyatGoster(toplamFiyat: Int) {
        val formattedToplamFiyat = NumberFormat.getCurrencyInstance(Locale("tr", "TR")).format(toplamFiyat)
        binding.toplamFiyat.text = formattedToplamFiyat
    }

    private fun sepetiTemizle() {
        val kullaniciId = auth.currentUser?.uid
        if (kullaniciId != null) {
            val sepetRef =
                FirebaseDatabase.getInstance().getReference("Kullanicilar/$kullaniciId/Sepet")
            sepetRef.removeValue()

            guncelToplamFiyatGoster(0)
            sepetUrunList.clear()
            sepetAdapter.notifyDataSetChanged();
            Toast.makeText(context, "Sepetiniz temizlenmiştir.", Toast.LENGTH_SHORT).show()
        }
    }
}
