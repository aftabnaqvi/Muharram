package schedule.com.syed.muharram;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    //private EditText usernameView;
    //private EditText passwordView;

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.input_email) EditText mUserName;
    @BindView(R.id.input_password) EditText mPassword;
    @BindView(R.id.btn_login) Button mLoginButton;
    @BindView(R.id.link_signup)
    TextView mSignupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parse.initialize(this);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        mSignupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            // Start the Signup activity
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        mLoginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();


        ParseUser.logInInBackground(mUserName.getText().toString(), mPassword.getText().toString(), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                ParseUser.logOut();
                progressDialog.dismiss();
                mLoginButton.setEnabled(true);
                if (parseUser != null) {
                    if(parseUser.getBoolean("emailVerified")) {
                        if(parseUser.getBoolean("isAllowed")){
                            launchMainActivity();
                        } else {
                            alertDisplayerOkCancel("Authentication Failed", "Please send an email to Br. Mubasher Khan with your contact details for access.", 1);
                        }
                    } else {
                        alertDisplayer("Login Fail", "Please Verify Your Email first", 1);
                    }
                } else {
                    alertDisplayer("Login Fail", e.getMessage() + " Please re-try", 1);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        mLoginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        mLoginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String userName = mUserName.getText().toString();
        String password = mPassword.getText().toString();

        if (userName.isEmpty()) {
            mUserName.setError("enter a valid username.");
            valid = false;
        } else {
            mUserName.setError(null);
        }

        if (password.isEmpty()) {
            mPassword.setError("password should be more than one character.");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }


    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 1;
    }

    private boolean isEmpty(EditText text) {
        if (text.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    private void alertDisplayer(String title, String message, final int errorCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }


                });
        AlertDialog ok = builder.create();
        ok.show();
    }

    private void alertDisplayerOkCancel(String title, String message, final int errorCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Email", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                        switch(errorCode){
                            case 1:
                                launchEMailActivity();
                                break;
                        }
                    }


                });
        AlertDialog ok = builder.create();
        ok.show();
    }

    private void launchMainActivity(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void launchEMailActivity(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:"
                + "mubasher@comcast.net"
                + "?subject=" + "Please give me access to use AAM-SABA - Bay Area Majalis Schedule" + "&body=" + "");
        intent.setData(data);
        startActivity(intent);
    }
}