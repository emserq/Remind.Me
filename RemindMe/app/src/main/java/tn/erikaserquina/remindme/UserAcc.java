package tn.erikaserquina.remindme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserAcc extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference dbRef;

    String userID;
    TextView UserName, UserEmail, UserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_user_acc );

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        UserName = findViewById(R.id.UrName);
        UserEmail = findViewById(R.id.UrEmail);
        UserPassword = findViewById(R.id.UrPassword);

        dbRef.child( userID ).addListenerForSingleValueEvent( new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                User user= snapshot.getValue(User.class);

                if (user!= null){
                    String name, email, password;

                    name = user.name;
                    email = user.email;
                    password = user.password;

                    UserName.setText( "" +name );
                    UserEmail.setText( "" +email);
                    UserPassword.setText( "" +password);
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error) {
                Toast.makeText( UserAcc.this, "There is an error!", Toast.LENGTH_LONG ).show();

            }
        });

    }

    public void logout(View view) {
        mAuth.signOut();
        Intent intent = new Intent(UserAcc.this, MainActivity.class);
        startActivity( intent );
   }
}