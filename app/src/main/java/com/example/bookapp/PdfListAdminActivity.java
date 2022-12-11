package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.bookapp.Models.ModelPdf;
import com.example.bookapp.adapters.AdapterPdfAdmin;
import com.example.bookapp.databinding.ActivityPdfListAdminBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {

    private ActivityPdfListAdminBinding binding;
    private ArrayList<ModelPdf> arrayList;
    private AdapterPdfAdmin adapterPdfAdmin;

    private String categoryId, categoryTitle;

    private static final String TAG ="PDF_LIST_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        categoryId = intent.getStringExtra("CategoryId");
        categoryTitle = intent.getStringExtra("CategoryTitle");

        binding.subTitleTv.setText(categoryTitle);
        loadPdfList();

        binding.backLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterPdfAdmin.getFilter().filter(charSequence);
                }
                catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void loadPdfList() {
        arrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPdf modelPdf = ds.getValue(ModelPdf.class);
                            arrayList.add(modelPdf);
                            Log.d(TAG, "onDataChange: "+modelPdf.getId() +""+modelPdf.getTitle());
                        }

                        adapterPdfAdmin = new AdapterPdfAdmin(PdfListAdminActivity.this,arrayList);
                        binding.bookRv.setAdapter(adapterPdfAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}