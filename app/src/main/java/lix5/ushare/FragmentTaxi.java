package lix5.ushare;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FragmentTaxi extends Fragment {
    ArrayList<String> myDataset;

    public FragmentTaxi() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDataset = new ArrayList<>();
        for(int i=0;i<10;i++){
            myDataset.add(i+"");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView rv = new RecyclerView(getContext());
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new rvAdapter(myDataset));
        return rv;

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_taxi, container, false);
    }


    public class rvAdapter extends RecyclerView.Adapter<rvAdapter.ViewHolder>{
        private List<String> mData;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.info_text);
            }
        }

        public rvAdapter(List<String> data) {
            mData = data;
        }

        @Override
        public rvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.taxi_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTextView.setText(mData.get(position));

        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

    }

}
