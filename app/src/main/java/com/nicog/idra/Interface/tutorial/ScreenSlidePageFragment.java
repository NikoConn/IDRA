package com.nicog.idra.Interface.tutorial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.nicog.idra.Interface.MainActivity;
import com.nicog.idra.R;

public class ScreenSlidePageFragment extends Fragment {
    private int position;
    private ViewPager2 viewPager;

    private ImageView imageView;
    private ConstraintLayout layout;
    private ImageView nextButton;
    private TextView textView1;
    private TextView textView2;
    private ImageView step;

    private Button signinButton;

    public ScreenSlidePageFragment(int position, ViewPager2 viewPager){
        this.position = position;
        this.viewPager = viewPager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView;

        if(position == 3){
            rootView = (ViewGroup) inflater.inflate(R.layout.inicio_sesion, container, false);
            signinButton = rootView.findViewById(R.id.signinButton);
            signinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    googleSignIn();
                }
            });
        }else{
            rootView = (ViewGroup) inflater.inflate(R.layout.tutorialimagetext, container, false);
            imageView = rootView.findViewById(R.id.tutorialImageView);
            layout = rootView.findViewById(R.id.tutorialLayout);
            nextButton = rootView.findViewById(R.id.tutorialNextImageView);
            textView1 = rootView.findViewById(R.id.tutorialTextView1);
            textView2 = rootView.findViewById(R.id.tutorialTextView2);
            step = rootView.findViewById(R.id.stepImageView);

            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewPager.setCurrentItem(position+1);
                }
            });
        }

        switch (position){
            case 0:
                imageView.setImageResource(R.drawable.info1);
                step.setImageResource(R.drawable.step1);
                layout.setBackgroundResource(R.drawable.tutorial2);
                textView1.setText(R.string.tut2_1_1);
                textView2.setText(R.string.tut2_1_2);
                nextButton.setImageResource(R.drawable.next1);
                break;
            case 1:
                imageView.setImageResource(R.drawable.info2);
                step.setImageResource(R.drawable.step2);
                layout.setBackgroundColor(ContextCompat.getColor(rootView.getContext(), R.color.blue5));
                textView1.setText(R.string.tut2_2_1);
                textView2.setText(R.string.tut2_2_2);
                nextButton.setImageResource(R.drawable.next2);
                break;
            case 2:
                imageView.setImageResource(R.drawable.gota);
                step.setImageResource(R.drawable.step3);
                layout.setBackgroundResource(R.drawable.tutorial4);
                //textView1.setText(R.string.tut2_1_1);
                textView2.setText(R.string.tut3);
                nextButton.setImageResource(R.drawable.finish);
                break;
        }

        return rootView;
    }

    public void googleSignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this.getActivity().getApplicationContext(), gso);

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 7);
        signinButton.setEnabled(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 7) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                GoogleSignInAccount acc = result.getSignInAccount();
                AuthCredential credential = GoogleAuthProvider.getCredential(acc.getIdToken(), null);
                mAuth.signInWithCredential(credential).addOnCompleteListener(this.getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        openApp();
                    }
                });
            }else{
                signinButton.setEnabled(true);
                Log.i("signin", result.getStatus().toString());
            }
        }

    }

    private void openApp(){
        Intent i = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        startActivity(i);
        getActivity().finish();
    }



}
