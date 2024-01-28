package com.example.chitfund;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {

    protected EditText etPhoneNumber, etName, etScheme;
    protected Button btnSendData, btnScanQR, btnGenerateQR;
    protected TextView tvShowData;
    protected ImageView ivShowQRCOde;
    protected Dialog dialog;

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

        btnScanQR.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                openCameraToScanQR();
            else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            else
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        });

        btnGenerateQR.setOnClickListener( v -> {
           customDialogForTakingInputToGenerateQR();
        });
    }
    private void customDialogForTakingInputToGenerateQR() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.qr_code_generater_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        EditText etName = dialog.findViewById(R.id.etGetName);
        EditText etScheme = dialog.findViewById(R.id.etGetScheme);
        EditText etPhoneNo = dialog.findViewById(R.id.etGetPhoneNo);
        Button generateButton = dialog.findViewById(R.id.btnGenerateQR);
        Button cancelButton = dialog.findViewById(R.id.btnCancelQR);
        ProgressBar pbGenerateQR  = dialog.findViewById(R.id.pbGenerateQR);

        generateButton.setOnClickListener(v -> {
            pbGenerateQR.setVisibility(View.VISIBLE);
            String data = etName.getText().toString() + ":" + etScheme.getText().toString() + ":" + etPhoneNo.getText().toString();
            generateQRCodeWithTheInput(data);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }
    private void generateQRCodeWithTheInput(String data) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300);

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            ivShowQRCOde.setImageBitmap(bitmap);

        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }
    private void sendDataToExcel(String phNumber, String name, String scheme) {
        String baseURL = "https://script.google.com/macros/s/AKfycbzRsvJb3PBQ7vnruPWnePu9xgqEWEYJq0hXva_EhoPG-WFIuSDeGbv0a_TN2A7EAbdFNg/exec?";
        String createURL = baseURL+"action=create&phNumber="+phNumber+"&name="+name+"&scheme="+scheme;

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
    private void openCameraToScanQR() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(true);
        integrator.setPrompt("Scan a QR code");
        integrator.initiateScan();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                parseResult(result.toString());
            } else
                Toast.makeText(this, "no data", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "No data in QR code", Toast.LENGTH_SHORT).show();
    }

    private void parseResult(String result) {
        String content[] = result.split(":");
        Log.d("TAG", content[2]);
        String data = content[2];
        separateCharsAndInts(data);
    }

    private void separateCharsAndInts(String data) {
        StringBuilder name = new StringBuilder();
        StringBuilder scheme = new StringBuilder();
        for (char c: data.toCharArray()) {
            if (Character.isLetter(c))
                name.append(c);
            else if (Character.isDigit(c))
                scheme.append(c);
        }

        etName.setText(name);
        etScheme.setText(scheme);
    }
    private void initViews() {
        etPhoneNumber = findViewById(R.id.et_phNumber);
        etName = findViewById(R.id.et_name);
        etScheme = findViewById(R.id.et_scheme);
        btnSendData = findViewById(R.id.btn_sendData);
        btnScanQR = findViewById(R.id.btn_scan_qr);
        tvShowData = findViewById(R.id.tv_retrieved_data);
        btnGenerateQR = findViewById(R.id.btn_generate_qr);
        ivShowQRCOde = findViewById(R.id.ivQRCodeShow);
    }
}