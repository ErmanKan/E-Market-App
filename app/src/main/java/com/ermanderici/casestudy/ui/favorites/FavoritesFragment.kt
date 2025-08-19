package com.ermanderici.casestudy.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ermanderici.casestudy.databinding.FragmentFavoritesBinding
import com.ermanderici.casestudy.ui.home.ProductAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment :
    Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val favoritesViewModel: FavoritesViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            emptyList(),
            onFavoriteClick = { product, _ ->
                favoritesViewModel.toggleFavoriteStatus(product)
            },
            onAddToCartClick = { product ->
                Toast.makeText(context, "onAddToCartClicked ${product.name}", Toast.LENGTH_SHORT)
                    .show()
            },
            onProductClick = { product ->
                val action = FavoritesFragmentDirections.actionFavoritesFragmentToProductDetailFragment(product.id)
                findNavController().navigate(action)
            }
        )

        binding.favoritesRecyclerView.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    favoritesViewModel.uiState.collectLatest { state ->
                        binding.favoritesProgressBar.isVisible =
                            state.isLoading && state.products.isEmpty()
                        binding.favoritesEmptyTextView.isVisible =
                            !state.isLoading && state.products.isEmpty()
                        binding.favoritesRecyclerView.isVisible =
                            !state.isLoading && state.products.isNotEmpty()

                        productAdapter.updateProducts(state.products)

                        if (state.errorMessage != null) {
                            Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
                            favoritesViewModel.onErrorMessageShown()
                        }

                        state.productFavoriteUpdatedEvent?.let { product ->
                            val message = if (product.isFavorite) {
                                "${product.name} added to favorites."
                            } else {
                                "${product.name} removed from favorites."
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            favoritesViewModel.onFavoriteEventHandled()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.favoritesRecyclerView.adapter = null
        _binding = null
    }
}
