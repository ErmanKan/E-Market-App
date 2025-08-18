package com.ermanderici.casestudy.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager // Import if not already
import com.ermanderici.casestudy.databinding.FragmentHomeBinding
import com.ermanderici.casestudy.model.ProductModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setObservers()

        return root
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            emptyList(),
            onFavoriteClick = { product, position ->
                homeViewModel.toggleFavoriteStatus(product)
            },
            onAddToCartClick = { product ->
                homeViewModel.addToCart(product)
            }
        )

        val spanCount = 2
        binding.productsRecyclerView.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), spanCount)
        }
    }

    private fun setObservers() {
        homeViewModel.products.observe(viewLifecycleOwner) { productsList ->
            if (productsList != null) {
                productAdapter.updateProducts(productsList)
                Log.d("HomeFragment", "Products updated: ${productsList.size} items")
            } else {
                Log.d("HomeFragment", "Products list is null")
            }
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                Log.e("HomeFragment", "Error: $errorMessage")
            }
        }
    }

    private fun observeViewModel() {
        homeViewModel.products.observe(viewLifecycleOwner) { productsList ->
            productsList?.let {
                productAdapter.updateProducts(it)
            }
        }

        homeViewModel.productUpdatedEvent.observe(viewLifecycleOwner) { product ->
            product?.let { updatedProduct ->
                val currentList = productAdapter.currentList()
                val position = currentList.indexOfFirst { it.id == updatedProduct.id }
                if (position != -1) {
                    productAdapter.productChanged(position)
                }
            }
        }

        homeViewModel.cartMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                 homeViewModel.onCartMessageShown() // To prevent re-showing on config change
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.productsRecyclerView.adapter = null
        _binding = null
    }
}
