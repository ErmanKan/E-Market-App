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
class HomeFragment : Fragment() {

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
                    binding.progressBar.visibility = if (uiState.isLoading && uiState.products.isEmpty() && uiState.searchQuery.isEmpty()) View.VISIBLE else View.GONE
                    productAdapter.updateProducts(uiState.products)

                    if (!uiState.isLoading && uiState.errorMessage != null) {
                        binding.productsRecyclerView.visibility = View.GONE
                        binding.textViewEmptyOrError.text = uiState.errorMessage
                        binding.textViewEmptyOrError.visibility = View.VISIBLE
                    } else if (!uiState.isLoading && uiState.products.isEmpty()) {
                        binding.productsRecyclerView.visibility = View.GONE
                        if (uiState.searchQuery.isNotBlank()) {
                            binding.textViewEmptyOrError.text = getString(R.string.no_search_results_found)
                        } else {
                            binding.textViewEmptyOrError.text = getString(R.string.no_products_found)
                        }
                        binding.textViewEmptyOrError.visibility = View.VISIBLE
                    } else if(!uiState.isLoading) {
                        binding.textViewEmptyOrError.visibility = View.GONE
                        binding.productsRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.productsSearchView.setOnQueryTextListener(null)
        _binding = null
    }
}
