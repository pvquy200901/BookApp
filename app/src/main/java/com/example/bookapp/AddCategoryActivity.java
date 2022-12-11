package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityAddCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddCategoryActivity extends AppCompatActivity {
    private ActivityAddCategoryBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCategoryBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog= new ProgressDialog(this);

        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    private String category="";
    private void validateData() {
        category = binding.categoryEt.getText().toString().trim();
        if (TextUtils.isEmpty(category)){
            Toast.makeText(this, "Vui lòng nhập loại sách", Toast.LENGTH_SHORT).show();
        }
        else {
            addCategory();
        }
    }

    private void addCategory() {
        progressDialog.setMessage("Đang thêm loại sách");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("category",""+category);
        hashMap.put("uid",""+firebaseAuth.getUid());
        hashMap.put("timestamp",timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(AddCategoryActivity.this, "Đã thêm thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddCategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}