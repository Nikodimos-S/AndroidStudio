package com.freddokles.unipiplishopping;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.HashMap;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<String> productNames;
    private final HashMap<String, Integer> itemCounts;
    private final List<Double> productPrices;
    private final OnQuantityChangeListener listener;

    public CartAdapter(Context context, List<String> productNames, HashMap<String, Integer> itemCounts,
                       List<Double> productPrices, OnQuantityChangeListener listener) {
        this.context = context;
        this.productNames = productNames;
        this.itemCounts = itemCounts;
        this.productPrices = productPrices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        String productName = productNames.get(position);
        int quantity = itemCounts.get(productName);
        double price = productPrices.get(position);

        holder.productName.setText(productName);
        holder.productPrice.setText(String.format("€%.2f", price));
        holder.productQuantity.setText(String.valueOf(quantity));

        //add image(optional)
        Glide.with(context)
                .load(R.drawable.ic_launcher_foreground)
                .into(holder.productImage);

        holder.increaseButton.setOnClickListener(v -> listener.onIncreaseQuantity(productName));
        holder.decreaseButton.setOnClickListener(v -> listener.onDecreaseQuantity(productName));
    }

    @Override
    public int getItemCount() {
        return productNames.size();
    }

    public void removeItem(String productName) {
        int index = productNames.indexOf(productName);
        if (index != -1) {
            productNames.remove(index);
            productPrices.remove(index);
            itemCounts.remove(productName);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, productNames.size());
        }
    }

    public interface OnQuantityChangeListener {
        void onIncreaseQuantity(String productName);
        void onDecreaseQuantity(String productName);
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName, productPrice, productQuantity;
        Button increaseButton, decreaseButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            increaseButton = itemView.findViewById(R.id.increase_button);
            decreaseButton = itemView.findViewById(R.id.decrease_button);
        }
    }
}
