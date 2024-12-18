package com.muhammedaliderindag.sarisayfalar.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.muhammedaliderindag.sarisayfalar.dataclass.UrunDataClass
import com.muhammedaliderindag.sarisayfalar.UrunDetayActivity
import com.muhammedaliderindag.sarisayfalar.databinding.RvItemBinding
import com.squareup.picasso.Picasso

class UrunAdapter(private val context: Context, private val urunList: List<UrunDataClass>) :
    RecyclerView.Adapter<UrunAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: RvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SuspiciousIndentation")
        fun bind(urun: UrunDataClass) {
            binding.urunIsmi.text = urun.urunIsim
            binding.urunAciklama.text = urun.urunAciklama
            binding.urunFiyat.text = "${urun.urunFiyat?.toString()} TL"
            binding.urunStok.text = "Stok ${urun.urunStok?.toString()}"

            // Picasso kütüphanesi ile resmi yükleyin (Varsa)

            Picasso.get().load(urun.newimageurl).into(binding.imgItem)

            binding.root.setOnClickListener {
                val intent = Intent(context, UrunDetayActivity::class.java)
                intent.putExtra("urunId", urun.urunId)
                intent.putExtra("urunIsim", urun.urunIsim)
                intent.putExtra("urunAciklama", urun.urunAciklama)
                intent.putExtra("urunFiyat", urun.urunFiyat)
                intent.putExtra("urunStok", urun.urunStok)
                intent.putExtra("newimageurl", urun.newimageurl)
                context.startActivity(intent)
            }
        }
        fun clear() {
            binding.urunIsmi.text = null
            binding.urunAciklama.text = null
            binding.urunFiyat.text = null
            binding.urunStok.text = null
            binding.imgItem.setImageDrawable(null)
            binding.root.setOnClickListener(null)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RvItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.clear()

        holder.bind(urunList[position])
    }

    override fun getItemCount(): Int {
        return urunList.size
    }
}
