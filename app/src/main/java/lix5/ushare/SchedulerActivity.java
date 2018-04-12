package lix5.ushare;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

public class SchedulerActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedular);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_schedule);
        fab.setOnClickListener(v -> startActivity(new Intent(SchedulerActivity.this, AddScheduleActivity.class)));

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}
