package com.daftar.taqwimplanetarium

class LabelXYT(
    var x: Float,
    var y: Float,
    var z: Float,
    var azimuth: String,
    var altitude: String
) {
    var x2d: Float = 0f
    var y2d: Float = 0f
    var z2d: Float = 0f

    override fun toString(): String {
        return "$azimuth/$altitude: $x/$y/$z -> $x2d/$y2d/$z2d"
    }
}
