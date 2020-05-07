package com.example.storagedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.max;

public class MainActivity extends AppCompatActivity {
    private StorageReference mStorageRef,storageRef;
    private Button syncBtn,downBtn,loginBtn;
    private ArrayList<StorageReference> storageReferencesLoc=new ArrayList<>();
    private ArrayList<StorageReference> storageReferencesCl=new ArrayList<>();
    private ArrayList<String> mdKeyLoc=new ArrayList<>();
    private ArrayList<String> mdKeyCl=new ArrayList<>();
    private String TAG="Traverse";
    private File file;
    private ArrayList<String > fileNameLoc=new ArrayList<>();
    private ArrayList<String > fileNameCl=new ArrayList<>();
    private int j,k;
    private ImageView imageView;
    private TextView remoteTv;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        firebaseAnalytics=FirebaseAnalytics.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        syncBtn = findViewById(R.id.sync_btn);
        remoteTv=findViewById(R.id.remoteTv);
        imageView = findViewById(R.id.image);
        downBtn = findViewById(R.id.down_btn);
        loginBtn=findViewById(R.id.login_btn);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

       loginBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                       startActivity(intent);
                       finish();
                   }
               },3000);
           }
       });


        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        Map<String,Object> default_data=new HashMap<>();
        default_data.put("version","v1");
        default_data.put("btn_enable",false);
        mFirebaseRemoteConfig.setDefaultsAsync(default_data);
        //traversePathCl(mStorageRef);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            file = getApplicationContext().getExternalFilesDir("");
            compare(file);
        }

        long cacheExpiration=0;
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Fetch Succeeded",
                                    Toast.LENGTH_SHORT).show();

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            Toast.makeText(MainActivity.this, "Fetch Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        displayWelcomeMessage();
                    }
                });


        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle params=new Bundle();
                params.putString("Button_id","downBtn");
                firebaseAnalytics.logEvent("Button_clicked", params);
                //    traversePathLoc(mStorageRef);
                //file();
                mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                   if(task.isSuccessful()){
                        mFirebaseRemoteConfig.activate();
                        downBtn.setText(mFirebaseRemoteConfig.getString("version"));
                        syncBtn.setEnabled(mFirebaseRemoteConfig.getBoolean("btn_enable"));
                   }
                   else{
                       Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
                   }
                    }
                });

            }
        });

        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle params=new Bundle();
                params.putString("Button_id","syncBtn");
                firebaseAnalytics.logEvent("Button_clicked", params);
              //  fileNameLoc.clear();
                //compare(file);
                //display();
                //sync();


            }
        });
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }

    private void displayWelcomeMessage() {
        String welcomeMessage = mFirebaseRemoteConfig.getString("welcome_message");
        remoteTv.setText(welcomeMessage);
    }
/*
    public void uploadFile(){

        StorageReference mountainsRef = storageRef.child("mountains.jpg");

        StorageReference mountainImagesRef = storageRef.child("images/mountains.jpg");

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });

    }

 */

    public void compare(File files){

            if(files.isDirectory()){
                for(File file:files.listFiles()){
                compare(file);
                Log.i("Local dir folder", file.getAbsolutePath().substring(63));
                }
            }
            else{
                Log.i("Local  file", files.getAbsolutePath().substring(63));
               fileNameLoc.add(files.getAbsolutePath().substring(63));
            }
    }

    public void display(){
        for(int i=0;i<fileNameLoc.size();i++){
            Log.i("Display loc", fileNameLoc.get(i));

        }
        for(int i=0;i<fileNameCl.size();i++){
            Log.i("Display cl", fileNameCl.get(i));

        }
        Log.i("Size", Integer.toString(fileNameCl.size()))
        ;}

   /*
    public void traversePathLoc(StorageReference rootPath){
        rootPath.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                    for(int i=0;i<listResult.getPrefixes().size();i++){
                        StorageReference prefix=listResult.getPrefixes().get(i);
                        Log.i(TAG, "onSuccess: "+prefix.toString().substring(34));

                        traversePathLoc(prefix);
                    }
                    for(int i=0;i<listResult.getItems().size();i++){
                        Log.i("FILE", "onSuccess: Folder "+listResult.getItems().get(i).toString().substring(34));
                        storageReferencesLoc.add(listResult.getItems().get(i));
                        listResult.getItems().get(i).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                mdKeyLoc.add(storageMetadata.getMd5Hash());
                            }
                        });
                    }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

    }
*/
    public void traversePathCl(StorageReference rootPath){
        rootPath.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                    for(int i=0;i<listResult.getPrefixes().size();i++){
                        StorageReference prefix=listResult.getPrefixes().get(i);
                       Log.i(TAG+"Cloud", "onSuccess: "+prefix.toString().substring(34));
                        traversePathCl(prefix);

                    }
                    for(int i=0;i<listResult.getItems().size();i++){

                        Log.i("CLoud FILE", "onSuccess: Folder "+listResult.getItems().get(i).toString().substring(34));
                        storageReferencesCl.add(listResult.getItems().get(i));
                    //    fileNameCl.add(listResult.getItems().get(i).toString().substring(34));
                     //   Log.i("Cloud", "onSuccess: "+fileNameCl.get(i));
                        listResult.getItems().get(i).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                mdKeyCl.add(storageMetadata.getMd5Hash());
                                fileNameCl.add(storageMetadata.getPath());
                                Log.i("Path", storageMetadata.getPath());
                            }
                        });
                    }
                }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

    }

    public void sync(){
        Log.i("Sync","called");
        for(j=0;j<fileNameCl.size();j++){
            Log.i("Sync","called");

            if(!fileNameLoc.contains(fileNameCl.get(j))){
                Log.i("Sync",storageReferencesCl.get(j).getPath());
                storageReferencesCl.get(j).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                      //  downloadFile(MainActivity.this,storageReferencesCl.get(j).getName(),"",storageReferencesCl.get(j).getParent().getPath(),uri.toString());
                    }
                });
            }
        }

    }


    public void file(){

        for(final StorageReference storageReference:storageReferencesCl){
/*
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                downloadFile(MainActivity.this,storageReference.getName(),"",storageReference.getParent().getPath(),uri.toString());
                }
            });

 */
            File[] rootfile= new File[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                rootfile = getExternalFilesDirs("");
            }

            String dlPath=rootfile[rootfile.length-1].getAbsolutePath();
            String finalPath=dlPath+storageReference.getParent().getPath();
            File testFile= new File(finalPath);
            if(!testFile.exists()){
                testFile.mkdirs();
            }
             final File localfile=new File(finalPath,storageReference.getName());
            storageReference.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.i("Download", localfile.getName());
                }
            });


        }
        storageReferencesCl.clear();

    }
/*
    public void downloadFile(Context context,String fileName,String fileExtension,String destDirectory,String url){

        DownloadManager downloadManager=(DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
        Uri uri=Uri.parse(url);
        DownloadManager.Request request=new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context,destDirectory,fileName+fileExtension);
        downloadManager.enqueue(request);
    }

 */
}
