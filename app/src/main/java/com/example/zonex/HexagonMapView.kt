package com.example.zonex

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.sin

class HexagonMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val hexRadius = 60f
    private val hexWidth = hexRadius * 2
    private val hexHeight = Math.sqrt(3.0).toFloat() * hexRadius

    private val hexPath = Path()

    // Paints
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    
    private val strokePaintUser = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f // Thicker for glow effect
    }
    
    private val strokePaintEnemy = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val fillPaintUser = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val fillPaintEnemy = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Coordinates of hexagons owned by user and enemy (col, row)
    val userHexagons = mutableSetOf(Pair(3, 3), Pair(3, 4), Pair(4, 3))
    val enemyHexagons = mutableSetOf(Pair(6, 2), Pair(6, 3), Pair(7, 2))

    init {
        // Resolve Colors using Context
        val colorBg = ContextCompat.getColor(context, R.color.zx_bg)
        val colorGridLine = ContextCompat.getColor(context, R.color.zx_grid_line)
        val colorPrimary = ContextCompat.getColor(context, R.color.zx_primary)
        val colorPrimaryAlpha = ContextCompat.getColor(context, R.color.zx_primary_alpha)
        val colorEnemy = ContextCompat.getColor(context, R.color.zx_enemy)
        val colorEnemyAlpha = ContextCompat.getColor(context, R.color.zx_enemy_alpha)

        setBackgroundColor(colorBg)
        
        strokePaint.color = colorGridLine
        
        strokePaintUser.color = colorPrimary
        fillPaintUser.color = colorPrimaryAlpha
        
        strokePaintEnemy.color = colorEnemy
        fillPaintEnemy.color = colorEnemyAlpha
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cols = (width / (hexWidth * 0.75f)).toInt() + 2
        val rows = (height / hexHeight).toInt() + 2

        for (row in -1..rows) {
            for (col in -1..cols) {
                val x = col * hexWidth * 0.75f
                val y = row * hexHeight + if (col % 2 == 1) hexHeight / 2 else 0f

                val isUser = userHexagons.contains(Pair(col, row))
                val isEnemy = enemyHexagons.contains(Pair(col, row))

                drawHexagon(canvas, x, y, isUser, isEnemy)
            }
        }
    }

    private fun drawHexagon(canvas: Canvas, cx: Float, cy: Float, isUser: Boolean, isEnemy: Boolean) {
        hexPath.reset()
        for (i in 0 until 6) {
            val angle = Math.toRadians((60 * i).toDouble())
            val x = cx + hexRadius * cos(angle).toFloat()
            val y = cy + hexRadius * sin(angle).toFloat()
            if (i == 0) hexPath.moveTo(x, y)
            else hexPath.lineTo(x, y)
        }
        hexPath.close()

        if (isUser) {
            canvas.drawPath(hexPath, fillPaintUser)
            canvas.drawPath(hexPath, strokePaintUser)
        } else if (isEnemy) {
            canvas.drawPath(hexPath, fillPaintEnemy)
            canvas.drawPath(hexPath, strokePaintEnemy)
        } else {
            canvas.drawPath(hexPath, strokePaint)
        }
    }

    fun captureNewHexagons(newHexs: List<Pair<Int, Int>>) {
        userHexagons.addAll(newHexs)
        invalidate() // Redraw
    }
}
