package space.gcy.androidmqtt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    List<String> data = new ArrayList<>();

    public void setData(List<String> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        String s = data.get(position);
        holder.mTextView.setText(s);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder{
        TextView mTextView;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
