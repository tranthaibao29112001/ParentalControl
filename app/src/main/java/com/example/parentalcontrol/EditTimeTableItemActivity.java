package com.example.parentalcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class EditTimeTableItemActivity extends AppCompatActivity {
    TimeTableItem tableItem;

    EditText fromEditText,toEditText,durationEditText,intervalEditText,sumEditText;
    Button submitBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_time_table_item);
        if(getIntent().getExtras()!=null){
            tableItem = (TimeTableItem) getIntent().getExtras().get("time-item");
        }
        else{
            tableItem = new TimeTableItem();
        }
        fromEditText = findViewById(R.id.fromEditText);
        toEditText = findViewById(R.id.toEditText);
        durationEditText = findViewById(R.id.durationEditText);
        intervalEditText = findViewById(R.id.intervalEditText);
        sumEditText = findViewById(R.id.sumEditText);

        fromEditText.setText(tableItem.getFrom());
        toEditText.setText(tableItem.getTo());
        durationEditText.setText(tableItem.getDuration());
        intervalEditText.setText(tableItem.getInterval());
        sumEditText.setText(tableItem.getSum());

        fromEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar myCalender = Calendar.getInstance();
                int hour = myCalender.get(Calendar.HOUR_OF_DAY);
                int minute = myCalender.get(Calendar.MINUTE);
                TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (view.isShown()) {
                            myCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            myCalender.set(Calendar.MINUTE, minute);
                        }
                        String hourStr = String.valueOf(hourOfDay);
                        String minuteStr = String.valueOf(minute);
                        if(hourOfDay<10){
                            hourStr = "0"+hourStr;
                        }
                        if(minute<10){
                            minuteStr = "0"+minuteStr;
                        }
                        fromEditText.setText( hourStr + ":" + minuteStr);
                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(EditTimeTableItemActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, myTimeListener, hour, minute, true);
                timePickerDialog.setTitle("Choose hour:");
                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                timePickerDialog.show();
            }
        });
        toEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar myCalender = Calendar.getInstance();
                int hour = myCalender.get(Calendar.HOUR_OF_DAY);
                int minute = myCalender.get(Calendar.MINUTE);
                TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (view.isShown()) {
                            myCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            myCalender.set(Calendar.MINUTE, minute);
                        }
                        String hourStr = String.valueOf(hourOfDay);
                        String minuteStr = String.valueOf(minute);
                        if(hourOfDay<10){
                            hourStr = "0"+hourStr;
                        }
                        if(minute<10){
                            minuteStr = "0"+minuteStr;
                        }
                        toEditText.setText( hourStr + ":" + minuteStr);
                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(EditTimeTableItemActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, myTimeListener, hour, minute, true);
                timePickerDialog.setTitle("Choose hour:");
                timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                timePickerDialog.show();
            }
        });
        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String from = "";
                String to = "";
                String duration = "";
                String interval = "";
                String sum = "";
                from = fromEditText.getText().toString();
                to = toEditText.getText().toString();
                if(!checkValidInput(from,to)){
                    return;
                }
                duration = durationEditText.getText().toString();
                interval = intervalEditText.getText().toString();
                sum = sumEditText.getText().toString();
                Intent resIntent = new Intent(EditTimeTableItemActivity.this,MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("res-item",new TimeTableItem(from,to,duration,interval,sum));
                resIntent.putExtras(bundle);
                setResult(RESULT_OK,resIntent);
                finish();
            }
        });
    }
    private Boolean checkValidInput(String from, String to){
        int fromHour,fromMinutes,toHour,toMinutes;
        if(from.equals("")||to.equals("")){
            Toast.makeText(this, "Thời gian bắt đầu và kết thúc không được bỏ trống", Toast.LENGTH_SHORT).show();
            return false;
        }
        String[] parts = from.split(":");
        fromHour = Integer.parseInt(parts[0]);
        fromMinutes = Integer.parseInt(parts[1]);
        parts = to.split(":");
        toHour = Integer.parseInt(parts[0]);
        toMinutes = Integer.parseInt(parts[1]);

        if(fromHour>toHour ||((fromHour==toHour)&&(fromMinutes>toMinutes))){
            Toast.makeText(this, "Thời gian bắt đầu cần nhỏ hơn thời gian kết thúc", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }
}