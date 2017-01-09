package com.kyaracter.annotation.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kyaracter.annotation.example.entity.ExampleEntity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DON'T forget Rebuild Project
        ExampleEntity exampleEntity = new ExampleEntity();
    }
}
