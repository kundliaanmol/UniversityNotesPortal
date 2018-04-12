package com.example.hp.universitynotes;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class USER{
    public String username,status,sem,course;
    public USER(){}
    public USER(String username,String status, String sem,String course){
        this.username=username;
        this.status=status;
        this.sem=sem;
        this.course=course;
    }
}
public class Login extends BaseActivity implements
        View.OnClickListener,AdapterView.OnItemSelectedListener{
    public static final int PICK_IMAGE_REQUEST=12;
    //public static String userkey;
    public void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    // Create an ArrayAdapter using the string array and a default spinner layout

      //  Specify the layout to use when the list of choices appears

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("database/university-notes-portal/data/");
    private static final String TAG = "EmailPassword";
    FirebaseUser currentUser;
    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button signin,create,signout,verify,createAccount,btnchoose;
    private String ucourse;
    private EditText reg_email,reg_password,reg_name,_reg_sem,reg_status;
    Spinner spinner;
    private boolean registered=false;
    private ImageView upldimg;
    private Uri imagepath;
    FirebaseStorage storage;
    StorageReference storageReference;
    private String emailuname;



    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        spinner = (Spinner) findViewById(R.id.spinner_course);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_course, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // Views
        mStatusTextView = (TextView)findViewById(R.id.status);
        mDetailTextView = (TextView)findViewById(R.id.detail);
        mEmailField = (EditText)findViewById(R.id.field_email);
        mPasswordField = (EditText)findViewById(R.id.field_password);
        reg_email = (EditText)findViewById(R.id.reg_email);
        reg_password = (EditText)findViewById(R.id.reg_password);
        reg_name = (EditText)findViewById(R.id.reg_name);
        _reg_sem = (EditText)findViewById(R.id.reg_sem);
        upldimg=(ImageView)findViewById(R.id.reg_selimg);

        // Buttons
        signin=(Button)findViewById(R.id.email_sign_in_button);
        signout=(Button)findViewById(R.id.email_create_account_button);
        create=(Button)findViewById(R.id.sign_out_button);
        createAccount=(Button)findViewById(R.id.reg_crtact_btn);
        btnchoose=(Button)findViewById(R.id.reg_upldbtn);
        signin.setOnClickListener(this);
        signout.setOnClickListener(this);
        create.setOnClickListener(this);
        createAccount.setOnClickListener(this);
        btnchoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });


        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // [END on_start_check_user]

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        showProgressDialog();
        findViewById(R.id.imagelogin).setVisibility(View.GONE);
        findViewById(R.id.formlogin).setVisibility(View.GONE);
        findViewById(R.id.form_create).setVisibility(View.VISIBLE);

        // [START create_user_with_email]

    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            currentUser=user;
                            if(registered==true) {
                                updateUI(user);
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            mStatusTextView.setText("Email already registered! Authentication Failed!");
                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }





    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(user.isEmailVerified());
            {
                Intent i=new Intent(this,StudentActivity.class);
                startActivity(i);
            }
        } else {
            mStatusTextView.setText("Not logged in! please log in!");
            mDetailTextView.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.signed_in_buttons).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            findViewById(R.id.imagelogin).setVisibility(View.GONE);
            findViewById(R.id.formlogin).setVisibility(View.GONE);
            findViewById(R.id.form_create).setVisibility(View.VISIBLE);
        } else if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.sign_out_button) {
            signOut();
        } else if (i== R.id.button6) {
            findViewById(R.id.imagelogin).setVisibility(View.VISIBLE);
            findViewById(R.id.formlogin).setVisibility(View.VISIBLE);
            findViewById(R.id.form_create).setVisibility(View.GONE);
        }
        else if(i == R.id.reg_crtact_btn){
            if(reg_email.getText().toString().isEmpty())
            {
                reg_email.setError("required");
                reg_password.setError("required");
            }else {
                createAccount(reg_email.getText().toString(), reg_password.getText().toString());
                DatabaseReference usersRef = ref.child("users");
                String status;
                RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup);
                if (rg.getCheckedRadioButtonId() == R.id.faculty) {
                    status = "faculty";
                } else {
                    status = "student";
                }
                mAuth.createUserWithEmailAndPassword(reg_email.getText().toString(), reg_password.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(Login.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }

                                // [START_EXCLUDE]
                                hideProgressDialog();
                                // [END_EXCLUDE]
                            }
                        });
                // [END create_user_with_email]
                emailuname= reg_email.getText().toString();
                String arr[]=emailuname.split("@");
                emailuname=arr[0];
                Toast.makeText(Login.this, "User Created! logging in!",
                        Toast.LENGTH_SHORT).show();
                Map<String, USER> users = new HashMap<>();
                users.put(emailuname, new USER(reg_name.getText().toString(), status,_reg_sem.getText().toString(),ucourse));
                //DatabaseReference keyReference = usersRef.push();
                //userkey=keyReference.getRef().getKey();
               // keyReference.setValue(users);
                usersRef.setValue(users);

                registered=true;
                uploadImage();

            }
        }
    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
       switch(parent.getItemAtPosition(pos).toString()){
            case "Btech(CS/IT)": ucourse="btechcs";break;
            case "Btech(EC/EE)": ucourse="btechec";break;
            case "Btech(Civil/Mech)": ucourse="btechcivil";break;
            case "BSc(IT)": ucourse="btechit";break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/users/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            imagepath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagepath);
                upldimg.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    private void uploadImage() {

        if(imagepath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("image/users/"+emailuname);
            ref.putFile(imagepath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(Login.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            student();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(Login.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
    public void student(){
        Intent k=new Intent(this,StudentActivity.class);
        startActivity(k);
    }

}
