package com.example.bookapp.adapters;

import static com.example.bookapp.Contains.MAX_BYTES_PDF;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.Fillters.FilterCategory;
import com.example.bookapp.Fillters.FilterPdfAdmin;
import com.example.bookapp.Models.ModelPdf;
import com.example.bookapp.MyApplication;
import com.example.bookapp.PdfEditActivity;
import com.example.bookapp.databinding.RowPdfAdminBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.holderPdfAdmin> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, fillterList;
    private RowPdfAdminBinding binding;
    private FilterPdfAdmin filter;

    private static final String TAG = "PDF_ADAPTER_TAG";
    private ProgressDialog progressDialog;

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.fillterList =  pdfArrayList;
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Vui lòng đợi...!!");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public holderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return new holderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull holderPdfAdmin holder, int position) {

        ModelPdf modelPdf = pdfArrayList.get(position);
        String title = modelPdf.getTitle();
        String des = modelPdf.getDescription();
        long timestamp = modelPdf.getTimestamp();
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.desTv.setText(des);
        holder.dateTv.setText(formattedDate);

        loadCategory(modelPdf, holder);
        LoadPdfFromUrl(modelPdf,holder);
        loadPdfSize(modelPdf,holder);

        binding.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionDialog(modelPdf, holder);
            }
        });
    }

    private void moreOptionDialog(ModelPdf modelPdf, holderPdfAdmin holder) {
        String bookId = modelPdf.getId();
        String bookUrl = modelPdf.getUrl();
        String bookTitle = modelPdf.getTitle();
        String[] option ={"Edit","Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Option")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0){
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);
                            editBook(modelPdf, holder);
                        }
                        else if (i == 1){
                            deleteBook(modelPdf, holder);
                        }
                    }
                }).show();
    }

    private void editBook(ModelPdf modelPdf, holderPdfAdmin holder) {

    }

    private void deleteBook(ModelPdf modelPdf, holderPdfAdmin holder) {
        String bookId = modelPdf.getId();
        String bookUrl = modelPdf.getUrl();
        String bookTitle = modelPdf.getTitle();

        Log.d(TAG, "deleteBook: deleting...");

        progressDialog.setMessage("Đang xóa" + bookTitle);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: xóa thành công");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Đã xóa sách", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Thất bại khi xóa");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Lỗi" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Thất bại");
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfSize(ModelPdf modelPdf, holderPdfAdmin holder) {
        String pdfUrl = modelPdf.getUrl();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();

                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if(mb >= 1){
                            holder.sizeTv.setText(String.format("%.2f",mb)+"MB");

                        }
                        else if (kb >= 1){
                            holder.sizeTv.setText(String.format("%.2f",kb)+"KB");
                        }
                        else {
                            holder.sizeTv.setText(String.format("%.2f",bytes)+"Bytes");
                        }
                        Log.d(TAG, "onSuccess: "+modelPdf.getTitle() + "" +bytes);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    private void LoadPdfFromUrl(ModelPdf modelPdf, holderPdfAdmin holder) {

        String pdfUrl = modelPdf.getUrl();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        holder.pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+ t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+ t.getMessage());

                                    }
                                }).onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded");
                                    }
                                })
                                .load();
                        Log.d(TAG, "onSuccess: " + modelPdf.getTitle() +"success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: "+ e.getMessage());
                    }
                });
    }

    private void loadCategory(ModelPdf modelPdf, holderPdfAdmin holder) {
        String categoryId = modelPdf.getCategoryId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();

                        holder.categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(fillterList, this);
        }
        return filter;
    }

    class holderPdfAdmin extends RecyclerView.ViewHolder{
        PDFView pdfView;
        TextView titleTv,desTv,dateTv,sizeTv,categoryTv;
        ProgressBar progressBar;
        ImageButton moreBtn;

        public holderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            desTv = binding.desTv;
            dateTv = binding.dateTv;
            sizeTv = binding.sizeTv;
            categoryTv = binding.categoryTv;
            moreBtn = binding.moreBtn;
        }
    }
}
