package tn.erikaserquina.remindme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    EditText editEmail, editPassword;
    FirebaseAuth mAuth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        editEmail = findViewById( R.id.editLogEmail );
        editPassword = findViewById( R.id.editLogPassword );
        mAuth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog( this );
    }

    private Boolean ValidationEmail (CharSequence target){
        return (!TextUtils.isEmpty( target )&& Patterns.EMAIL_ADDRESS.matcher( target ).matches());
    }

    public void goToReg(View view) {

        Intent intent = new Intent(MainActivity.this, activity_register.class);
        startActivity( intent );
    }

    public void login(View view) {
        String email, password;

        email = editEmail.getText().toString().trim();
        password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty( email )) {
            editEmail.setError( "Enter your Email" );
            return;
        } else if (TextUtils.isEmpty( password )) {
            editPassword.setError( "Enter your Password" );
            return;
        } else {
            dialog.setMessage( "Loading..." );
            dialog.show();
            dialog.setCanceledOnTouchOutside( false );

            mAuth.signInWithEmailAndPassword( email, password )
                    .addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText( MainActivity.this,"Logged In Successfully", Toast.LENGTH_LONG ).show();
                                Intent intent = new Intent( getApplicationContext(), MainPage.class );
                                startActivity( intent );
                            } else {
                                Toast.makeText( MainActivity.this, "Invalid Email or Password", Toast.LENGTH_LONG ).show();
                                Intent intent = new Intent( getApplicationContext(), MainActivity.class );
                                startActivity( intent );
                            }
                        }
                    } );
        }
    }
}