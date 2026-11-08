package com.example.kisansathi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kisansathi.R;
import com.example.kisansathi.models.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Product product, boolean isCurrentlyFavorite);
    }

    private final Context context;
    private List<Product> productList;
    private List<Product> fullProductList;
    private final OnProductClickListener clickListener;
    private OnFavoriteClickListener favoriteClickListener;
    private Set<String> favoriteProductIds = new HashSet<>();

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener clickListener) {
        this.context = context;
        this.productList = productList;
        this.fullProductList = new ArrayList<>(productList);
        this.clickListener = clickListener;
    }

    public ProductAdapter(Context context, List<Product> productList,
                          OnProductClickListener clickListener,
                          OnFavoriteClickListener favoriteClickListener) {
        this.context = context;
        this.productList = productList;
        this.fullProductList = new ArrayList<>(productList);
        this.clickListener = clickListener;
        this.favoriteClickListener = favoriteClickListener;
    }

    public void setFavoriteClickListener(OnFavoriteClickListener favoriteClickListener) {
        this.favoriteClickListener = favoriteClickListener;
    }

    public void setFavoriteProductIds(Set<String> favoriteProductIds) {
        this.favoriteProductIds = (favoriteProductIds != null) ? new HashSet<>(favoriteProductIds) : new HashSet<>();
        notifyDataSetChanged();
    }

    public void setProductList(List<Product> newList) {
        this.productList = newList;
        this.fullProductList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvProductCategoryBadge.setText(product.getCategory());
        holder.tvProductCompany.setText(product.getCompany());
        holder.tvProductPackSize.setText(product.getPackSize());

        holder.tvProductPrice.setText(String.format(Locale.US, "₹%.2f", product.getPrice()));
        holder.tvProductUnitPrice.setText(product.getFormattedUnitPrice());

        if (product.getUpdatedDate() != null) {
            holder.tvProductUpdatedDate.setText(context.getString(R.string.last_updated, product.getUpdatedDate()));
        } else {
            holder.tvProductUpdatedDate.setText(context.getString(R.string.last_updated, "N/A"));
        }

        if (product.isSampleData()) {
            holder.tvDemoDataNotice.setVisibility(View.VISIBLE);
        } else {
            holder.tvDemoDataNotice.setVisibility(View.GONE);
        }

        boolean isFav = product.getProductId() != null && favoriteProductIds.contains(product.getProductId());
        if (isFav) {
            holder.btnItemFavorite.setImageResource(R.drawable.ic_favorite);
            holder.btnItemFavorite.setContentDescription(context.getString(R.string.remove_favorite));
        } else {
            holder.btnItemFavorite.setImageResource(R.drawable.ic_favorite_border);
            holder.btnItemFavorite.setContentDescription(context.getString(R.string.add_favorite));
        }

        holder.btnItemFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(product, isFav);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private final Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Product> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(fullProductList);
            } else {
                String filterPattern = constraint.toString().toLowerCase(Locale.US).trim();

                for (Product item : fullProductList) {
                    boolean matchesName = item.getName() != null && item.getName().toLowerCase(Locale.US).contains(filterPattern);
                    boolean matchesCompany = item.getCompany() != null && item.getCompany().toLowerCase(Locale.US).contains(filterPattern);
                    boolean matchesCategory = item.getCategory() != null && item.getCategory().toLowerCase(Locale.US).contains(filterPattern);

                    if (matchesName || matchesCompany || matchesCategory) {
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
            productList.clear();
            if (results.values != null) {
                //noinspection unchecked
                productList.addAll((List<Product>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductCategoryBadge, tvProductCompany;
        TextView tvProductPackSize, tvProductPrice, tvProductUnitPrice;
        TextView tvProductUpdatedDate, tvDemoDataNotice;
        ImageButton btnItemFavorite;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductCategoryBadge = itemView.findViewById(R.id.tvProductCategoryBadge);
            tvProductCompany = itemView.findViewById(R.id.tvProductCompany);
            tvProductPackSize = itemView.findViewById(R.id.tvProductPackSize);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductUnitPrice = itemView.findViewById(R.id.tvProductUnitPrice);
            tvProductUpdatedDate = itemView.findViewById(R.id.tvProductUpdatedDate);
            tvDemoDataNotice = itemView.findViewById(R.id.tvDemoDataNotice);
            btnItemFavorite = itemView.findViewById(R.id.btnItemFavorite);
        }
    }
}
