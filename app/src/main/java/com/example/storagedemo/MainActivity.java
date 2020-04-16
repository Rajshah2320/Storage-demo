package com.example.storagedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.max;

public class MainActivity extends AppCompatActivity {
    private StorageReference mStorageRef;
    private Task<ListResult> ref;
    private Button syncBtn,downBtn;
    private String filetype,name;
    private Task<StorageMetadata> metadata;
    private ArrayList<StorageReference> storageReferencesLoc=new ArrayList<>();
    private ArrayList<StorageReference> storageReferencesCl=new ArrayList<>();
    private StorageReference mref;
    private StorageReference mref1;
    private ListResult result;
    private ArrayList<String> mdKeyLoc=new ArrayList<>();
    private ArrayList<String> mdKeyCl=new ArrayList<>();
    private ArrayList<String> mdKey=new ArrayList<>();
    private String TAG="Traverse";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        syncBtn = findViewById(R.id.sync_btn);
        downBtn = findViewById(R.id.down_btn);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        traversePathLoc(mStorageRef);

        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                file();
            }
        });

        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                traversePathCl(mStorageRef);
                sync();

            }
        });
    }

    public void traversePathLoc(StorageReference rootPath){
        ref=rootPath.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                if(!listResult.getPrefixes().isEmpty()){
                    for(int i=0;i<listResult.getPrefixes().size();i++){
                        StorageReference prefix=listResult.getPrefixes().get(i);
                        Log.i(TAG, "onSuccess: "+prefix.toString());

                        traversePathLoc(prefix);
                    }
                    for(int i=0;i<listResult.getItems().size();i++){
                        Log.i("FILE", "onSuccess: Folder "+listResult.getItems().get(i).toString());
                        storageReferencesLoc.add(listResult.getItems().get(i));
                        listResult.getItems().get(i).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                mdKey.add(storageMetadata.getMd5Hash());
                            }
                        });


                    }
                }
                else{
                    for (StorageReference prefix : listResult.getItems()) {
                        Log.i("File Name", prefix.toString());
                        storageReferencesLoc.add(prefix);
                        prefix.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                mdKeyLoc.add(storageMetadata.getMd5Hash());
                            }
                        });
                    }

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

    }

    public void traversePathCl(StorageReference rootPath){
        ref=rootPath.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                if(!listResult.getPrefixes().isEmpty()){
                    for(int i=0;i<listResult.getPrefixes().size();i++){
                        StorageReference prefix=listResult.getPrefixes().get(i);
                        Log.i(TAG, "onSuccess: "+prefix.toString());

                        traversePathLoc(prefix);
                    }
                    for(int i=0;i<listResult.getItems().size();i++){
                        Log.i("FILE", "onSuccess: Folder "+listResult.getItems().get(i).toString());
                        storageReferencesCl.add(listResult.getItems().get(i));
                        listResult.getItems().get(i).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                mdKeyCl.add(storageMetadata.getMd5Hash());
                            }
                        });


                    }
                }
                else{
                    for (StorageReference prefix : listResult.getItems()) {
                        Log.i("File Name", prefix.toString());
                        storageReferencesCl.add(prefix);
                        prefix.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                mdKeyCl.add(storageMetadata.getMd5Hash());
                            }
                        });
                    }

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
        for(final StorageReference storageReference:storageReferencesCl){

            if(!storageReferencesLoc.contains(storageReference)){
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        downloadFile(MainActivity.this,storageReference.getName(),"",storageReference.getParent().getPath(),uri.toString());
                    }
                });
            }
        }

        for(StorageReference storageReference:storageReferencesLoc){

            if(!storageReferencesCl.contains(storageReference)){
                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("file deleted", "onSuccess: ");
                    }
                });
            }
        }


    }


    public void file(){

        for(final StorageReference storageReference:storageReferencesLoc){
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                downloadFile(MainActivity.this,storageReference.getName(),"",storageReference.getParent().getPath(),uri.toString());
                }
            });

        }
        storageReferencesLoc.clear();

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
