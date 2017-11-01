package com.realpower.threelinkage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.realpower.threelinkage.view.PickAddressInterface;
import com.realpower.threelinkage.view.PickAddressView;

public class MainActivity extends AppCompatActivity {
    private PickAddressView pickView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickView = (PickAddressView) findViewById(R.id.pickView);
        Button btnPick = (Button) findViewById(R.id.btnPick);
        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pickView.getVisibility() == View.GONE) {
                    pickView.setVisibility(View.VISIBLE);
                }
            }
        });
        pickView.setOnTopClicklistener(new PickAddressInterface() {
            @Override
            public void onOkClick(String name) {
                Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelClick() {
                pickView.setVisibility(View.GONE);
            }
        });
    }
}
