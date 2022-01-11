package com.gyso.gysotreeviewapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class TestForceDirectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new ForceDirectKotlin(this));
    }
}