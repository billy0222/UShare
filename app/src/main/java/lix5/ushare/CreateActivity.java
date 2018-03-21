package lix5.ushare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class CreateActivity extends AppCompatActivity {
    private EditText seats;
    private ImageView add, remove;
    private ImageButton taxiButton, carButton;
    private CheckBox boys, girls, isRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);

        seats = (EditText) findViewById(R.id.number_of_seats);
        add = (ImageView) findViewById(R.id.add_seat);
        remove = (ImageView) findViewById(R.id.remove_seat);
        taxiButton = (ImageButton) findViewById(R.id.taxi_button);
        carButton = (ImageButton) findViewById(R.id.car_button);
        boys = (CheckBox) findViewById(R.id.boys);
        girls = (CheckBox) findViewById(R.id.girls);
        isRequest = (CheckBox) findViewById(R.id.isRequest);
        seats.setFilters(new InputFilter[]{new InputFilterMinMax("1", "10")});
        add.setOnClickListener(event->{
            if (!String.valueOf(seats.getText()).equals("10")) {
                seats.setText(String.valueOf(Integer.parseInt(seats.getText().toString()) + 1));
                remove.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_circle_outline_black_18dp));
                if (String.valueOf(seats.getText()).equals("10"))
                    add.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_circle_outline_gray_18dp));
            }
        });
        remove.setOnClickListener(event->{
            if (!String.valueOf(seats.getText()).equals("1")){
                seats.setText(String.valueOf(Integer.parseInt(seats.getText().toString())-1));
                add.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_circle_outline_black_18dp));
                if (String.valueOf(seats.getText()).equals("1"))
                 remove.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_circle_outline_gray_18dp));
            }
        });


    }

    public void onTypeSelected(View view){
        if(view.getId()==R.id.taxi_button){
            taxiButton.setImageDrawable(getResources().getDrawable(R.drawable.taxi_sign_icon));
            carButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_directions_car_gray_48dp));
        }
        if(view.getId()==R.id.car_button){
            carButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_directions_car_black_48dp));
            taxiButton.setImageDrawable(getResources().getDrawable(R.drawable.taxi_sign_gray));
            //TODO if no car plate -> isRequest.setChecked(true);
        }
    }



    public void onCheckboxClicked(View view) {
        if(view.getId()==R.id.boys)
            girls.setChecked(false);
        if(view.getId()==R.id.girls)
            boys.setChecked(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent();
            intent = new Intent(CreateActivity.this, MainActivity.class);
            startActivity(intent);
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public class InputFilterMinMax implements InputFilter {
        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

}
