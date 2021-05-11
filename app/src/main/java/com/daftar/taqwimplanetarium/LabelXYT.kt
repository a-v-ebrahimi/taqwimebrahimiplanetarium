package com.daftar.taqwimplanetarium

class LabelXYT(
    var x: Float,
    var y: Float,
    var z: Float,
    var label: String,
) {
    var x2d: Float = 0f
    var y2d: Float = 0f
    var z2d: Float = 0f

    override fun toString(): String {
        return "$label: $x/$y/$z -> $x2d/$y2d/$z2d"
    }
}
