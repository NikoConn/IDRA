package com.nicog.idra.Interface.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nicog.idra.R;
import com.nicog.idra.logic.Service;
import com.squareup.picasso.Picasso;


public class User extends AppCompatActivity {
    private ImageView userImageView;
    private EditText nicknameTextView;
    private ImageView editNicknameImageView;
    private Button modMenuButton;
    private Button addAdminButton;

    private boolean editing = false;
    private int fotoClickCount;

    private Service service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        userImageView = findViewById(R.id.userImageView);
        nicknameTextView = findViewById(R.id.nicknameEditText);
        editNicknameImageView = findViewById(R.id.editNicknameImageView);
        modMenuButton = findViewById(R.id.modMenuButton);
        addAdminButton = findViewById(R.id.addAdminButton);

        fotoClickCount = 0;
        service = new Service();

        //set user
        Picasso.get().load(service.getUserPhotoUrl()).into(userImageView);

        service.getUserNickname(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String nick = documentSnapshot.getString("nickname");
                if(nick == null || nick.equals("")){
                    nicknameTextView.setText(getString(R.string.Anonymous));
                }else{
                    nicknameTextView.setText(documentSnapshot.getString("nickname"));
                }
            }
        });

        nicknameTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                editing = b;
                Log.i("b", String.valueOf(editing));
                if(b){
                    editNicknameImageView.setImageResource(android.R.drawable.ic_menu_save);
                }
            }
        });

        service.getUserPrivileges(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists()){return;}
                Boolean isAdmin = documentSnapshot.getBoolean("admin");
                Boolean isMod = documentSnapshot.getBoolean("mod");
                if(isAdmin == null){ isAdmin = false; }
                if(isMod == null){ isMod = false; }
                isMod = isMod || isAdmin;

                if(isMod) modMenuButton.setVisibility(View.VISIBLE);
                if(isAdmin) addAdminButton.setVisibility(View.VISIBLE);
            }
        });

    }

    private void openQrCamera(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 756);

        }else{
            Intent i = new Intent(this, qrReader.class);
            startActivityForResult(i, 546);
        }

    }

        public void fotoClick(View v){
        fotoClickCount++;
        if (fotoClickCount == 10){
            String uid = service.getUserUid();

            Intent i = new Intent(this, qr.class);
            i.putExtra("uid", uid);
            startActivity(i);

            fotoClickCount = 0;
        }
    }

    public void logout(View v){
        FirebaseAuth.getInstance().signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                    }
                });
    }

    public void onClickEdit(View v){
        Log.i("editing", String.valueOf(editing));
        if(editing){
            nicknameTextView.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(nicknameTextView.getWindowToken(), 0);

            editNicknameImageView.setImageResource(android.R.drawable.ic_menu_edit);

            service.changeNickname(nicknameTextView.getText().toString());
        }else{
            nicknameTextView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(nicknameTextView, InputMethodManager.SHOW_IMPLICIT);

            editNicknameImageView.setImageResource(android.R.drawable.ic_menu_save);
        }
    }

    public void addAdmin(View v){
        openQrCamera();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 546 && resultCode == RESULT_OK) {
            String uid = data.getExtras().getString("uid");
            service.addMod(uid);
            Toast.makeText(this, getText(R.string.Done), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if(requestCode == 756 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            openQrCamera();
        }
    }
}
