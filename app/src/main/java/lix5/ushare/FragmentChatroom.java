package lix5.ushare;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FragmentChatroom extends Fragment {
    private EditText input;
    private Button send;
    private ListView listOfMessages;
    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database
    private FirebaseListAdapter<ChatMessage> adapter;
    private String event_key;
    private Event event;

    public FragmentChatroom(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_chatroom, container, false);
        input = view.findViewById(R.id.chatText);
        send = view.findViewById(R.id.buttonSend);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //listOfMessages = view.findViewById(R.id.list_of_messages);
        event_key = getActivity().getIntent().getStringExtra("event_key");
        event = (Event)getActivity().getIntent().getSerializableExtra("event");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*fab.setOnClickListener(view1 -> {
            Event tempEvent = event;
            mDatabase.child("users/").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tempEvent.getChatMessages().add(new ChatMessage(input.getText().toString(), dataSnapshot.getValue(User.class).getUsername()));
                    Map<String, Object> postValues = tempEvent.toMapEvent();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(event_key, postValues);
                    mDatabase.child("events").updateChildren(childUpdates);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        });*/
    }
}
