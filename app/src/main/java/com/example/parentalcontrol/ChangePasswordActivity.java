package com.example.parentalcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordActivity extends AppCompatActivity {
    String parentPass;
    String childrenPass;

    EditText parentEditText,childrenEditText;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Bundle bundle = getIntent().getExtras();
        parentPass = bundle.getString("parent");
        childrenPass = bundle.getString("children");
        Log.e("TAG", "onCreate: "+parentPass );

        parentEditText = findViewById(R.id.parentEditText);
        childrenEditText = findViewById(R.id.childrenEditText);

        parentEditText.setText(parentPass);
        childrenEditText.setText(childrenPass);

        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resIntent = new Intent(ChangePasswordActivity.this,MainActivity.class);
                Bundle bundle = new Bundle();
                parentPass = parentEditText.getText().toString();
                childrenPass = childrenEditText.getText().toString();
                if(parentPass.equals("")){
                    Toast.makeText(ChangePasswordActivity.this, "Không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(childrenPass.equals("")){
                    Toast.makeText(ChangePasswordActivity.this, "Không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                bundle.putString("newParentPass",parentPass);
                bundle.putString("newChildrenPass",childrenPass);
                resIntent.putExtras(bundle);
                setResult(RESULT_OK,resIntent);
                finish();
            }
        });


    }
}