package com.ermanderici.casestudy.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ermanderici.casestudy.databinding.FragmentCartBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class CartFragment : Fragment() {

    // Use View Binding
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()

        observeLoadingState()
        observeCartItemsAndUpdateUi()
        observeTotalPrice()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartViewModel)
        binding.cartRecyclerView.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.clearCartButton.setOnClickListener {
            cartViewModel.clearCart()
        }
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartViewModel.isLoading.collectLatest { isLoading ->
                    binding.cartProgressBar.isVisible = isLoading
                    if (isLoading) {
                        binding.cartRecyclerView.isVisible = false
                        binding.emptyCartTextView.isVisible = false
                        binding.totalPriceLayout.isVisible = false
                    }
                }
            }
        }
    }

    private fun observeCartItemsAndUpdateUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartViewModel.cartItems.collectLatest { items ->
                    cartAdapter.submitList(items)
                    if (!cartViewModel.isLoading.value) {
                        binding.cartRecyclerView.isVisible = items.isNotEmpty()
                        binding.emptyCartTextView.isVisible = items.isEmpty()
                        binding.totalPriceLayout.isVisible = items.isNotEmpty()
                    }
                }
            }
        }
    }

    private fun observeTotalPrice() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartViewModel.totalPrice.collectLatest { price ->
                    val format = NumberFormat.getCurrencyInstance(Locale.US)
                    binding.totalPriceTextView.text = format.format(price)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.cartRecyclerView.adapter = null
        _binding = null
    }
}
