package com.example.parentalcontrol.ui.time;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.parentalcontrol.EditTimeTableItemActivity;
import com.example.parentalcontrol.MainActivity;
import com.example.parentalcontrol.R;
import com.example.parentalcontrol.TimeTableItem;
import com.example.parentalcontrol.TimeTableRecyclerViewAdapter;
import com.google.api.services.drive.model.File;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.ArrayList;

public class TimeFragment extends Fragment {

    RecyclerView timeTableRecyclerView;
    TimeTableRecyclerViewAdapter adapter;
    MainActivity mainActivity;
    ArrayList<TimeTableItem> timeTable = new ArrayList<>();

    private static String mTimeFileID;
    SwipeRefreshLayout swipeRefreshLayout;
    Button addBtn, deleteBtn, editBtn, updateToCloudBtn;

    private ActivityResultLauncher<Intent> activityAddLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                Intent intent = result.getData();
                TimeTableItem item = (TimeTableItem) intent.getExtras().getSerializable("res-item");
                timeTable.add(item);
            }
        }
    });
    private ActivityResultLauncher<Intent> activityEditLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                Intent intent = result.getData();
                TimeTableItem item = (TimeTableItem) intent.getExtras().getSerializable("res-item");
                for(int i=0;i<timeTable.size();i++){
                    if(timeTable.get(i).isSelected){
                        timeTable.set(i,item);
                    }
                }
            }
        }
    });
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_time,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity)getActivity();
        addBtn = view.findViewById(R.id.addBtn);
        deleteBtn = view.findViewById(R.id.deleteBtn);
        editBtn = view.findViewById(R.id.editBtn);
        updateToCloudBtn = view.findViewById(R.id.updateToCloud);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditTimeTableItemActivity.class);
                activityAddLauncher.launch(intent);
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0;i<timeTable.size();i++){
                    if(timeTable.get(i).isSelected){
                        timeTable.remove(i);
                    }
                }
                String content = convertTimeTableToString();
                FileOutputStream fos = null;
                try {
                    fos = mainActivity.openFileOutput("time.txt",mainActivity.MODE_PRIVATE);
                    fos.write(content.getBytes());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    if(fos!=null){
                        try{
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                adapter.setData(timeTable);
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditTimeTableItemActivity.class);
                Bundle bundle =  new Bundle();
                for(int i=0;i<timeTable.size();i++){
                    if(timeTable.get(i).isSelected){
                        bundle.putSerializable("time-item",  timeTable.get(i));
                        intent.putExtras(bundle);
                    }
                }
                activityEditLauncher.launch(intent);
            }
        });
        updateToCloudBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readDeviceIdFile();
            }
        });
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initTimeTable(mTimeFileID);
                mainActivity.readPasswordFile();
            }
        });
        timeTableRecyclerView = view.findViewById(R.id.timeTableRecylerView);
        adapter = new TimeTableRecyclerViewAdapter(getContext(),timeTable);
        timeTableRecyclerView.setAdapter(adapter);
        timeTableRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FileInputStream fis = null;
        try {
            fis = mainActivity.openFileInput("time.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br= new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            while((text = br.readLine())!=null){
                sb.append(text).append("\n");
            }
            String[] parts = sb.toString().split("F");
            timeTable = new ArrayList<>();
            for(int i=0;i<parts.length;i++){
                if(parts[i].length()!=0){
                    TimeTableItem timeTableItem = convertStringToTimeTable('F'+parts[i]);
                    timeTable.add(timeTableItem);
                }
            }
            adapter.setData(timeTable);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fis!=null){
                try{
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onResume() {
        adapter.setData(timeTable);
        String content = convertTimeTableToString();
        FileOutputStream fos = null;
        try {
            fos = mainActivity.openFileOutput("time.txt",mainActivity.MODE_PRIVATE);
            fos.write(content.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(fos!=null){
                try{
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    public void notifyFromMain(){
        getTimeTableFromCloud();
    }
    /**
     * Queries the Drive REST API for files visible to this app and lists them in the content view.
     */
    private void getTimeTableFromCloud() {
        if (mainActivity.mDriveServiceHelper != null) {
            swipeRefreshLayout.setRefreshing(true);
            Log.d("TAG", "Querying for files.");
            mainActivity.mDriveServiceHelper.queryTimeFiles()
                    .addOnSuccessListener(fileList -> {
                        for (File file : fileList.getFiles()) {
                            mTimeFileID = file.getId();
                            initTimeTable(mTimeFileID);
                        }
                        setReadOnlyMode();
                    })
                    .addOnFailureListener(exception -> swipeRefreshLayout.setRefreshing(false));

        }
    }

    private void readDeviceIdFile() {
        if (mainActivity.mDriveServiceHelper != null) {
            Log.e("TAG", "Reading file " + mainActivity.mDeviceIdFileId);
            mainActivity.mDriveServiceHelper.readFile(mainActivity.mDeviceIdFileId)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;
                        String[] parts = content.split("ID:");
                        if(parts.length==0||parts.length==1){
                            return;
                        }
                        String currentProcessId = parts[1];
                        StringBuilder stringBuilder =  new StringBuilder();
                        String newDeviceFileContent = "";
                        if(currentProcessId.equals(mainActivity.deviceId)){
                            for(int i=0;i<parts.length;i++){
                                if(!parts[i].equals("")){
                                    if(!parts[i].equals(mainActivity.deviceId)){
                                        stringBuilder.append("ID:").append(parts[i]).append("\n");
                                    }
                                }
                            }
                            mainActivity.updateDeviceIdFile(stringBuilder.toString());
                            updateAllFileToCloud();
                        }
                        else{
                            Toast.makeText(mainActivity, "Other Device Is Editing", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(exception ->
                            Log.e("TAG", "Couldn't read file.", exception));
        }
    }

    private void updateAllFileToCloud() {
        updateTimeTableFileToCloud();
        mainActivity.updatePasswordToCloud();
    }
    private void updateTimeTableFileToCloud(){
        StringBuilder stringBuilder = new StringBuilder();
        String content = convertTimeTableToString();
        stringBuilder.append(content);
        if (mainActivity.mDriveServiceHelper != null && mTimeFileID != null) {
            mainActivity.mDriveServiceHelper.saveFile(mTimeFileID, "time.txt",stringBuilder.toString())
                    .addOnFailureListener(exception ->
                            Log.e("TAG", "Unable to save file via REST.", exception));
            Toast.makeText(mainActivity, "Update TimeTable Success", Toast.LENGTH_SHORT).show();
        }

    }
    private void initTimeTable(String fileId) {
        if (mainActivity.mDriveServiceHelper != null) {
            swipeRefreshLayout.setRefreshing(true);
            Log.e("TAG", "Reading file " + fileId);
            mainActivity.mDriveServiceHelper.readFile(fileId)
                    .addOnSuccessListener(nameAndContent -> {
                        String content = nameAndContent.second;
                        FileOutputStream fos = null;
                        try {
                            fos = mainActivity.openFileOutput("time.txt",mainActivity.MODE_PRIVATE);
                            fos.write(content.getBytes());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        finally {
                            if(fos!=null){
                                try{
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        String[] parts = content.split("F");
                        timeTable = new ArrayList<>();
                        for(int i=0;i<parts.length;i++){
                            if(parts[i].length()!=0){
                                TimeTableItem timeTableItem = convertStringToTimeTable('F'+parts[i]);
                                timeTable.add(timeTableItem);
                            }
                        }
                        adapter.setData(timeTable);
                        swipeRefreshLayout.setRefreshing(false);
                        setReadWriteMode(fileId);
                    })
                    .addOnFailureListener(exception ->
                            Log.e("TAG", "Couldn't read file.", exception));
        }
    }
    private TimeTableItem convertStringToTimeTable(String buffer){
        String[] parts = buffer.split(" ");
        String from ="";
        String to ="";
        String duration ="";
        String interval ="";
        String sum ="";
        for(int i=0;i<parts.length;i++){
            switch (parts[i].charAt(0)){
                case 'F':{
                    from= parts[i].substring(1);
                    break;
                }
                case 'T':{
                    to= parts[i].substring(1);
                    break;
                }
                case 'D':{
                    duration = parts[i].substring(1);
                    break;
                }
                case 'I':{
                    interval = parts[i].substring(1);
                    break;
                }
                case 'S':{
                    sum = parts[i].substring(1);
                    break;
                }
            }
        }
        return new TimeTableItem(from,to,duration,interval,sum);
    }
    /**
     * Updates the UI to read-only mode.
     */
    private void setReadOnlyMode() {

        mTimeFileID = null;
    }

    /**
     * Updates the UI to read/write mode on the document identified by {@code fileId}.
     */
    private void setReadWriteMode(String fileId) {

        mTimeFileID = fileId;
    }
    private String convertTimeTableToString(){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<timeTable.size();i++){
            sb.append(timeTable.get(i).toString());
        }
        return sb.toString();
    }
}