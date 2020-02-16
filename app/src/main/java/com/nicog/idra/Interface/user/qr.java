package com.nicog.idra.Interface.user;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.nicog.idra.R;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class qr extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        Bundle extras = getIntent().getExtras();
        String uid = extras.getString("uid");

        ImageView qrImageView = findViewById(R.id.qrImageView);
        TextView textView = findViewById(R.id.uidTextView);

        textView.setText(uid);

        QRGEncoder qrgEncoder = new QRGEncoder(uid, null, QRGContents.Type.TEXT, 500);

        try {
            qrImageView.setImageBitmap(qrgEncoder.encodeAsBitmap());
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
