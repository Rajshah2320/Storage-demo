package com.example.storagedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;
import java.util.Vector;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity {
    private StorageReference mStorageRef;
    private StorageReference ref1;
    private StorageReference ref2;
    private StorageReference ref3;
    private StorageReference ref4;
    private Task<ListResult> ref;
    private Button syncBtn,downBtn;
    private String filetype,name;
    private Task<StorageMetadata> metadata;
    private List<StorageReference> storageReferenceListCl;
    private List<StorageReference>storageReferenceListLoc;
    private StorageReference mref;
    private StorageReference mref1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        syncBtn = findViewById(R.id.sync_btn);
        downBtn=findViewById(R.id.down_btn);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        /*  metadata=mStorageRef.child("class1").child("episode2").child("topic3").child("Assignment for Android.docx").getMetadata();

        metadata.addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                String type=storageMetadata.getBucket();
                Log.d("Metadata",type);
            }
        });

       */
        ref=mStorageRef.child("class1").child("episode2").child("topic1").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {

                storageReferenceListCl=listResult.getItems();
            }
        });

        ref1 = mStorageRef.child("class1").child("episode1").child("topic1").child("RajShah_InternshalaResume.pdf");
        ref2 = mStorageRef.child("class1").child("episode2").child("topic2").child("Links for uncovered topics.pdf");
        ref3 = mStorageRef.child("class1").child("episode2").child("topic3").child("Assignment for Android.docx");
        ref4 = mStorageRef.child("class1").child("episode2").child("topic1").child("AOA Assignment 1 (Sap 13).docx");

       storageReferenceListLoc=new Vector<>();

        storageReferenceListLoc.add(ref1);
        storageReferenceListLoc.add(ref2);
        storageReferenceListLoc.add(ref3);
        storageReferenceListLoc.add(ref4);

        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                file();

            }
        });

        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sync();
               // delete();
            }
        });
    }

    public void sync() {

        for (int i=0;i<storageReferenceListCl.size();i++){

           mref= storageReferenceListCl.get(i);
           name=mref.getName();

           mref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
               @Override
               public void onSuccess(StorageMetadata storageMetadata) {
                   filetype= storageMetadata.getName();
               }
           });
           if(!storageReferenceListLoc.contains(mref)){

               mref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                   @Override
                   public void onSuccess(Uri uri) {

                       downloadFile(MainActivity.this,name,"","class1/episode2/topic1",uri.toString());

                   }
               });

           }
        }
    }

    public void delete(){

        for (int i=0;i<storageReferenceListLoc.size();i++){

            mref1=storageReferenceListLoc.get(i);
            if(!storageReferenceListCl.contains(mref1)){
                mref1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("File deleted", " success ");
                    }
                });
            }
        }


    }

    public void file(){

       ref1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                downloadFile(MainActivity.this,"RajShah_InternshalaResume", ".pdf","class1/episode1/topic1",uri.toString());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

        ref2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                downloadFile(MainActivity.this,"Links for uncovered topics",".pdf","class1/episode2/topic2",uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

        ref3.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                downloadFile(MainActivity.this,"Assignment for Android",".docx","class1/episode2/topic3",uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

        ref4.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                downloadFile(MainActivity.this,"AOA Assignment 1 (Sap 13)",".docx","class1/episode2/topic1",uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void downloadFile(Context context,String fileName,String fileExtension,String destDirectory,String url){

        DownloadManager downloadManager=(DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
        Uri uri=Uri.parse(url);
        DownloadManager.Request request=new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context,destDirectory,fileName+fileExtension);
        downloadManager.enqueue(request);
    }
}
