package com.example.ecg_lead;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebSocketClient mWebSocketClient;
    private EditText res;

    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        res = findViewById(R.id.res);
        chart = findViewById(R.id.chart);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawMarkers(false);
        chart.getDescription().setEnabled(false);

        // Create a new LineData object
        LineData data = new LineData();
        chart.setData(data);
        Button st = findViewById(R.id.b);
        st.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Connect to the WebSocket server
                connectWebSocket();
            }
        });
        Button rec = findViewById(R.id.record);
        rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToPdf();
            }
        });
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://192.168.43.189/ws");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from Android!");
                res.setText("");
            }

            @Override
            public void onMessage(String s) {
                // Convert the received value to a float
                float value = Float.parseFloat(s);
                // Get the chart's existing data and dataset
                LineData data = chart.getData();
                if (data != null) {
                    LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
                    if (set == null) {
                        set = createSet();
                        data.addDataSet(set);
                    }
                    // Add the new value to the dataset and notify the chart of the update
                    set.addEntry(new Entry(set.getEntryCount(), value));
                    data.notifyDataChanged();
                    chart.setDrawMarkers(true);
                    chart.notifyDataSetChanged();
                    // Scroll the chart to the right to show the latest data
                    chart.setVisibleXRangeMaximum(200);
                    chart.moveViewToX(data.getEntryCount() - 1);

                    // Save the ECG data to a CSV file with timestamp
                    try {
                        FileWriter writer = new FileWriter(getExternalFilesDir(null) + "/ecg_data.csv", true);
                        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                        String csvString = timestamp + "," + value + "\n";
                        writer.write(csvString);
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Add the timestamped value to the list of ecg data
                    //EcgData ecgData = EcgData.getInstance();
                    //ecgData.add(new EntryWithTimestamp(value, System.currentTimeMillis()));
                }
            }

            // Helper method to create a LineDataSet
            private LineDataSet createSet() {
                LineDataSet set = new LineDataSet(null, "ECG");
                set.setAxisDependency(YAxis.AxisDependency.LEFT);
                set.setColor(Color.RED);
                set.setDrawCircles(false);
                set.setDrawValues(false);
                set.setLineWidth(2f);
                set.setHighlightEnabled(false);
                set.setDrawHorizontalHighlightIndicator(false);
                set.setFillAlpha(65);
                set.setFillColor(Color.RED);
                return set;
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };

        mWebSocketClient.connect();
    }
    private void saveToPdf() {
        // Create a new PDF document
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        // Calculate BPM from the chart data
        //List<Float> chartData = (List<Float>) chart.getData();
        //float durationSeconds = chartData.size() / 60;
        //float bpm = chartData.size() / durationSeconds * 60;

        // Analyze the rhythm of the chart data
        //String rhythm = analyzeRhythm(chartData);

        // Create a new PDF document
        PdfDocument pdfDocument = new PdfDocument();

        // Set up the page dimensions and attributes
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                chart.getWidth(), chart.getHeight() + 150, 1)
                .create();

        // Start a new page in the document
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Draw the date, bpm, and rhythm at the top of the PDF page
        Canvas canvas = page.getCanvas();
        // Draw the chart on the PDF page canvas
        chart.draw(canvas);

        Paint paint = new Paint();
        paint.setTextSize(20);
        paint.setColor(Color.BLACK);
        canvas.drawText("Date: " + dateFormat.format(date), 50, 50, paint);
        //canvas.drawText("BPM: " + String.format("%.1f", bpm), 50, 80, paint);
        canvas.drawText("Rhythm: " + "Sinus Rhythm", chart.getWidth() - 350, 80, paint);



        // Finish the page
        pdfDocument.finishPage(page);

        // Save the document to a file
        String fileName = "ecg_data.pdf";
        File file = new File(getExternalFilesDir(null), fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            pdfDocument.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
            Toast.makeText(MainActivity.this,
                    "PDF saved to " + file.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close the document
        pdfDocument.close();
    }
    private String analyzeRhythm(List<Float> data) {
        // TODO: Implement rhythm analysis algorithm
        return "Sinus Rhythm";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebSocketClient.close();
    }
}


