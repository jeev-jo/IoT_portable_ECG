package com.example.ecg_lead;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private WebSocketClient mWebSocketClient;
    //EditText res;
    Button st;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            uri = new URI("ws://192.168.43.114/ws");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from Android!");
            }


            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(() -> {
                    // Inside the WebSocketClient.onMessage() method
                    int dataPoint = Integer.parseInt(message);
                    LineChart chart = findViewById(R.id.chart);
                    //res = findViewById(R.id.res);
                    //res.append(s);
                    LineData lineData = chart.getData();
                    if (lineData != null) {
                        ILineDataSet dataSet = lineData.getDataSetByIndex(0);
                        if (dataSet == null) {
                            dataSet = createSet();
                            lineData.addDataSet(dataSet);
                        }
                        dataSet.addEntry(new Entry(dataSet.getEntryCount(), dataPoint));
                        lineData.notifyDataChanged();
                        chart.notifyDataSetChanged();
                        chart.setVisibleXRangeMaximum(120); // Adjust visible range to show last 120 entries
                        chart.moveViewToX(lineData.getEntryCount()); // Move the view to the end of the data
                    }
                });
            }

            private LineDataSet createSet() {
                LineDataSet set = new LineDataSet(null, "ECG Data");
                set.setLineWidth(2.5f);
                set.setColor(Color.BLUE);
                set.setDrawCircles(false);
                set.setDrawValues(false);
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


