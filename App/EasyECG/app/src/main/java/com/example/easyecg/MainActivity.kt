package com.example.easyecg

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import uk.me.berndporr.iirj.Butterworth
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var mWebSocketClient: WebSocketClient
    private lateinit var res: TextView
    private lateinit var chart: LineChart
    private var ecgData: MutableList<Double> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        res = findViewById(R.id.res)
        chart = findViewById(R.id.chart)
        //chart.isTouchEnabled = true
        chart.isDragEnabled = true
        //chart.isScaleEnabled = true
        chart.setDrawMarkers(false)
        chart.description.isEnabled = false

        val data = LineData()
        chart.data = data

        val st = findViewById<Button>(R.id.b)
        ecgData = mutableListOf()

        st.setOnClickListener {
            connectWebSocket()
        }

        val rec = findViewById<Button>(R.id.record)
        rec.setOnClickListener {
            if (ecgData.isEmpty()) {
                Toast.makeText(this@MainActivity, "Error...... Click start first", Toast.LENGTH_SHORT).show()
            } else {
                saveToPdf(ecgData)
            }
        }
    }

    private fun connectWebSocket() {
        val uri: URI
        try {
            uri = URI("ws://192.168.43.189/ws")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }

        mWebSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(serverHandshake: ServerHandshake) {
                Log.i("Websocket", "Opened")
                mWebSocketClient.send("Hello from Android!")
                runOnUiThread {
                    res.text = "Web Socket Connected"
                }
            }

            override fun onMessage(s: String) {
                try {
                    val value = s.toFloat()

                    GlobalScope.launch(Dispatchers.Default) {
                        val butterworth = Butterworth()
                        val filteredValue = butterworth.filter(value.toDouble())

                        if (!filteredValue.isNaN()) {
                            withContext(Dispatchers.Main) {
                                drawData(filteredValue)
                                addData(filteredValue)
                            }
                        }
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }

            override fun onClose(i: Int, s: String, b: Boolean) {
                Log.i("Websocket", "Closed $s")
            }

            override fun onError(e: Exception) {
                Log.i("Websocket", "Error " + e.message)
            }
        }

        mWebSocketClient.connect()
    }

    private fun drawData(filteredValue: Double) {
        GlobalScope.launch(Dispatchers.Main) {
            val data = chart.data

            if (data != null) {
                var set = data.getDataSetByIndex(0) as LineDataSet?
                if (set == null) {
                    set = createSet()
                    data.addDataSet(set)
                }
                set.addEntry(Entry(set.entryCount.toFloat(), filteredValue.toFloat()))
                data.notifyDataChanged()
                chart.setDrawMarkers(true)
                chart.notifyDataSetChanged()
                chart.setVisibleXRangeMaximum(1000f)
                chart.moveViewToX(data.entryCount.toFloat() - 1)

                withContext(Dispatchers.IO) {
                    val writer = FileWriter(getExternalFilesDir(null).toString() + "/ecg_data.csv", true)
                    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())
                    var count = set.entryCount
                    if (count < 3000) {
                        val csvString = "$timestamp,$filteredValue\n"
                        count += count
                        writer.write(csvString)
                        writer.flush()
                        writer.close()
                    }
                }
            }
        }
    }

    private fun createSet(): LineDataSet {
        val set = LineDataSet(null, "ECG Data")
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.color = Color.RED
        set.lineWidth = 2f
        set.setDrawCircles(false)
        set.setDrawValues(false)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        return set
    }

    fun saveToPdf(ecgData: List<Double>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = Date()

        GlobalScope.launch(Dispatchers.IO) {
            val analysis = analyzeECGData(ecgData)

            withContext(Dispatchers.Main) {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(chart.width, chart.height + 200, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                chart.draw(canvas)

                val paint = Paint()
                paint.textSize = 20f
                paint.color = Color.BLACK

                val canvasHeight = canvas.height

                canvas.drawText("Date: " + dateFormat.format(date), 50f, canvasHeight - 150f, paint)

                val lines = analysis.split(",")
                var y = 130
                for (line in lines) {
                    canvas.drawText(line, 50f, canvasHeight - y.toFloat(), paint)
                    y -= 20
                }

                var normal = "Normal Range,Heart Rate: 60-80 BPM,P wave: 0.08-0.12 sec,PR Interval: 0.12-0.2 sec," +
                        "QT Interval: 0.35-0.45 sec,QRS Complex: 0.08-0.1 sec"
                val norm = normal.split(",")
                var xy = 150
                for (n in norm) {
                    canvas.drawText(n, 350f, canvasHeight - xy.toFloat(), paint)
                    xy -= 20
                }

                pdfDocument.finishPage(page)

                val fileName = "ecg_data$date.pdf"
                val file = File(getExternalFilesDir(null), fileName)
                try {
                    val outputStream = FileOutputStream(file)
                    pdfDocument.writeTo(outputStream)
                    outputStream.flush()
                    outputStream.close()
                    Toast.makeText(this@MainActivity,
                        "PDF saved to " + file.absolutePath,
                        Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                pdfDocument.close()
            }
        }
    }


    fun addData(filteredValue: Double) {
        ecgData.add(filteredValue)
    }

    fun analyzeECGData(ecgData: List<Double>): String {
        val pInterval = calculatePInterval(ecgData)
        val prInterval = calculatePRInterval(ecgData)
        val qtInterval = calculateQTInterval(ecgData)
        val qrsInterval = calculateQRSInterval(ecgData)
        val heartRate = calculateHeartRate(ecgData)

        val result = "Heart Rate: "+heartRate.toString()+" BPM,P Wave: "+pInterval.toString()+" sec,PR Interval: "+
                prInterval.toString()+" sec,QT Interval: "+qtInterval.toString()+ " sec,QRS Interval: "+
                qrsInterval.toString()+" / sec"

        return result
    }

    fun calculatePInterval(ecgData: List<Double>): Double {
        // TODO: Implement P interval calculation logic using ecgData
        // Example calculation:
        val pInterval = 0.09 // Placeholder value, replace with actual calculation
        return pInterval
    }

    fun calculatePRInterval(ecgData: List<Double>): Double {
        // TODO: Implement PR interval calculation logic using ecgData
        // Example calculation:
        val prInterval = 0.14 // Placeholder value, replace with actual calculation
        return prInterval
    }

    fun calculateQTInterval(ecgData: List<Double>): Double {
        // TODO: Implement QT interval calculation logic using ecgData
        // Example calculation:
        val qtInterval = 0.42 // Placeholder value, replace with actual calculation
        return qtInterval
    }

    fun calculateQRSInterval(ecgData: List<Double>): Double {
        // TODO: Implement QRS interval calculation logic using ecgData
        // Example calculation:
        val qrsInterval = 0.09 // Placeholder value, replace with actual calculation
        return qrsInterval
    }

    fun calculateHeartRate(ecgData: List<Double>): Double {
        // TODO: Implement heart rate calculation logic using the Pan-Tompkins algorithm and ecgData
        // Example calculation:
        val heartRate = 69.04 // Placeholder value, replace with actual calculation
        return heartRate
    }

   /* data class ECGAnalysisResult(
        val pInterval: Double,
        val prInterval: Double,
        val qtInterval: Double,
        val qrsInterval: Double,
        val heartRate: Double
    )*/


}

