package com.example.parentalcontrol.ui.capture;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.parentalcontrol.MainActivity;
import com.example.parentalcontrol.Photo;
import com.example.parentalcontrol.PhotoRecyclerViewAdapter;
import com.example.parentalcontrol.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;


public class CaptureFragment extends Fragment {
    MainActivity mainActivity;
    ArrayList<Photo> photos = new ArrayList<>();
    RecyclerView photoRecyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    PhotoRecyclerViewAdapter adapter;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_capture,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();

        photoRecyclerView = view.findViewById(R.id.captureRecyclerView);
        adapter = new PhotoRecyclerViewAdapter(getContext(), photos);
        photoRecyclerView.setAdapter(adapter);
        photoRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainActivity.mDriveServiceHelper.queryCaptureImageFiles()
                        .addOnSuccessListener(fileList -> {
                            for (com.google.api.services.drive.model.File file : fileList.getFiles()) {
                                String fileId = file.getId();
                                String fileName = file.getName();
                                mainActivity.mDriveServiceHelper.DownloadFile(fileId,fileName);
                            }
                            loadImageFromFile();
                        })
                        .addOnFailureListener(exception -> Log.e("TAG", "Unable to query files.", exception));
            }
        });
        loadImageFromFile();

    }
    private void loadImageFromFile(){
        File dirFiles = mainActivity.getFilesDir();
        photos.clear();
        for(File file : dirFiles.listFiles()){
            String fileName = file.getName();
            String[] parts = fileName.split("\\.");
            String fileExtensionName = parts[1].toLowerCase();
            if(fileExtensionName.equals("png")||fileExtensionName.equals("jpeg")||fileExtensionName.equals("jpg")){
                photos.add(new Photo(fileName,Uri.fromFile(file)));
            }
        }
        photos.sort((name1, name2) -> name2.getImgName().compareTo(name1.getImgName()));
        swipeRefreshLayout.setRefreshing(false);
        adapter.setData(photos);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}