package com.muhammedaliderindag.sarisayfalar

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.muhammedaliderindag.sarisayfalar.databinding.ActivityUrunYuklemeBinding
import com.muhammedaliderindag.sarisayfalar.dataclass.UrunDataClass
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UrunYuklemeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUrunYuklemeBinding
    private var filePath: Uri? = null

    private var storage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private lateinit var dbRef: DatabaseReference
    val PERMISSION_CODE = 1001

    private val resimSec =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                filePath = data?.data
                binding.secilmisResim.setImageURI(filePath)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUrunYuklemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().getReference("Urunler")

        binding.urunYukleButon.setOnClickListener {
            urunVerileriKaydet()
        }

        binding.resimSec.setOnClickListener {
            openImagePicker()
        }
    }

    private fun urunVerileriKaydet() {
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        val urnIsim = binding.yukleUrunIsim.text.toString()
        val urnAciklama = binding.yukleUrunAciklama.text.toString()
        val urnFiyatstr = binding.yukleUrunFiyat.text.toString()
        val urnFiyat = urnFiyatstr.toIntOrNull()
        val urnStokStr = binding.yukleUrunStok.text.toString()
        val urnStok: Int? = urnStokStr.toIntOrNull()

        if (urnIsim.isEmpty() || urnAciklama.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            return
        }

        if (filePath == null) {
            Toast.makeText(this, "Lütfen bir resim seçin", Toast.LENGTH_SHORT).show()
            return
        }

        val urnId = dbRef.push().key!!
        val urunler = UrunDataClass(urnId, urnIsim, "", urnAciklama, urnFiyat, urnStok)

        // Create a reference to the image in Firebase Storage
        val imageRef = storageReference!!.child("images/$urnId.jpg")

        // Upload the image to Firebase Storage
        imageRef.putFile(filePath!!)
            .addOnSuccessListener {
                // Image upload success, now update the UrunDataClass with the download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    urunler.newimageurl = uri.toString()

                    // Save the UrunDataClass to the database
                    dbRef.child(urnId).setValue(urunler)
                        .addOnCompleteListener {
                            Toast.makeText(this, "Veri başarıyla eklendi", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { err ->
                            Toast.makeText(this, "Hata: ${err.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { err ->
                Toast.makeText(
                    this,
                    "Resim yüklenirken hata oluştu: ${err.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun openImagePicker() {
        val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        resimSec.launch(pickImg)
    }


}
