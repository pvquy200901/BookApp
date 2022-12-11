package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityPdfaddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PDFaddActivity extends AppCompatActivity {

    private ActivityPdfaddBinding binding;

    private FirebaseAuth firebaseAuth;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private ProgressDialog progressDialog;

    private Uri pdfUri = null;

    private  static final int PDF_PICK_CODE = 1000;

    private static  final String TAG="ADD_PDF";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfaddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);


        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        binding.backLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfAdd();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private  String title="",des="";
    private void validateData() {

        title= binding.titleEt.getText().toString().trim();
        des= binding.desEt.getText().toString().trim();


        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
        } else  if (TextUtils.isEmpty(des)){
            Toast.makeText(this, "Vui lòng nhập miêu tả ", Toast.LENGTH_SHORT).show();
        }else  if (TextUtils.isEmpty(selectedCategoryTitle)){
            Toast.makeText(this, "Vui lòng chọn loại sách", Toast.LENGTH_SHORT).show();
        }
        else if(pdfUri==null){
            Toast.makeText(this, "Vui lòng thêm file PDF", Toast.LENGTH_SHORT).show();
        }
        else {
            uploadPDF();
        }
    }

    private void uploadPDF() {

        progressDialog.setTitle("Đang load PDF");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        String filePathAndName = "Books/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadPdfUrl =""+uriTask.getResult();

                        upLoadPdftoDB(uploadPdfUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PDFaddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void upLoadPdftoDB(String uploadPdfUrl, long timestamp) {
        String uid = FirebaseAuth.getInstance().getUid();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+des);
        hashMap.put("categoryId",""+selectedCategoryId);
        hashMap.put("url",""+uploadPdfUrl);
        hashMap.put("timestamp",timestamp);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(PDFaddActivity.this, "Đã tải xong", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PDFaddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading category");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String categoryId = ""+ ds.child("id").getValue();
                    String categoryTitle = ""+ ds.child("category").getValue();

                    categoryIdArrayList.add(categoryId);
                    categoryTitleArrayList.add(categoryTitle);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryId, selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing categories");
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn loại sách")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        selectedCategoryTitle = categoryTitleArrayList.get(i);
                        selectedCategoryId = categoryIdArrayList.get(i);
                        binding.categoryTv.setText(selectedCategoryTitle);


                    }
                }).show();
    }

    private void pdfAdd() {
        Log.d(TAG,"pdfAdd: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select PDF"),PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== RESULT_OK){
            if (requestCode == PDF_PICK_CODE){
                Log.d(TAG,"onActivityResult: PDF Picked");
                pdfUri = data.getData();
                Log.d(TAG, "onActivityResult: URI"+pdfUri);
            }
        }
        else {
            Log.d(TAG, "onActivityResult: cancelled");
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}