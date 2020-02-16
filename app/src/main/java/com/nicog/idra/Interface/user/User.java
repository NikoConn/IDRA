package com.nicog.idra.Interface.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nicog.idra.R;
import com.squareup.picasso.Picasso;

public class User extends AppCompatActivity {
    ImageView userImageView;
    TextView nicknameTextView;

    private FirebaseUser user;

    private int fotoClickCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        userImageView = findViewById(R.id.userImageView);
        nicknameTextView = findViewById(R.id.nickTextView);

        fotoClickCount = 0;

        //set user
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    user = firebaseAuth.getCurrentUser();
                    //set User image
                    Picasso.get().load(user.getPhotoUrl()).into(userImageView);
                    nicknameTextView.setText(user.getDisplayName());
                }
            }
        });
    }

    public void fotoClick(View v){
        fotoClickCount++;
        if (fotoClickCount == 10){
            String uid = user.getUid();

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
}
