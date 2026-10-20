package com.example.proyecto2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import java.util.LinkedList

class GraficaView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

  private val puntos = LinkedList<Float>()
  private val maxPuntos = 40
  private var maxRpmDinamico = 2000f

  private val paintLinea = Paint().apply {
    color = Color.parseColor("#00FFCC")
    strokeWidth = 6f
    style = Paint.Style.STROKE
    isAntiAlias = true
    strokeJoin = Paint.Join.ROUND
    strokeCap = Paint.Cap.ROUND
  }

  private val paintRelleno = Paint().apply {
    style = Paint.Style.FILL
    isAntiAlias = true
  }

  private val paintRejilla = Paint().apply {
    color = Color.parseColor("#22FFFFFF")
    strokeWidth = 2f
  }

  private val pathLinea = Path()
  private val pathRelleno = Path()

  fun agregarDato(rpm: Float) {
    puntos.add(rpm)
    if (puntos.size > maxPuntos) {
      puntos.removeFirst()
    }

    val maxActual = puntos.maxOrNull() ?: 0f
    maxRpmDinamico = maxOf(1000f, maxActual * 1.2f)
    invalidate()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    val alto = height.toFloat()
    val ancho = width.toFloat()

    // Rejilla de referencia
    for (i in 1..3) {
      val y = alto * (i / 4f)
      canvas.drawLine(0f, y, ancho, y, paintRejilla)
    }

    if (puntos.size < 2) return

    val pasoX = ancho / (maxPuntos - 1)
    pathLinea.reset()
    pathRelleno.reset()
    pathRelleno.moveTo(0f, alto)

    for (i in 0 until puntos.size) {
      val x = i * pasoX
      val y = (alto - (puntos[i] / maxRpmDinamico * alto)).coerceAtMost(alto).coerceAtLeast(0f)

      if (i == 0) {
        pathLinea.moveTo(x, y)
        pathRelleno.lineTo(x, y)
      } else {
        pathLinea.lineTo(x, y)
        pathRelleno.lineTo(x, y)
      }
    }

    val ultimoX = (puntos.size - 1) * pasoX
    pathRelleno.lineTo(ultimoX, alto)
    pathRelleno.close()

    // Sombreado inferior tipo neón
    paintRelleno.shader = LinearGradient(
      0f, 0f, 0f, alto,
      Color.parseColor("#4D00FFCC"),
      Color.TRANSPARENT,
      Shader.TileMode.CLAMP
    )

    canvas.drawPath(pathRelleno, paintRelleno)
    canvas.drawPath(pathLinea, paintLinea)
  }
}