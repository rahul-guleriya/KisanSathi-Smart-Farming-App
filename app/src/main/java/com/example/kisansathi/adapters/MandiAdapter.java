package com.example.kisansathi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kisansathi.R;
import com.example.kisansathi.models.MandiPrice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MandiAdapter extends RecyclerView.Adapter<MandiAdapter.MandiViewHolder> {

    private final Context context;
    private final List<MandiPrice> mandiList = new ArrayList<>();

    public MandiAdapter(Context context, List<MandiPrice> initialList) {
        this.context = context;
        if (initialList != null) {
            this.mandiList.addAll(initialList);
        }
    }

    public void updateList(List<MandiPrice> newList) {
        List<MandiPrice> newCopy = (newList != null) ? new ArrayList<>(newList) : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MandiDiffCallback(this.mandiList, newCopy));
        this.mandiList.clear();
        this.mandiList.addAll(newCopy);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public MandiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mandi_price, parent, false);
        return new MandiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MandiViewHolder holder, int position) {
        MandiPrice item = mandiList.get(position);

        holder.tvMandiCropName.setText(item.getCropName());
        holder.tvMandiLocation.setText(item.getMandiName() + ", " + item.getDistrict() + ", " + item.getState());

        String unit = item.getUnit() != null ? item.getUnit() : "Qtl";
        holder.tvMandiMinPrice.setText(String.format(Locale.US, "₹%.0f / %s", item.getMinPrice(), unit));
        holder.tvMandiModalPrice.setText(String.format(Locale.US, "₹%.0f / %s", item.getModalPrice(), unit));
        holder.tvMandiMaxPrice.setText(String.format(Locale.US, "₹%.0f / %s", item.getMaxPrice(), unit));

        String sourceStr = item.getSource() != null ? item.getSource() : "Market Committee";
        String dateStr = item.getDate() != null ? item.getDate() : "N/A";

        holder.tvMandiSource.setText(context.getString(R.string.source_format, sourceStr));
        holder.tvMandiDate.setText(context.getString(R.string.date_format, dateStr));

        if (item.isSampleData()) {
            holder.tvMandiDemoNotice.setVisibility(View.VISIBLE);
        } else {
            holder.tvMandiDemoNotice.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mandiList.size();
    }

    public static class MandiViewHolder extends RecyclerView.ViewHolder {
        TextView tvMandiCropName, tvMandiDemoNotice, tvMandiLocation;
        TextView tvMandiMinPrice, tvMandiModalPrice, tvMandiMaxPrice;
        TextView tvMandiSource, tvMandiDate;

        public MandiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMandiCropName = itemView.findViewById(R.id.tvMandiCropName);
            tvMandiDemoNotice = itemView.findViewById(R.id.tvMandiDemoNotice);
            tvMandiLocation = itemView.findViewById(R.id.tvMandiLocation);
            tvMandiMinPrice = itemView.findViewById(R.id.tvMandiMinPrice);
            tvMandiModalPrice = itemView.findViewById(R.id.tvMandiModalPrice);
            tvMandiMaxPrice = itemView.findViewById(R.id.tvMandiMaxPrice);
            tvMandiSource = itemView.findViewById(R.id.tvMandiSource);
            tvMandiDate = itemView.findViewById(R.id.tvMandiDate);
        }
    }

    private static class MandiDiffCallback extends DiffUtil.Callback {
        private final List<MandiPrice> oldList;
        private final List<MandiPrice> newList;

        public MandiDiffCallback(List<MandiPrice> oldList, List<MandiPrice> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList != null ? oldList.size() : 0;
        }

        @Override
        public int getNewListSize() {
            return newList != null ? newList.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            MandiPrice oldItem = oldList.get(oldItemPosition);
            MandiPrice newItem = newList.get(newItemPosition);
            if (oldItem.getRecordId() != null && newItem.getRecordId() != null) {
                return oldItem.getRecordId().equals(newItem.getRecordId());
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            MandiPrice oldItem = oldList.get(oldItemPosition);
            MandiPrice newItem = newList.get(newItemPosition);
            return oldItem.getModalPrice() == newItem.getModalPrice()
                    && oldItem.getMinPrice() == newItem.getMinPrice()
                    && oldItem.getMaxPrice() == newItem.getMaxPrice()
                    && equalsNullSafe(oldItem.getCropName(), newItem.getCropName())
                    && equalsNullSafe(oldItem.getState(), newItem.getState())
                    && equalsNullSafe(oldItem.getDistrict(), newItem.getDistrict())
                    && equalsNullSafe(oldItem.getMandiName(), newItem.getMandiName())
                    && equalsNullSafe(oldItem.getDate(), newItem.getDate());
        }

        private boolean equalsNullSafe(String a, String b) {
            if (a == null && b == null) return true;
            if (a == null || b == null) return false;
            return a.equals(b);
        }
    }
}
