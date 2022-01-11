package com.gyso.gysotreeviewapplication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.collections.ArrayList


data class Conf(val kq: Float, val ks: Float)
class Bean(val pos: PointF, val force: PointF) {
    var x = pos.x
    var y = pos.y
}


class ForceDirectKotlin constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null)

    private val paint: Paint = Paint()
    private val paintTxt: Paint = Paint()
    private val num = 10
    private val beans:ArrayList<Bean>
    private var random: Random
    private val conf: Conf

    init {
        paint.color = Color.CYAN
        paint.style = Paint.Style.FILL
        paintTxt.color = Color.BLACK
        paintTxt.textSize = 30f
        random = Random(30)
        conf = Conf(0.025f, 0.2f)
        beans = arrayListOf<Bean>().apply {
            for (i in 0..num) {
                val cx = random.nextInt(10) * 100
                val cy = random.nextInt(10) * 300
                add(i, Bean(PointF(cx.toFloat(), cy.toFloat()), PointF()))
            }
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        beans.forEachIndexed { index, bean ->
            run {
                canvas!!.drawCircle(bean.x, bean.y, 50f, paint)
                canvas.drawText(index.toString(), bean.x, bean.y, paintTxt)
            }
        }
    }
}