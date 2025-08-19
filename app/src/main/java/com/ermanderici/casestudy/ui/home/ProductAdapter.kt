package com.ermanderici.casestudy.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ermanderici.casestudy.R
import com.ermanderici.casestudy.model.ProductModel

class ProductAdapter(
    private var products: List<ProductModel>,
    private val onFavoriteClick: (product: ProductModel, position: Int) -> Unit,
    private val onAddToCartClick: (product: ProductModel) -> Unit,
    private val onProductClick: (ProductModel) -> Unit
) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product, position, onFavoriteClick, onAddToCartClick)
        holder.itemView.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<ProductModel>) {
        products = newProducts
        notifyDataSetChanged()
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImageView: ImageView = itemView.findViewById(R.id.item_imageView)
        val productNameTextView: TextView = itemView.findViewById(R.id.product_name_placeholder)
        val productPriceTextView: TextView = itemView.findViewById(R.id.product_price_placeholder)
        val productButton: Button = itemView.findViewById(R.id.btn_addToCart)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.item_add_favorite)

        fun bind(
            product: ProductModel,
            position: Int,
            onFavoriteClick: (product: ProductModel, position: Int) -> Unit,
            onAddToCartClick: (product: ProductModel) -> Unit
        ) {
            if (product.image.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(product.image)
                    .placeholder(R.drawable.icon_empty_item)
                    .into(productImageView)
            } else {
                productImageView.setImageResource(R.drawable.icon_empty_item)
            }
            if (product.isFavorite) {
                favoriteButton.setImageResource(R.drawable.icon_star_filled)
            } else {
                favoriteButton.setImageResource(R.drawable.icon_star_outline_)
            }
            productPriceTextView.text = product.price
            productNameTextView.text = product.name
            productButton.setOnClickListener {
                onAddToCartClick(product)
            }
            favoriteButton.setOnClickListener {
                onFavoriteClick(product, position)
            }
        }
    }
}
