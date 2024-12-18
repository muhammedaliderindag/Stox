package com.muhammedaliderindag.sarisayfalar.dataclass

data class UrunDataClass(
    var urunId: String? = null,
    var urunIsim: String? = null,
    var newimageurl: String = "",
    var urunAciklama: String? = null,
    var urunFiyat: Int? = null,
    var urunStok: Int? = null,
    var urunAdet: Int? = null
)