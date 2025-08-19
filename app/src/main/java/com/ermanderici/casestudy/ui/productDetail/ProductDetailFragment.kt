package com.ermanderici.casestudy.ui.productDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.ermanderici.casestudy.R
import com.ermanderici.casestudy.databinding.FragmentProductDetailBinding
import com.ermanderici.casestudy.model.ProductModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductDetailViewModel by viewModels()

    private val mainActivityActionBar get() = (activity as? AppCompatActivity)?.supportActionBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeUiState()
    }

    private fun setupClickListeners() {
        binding.productDetailFavoriteButton.setOnClickListener {
            viewModel.toggleFavorite()
        }
        binding.productDetailAddToCartButton.setOnClickListener {
            viewModel.addToCart()
        }
    }
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    binding.productDetailProgressBar.isVisible = state.isLoading

                    val isProductAvailable = state.product != null && !state.isLoading
                    binding.productDetailName.isVisible = isProductAvailable
                    binding.productDetailDescription.isVisible = isProductAvailable
                    binding.productDetailImage.isVisible = isProductAvailable
                    binding.productDetailFavoriteButton.isVisible = isProductAvailable

                    if (state.product != null) {
                        updateProductDetailsInUi(state.product, state.isFavorite)
                        mainActivityActionBar?.title = state.product.name
                    } else if (!state.isLoading && state.errorMessage != null) {
                        mainActivityActionBar?.title = getString(R.string.error_label)
                        binding.productDetailImage.isVisible = false
                        binding.productDetailName.text = ""
                        binding.productDetailDescription.text = ""
                        binding.productDetailPrice.text = ""
                    }
                    state.errorMessage?.let { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        viewModel.onErrorMessageShown()
                    }
                    state.actionMessage?.let { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        viewModel.onActionMessageShown()
                    }
                }
            }
        }
    }

    private fun updateProductDetailsInUi(product: ProductModel, isFavorite: Boolean) {
        binding.productDetailName.text = product.name
        binding.productDetailDescription.text = product.description
        try {
            val priceString = product.price.replace(",", "")
            val priceDouble = priceString.toDoubleOrNull() ?: 0.0
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            binding.productDetailPrice.text = format.format(priceDouble)
        } catch (e: NumberFormatException) {
            binding.productDetailPrice.text = "$0.00"
        }

        Glide.with(this)
            .load(product.image)
            .placeholder(R.drawable.icon_empty_item)
            .error(R.drawable.icon_empty_item)
            .into(binding.productDetailImage)

        binding.productDetailFavoriteButton.setImageResource(
            if (isFavorite) R.drawable.icon_star_filled else R.drawable.icon_star_outline_
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.uiState.value.let { state ->
            if (state.product != null && !state.isLoading) {
                mainActivityActionBar?.title = state.product.name
            } else if (state.errorMessage != null) {
                mainActivityActionBar?.title = getString(R.string.error_label)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
