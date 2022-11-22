package com.example.bt3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class InfoActivity extends AppCompatActivity {
    Button ret;
    String[] phone;

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ret = findViewById(R.id.ret_btn);

        TextView nameTextV = findViewById(R.id.nameTextV);
        TextView macTextV = findViewById(R.id.macTextV);
        TextView classTextV = findViewById(R.id.classTextV);
        TextView bondTextV = findViewById(R.id.bondTextV);
        TextView typeTextV = findViewById(R.id.typeTextV);

        phone = (String[]) getIntent().getSerializableExtra("blphone");
        System.out.println(Arrays.toString(phone));

        if (phone[0] == null){
            nameTextV.setText("null");
        }else{
            nameTextV.setText(phone[0]);
        }

        macTextV.setText(phone[1]);
        classTextV.setText(phone[2]);
        bondTextV.setText(phone[4]);
        typeTextV.setText(phone[5]);

        ret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        phone=null;
    }
}