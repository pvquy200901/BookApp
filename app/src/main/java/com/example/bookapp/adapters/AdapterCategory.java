package com.example.bookapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Fillters.FilterCategory;
import com.example.bookapp.Models.ModelCategory;
import com.example.bookapp.PdfListAdminActivity;
import com.example.bookapp.databinding.RowCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.hoderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategory> categoryArrayList,filterList;

    private FilterCategory filter;

    private RowCategoryBinding binding;
    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @NonNull
    @Override
    public hoderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent,false);

        return new hoderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull hoderCategory holder, int position) {

        ModelCategory model =  categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        holder.categoryTv.setText(category);

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder =  new AlertDialog.Builder(context);
                builder.setTitle("Xóa")
                        .setMessage("Bạn có muốn xóa sách này không ?")
                        .setPositiveButton("Chấp nhận", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(context, "Đang xóa....", Toast.LENGTH_SHORT).show();
                                deleteCattegory(model, holder);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("CategoryId",id);
                intent.putExtra("CategoryTitle",category);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCattegory(ModelCategory model, hoderCategory holder) {
            String id = model.getId();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
            ref.child(id).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Đã xóa....", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
    }


    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterCategory(filterList, this);
        }
        return filter;
    }


    class hoderCategory extends RecyclerView.ViewHolder{

        TextView categoryTv;
        ImageButton deleteBtn;

        public hoderCategory(@NonNull View itemView) {
            super(itemView);
            categoryTv = binding.categoryTv;
            deleteBtn = binding.deleteCategory;
        }
    }


}
