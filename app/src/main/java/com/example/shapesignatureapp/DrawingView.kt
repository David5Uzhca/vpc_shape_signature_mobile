package com.example.shapesignatureapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var path = Path()
    private var drawPaint = Paint()
    private var canvasPaint = Paint(Paint.DITHER_FLAG)
    private val bufferSize = 1000f
    private val canvasBitmap: Bitmap = Bitmap.createBitmap(bufferSize.toInt(), bufferSize.toInt(), Bitmap.Config.ARGB_8888)
    private val drawCanvas: Canvas = Canvas(canvasBitmap)
    
    private val viewToBufferMatrix = Matrix()
    private val bufferToViewMatrix = Matrix()

    init {
        canvasBitmap.eraseColor(Color.BLACK)
        drawPaint.color = Color.WHITE
        drawPaint.isAntiAlias = true
        drawPaint.strokeWidth = 25f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            val viewRect = RectF(0f, 0f, w.toFloat(), h.toFloat())
            val bufferRect = RectF(0f, 0f, bufferSize, bufferSize)
            
            bufferToViewMatrix.setRectToRect(bufferRect, viewRect, Matrix.ScaleToFit.CENTER)
            bufferToViewMatrix.invert(viewToBufferMatrix)
        }
    }

    override fun onDraw(canvas: Canvas) {
        // 1. Dibujamos el contenido ya guardado en el bitmap (escalado)
        canvas.drawBitmap(canvasBitmap, bufferToViewMatrix, canvasPaint)
        
        // 2. Dibujamos el trazo que el usuario está haciendo en este momento
        canvas.save()
        canvas.concat(bufferToViewMatrix)
        canvas.drawPath(path, drawPaint)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Convertimos las coordenadas del toque (pantalla) a coordenadas del buffer (1000x1000)
        val pts = floatArrayOf(event.x, event.y)
        viewToBufferMatrix.mapPoints(pts)
        val x = pts[0]
        val y = pts[1]

        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
            MotionEvent.ACTION_UP -> {
                drawCanvas.drawPath(path, drawPaint)
                path.reset()
            }
            else -> return false
        }
        invalidate()
        return true
    }

    fun clearCanvas() {
        canvasBitmap.eraseColor(Color.BLACK)
        invalidate()
    }

    fun getBitmap(): Bitmap {
        // Devolvemos una copia del buffer interno
        return canvasBitmap.copy(Bitmap.Config.ARGB_8888, false)
    }
}
