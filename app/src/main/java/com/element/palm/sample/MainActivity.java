package com.element.palm.sample;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import com.element.camera.ElementPalmAuthActivity;
import com.element.camera.ElementPalmEnrollActivity;
import com.element.camera.ProviderUtil;
import com.element.camera.UserInfo;
import com.element.common.PermissionUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static int ENROLL_REQ_CODE  = 12800;
    private static int AUTH_REQ_CODE    = 12801;

    private FloatingActionButton fab;
    private TextView userName;

    private List<UserInfo> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UserDataFormFragment userDataFormFragment = new UserDataFormFragment();
                userDataFormFragment.setMainActivity(MainActivity.this);
                userDataFormFragment.show(getSupportFragmentManager(), null);

                getUsers();

                if(!users.isEmpty()){
                    toastMessage(getString(R.string.msg_already_enrolled, users.get(0).getFullName()));
                }
            }
        });

        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUsers();

                if(users.isEmpty()){
                    showMessage(getString(R.string.enroll_first));
                }else{
                    startAuth(users.get(0).userId);
                }
            }
        });
    }

    private void getUsers(){
        users = ProviderUtil.getUsers(getBaseContext(), getPackageName(), null);
    }

    private void init(){
        fab = findViewById(R.id.fab);
        userName = findViewById(R.id.userName);
        getUsers();
    }

    public void startEnroll(String userId) {
        Intent intent = new Intent(this, ElementPalmEnrollActivity.class);
        intent.putExtra(ElementPalmEnrollActivity.EXTRA_ELEMENT_USER_ID, userId);
        startActivityForResult(intent, ENROLL_REQ_CODE);
    }

    private void startAuth(String userId) {
        Intent intent = new Intent(this, ElementPalmAuthActivity.class);
        intent.putExtra(ElementPalmAuthActivity.EXTRA_ELEMENT_USER_ID, userId);
        startActivityForResult(intent, AUTH_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ENROLL_REQ_CODE) {
            if (resultCode == this.RESULT_OK) {
                // User enrolled successfully
                showMessage(getString(R.string.enroll_completed));
            } else {
                // Enrollment cancelled
                showMessage(getString(R.string.enroll_cancelled));
            }
        } else if (requestCode == AUTH_REQ_CODE) {
            if (resultCode == this.RESULT_OK) {
                String userId = data.getStringExtra(ElementPalmAuthActivity.EXTRA_ELEMENT_USER_ID);
                UserInfo userInfo = ProviderUtil.getUser(getBaseContext(), BuildConfig.APPLICATION_ID, userId);
                String results = data.getStringExtra(ElementPalmAuthActivity.EXTRA_RESULTS);


                if (ElementPalmAuthActivity.USER_VERIFIED.equals(results)) {
                    // The user is verified
                    showMessage(getString(R.string.msg_verified, userInfo.getFullName()));
                } else if (ElementPalmAuthActivity.USER_FAKE.equals(results)) {
                    // the user was spoofing
                    showMessage(getString(R.string.msg_fake));
                } else {
                    // The user is not verified
                    showMessage(getString(R.string.msg_not_verified));
                }
            } else {
                // Verification cancelled
                showMessage(getString(R.string.auth_cancelled));
            }
        }
    }

    private void showUserView(){
        getUsers();
        if (users.isEmpty()) {
            userName.setVisibility(View.GONE);
        } else {
            userName.setVisibility(View.VISIBLE);
            userName.setText(users.get(0).getFullName());
        }
    }

    private void showMessage(String message) {
        Snackbar.make(
                findViewById(R.id.constraintLayout),
                message, Snackbar.LENGTH_SHORT
        ).show();
    }

    private void toastMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Show required permissions
        PermissionUtils.verifyPermissions(
                this,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        );

        showUserView();
    }
}
