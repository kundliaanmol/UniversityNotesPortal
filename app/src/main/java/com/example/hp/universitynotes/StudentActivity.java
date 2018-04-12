package com.example.hp.universitynotes;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.Manifest.permission;

import java.util.ArrayList;
import java.util.List;
import android.provider.SyncStateContract.Constants;

import static android.view.View.GONE;
import static java.security.AccessController.getContext;

class USER1{
    public String username,status,sem,course;
    public USER1(){}
    public USER1(String username,String status, String sem){
        this.username=username;
        this.status=status;
        this.sem=sem;
        //this.course=course;
    }
}
public class StudentActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseStorage storage;
    private ImageView mImageView,profileImage;
    private FrameLayout mainContent;
    private ConstraintLayout profile;
    private DrawerLayout mDrawerLayout;
    private FirebaseAuth mAuth;
    private EditText sname,ssem,scourse;
    private String username="empty",sem="empty",ucourse1,uname1,userstatus,course;
    private ConstraintLayout rootView;
    private int year;

    final static int PICK_PDF_CODE = 2342;
    private Button buttonUploadFile,back1,back2,back3;
    private TextView textViewUploads;
    //these are the views
    TextView textViewStatus;
    EditText editTextFilename;
    ProgressBar progressBar;

    //the firebase objects for storage and database
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;
    ListView listView;
    List<uploads> uploadList;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_student);
        mAuth = FirebaseAuth.getInstance();

        rootView=(ConstraintLayout)findViewById(R.id.profile);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String uname[]= mAuth.getCurrentUser().getEmail().split("@");
        DatabaseReference ref = database.getReference("database/university-notes-portal/data/users/");
        DatabaseReference usersRef = ref.child(uname[0]);
        storage = FirebaseStorage.getInstance();
        sname=(EditText)rootView.findViewById(R.id.sname);
        ssem=(EditText)rootView.findViewById(R.id.ssem);
        scourse=(EditText)findViewById(R.id.scourse);
        uploadList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);
        //getting the views
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        editTextFilename = (EditText) findViewById(R.id.editTextFileName);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        //attaching listeners to views
        buttonUploadFile=(Button)findViewById(R.id.buttonUploadFile);
        back1=(Button)findViewById(R.id.back1);

        back3=(Button)findViewById(R.id.back3);
        back1.setOnClickListener(this);

        back3.setOnClickListener(this);

        buttonUploadFile.setOnClickListener(this);
        textViewUploads=(TextView)findViewById(R.id.textViewUploads);
        textViewUploads.setOnClickListener(this);
        uname1=uname[0];
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //getting the upload
                uploads upload = uploadList.get(i);

                //Opening the upload file in browser using the upload url
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(upload.getUrl()));
                startActivity(intent);
            }
        });
         StorageReference storageRef = storage.getReferenceFromUrl("gs://university-notes-portal.appspot.com/image/users").child(uname[0]);
        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mImageView=(ImageView)findViewById(R.id.userImage);
                profileImage=(ImageView)findViewById(R.id.sprofile);
                mImageView.setImageBitmap(bitmap);
                profileImage.setImageBitmap(bitmap);
                mainContent=(FrameLayout)findViewById(R.id.mainContent);
            }
        });

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        profile=(ConstraintLayout)findViewById(R.id.profile);
        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        int id=menuItem.getItemId();
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        updateUInav(id);


                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://university-notes-portal.appspot.com/file/"+ucourse1+"/"+year+"yr");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("file/"+ucourse1+"/"+year+"yr/");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    uploads upload = postSnapshot.getValue(uploads.class);
                    uploadList.add(upload);
                }

                String[] uploads = new String[uploadList.size()];

                for (int i = 0; i < uploads.length; i++) {
                    uploads[i] = uploadList.get(i).getName();
                }

                //displaying it to list
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, uploads);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String ucourse=dataSnapshot.child("course").getValue(String.class);
                if(ucourse.equals("btechcs")){
                    scourse.setText("B.Tech (CS/IT)");
                } else if(ucourse.equals("btechec")){
                    scourse.setText("B.Tech (EC/EE)");
                } else if(ucourse.equals("btechcivil")){
                    scourse.setText("B.Tech (CIVIL/MECH)");
                } else if(ucourse.equals("btechit")){
                    scourse.setText("B.Sc (IT)");
                }
                sname.setText(dataSnapshot.child("username").getValue(String.class));
                ssem.setText(dataSnapshot.child("sem").getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


       //sname.setText(user.username);
       //ssem.setText(user.sem);



    }



    public void updateUInav(int id)
    {
        if(id==R.id.nav_profile)
        {
            profile.setVisibility(View.VISIBLE);
            findViewById(R.id.upload).setVisibility(View.GONE);
            findViewById(R.id.viewUploads).setVisibility(GONE);

        }
        else if(id==R.id.nav_mybooks)
        {

            findViewById(R.id.upload).setVisibility(View.GONE);
            findViewById(R.id.viewUploads).setVisibility(View.VISIBLE);

            profile.setVisibility(GONE);
        }
        else if(id==R.id.nav_Upload)
        {

            findViewById(R.id.upload).setVisibility(View.VISIBLE);
            findViewById(R.id.viewUploads).setVisibility(GONE);

            profile.setVisibility(GONE);
        }
        else if(id==R.id.nav_logout)
        {
            mAuth.signOut();
            this.finish();
        }
    }
    private void getPDF() {
        //for greater than lolipop versions we need the permissions asked on runtime
        //so if the permission is not available user will go to the screen to allow storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        Intent intent1 = new Intent();
        intent1.setType("application/pdf");
        intent1.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent1, "Select Picture"), PICK_PDF_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //when the user choses the file
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //if a file is selected
            if (data.getData() != null) {
                //uploading the file
                uploadFile(data.getData());
            }else{
                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void uploadFile(Uri data) {
        progressBar.setVisibility(View.VISIBLE);
        StorageReference sRef = mStorageReference.child(uname1+".pdf");
        sRef.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressBar.setVisibility(GONE);
                        textViewStatus.setText("File Uploaded Successfully");

                        uploads upload = new uploads(editTextFilename.getText().toString(), taskSnapshot.getDownloadUrl().toString());
                        mDatabaseReference.child(mDatabaseReference.push().getKey()).setValue(upload);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        textViewStatus.setText((int) progress + "% Uploading...");
                    }
                });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonUploadFile:
                getPDF();
                break;
            case R.id.textViewUploads:
                findViewById(R.id.upload).setVisibility(View.GONE);
                findViewById(R.id.viewUploads).setVisibility(View.VISIBLE);

                findViewById(R.id.profile).setVisibility(GONE);
                break;
            case R.id.back1: profile.setVisibility(View.VISIBLE);
                findViewById(R.id.upload).setVisibility(View.GONE);
                findViewById(R.id.viewUploads).setVisibility(GONE);

                break;
            case R.id.back3: profile.setVisibility(View.VISIBLE);
                findViewById(R.id.upload).setVisibility(View.GONE);
                findViewById(R.id.viewUploads).setVisibility(GONE);
                break;

        }

    }
}


