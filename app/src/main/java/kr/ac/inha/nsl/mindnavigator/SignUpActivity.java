package kr.ac.inha.nsl.mindnavigator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    // region Variables
    private EditText name;
    private EditText login;
    private EditText password;
    private EditText confPassword;
    private RelativeLayout loadingPanel;
    // endregion

    private void init() {
        // region Initialize UI Variables
        name = findViewById(R.id.txt_name);
        login = findViewById(R.id.txt_login);
        password = findViewById(R.id.txt_password);
        confPassword = findViewById(R.id.txt_conf_password);
        loadingPanel = findViewById(R.id.loadingPanel);
        // endregion
    }

    public void userRegister(String name, String username, String password) {
        loadingPanel.setVisibility(View.VISIBLE);

        Tools.execute(new MyRunnable(
                getString(R.string.url_user_register, getString(R.string.server_ip)),
                name,
                username,
                password
        ) {
            @Override
            public void run() {
                String url = (String) args[0];
                String name = (String) args[1];
                String username = (String) args[2];
                String password = (String) args[3];

                try {
                    JSONObject body = new JSONObject();
                    body.put("name", name);
                    body.put("username", username);
                    body.put("password", password);

                    JSONObject json = new JSONObject(Tools.post(url, body));

                    switch (json.getInt("result")) {
                        case Tools.RES_OK:
                            runOnUiThread(new MyRunnable(args) {
                                @Override
                                public void run() {
                                    Toast.makeText(SignUpActivity.this, "Successfully signed up. You can sign in now!", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                }
                            });
                            break;
                        case Tools.RES_FAIL:
                            Thread.sleep(2000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SignUpActivity.this, "Username already exists, please try another username!", Toast.LENGTH_SHORT).show();
                                    loadingPanel.setVisibility(View.GONE);
                                }
                            });
                            break;
                        case Tools.RES_SRV_ERR:
                            Thread.sleep(2000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SignUpActivity.this, "Failed to sign up. (SERVER SIDE ERROR)", Toast.LENGTH_SHORT).show();
                                    loadingPanel.setVisibility(View.GONE);
                                }
                            });
                            break;
                        default:
                            break;
                    }
                } catch (JSONException | InterruptedException | IOException e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignUpActivity.this, "Failed to sign up.", Toast.LENGTH_SHORT).show();
                            loadingPanel.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    public void registerClick(View view) {
        String usrFullName = name.getText().toString();
        String usrLogin = login.getText().toString();
        String usrPassword = password.getText().toString();
        String usrConfirmPass = confPassword.getText().toString();

        if (isRegistrationValid(usrFullName, usrLogin, usrPassword, usrConfirmPass))
            userRegister(usrFullName, usrLogin, usrPassword);
        else
            Toast.makeText(this, "Invalid input. Please recheck inputs and try again!", Toast.LENGTH_SHORT).show();
    }

    public boolean isRegistrationValid(String name, String login, String password, String confirmPass) {
        return name != null &&
                login != null &&
                password != null &&
                confirmPass != null &&
                name.length() != 0 &&
                login.length() >= 4 &&
                login.length() <= 12 &&
                password.length() >= 6 &&
                password.length() <= 16 &&
                password.equals(confirmPass);
    }
}
