package com.ermanderici.casestudy.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.ermanderici.casestudy.databinding.FragmentHomeBinding // Assuming ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!! // Property delegate to ensure non-null

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
                homeViewModel.toggleFavoriteStatus(product)
            },
            onAddToCartClick = { product ->
                homeViewModel.addToCart(product)
            }
        )
        binding.productsRecyclerView.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), 2) // Or your preferred layout
            // Add ItemDecoration for spacing if needed
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.productsUiState.collectLatest { uiState ->
                    binding.progressBar.visibility = if (uiState.isLoading && uiState.products.isEmpty()) View.VISIBLE else View.GONE // Show progress only if loading initial empty list

                    if (!uiState.isLoading && uiState.errorMessage != null) {
                        Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()

                    }

                    productAdapter.updateProducts(uiState.products)
                }
            }
        }

        homeViewModel.productFavoriteUpdatedEvent.observe(viewLifecycleOwner) { product ->
            product?.let {

                Toast.makeText(context, "${it.name} favorite: ${it.isFavorite}", Toast.LENGTH_SHORT).show()
                homeViewModel.onFavoriteEventHandled()
            }
        }

        homeViewModel.cartMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                homeViewModel.onCartMessageShown()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
