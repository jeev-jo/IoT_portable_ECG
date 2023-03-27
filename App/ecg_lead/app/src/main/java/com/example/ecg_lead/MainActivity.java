package com.example.ecg_lead;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private WebSocketClient mWebSocketClient;
    private EditText res;
    private Button st;
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
        st = findViewById(R.id.b);
        st.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Connect to the WebSocket server
                connectWebSocket();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebSocketClient.close();
    }
}


