package com.example.wangchengmeng_len.waveview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        WaveView waveView = findViewById(R.id.wave);
        waveView.startAnimation();
    }
}
