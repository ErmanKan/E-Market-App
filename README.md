# CaseStudy Android App

## Overview

CaseStudy is an Android application designed to showcase a product listing with features such as searching, filtering, adding to cart, and marking products as favorites. It serves as a demonstration of modern Android development practices using Kotlin, Jetpack libraries, and Hilt for dependency injection.

The application fetches product data from a repository (simulated or remote) and displays it in a user-friendly interface. Users can interact with the product list by searching for specific items, applying filters based on brand and model, and managing their cart and favorite items.

## Features

*   **Product Listing:** Displays a list of products in a grid layout.
*   **Search:** Allows users to search for products by name.
*   **Filtering:**
    *   Filter products by **Brand**.
    *   Filter products by **Model**.
    *   A filter dialog allows users to select multiple filter options.
    *   Selected filters are displayed, and the product list updates accordingly.
*   **Add to Cart:** Users can add products to a shopping cart.
*   **Favorites:** Users can mark/unmark products as favorites.
*   **Product Detail View:** (Assumed, as a typical feature) Navigate to a detailed view for each product.
*   **State Management:** Utilizes `ViewModel` and Kotlin `StateFlow`/`LiveData` for robust UI state management.
*   **Dependency Injection:** Uses Hilt for managing dependencies.
*   **Asynchronous Operations:** LeverLeverages Kotlin Coroutines for background tasks and data fetching.

## Environment Setup

To ensure compatibility and a smooth build process, the project was developed and tested using the following environment:

*   **Android Studio:** `Android Studio Narwhal Feature Drop | 2025.1.2]`
*   **Android Gradle Plugin Version:** `8.12.0`
*   **Gradle Version:** `-8.13`
*   **Kotlin Version:** `2.0.21`
*   **Compile SDK Version:** `36`
*   **Min SDK Version:** `24`
*   **Target SDK Version:** `36`

## Project Structure

The project follows a standard Android project structure:

*   **`casestudy/`**
    *   **`data/`**: Contains data sources, repositories, and local/remote data handling logic.
        *   `ProductRepository.kt`
        *   `CartRepository.kt`
        *   `model/`: Data model classes (e.g., `ProductModel.kt`).
        *   `network/`: (If applicable) Network service interfaces and implementations.
        *   `db/`: (If applicable) Room database definitions.
    *   **`di/`**: Hilt dependency injection modules.
    *   **`ui/`**: Contains UI-related classes (Activities, Fragments, ViewModels, Adapters).
        *   `home/`: Features related to the home screen (product listing, search, filter).
            *   `HomeFragment.kt`
            *   `HomeViewModel.kt`
            *   `ProductAdapter.kt`
            *   `ProductFilterDialogFragment.kt`
        *   `detail/`: (If applicable) Features related to the product detail screen.
        *   `cart/`: (If applicable) Features related to the shopping cart.
    *   **`util/`**: Utility classes and extension functions (e.g., `Resource.kt`).
*   **`app/src/main/res/`**: Android resources.
    *   `layout/`: XML layout files for UI screens and components.
    *   `drawable/`: Vector drawables and other image assets.
    *   `values/`: Strings, colors, styles, dimensions.
    *   `navigation/`: Navigation graphs for Jetpack Navigation.
    