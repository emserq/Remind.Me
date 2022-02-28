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
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class activity_register extends AppCompatActivity {

    EditText editName, editEmail, editPassword;
    FirebaseAuth mAuth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register );

        editName = findViewById( R.id.editRegName );
        editEmail = findViewById( R.id.editRegEmail );
        editPassword = findViewById( R.id.editRegPassword );
        mAuth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog( this );
    }

    private Boolean ValidationEmail (CharSequence target){
        return (!TextUtils.isEmpty( target )&& Patterns.EMAIL_ADDRESS.matcher( target ).matches());
    }

    public void register(View view) {
        String name, email, password;

        name = editName.getText().toString().trim();
        email = editEmail.getText().toString().trim();
        password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty( name )){
            editName.setError( "Enter your Name" );
            return;
        } else if (TextUtils.isEmpty( email )){
            editEmail.setError( "Enter your Email" );
            return;
        } else if (!ValidationEmail( email )){
            editEmail.setError( "Invalid Email" );
            return;
        } else if (TextUtils.isEmpty( password )){
            editPassword.setError( "Enter your Password" );
            return;
        } else if (password.length() < 5) {
            editPassword.setError( "Password is too short (must be at least 5 characters)" );
        } else {
            dialog.setMessage( "Loading..." );
            dialog.show();
            dialog.setCanceledOnTouchOutside( false );

            mAuth.createUserWithEmailAndPassword( email, password )
                    .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                User user = new User( name, email, password );

                                FirebaseDatabase.getInstance().getReference( "Users" )
                                        .child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                                        .setValue( user ).addOnCompleteListener( new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText( activity_register.this, "Registered Succesfully!", Toast.LENGTH_LONG ).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity( intent );

                                        } else {
                                            Toast.makeText( activity_register.this, "Registration Failed, Please try again.", Toast.LENGTH_LONG ).show();
                                        }
                                    }
                                } );
                            } else {
                                Toast.makeText( activity_register.this, "Registration Failed", Toast.LENGTH_LONG ).show();
                            }
                        }
                    } );
        }
    }


    public void backToLog(View view) {
        Intent intent = new Intent(activity_register.this, MainActivity.class);
        startActivity( intent );
    }
}