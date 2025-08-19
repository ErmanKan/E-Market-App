package com.ermanderici.casestudy.ui.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.ermanderici.casestudy.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProductFilterDialogFragment : DialogFragment() {

    interface FilterDialogListener {
        fun onFiltersApplied(selectedBrands: Set<String>, selectedModels: Set<String>)
    }

    private var listener: FilterDialogListener? = null

    // Keep these as they are used to receive data and initialize selections
    private lateinit var availableBrands: Array<String>
    private lateinit var availableModels: Array<String>
    private lateinit var initiallySelectedBrandsBooleanArray: BooleanArray
    private lateinit var initiallySelectedModelsBooleanArray: BooleanArray

    // Store CheckBox views to easily retrieve their state later
    private val brandCheckBoxes = mutableListOf<CheckBox>()
    private val modelCheckBoxes = mutableListOf<CheckBox>()

    companion object {
        private const val ARG_AVAILABLE_BRANDS = "available_brands"
        private const val ARG_AVAILABLE_MODELS = "available_models"
        private const val ARG_SELECTED_BRANDS = "selected_brands"
        private const val ARG_SELECTED_MODELS = "selected_models"

        fun newInstance(
            availableBrands: List<String>,
            availableModels: List<String>,
            selectedBrands: Set<String>,
            selectedModels: Set<String>
        ): ProductFilterDialogFragment {
            val fragment = ProductFilterDialogFragment()
            val args = Bundle().apply {
                putStringArray(ARG_AVAILABLE_BRANDS, availableBrands.toTypedArray())
                putStringArray(ARG_AVAILABLE_MODELS, availableModels.toTypedArray())
                putStringArray(ARG_SELECTED_BRANDS, selectedBrands.toTypedArray())
                putStringArray(ARG_SELECTED_MODELS, selectedModels.toTypedArray())
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            availableBrands = it.getStringArray(ARG_AVAILABLE_BRANDS) ?: emptyArray()
            availableModels = it.getStringArray(ARG_AVAILABLE_MODELS) ?: emptyArray()
            val selectedBrandsSet = it.getStringArray(ARG_SELECTED_BRANDS)?.toSet() ?: emptySet()
            val selectedModelsSet = it.getStringArray(ARG_SELECTED_MODELS)?.toSet() ?: emptySet()

            // Initialize boolean arrays based on the passed-in selected sets
            initiallySelectedBrandsBooleanArray = availableBrands.map { brand -> selectedBrandsSet.contains(brand) }.toBooleanArray()
            initiallySelectedModelsBooleanArray = availableModels.map { model -> selectedModelsSet.contains(model) }.toBooleanArray()
        }

        if (parentFragment is FilterDialogListener) {
            listener = parentFragment as FilterDialogListener
        } else if (activity is FilterDialogListener) {
            listener = activity as FilterDialogListener
        } else {
            // Consider throwing an exception if the listener is not implemented by host
            // throw ClassCastException("$context must implement FilterDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_product_filters, null)

        val brandsContainer = dialogView.findViewById<LinearLayout>(R.id.brands_checkbox_container)
        val modelsContainer = dialogView.findViewById<LinearLayout>(R.id.models_checkbox_container)
        val brandTitle = dialogView.findViewById<TextView>(R.id.text_view_brand_title)
        val modelTitle = dialogView.findViewById<TextView>(R.id.text_view_model_title)

        brandCheckBoxes.clear()
        modelCheckBoxes.clear()

        // Populate Brand Checkboxes
        if (availableBrands.isNotEmpty()) {
            brandTitle.isVisible = true
            brandsContainer.isVisible = true
            availableBrands.forEachIndexed { index, brand ->
                val checkBox = CheckBox(requireContext()).apply {
                    text = brand
                    isChecked = initiallySelectedBrandsBooleanArray.getOrElse(index) { false }
                }
                brandsContainer.addView(checkBox)
                brandCheckBoxes.add(checkBox)
            }
        } else {
            brandTitle.isVisible = false
            brandsContainer.isVisible = false
        }

        // Populate Model Checkboxes
        if (availableModels.isNotEmpty()) {
            modelTitle.isVisible = true
            modelsContainer.isVisible = true
            availableModels.forEachIndexed { index, model ->
                val checkBox = CheckBox(requireContext()).apply {
                    text = model
                    isChecked = initiallySelectedModelsBooleanArray.getOrElse(index) { false }
                }
                modelsContainer.addView(checkBox)
                modelCheckBoxes.add(checkBox)
            }
        } else {
            modelTitle.isVisible = false
            modelsContainer.isVisible = false
        }


        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_filter_products)
            .setView(dialogView) // Set the custom inflated view
            .setPositiveButton(R.string.apply_filters) { _, _ ->
                val finalSelectedBrands = brandCheckBoxes
                    .filter { it.isChecked }
                    .map { it.text.toString() }
                    .toSet()

                val finalSelectedModels = modelCheckBoxes
                    .filter { it.isChecked }
                    .map { it.text.toString() }
                    .toSet()

                listener?.onFiltersApplied(finalSelectedBrands, finalSelectedModels)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.clear_filters) { _, _ ->
                listener?.onFiltersApplied(emptySet(), emptySet())
                // Optionally, uncheck all checkboxes in the UI here if dialog doesn't auto-dismiss
                // brandCheckBoxes.forEach { it.isChecked = false }
                // modelCheckBoxes.forEach { it.isChecked = false }
                // dismiss() // Already happens by default for neutral button
            }

        return builder.create()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
