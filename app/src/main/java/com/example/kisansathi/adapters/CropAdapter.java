package com.example.kisansathi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kisansathi.R;
import com.example.kisansathi.models.CropGuide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> implements Filterable {

    private final Context context;
    private List<CropGuide> cropList;
    private List<CropGuide> fullCropList;

    public CropAdapter(Context context, List<CropGuide> cropList) {
        this.context = context;
        this.cropList = cropList;
        this.fullCropList = new ArrayList<>(cropList);
    }

    public void setCropList(List<CropGuide> newList) {
        this.cropList = newList;
        this.fullCropList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_crop_guide, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        CropGuide crop = cropList.get(position);

        holder.tvCropGuideName.setText(crop.getCropName());
        holder.tvCropGuideSeasonBadge.setText(crop.getSeason());

        String soilText = crop.getSoil() != null ? crop.getSoil() : "";
        holder.tvCropGuideSoil.setText(context.getString(R.string.soil_label) + ": " + soilText);

        holder.tvCropGuideSteps.setText(crop.getFarmingSteps());
        holder.tvCropGuideIrrigation.setText(crop.getIrrigation());

        if (crop.getFertilizerGuidance() != null && !crop.getFertilizerGuidance().trim().isEmpty()) {
            holder.tvCropGuideFertilizer.setText(crop.getFertilizerGuidance());
        } else {
            holder.tvCropGuideFertilizer.setText(context.getString(R.string.subtitle_crop_guide));
        }

        holder.tvCropGuideHarvesting.setText(crop.getHarvesting());
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    @Override
    public Filter getFilter() {
        return cropFilter;
    }

    private final Filter cropFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CropGuide> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(fullCropList);
            } else {
                String filterPattern = constraint.toString().toLowerCase(Locale.US).trim();

                for (CropGuide item : fullCropList) {
                    boolean matchName = item.getCropName() != null && item.getCropName().toLowerCase(Locale.US).contains(filterPattern);
                    boolean matchSeason = item.getSeason() != null && item.getSeason().toLowerCase(Locale.US).contains(filterPattern);
                    boolean matchSoil = item.getSoil() != null && item.getSoil().toLowerCase(Locale.US).contains(filterPattern);

                    if (matchName || matchSeason || matchSoil) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            cropList.clear();
            if (results.values != null) {
                //noinspection unchecked
                cropList.addAll((List<CropGuide>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    static class CropViewHolder extends RecyclerView.ViewHolder {
        TextView tvCropGuideName, tvCropGuideSeasonBadge, tvCropGuideSoil;
        TextView tvCropGuideSteps, tvCropGuideIrrigation, tvCropGuideFertilizer, tvCropGuideHarvesting;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCropGuideName = itemView.findViewById(R.id.tvCropGuideName);
            tvCropGuideSeasonBadge = itemView.findViewById(R.id.tvCropGuideSeasonBadge);
            tvCropGuideSoil = itemView.findViewById(R.id.tvCropGuideSoil);
            tvCropGuideSteps = itemView.findViewById(R.id.tvCropGuideSteps);
            tvCropGuideIrrigation = itemView.findViewById(R.id.tvCropGuideIrrigation);
            tvCropGuideFertilizer = itemView.findViewById(R.id.tvCropGuideFertilizer);
            tvCropGuideHarvesting = itemView.findViewById(R.id.tvCropGuideHarvesting);
        }
    }
}
