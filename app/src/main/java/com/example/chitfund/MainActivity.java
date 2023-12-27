package com.example.chitfund;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    protected EditText etPhoneNumber, etName, etScheme;
    protected Button btnSendData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        btnSendData.setOnClickListener(view -> {
            if (!etPhoneNumber.getText().toString().isEmpty() &&
            !etName.getText().toString().isEmpty() &&
            !etScheme.getText().toString().isEmpty()) {
                sendDataToExcel( etPhoneNumber.getText().toString(), etName.getText().toString(), etScheme.getText().toString());

                etPhoneNumber.setText("");
                etName.setText("");
                etScheme.setText("");
            }
        });
    }

    private void sendDataToExcel(String phNumber, String name, String scheme) {
        String baseURL = "https://script.google.com/macros/s/AKfycbzRsvJb3PBQ7vnruPWnePu9xgqEWEYJq0hXva_EhoPG-WFIuSDeGbv0a_TN2A7EAbdFNg/exec?";
        String createURL = baseURL+"action=create&phNumber="+phNumber+"&name="+name+"&scheme="+scheme;
//        https://script.google.com/macros/s/AKfycbzRsvJb3PBQ7vnruPWnePu9xgqEWEYJq0hXva_EhoPG-WFIuSDeGbv0a_TN2A7EAbdFNg/exec?
//        action=create&phNumber=9876543210&name=Hello&scheme=10

        StringRequest stringRequest = new StringRequest(Request.Method.GET, createURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(MainActivity.this, "Data inserted", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", error.toString());
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void initViews() {
        etPhoneNumber = findViewById(R.id.et_phNumber);
        etName = findViewById(R.id.et_name);
        etScheme = findViewById(R.id.et_scheme);
        btnSendData = findViewById(R.id.btn_sendData);
    }
}