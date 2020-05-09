package com.nemido.linegraphexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.os.Bundle;

import com.nemido.linegraphexample.graphs.LineGraph;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        float[] x = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
        float[] y = {0, 43, 64, 57, 86, 43, 86, 79, 45, 84, 22,
                48, 59, 59, 59, 49, 48, 25, 48, 80, 35,
                76, 54, 24, 45, 87, 45, 76, 85, 23, 43};

        float[] a = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        float[] b = {0, 43, 64, 57, 86, 43, 199, 34, 8000};
        float[] c = {0, 2, 4, 8, 16, 32, 64, 128, 256};

        LineGraph lineGraph = findViewById(R.id.graph);
        lineGraph.add(a, b, "Net Sales");
        lineGraph.add(a, c, "Profit");
        lineGraph.add(x, y, "Sales");
    }
}
