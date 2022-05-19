package com.example.parentalcontrol;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaParser;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.parentalcontrol.ui.time.TimeFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Collections;

import com.example.parentalcontrol.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 2;

    public DriveServiceHelper mDriveServiceHelper;
    private String mOpenFileId;
    public String deviceId;

    private TimeFragment timeFragment;
    private String mPasswordFileID;
    public String mDeviceIdFileId;
    private String childrenPass;
    private String parentPass;

    private ActivityResultLauncher<Intent> activityPasswordLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                Intent intent = result.getData();
                Bundle bundle = intent.getExtras();
                parentPass = bundle.getString("newParentPass");
                Log.e(TAG, "onActivityResult: "+parentPass );
                childrenPass = bundle.getString("newChildrenPass");
                writePasswordFile();
            }
        }
    });

    private ArrayList<TimeTableItem> timeTable = new ArrayList<>();


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_time, R.id.nav_capture)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if(navHostFragment!=null){
            try {
                timeFragment = (TimeFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        //timeFragment = (TimeFragment) fragment.getChildFragmentManager().findFragmentById(R.id.nav_time);
        requestSignIn();
        FileInputStream fis = null;
        try {
            fis = openFileInput("password.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br= new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            while((text = br.readLine())!=null){
                sb.append(text).append("\n");
            }
            String content = sb.toString();
            String[] parts = content.split("Children:");
            if(parts.length == 1||parts.length==0){
                return;
            }
            childrenPass = parts[1];
            parentPass = parts[0].split("Parent:")[1];
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
        requestStoragePermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    Drive googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Drive API Migration")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new DriveServiceHelper(googleDriveService,this);
                    timeFragment.notifyFromMain();
                    getTimeTableFromCloud();

                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }


    /**
     * Retrieves the title and content of a file identified by {@code fileId} and populates the UI.
     */
    private void writePasswordFile(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Parent:").append(parentPass).append("\n");
        stringBuilder.append("Children:").append(childrenPass).append("\n");
        String content = stringBuilder.toString();
        Log.e(TAG, "writePasswordFile: "+content );
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("password.txt",MODE_PRIVATE);
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
    }
    public void updatePasswordToCloud(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Parent:").append(parentPass).append("\n");
        stringBuilder.append("Children:").append(childrenPass).append("\n");
        String content = stringBuilder.toString();
        Log.e(TAG, "updatePasswordToCloud: "+content );
        if (mDriveServiceHelper != null && mPasswordFileID != null) {
            mDriveServiceHelper.saveFile(mPasswordFileID, "password.txt",content )
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Unable to save file via REST.", exception));
        }
    }
    public void readPasswordFile() {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Reading file " + mPasswordFileID);

            mDriveServiceHelper.readFile(mPasswordFileID)
                    .addOnSuccessListener(nameAndContent -> {
                        String content = nameAndContent.second;
                        FileOutputStream fos = null;
                        try {
                            fos = openFileOutput("password.txt",MODE_PRIVATE);
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
                        String[] parts = content.split("Children:");
                        if(parts.length==1){
                            return;
                        }
                        childrenPass = parts[1];
                        parentPass = parts[0].split("Parent:")[1];
                        setReadWriteMode(mPasswordFileID);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't read file.", exception));
        }
    }
    public void updateDeviceIdFile(String content){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(content).append("ID:").append(deviceId);
        if (mDriveServiceHelper != null && mDeviceIdFileId != null) {
            mDriveServiceHelper.saveFile(mDeviceIdFileId, "deviceId.txt",stringBuilder.toString() )
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Unable to save file via REST.", exception));
        }

    }
    private void readDeviceIdFile() {
        if (mDriveServiceHelper != null) {
            Log.e(TAG, "Reading file " + mDeviceIdFileId);
            mDriveServiceHelper.readFile(mDeviceIdFileId)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;
                        String[] parts = content.split("ID:");
                        String currentProcessId = "";

                        if(parts.length!=1){
                            currentProcessId = parts[1];
                        }
                        if(currentProcessId.equals(deviceId)){
                            //do something
                        }
                        else{
                            updateDeviceIdFile(content);
                        }
                        setReadWriteMode(mDeviceIdFileId);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't read file.", exception));
        }
    }

    private void saveFile() {
        if (mDriveServiceHelper != null && mOpenFileId != null) {
            Log.d(TAG, "Saving " + mOpenFileId);
            mDriveServiceHelper.saveFile(mOpenFileId, "time.txt", "fileContent")
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Unable to save file via REST.", exception));
        }
    }

    /**
     * Queries the Drive REST API for files visible to this app and lists them in the content view.
     */
    private void getTimeTableFromCloud() {
        if (mDriveServiceHelper != null) {
            mDriveServiceHelper.queryDeviceIdFiles()
                    .addOnSuccessListener(fileList -> {
                        for (File file : fileList.getFiles()) {
                            mDeviceIdFileId = file.getId();
                            readDeviceIdFile();
                        }
                        setReadOnlyMode();
                    })
                    .addOnFailureListener(exception -> Log.e(TAG, "Unable to query files.", exception));
            mDriveServiceHelper.queryPasswordFiles()
                    .addOnSuccessListener(fileList -> {
                        for (File file : fileList.getFiles()) {
                            mPasswordFileID = file.getId();
                            readPasswordFile();
                        }
                        setReadOnlyMode();
                    })
                    .addOnFailureListener(exception -> Log.e(TAG, "Unable to query files.", exception));
            mDriveServiceHelper.queryCaptureImageFiles()
                    .addOnSuccessListener(fileList -> {
                        for (File file : fileList.getFiles()) {
                            String fileId = file.getId();
                            String fileName = file.getName();
                            mDriveServiceHelper.DownloadFile(fileId,fileName);
                        }
                        setReadOnlyMode();
                    })
                    .addOnFailureListener(exception -> Log.e(TAG, "Unable to query files.", exception));

        }
    }

    private void initTimeTable(String fileId) {
        if (mDriveServiceHelper != null) {
            Log.e(TAG, "Reading file " + fileId);

            mDriveServiceHelper.readFile(fileId)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;
                        String[] parts = content.split("F");
                        timeTable = new ArrayList<>();
                        for(int i=0;i<parts.length;i++){
                            if(parts[i].length()!=0){
                                TimeTableItem timeTableItem = convertStringToTimeTable('F'+parts[i]);
                                timeTable.add(timeTableItem);
                            }
                        }

                        setReadWriteMode(fileId);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't read file.", exception));
        }
    }
    public ArrayList<TimeTableItem> getTimeTable(){
        return this.timeTable;
    }
    public void notifyFromFragment(){
        getTimeTableFromCloud();
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
        return new TimeTableItem(from,to,interval,duration,sum);
    }
    /**
     * Updates the UI to read-only mode.
     */
    private void setReadOnlyMode() {

        mOpenFileId = null;
    }

    /**
     * Updates the UI to read/write mode on the document identified by {@code fileId}.
     */
    private void setReadWriteMode(String fileId) {

        mOpenFileId = fileId;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:{
                Intent intent = new Intent(MainActivity.this, ChangePasswordActivity.class);
                Bundle bundle =  new Bundle();
                bundle.putString("parent",parentPass);
                bundle.putString("children",childrenPass);
                intent.putExtras(bundle);
                activityPasswordLauncher.launch(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {
        super.onResume();
        readDeviceIdFile();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: " );
        if (mDriveServiceHelper != null) {
            Log.e("TAG", "Reading file " + mDeviceIdFileId);
            mDriveServiceHelper.readFile(mDeviceIdFileId)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;
                        String[] parts = content.split("ID:");
                        if(parts.length == 1||parts.length==0){
                            return;
                        }
                        String currentProcessId = parts[1];
                        StringBuilder stringBuilder =  new StringBuilder();
                        String newDeviceFileContent = "";
                        for(int i=0;i<parts.length;i++){
                            if(!parts[i].equals("")){
                                if(!parts[i].equals(deviceId)){
                                    Log.e(TAG, "onStop: "+parts[i] );
                                    stringBuilder.append("ID:").append(parts[i]).append("\n");
                                }
                            }
                        }
                        if (mDeviceIdFileId != null) {
                            mDriveServiceHelper.saveFile(mDeviceIdFileId, "deviceId.txt",stringBuilder.toString() )
                                    .addOnFailureListener(exception ->
                                            Log.e(TAG, "Unable to save file via REST.", exception));
                        }
                    })
                    .addOnFailureListener(exception ->
                            Log.e("TAG", "Couldn't read file.", exception));
        }
    }
    private void requestStoragePermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        } else {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions, 1);
        }
    }
    // Kiểm tra kết quả sau khi xin quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Thành công
            } else {
                // Thất bại
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}