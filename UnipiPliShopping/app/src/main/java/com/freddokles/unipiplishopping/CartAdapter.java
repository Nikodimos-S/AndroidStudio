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

    private final Context context;//context for layout inflation
    private final List<String> productNames;//list of product names
    private final HashMap<String, Integer> itemCounts;//map of product quantities
    private final List<Double> productPrices;//list of product prices
    private final OnQuantityChangeListener listener;//listener for quantity changes

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
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);//inflate layout
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        String productName = productNames.get(position);//get product name
        int quantity = itemCounts.get(productName);//get quantity
        double price = productPrices.get(position);//get price

        holder.productName.setText(productName);
        holder.productPrice.setText(String.format("€%.2f", price));
        holder.productQuantity.setText(String.valueOf(quantity));

        Glide.with(context)//load image
                .load(R.drawable.ic_launcher_foreground)
                .into(holder.productImage);

        holder.increaseButton.setOnClickListener(v -> listener.onIncreaseQuantity(productName));
        holder.decreaseButton.setOnClickListener(v -> listener.onDecreaseQuantity(productName));
    }

    @Override
    public int getItemCount() {
        return productNames.size();//return item count
    }

    public void removeItem(String productName) {
        int index = productNames.indexOf(productName);//find product index
        if (index != -1) {
            productNames.remove(index);
            productPrices.remove(index);
            itemCounts.remove(productName);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, productNames.size());
        }
    }

    public interface OnQuantityChangeListener {
        void onIncreaseQuantity(String productName);//increase quantity method
        void onDecreaseQuantity(String productName);//decrease quantity method
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName, productPrice, productQuantity;
        Button increaseButton, decreaseButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);//init image
            productName = itemView.findViewById(R.id.product_name);//init name
            productPrice = itemView.findViewById(R.id.product_price);//init price
            productQuantity = itemView.findViewById(R.id.product_quantity);//init quantity
            increaseButton = itemView.findViewById(R.id.increase_button);//init increase btn
            decreaseButton = itemView.findViewById(R.id.decrease_button);//init decrease btn
        }
    }
}