package com.example.ecg_lead;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import uk.me.berndporr.iirj.Butterworth;


public class MainActivity extends AppCompatActivity {

    private WebSocketClient mWebSocketClient;
    private TextView res;

    private LineChart chart;
    private List<Double> ecgData;


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
        //normal ecg data set gpt
        //ecgData = List.of(-0.002,-0.008,-0.012,-0.008,-0.002,0.008,0.020,0.026,0.024,0.018,0.010,0.000,-0.008,-0.012,-0.010,-0.006,0.000,0.008,0.012,0.008,0.002,-0.006,-0.010,-0.008,-0.004,0.000,0.004,0.008,0.008,0.006,0.002,-0.002,-0.004,-0.002,0.002,0.008,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002,0.006,0.010,0.008,0.002,-0.006,-0.010,-0.008,-0.002);
        //example data gpt
        //ecgData = List.of(0.2, 0.4, 0.6, 0.8, 1.0, 0.9, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.2, 0.4, 0.6, 0.8, 1.0, 0.9, 0.7, 0.6);
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
                if(ecgData == null) {
                    Toast.makeText(MainActivity.this, "Error...... Click start first", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveToPdf(ecgData);
            }
        });
    }


    public void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://192.168.43.189/ws");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from Android!");
                res.setText("Web Socket Connected");
            }

            @Override
            public void onMessage(String s) {
                //List<Double> ecgData = new ArrayList<>();
                try {
                    // Convert the received value to a float
                    float value = Float.parseFloat(s);

                    // Apply Butterworth filter to incoming data
                    Butterworth butterworth = new Butterworth();
                    float filteredValue = (float) butterworth.filter(value);

                    // Get the chart's existing data and dataset
                    LineData data = chart.getData();
                    if (data != null) {
                        LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
                        if (set == null) {
                            set = createSet();
                            data.addDataSet(set);
                        }
                        // Add the new value to the dataset and notify the chart of the update
                        set.addEntry(new Entry(set.getEntryCount(), filteredValue));
                        data.notifyDataChanged();
                        chart.setDrawMarkers(true);
                        chart.notifyDataSetChanged();
                        // Scroll the chart to the right to show the latest data
                        chart.setVisibleXRangeMaximum(200);
                        chart.moveViewToX(data.getEntryCount() - 1);

                        // Save the ECG data to a CSV file with timestamp and creating a double list

                        try {
                            ecgData.add((double) filteredValue);
                            FileWriter writer = new FileWriter(getExternalFilesDir(null) + "/ecg_data.csv", true);
                            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                            int count = set.getEntryCount();
                            if (count <= 3000) { // 3000 samples = 30 seconds assuming 100 Hz sampling rate
                                String csvString = timestamp + "," + filteredValue + "\n";
                                writer.write(csvString);
                            }
                            writer.flush();
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        // Add the timestamped value to the list of ecg data
                        //EcgData ecgData = EcgData.getInstance();
                        //ecgData.add(new EntryWithTimestamp(filteredValue, System.currentTimeMillis()));
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    // Handle the exception
                }
            }


            // Helper method to create a LineDataSet
            public LineDataSet createSet() {
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

    public void saveToPdf(List<Double> ecgData) {
        // Create a new PDF document
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        String analysis = analyzeEcg(ecgData);


        // Create a new PDF document
        PdfDocument pdfDocument = new PdfDocument();

        // Set up the page dimensions and attributes
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                chart.getWidth(), chart.getHeight() + 200, 1)
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

        // Get the canvas dimensions
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        // Draw the date
        canvas.drawText("Date: " + dateFormat.format(date), 50, canvasHeight - 150, paint);

        // Draw the rhythm
        String[] lines = analysis.split(",");
        int y = 130;
        for (String line : lines) {
            // create a new StaticLayout for each line
            // draw the StaticLayout onto the canvas
            //canvas.save();
           //canvas.translate(50, canvas.getHeight() - 80);
            canvas.drawText(line,50,canvasHeight - y, paint);
            //canvas.restore();
            y-=20;
        }


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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebSocketClient.close();
    }



        public String analyzeEcg(List<Double> ecgData) {

            // Example indices for analyzing intervals and rhythm
            int[] indices = new int[3];
            indices = findQRSIndices(ecgData);
            int pStartIndex = indices[0];
            //int pEndIndex = 6;
            int qrsOnsetIndex = indices[1];
            int qrsEndIndex = indices[2];

            // Calculate intervals
            double prInterval = calculatePRInterval(ecgData, pStartIndex, qrsOnsetIndex);
            double qtInterval = calculateQTInterval(ecgData, qrsOnsetIndex, qrsEndIndex);
            double qrsDuration = calculateQRSDuration(ecgData, qrsOnsetIndex, qrsEndIndex);

            // Analyze rhythm
            String rhythm = analyzeRhythm(ecgData, qrsOnsetIndex, qrsEndIndex);
            String bpm = analyzeBpm(ecgData, qrsOnsetIndex, qrsEndIndex);

            // Provide feedback
            String feedback = " BPM: " + bpm + ",PR Interval: " + prInterval + " ms, QT Interval: " + qtInterval + " ms, QRS Duration: " + qrsDuration + " ms, Rhythm: " + rhythm;
            return feedback;


        }

    private String analyzeBpm(List<Double> ecgData, int qrsOnsetIndex, int qrsEndIndex) {
        int numQrsComplexes = countQrsComplexes(ecgData, qrsOnsetIndex, qrsEndIndex);
        double timePeriod = (qrsEndIndex - qrsOnsetIndex) * getSamplePeriod(ecgData);
        double qrsRate = numQrsComplexes / timePeriod;
        //int bpm = (int) (qrsRate * 60);
        return String.valueOf(qrsRate);
    }

    private double calculatePRInterval(List<Double> ecgData, int pStartIndex, int qrsOnsetIndex) {
            // Calculate the PR interval as the time between the P wave start and the QRS onset
            double prInterval = (qrsOnsetIndex - pStartIndex) * getSamplePeriod(ecgData);
            return prInterval;
        }

        private double calculateQTInterval(List<Double> ecgData, int qrsOnsetIndex, int qrsEndIndex) {
            // Calculate the QT interval as the time between the QRS onset and the end of the T wave
            double qtInterval = (qrsEndIndex - qrsOnsetIndex) * getSamplePeriod(ecgData);
            return qtInterval;
        }

        private double calculateQRSDuration(List<Double> ecgData, int qrsOnsetIndex, int qrsEndIndex) {
            // Calculate the QRS duration as the time between the QRS onset and the QRS end
            double qrsDuration = (qrsEndIndex - qrsOnsetIndex) * getSamplePeriod(ecgData);
            return qrsDuration;
        }

        private String analyzeRhythm(List<Double> ecgData, int qrsOnsetIndex, int qrsEndIndex) {
            // Find the rhythm based on the number of QRS complexes in a given time period
            int numQrsComplexes = countQrsComplexes(ecgData, qrsOnsetIndex, qrsEndIndex);
            double timePeriod = (qrsEndIndex - qrsOnsetIndex) * getSamplePeriod(ecgData);
            double qrsRate = numQrsComplexes / timePeriod;
            if (qrsRate < 60) {
                return "Bradycardia";
            } else if (qrsRate > 100) {
                return "Tachycardia";
            } else {
                return "Normal sinus rhythm";
            }
        }

        private int countQrsComplexes(List<Double> ecgData, int qrsOnsetIndex, int qrsEndIndex) {
            int count = 0;
            boolean inQrsComplex = false;
            int QRS_THRESHOLD = (int) findQrsThreshold(ecgData);

            for (int i = qrsOnsetIndex; i <= qrsEndIndex; i++) {
                double signalValue = ecgData.get(i);

                // Check if the signal is part of a QRS complex
                if (signalValue > QRS_THRESHOLD && !inQrsComplex) {
                    inQrsComplex = true;
                    count++;
                } else if (signalValue < QRS_THRESHOLD && inQrsComplex) {
                    inQrsComplex = false;
                }
            }

            return count;
        }
        public double getSamplePeriod(List<Double> ecgData) {
            double sampleFrequency = 360.0; // replace with your actual sample frequency
            double samplePeriod = 1.0 / sampleFrequency;
            return samplePeriod;
        }
        public double estimateQrsThreshold(List<Double> ecgData) {
            // Calculate the median absolute deviation (MAD) of the ECG signal
            double[] array = ecgData.stream().mapToDouble(Double::doubleValue).toArray();
            double median = median(array);

            double[] deviations = new double[ecgData.size()];
            for (int i = 0; i < ecgData.size(); i++) {
                deviations[i] = Math.abs(ecgData.get(i) - median);
            }
            double mad = median(deviations);

            // Estimate the QRS threshold as a multiple of the MAD
            double thresholdMultiplier = 3;
            double qrsThreshold = thresholdMultiplier * mad;

            return qrsThreshold;
        }

        public double median(double[] values) {
            Arrays.sort(values);
            int middle = values.length / 2;
            if (values.length % 2 == 0) {
                double medianA = values[middle - 1];
                double medianB = values[middle];
                return (medianA + medianB) / 2.0;
            } else {
                return values[middle];
            }
        }
        private double findQrsThreshold(List<Double> ecgData) {
            double[] ecgArray = ecgData.stream().mapToDouble(Double::doubleValue).toArray();
            double median = median(ecgArray);
            double mad = medianAbsoluteDeviation(ecgArray);
            return median + 1.4826 * mad;
        }
        private double medianAbsoluteDeviation(double[] data) {
            double median = median(data);
            double[] absoluteDeviations = new double[data.length];
            for (int i = 0; i < data.length; i++) {
                absoluteDeviations[i] = Math.abs(data[i] - median);
            }
            return median(absoluteDeviations);
        }
    public static int[] findQRSIndices(List<Double> ecgData) {
        int[] indices = new int[3];
        int qrsOnsetIndex = 0;
        int qrsEndIndex = 0;
        int maxIndex = 0;
        double maxValue = ecgData.get(0);

        // Find the maximum value and its index in the ecgData
        for (int i = 0; i < ecgData.size(); i++) {
            double currentValue = ecgData.get(i);
            if (currentValue > maxValue) {
                maxValue = currentValue;
                maxIndex = i;
            }
        }

        // Find the onset index of the QRS complex
        for (int i = maxIndex; i >= 0; i--) {
            if (ecgData.get(i) < 0.5 * maxValue) {
                qrsOnsetIndex = i + 1;
                break;
            }
        }

        // Find the end index of the QRS complex
        for (int i = maxIndex; i < ecgData.size(); i++) {
            if (ecgData.get(i) < 0.5 * maxValue) {
                qrsEndIndex = i - 1;
                break;
            }
        }

        // Find the start index of the P wave
        int pStartIndex = -1;
        for (int i = qrsOnsetIndex - 1; i >= 0; i--) {
            if (ecgData.get(i) < 0.25 * maxValue) {
                pStartIndex = i + 1;
                break;
            }
        }

        indices[0] = pStartIndex;
        indices[1] = qrsOnsetIndex;
        indices[2] = qrsEndIndex;
        //indices[3] = maxIndex;

        return indices;
    }



}



