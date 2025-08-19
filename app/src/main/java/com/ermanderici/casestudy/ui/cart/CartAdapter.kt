package com.ermanderici.casestudy.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ermanderici.casestudy.databinding.ItemCartBinding
import com.ermanderici.casestudy.model.CartItemActions
import com.ermanderici.casestudy.model.CartItemModel
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val cartItemActions: CartItemActions
) : ListAdapter<CartItemModel, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCartBinding.inflate(inflater, parent, false)
        return CartViewHolder(binding, cartItemActions)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = getItem(position)
        holder.bind(cartItem)
    }

    class CartViewHolder(
        private val binding: ItemCartBinding,
        private val actions: CartItemActions
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentCartItem: CartItemModel? = null

        init {
            binding.quantityAddButton.setOnClickListener {
                currentCartItem?.let { item -> actions.onQuantityIncrease(item) }
            }
            binding.quantityRemoveButton.setOnClickListener {
                currentCartItem?.let { item -> actions.onQuantityDecrease(item) }
            }
        }

        fun bind(item: CartItemModel) {
            currentCartItem = item

            binding.cartItemNameTextView.text = item.name
            binding.cartItemQuantityTextView.text = item.quantity.toString()

            try {
                val priceDouble = item.price.replace(",", ".").toDoubleOrNull() ?: 0.0
                val format = NumberFormat.getCurrencyInstance(Locale.US)
                binding.cartItemPriceTextView.text = format.format(priceDouble)
            } catch (e: NumberFormatException) {
                binding.cartItemPriceTextView.text = "$0.00"
            }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItemModel>() {
        override fun areItemsTheSame(oldItem: CartItemModel, newItem: CartItemModel): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartItemModel, newItem: CartItemModel): Boolean {
            return oldItem == newItem
        }
    }
}
