package lix5.ushare;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentChatroom extends Fragment {
    private EditText chatText;
    private Button buttonSend;
    private RecyclerView mRecyclerView;
    private String event_key;

    private FirebaseAuth mAuth; //instance of FirebaseAuth
    private DatabaseReference mDatabase; //instance of Database
    private ArrayList<ChatMessage> myDataset;

    public FragmentChatroom() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_chatroom, container, false);
        chatText = view.findViewById(R.id.chatText);
        buttonSend = view.findViewById(R.id.buttonSend);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        event_key = getActivity().getIntent().getStringExtra("event_key");
        myDataset = new ArrayList<>();

        mRecyclerView = view.findViewById(R.id.recyclerView_message_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new FragmentChatroom.MyAdapter(myDataset));

        mRecyclerView.addOnLayoutChangeListener((view12, i, i1, i2, i3, i4, i5, i6, i7) -> mRecyclerView.scrollToPosition(myDataset.size() - 1));

        buttonSend.setOnClickListener(view1 -> {
            @SuppressLint("RestrictedApi")
            ChatMessage chatMessage = new ChatMessage(chatText.getText().toString(), mAuth.getUid());
            mDatabase.child("events").child(event_key).child("chatMessage").push().setValue(chatMessage);
            chatText.setText("");
        });

        mDatabase.child("events").child(event_key).child("chatMessage").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                myDataset.add(dataSnapshot.getValue(ChatMessage.class));
                mRecyclerView.getAdapter().notifyItemInserted(myDataset.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mRecyclerView.scrollToPosition(myDataset.size() - 1);
    }

    public class MyAdapter extends RecyclerView.Adapter<FragmentChatroom.MyAdapter.ViewHolder>{
        private ArrayList<ChatMessage> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder{
            public CircleImageView pic;
            public TextView message_user, message_time, message_text;

            public ViewHolder(View v){
                super(v);
                pic = v.findViewById(R.id.message_pic);
                message_user = v.findViewById(R.id.message_user);
                message_time = v.findViewById(R.id.message_time);
                message_text = v.findViewById(R.id.message_text);
            }
        }

        public MyAdapter(ArrayList<ChatMessage> mDataset){this.mDataset = mDataset;}

        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        public void onBindViewHolder(ViewHolder holder, int position){
            mDatabase.child("users").child(mDataset.get(position).getMessageUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Picasso.get().load(dataSnapshot.child("avatar").getValue(String.class)).into(holder.pic);
                    holder.message_user.setText(dataSnapshot.child("username").getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            holder.message_text.setText(mDataset.get(position).getMessageText());
            holder.message_time.setText(mDataset.get(position).getMessageTime().substring(0, 19));
        }

        public int getItemCount(){
            return mDataset.size();
        }
    }
}
