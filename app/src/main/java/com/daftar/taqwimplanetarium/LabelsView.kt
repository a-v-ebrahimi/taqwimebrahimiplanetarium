package com.daftar.taqwimplanetarium

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View


class LabelsView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    var list = arrayListOf<LabelXYT>()
    private var textPaint: Paint = Paint()

    init {
        val value = resources.displayMetrics.density

        textPaint.color = Color.BLACK
        textPaint.textSize = 8f * value
        textPaint.typeface = Typeface.MONOSPACE

    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            for (label in list)
                if (label.z2d > 0 && label.x2d > 0 && label.z2d < 1 && label.x2d < width) {
                    textPaint.color = Color.DKGRAY
                    it.drawText(
                        "${label.azimuth},${label.altitude}",
                        label.x2d,
                        height - label.y2d - 10,
                        textPaint
                    )
//                    textPaint.color = Color.RED
//                    it.drawText(label.altitude, label.x2d+40, height - label.y2d, textPaint)
                }

        }

    }
}