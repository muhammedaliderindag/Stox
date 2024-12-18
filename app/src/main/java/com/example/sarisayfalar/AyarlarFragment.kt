package com.muhammedaliderindag.sarisayfalar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.muhammedaliderindag.sarisayfalar.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class AyarlarFragment : Fragment() {

    private lateinit var editTextEmail: EditText
    private lateinit var emailGuncelleButon: Button
    private lateinit var editTextPassword: EditText
    private lateinit var changePassButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ayarlar, container, false)

        editTextEmail = view.findViewById(R.id.editTextTextEmailAddress)
        emailGuncelleButon = view.findViewById(R.id.emailGuncelleButon)
        editTextPassword = view.findViewById(R.id.editTextTextPassword)
        changePassButton = view.findViewById(R.id.changePassButon)
        auth = FirebaseAuth.getInstance()

        emailGuncelleButon.setOnClickListener {
            guncelleEmail()
            profilGeriDon()

        }
        changePassButton.setOnClickListener {
            guncelleSifre()
            profilGeriDon()
        }

        return view
    }

    private fun guncelleEmail() {
        val yeniEmail = editTextEmail.text.toString()
        if (yeniEmail.isNotEmpty()) {
            val user = Firebase.auth.currentUser
            user!!.updateEmail(yeniEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val database = FirebaseDatabase.getInstance()
                        val myRef = database.getReference("Kullanicilar")

                        myRef.child(user.uid).child("email").setValue(yeniEmail)
                    } else {

                    }
                }
        } else {
            Toast.makeText(
                requireContext(),
                "E-mail Girmediniz.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun guncelleSifre() {
        val newPassword = editTextPassword.text.toString()
        if (newPassword.isNotEmpty()) {
            val user = Firebase.auth.currentUser
            user!!.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Şifre güncelleme başarılı
                        Toast.makeText(
                            requireContext(),
                            "Şifre başarıyla güncellendi",
                            Toast.LENGTH_SHORT
                        ).show()
                        // İstediğiniz ek işlemleri burada gerçekleştirebilirsiniz
                    } else {
                        // Hata durumunda kullanıcıya bilgi vermek için burada işlemleri gerçekleştirebilirsiniz
                        Log.d(
                            "AyarlarFragment",
                            "Şifre güncelleme başarısız: ${task.exception?.message}"
                        )
                        Toast.makeText(
                            requireContext(),
                            "Şifre güncelleme başarısız: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(
                requireContext(),
                "Yeni Şifre Girmediniz.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun profilGeriDon() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigate(R.id.action_ayarlarFragment_to_profilFragment)
    }
}
