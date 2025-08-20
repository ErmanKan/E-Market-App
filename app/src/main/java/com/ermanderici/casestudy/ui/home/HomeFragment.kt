package com.ermanderici.casestudy.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ermanderici.casestudy.R
import com.ermanderici.casestudy.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), ProductFilterDialogFragment.FilterDialogListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
        setupSearchView()
        setupFilterButton()
        observeViewModel()
        observeUiEvents()
    }

    private fun setupSearchView() {
        binding.productsSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                homeViewModel.searchProducts(query.orEmpty())
                binding.productsSearchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                homeViewModel.searchProducts(newText.orEmpty())
                return true
            }
        })
        val currentQuery = homeViewModel.searchQuery.value
        if (currentQuery.isNotEmpty()) {
            binding.productsSearchView.setQuery(currentQuery, false)
        }
    }

    private fun setupFilterButton() {
        binding.buttonOpenFilterDialog.setOnClickListener {
            val currentState = homeViewModel.productsUiState.value
            if (currentState.availableBrands.isNotEmpty() || currentState.availableModels.isNotEmpty()) {
                val dialog = ProductFilterDialogFragment.newInstance(
                    currentState.availableBrands,
                    currentState.availableModels,
                    currentState.selectedBrands,
                    currentState.selectedModels
                )
                dialog.show(childFragmentManager, "ProductFilterDialog")
            } else {
                Toast.makeText(context, "Filter options not available yet.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            emptyList(),
            onFavoriteClick = { product, _ ->
                homeViewModel.toggleFavoriteStatus(product)
            },
            onAddToCartClick = { product ->
                homeViewModel.addToCart(product)
            },
            onProductClick = { product ->
                val action = HomeFragmentDirections.actionHomeFragmentToProductDetailFragment(product.id)
                findNavController().navigate(action)
            }
        )
        binding.productsRecyclerView.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.productsUiState.collectLatest { uiState ->
                    binding.progressBar.visibility = if (uiState.isLoading && uiState.products.isEmpty() && uiState.searchQuery.isEmpty() && uiState.selectedBrands.isEmpty() && uiState.selectedModels.isEmpty()) View.VISIBLE else View.GONE
                    productAdapter.updateProducts(uiState.products)

                    updateSelectedFiltersDisplay(uiState.selectedBrands, uiState.selectedModels)

                    if (!uiState.isLoading && uiState.errorMessage != null) {
                        binding.productsRecyclerView.visibility = View.GONE
                        binding.textViewEmptyOrError.text = uiState.errorMessage
                        binding.textViewEmptyOrError.visibility = View.VISIBLE
                    } else if (!uiState.isLoading && uiState.products.isEmpty()) {
                        binding.productsRecyclerView.visibility = View.GONE
                        var emptyText = getString(R.string.no_products_found)
                        if (uiState.searchQuery.isNotBlank() || uiState.selectedBrands.isNotEmpty() || uiState.selectedModels.isNotEmpty()) {
                            emptyText = getString(R.string.no_search_results_found)
                        }
                        binding.textViewEmptyOrError.text = emptyText
                        binding.textViewEmptyOrError.visibility = View.VISIBLE
                    } else if(!uiState.isLoading) {
                        binding.textViewEmptyOrError.visibility = View.GONE
                        binding.productsRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateSelectedFiltersDisplay(selectedBrands: Set<String>, selectedModels: Set<String>) {
        val filtersText = mutableListOf<String>()
        if (selectedBrands.isNotEmpty()) {
            filtersText.add("Brands: ${selectedBrands.joinToString()}")
        }
        if (selectedModels.isNotEmpty()) {
            filtersText.add("Models: ${selectedModels.joinToString()}")
        }

        if (filtersText.isEmpty()) {
            binding.textViewSelectedFilters.text = getString(R.string.no_filters_applied)
        } else {
            binding.textViewSelectedFilters.text = filtersText.joinToString(" | ")
        }
    }

    private fun observeUiEvents() {
        homeViewModel.productFavoriteUpdatedEvent.observe(viewLifecycleOwner) { product ->
            product?.let {
                Toast.makeText(context, "${it.name} favorite status: ${if(it.isFavorite) "Added" else "Removed"}", Toast.LENGTH_SHORT).show()
                homeViewModel.onFavoriteEventHandled()
            }
        }

        homeViewModel.cartMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                homeViewModel.onCartMessageShown()
            }
        }

        homeViewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                homeViewModel.onToastMessageShown()
            }
        }
    }

    override fun onFiltersApplied(selectedBrands: Set<String>, selectedModels: Set<String>) {
        homeViewModel.setSelectedBrands(selectedBrands)
        homeViewModel.setSelectedModels(selectedModels)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.productsSearchView.setOnQueryTextListener(null)
        _binding = null
    }
}
