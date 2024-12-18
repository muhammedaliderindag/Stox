package com.muhammedaliderindag.sarisayfalar.dataclass

data class SiparisDataClass(
    val userId: String? = null,
    val urunIsim: String? = null,
    val urunFiyat: Double? = null,
    val urunAdet: Int? = null,
    val toplamFiyat: Double? = null,
    val siparisTarihi: String? = null
)
