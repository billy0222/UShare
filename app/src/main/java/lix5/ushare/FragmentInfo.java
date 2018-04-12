package lix5.ushare;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FragmentInfo extends Fragment {
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database

    private TextView pickUp;
    private TextView dropOff;
    private TextView dateTime;
    private TextView remark;


    public FragmentInfo() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_info, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        pickUp = view.findViewById(R.id.info_pick_up);
        dropOff = view.findViewById(R.id.info_drop_off);
        dateTime = view.findViewById(R.id.info_time);
        remark = view.findViewById(R.id.remarks_input);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Event event = (Event) getActivity().getIntent().getSerializableExtra("event");
        pickUp.setText(event.getPickUp());
        dropOff.setText(event.getDropOff());
        dateTime.setText(event.getDateTime());
        remark.setText(event.getMessage());
    }
}
