package com.satchlapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.satchlapp.R;

public class TestActivity extends AppCompatActivity {

    private Button buttonWritingActivity;
    private Button buttonLoginActivity;
    private Button buttonMainActivity;
    private Button buttonReadingActivity;
    private Button buttonHomeActivity;
    private Button buttonStoryPreviewActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        buttonLoginActivity = (Button) findViewById(R.id.testActivityButtonLoginActivity);
        buttonMainActivity = (Button) findViewById(R.id.testActivityButtonMainActivity);
        buttonWritingActivity = (Button) findViewById(R.id.testActivityButtonWritingActivity);
        buttonReadingActivity = (Button) findViewById(R.id.testActivityButtonReadingActivity);
        buttonHomeActivity = (Button) findViewById(R.id.testActivityButtonHomeActivity);
        buttonStoryPreviewActivity = (Button) findViewById(R.id.testActivityButtonStoryPreviewActivity);

        buttonLoginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }
        });
        buttonMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });
        buttonWritingActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),WritingActivity.class);
                startActivity(intent);
            }
        });
        buttonReadingActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ReadingActivity.class);
                startActivity(intent);
            }
        });
        buttonHomeActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                startActivity(intent);
            }
        });
        buttonStoryPreviewActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),StoryPreviewActivity.class);
                startActivity(intent);
            }
        });
    }
}
