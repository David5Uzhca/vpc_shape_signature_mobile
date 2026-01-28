package com.example.shapesignatureapp

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.example.shapesignatureapp.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var corpusClasses: Array<String> = arrayOf()
    private var corpusSignatures: Array<FloatArray> = arrayOf()
    
    // Variables para el reporte
    private var lastResult: String = ""
    private var lastAccuracy: String = ""
    private var lastDistance: String = ""
    private var lastDescriptor: String = ""
    private var lastBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "Error al cargar OpenCV", Toast.LENGTH_LONG).show()
        }

        val corpus = loadCorpusFromAssets()
        corpusClasses = corpus.keys.toTypedArray()
        corpusSignatures = corpus.values.toTypedArray()

        binding.btnClear.setOnClickListener {
            binding.drawingView.clearCanvas()
            binding.resultText.text = "Dibuja una figura"
            binding.detailsText.text = "Precisión: --% | Distancia: --"
            binding.descriptorText.text = "Esperando reconocimiento..."
            lastBitmap = null
        }

        binding.btnRecognize.setOnClickListener {
            recognizeDrawing()
        }

        binding.btnReport.setOnClickListener {
            saveReport()
        }
    }

    private fun recognizeDrawing() {
        // Obtenemos una copia para no interferir con la vista
        val bitmap = binding.drawingView.getBitmap()
        if (bitmap == null) {
            binding.resultText.text = "Error al obtener imagen"
            return
        }
        lastBitmap = bitmap
        
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        
        // Llamada JNI
        val rawResult = classifyShape(mat.nativeObjAddr, corpusClasses, corpusSignatures)
        
        val parts = rawResult.split("|")
        if (parts.size >= 4) {
            lastResult = parts[0]
            lastAccuracy = parts[1]
            lastDistance = parts[2]
            lastDescriptor = parts[3]
            
            // Actualizar UI con los nuevos campos
            binding.resultText.text = "Resultado: $lastResult"
            binding.detailsText.text = "Precisión: $lastAccuracy% | Distancia: $lastDistance"
            binding.descriptorText.text = lastDescriptor.replace(",", ", ")
            
        } else {
            binding.resultText.text = "Error en reconocimiento"
        }
        
        mat.release()
    }

    private fun saveReport() {
        if (lastBitmap == null) {
            Toast.makeText(this, "No hay nada que reportar. Primero pulsa Reconocer.", Toast.LENGTH_SHORT).show()
            return
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileNameImage = "SHAPE_$timeStamp.png"
        val fileNameText = "REPORT_$timeStamp.txt"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                
                // 1. Guardar Imagen
                val contentValuesImage = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileNameImage)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/ShapeSignatureReports")
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValuesImage)
                imageUri?.let { uri ->
                    val out: OutputStream? = resolver.openOutputStream(uri)
                    out?.use {
                        lastBitmap?.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }

                // 2. Guardar Reporte TXT
                val contentValuesText = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileNameText)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/ShapeSignatureReports")
                }
                val textUri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValuesText)
                textUri?.let { uri ->
                    val out: OutputStream? = resolver.openOutputStream(uri)
                    out?.use {
                        val reportContent = "Fecha: $timeStamp\n" +
                                "Predicción: $lastResult\n" +
                                "Confianza: $lastAccuracy%\n" +
                                "Distancia: $lastDistance\n\n" +
                                "Descriptor de Fourier:\n$lastDescriptor"
                        it.write(reportContent.toByteArray())
                    }
                }
                Toast.makeText(this, "Reporte guardado en carpeta Download", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "SDK insuficiente para MediaStore", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCorpusFromAssets(): Map<String, FloatArray> {
        val corpus = mutableMapOf<String, FloatArray>()
        try {
            val reader = BufferedReader(InputStreamReader(assets.open("class_averages.txt")))
            reader.useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(",")
                    if (parts.size > 1) {
                        val className = parts[0]
                        val signature = parts.subList(1, parts.size).map { it.toFloat() }.toFloatArray()
                        corpus[className] = signature
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return corpus
    }

    external fun classifyShape(matAddr: Long, classes: Array<String>, signatures: Array<FloatArray>): String

    companion object {
        init {
            System.loadLibrary("shapesignatureapp")
        }
    }
}
