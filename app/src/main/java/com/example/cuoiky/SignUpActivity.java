package com.example.cuoiky;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText, repasswordEditText;
    private Button signUpButton;
    private DatabaseHelper dbHelper;
    private String verificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        repasswordEditText = findViewById(R.id.repassword);
        signUpButton = findViewById(R.id.signupbtn);

        dbHelper = new DatabaseHelper(this);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    private void signUp() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repassword = repasswordEditText.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || repassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 6 || username.length() > 32 || !username.matches("^[a-zA-Z0-9]+$")) {
            Toast.makeText(this, "Username must be between 6 and 32 characters and should not contain special characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8 || !password.matches("^(?=.*[a-zA-Z])(?=.*[0-9]).*$")) {
            Toast.makeText(this, "Password must be at least 8 characters and contain both letters and numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(repassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        showVerificationCodePopup();

    }
    private void showVerificationCodePopup() {
        View view = LayoutInflater.from(this).inflate(R.layout.verification_popup, null);
        EditText verificationCodeInput = view.findViewById(R.id.verification_code_input);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle("");
        builder.setCancelable(false);

        verificationCode = generateVerificationCode();
        sendVerificationCodeByEmail(emailEditText.getText().toString().trim(), verificationCode);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String enteredVerificationCode = verificationCodeInput.getText().toString().trim();
            verifyVerificationCode(enteredVerificationCode);
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void verifyVerificationCode(String enteredVerificationCode) {
        if (enteredVerificationCode.equals(verificationCode)) {
            new SignUpTask().execute(usernameEditText.getText().toString().trim(), emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim());
            //completeSignUp();
        } else {
            Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show();
        }
    }
    private class SignUpTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String username = params[0];
            String email = params[1];
            String password = params[2];

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                JSONObject jsonParams = new JSONObject();
                jsonParams.put("username", username);
                jsonParams.put("email", email);
                jsonParams.put("password", password);

                RequestBody requestBody = RequestBody.create(jsonParams.toString(), JSON);

                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/v1/user/signup")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                finish();
                if (response.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "Đăng kí thành công", Toast.LENGTH_SHORT).show();

                    return true;
                } else {
                    Toast.makeText(SignUpActivity.this, "Đăng kí thất bại", Toast.LENGTH_SHORT).show();

                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private void completeSignUp() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, usernameEditText.getText().toString().trim());
        values.put(DatabaseHelper.COLUMN_EMAIL, emailEditText.getText().toString().trim());
        values.put(DatabaseHelper.COLUMN_PASSWORD, passwordEditText.getText().toString().trim());

        long newRowId = db.insert(DatabaseHelper.TABLE_USERS, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendVerificationCodeByEmail(String email, String verificationCode) {
        final String username = "phatkhongden544@gmail.com";
        final String password = "zrdututepdhskonq";

        Session session = createSession(username, password);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(username));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                    message.setSubject("Verification Code");
                    message.setText("Your verification code is: " + verificationCode);


                    Transport.send(message);

                    Log.d("EmailSending", "Email sent successfully to: " + email);

                } catch (MessagingException e) {
                    Log.e("EmailSending", "Error sending email", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

            }
        }.execute();
    }

    private Session createSession(final String username, final String password) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
}