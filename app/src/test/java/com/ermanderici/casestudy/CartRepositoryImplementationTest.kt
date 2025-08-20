package com.ermanderici.casestudy.data

import com.ermanderici.casestudy.model.CartItemModel
import com.ermanderici.casestudy.model.ProductModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CartRepositoryImplTest {

    @Mock
    private lateinit var mockCartDao: CartDao

    private lateinit var cartRepository: CartRepositoryImpl

    @Captor
    private lateinit var cartItemCaptor: ArgumentCaptor<CartItemModel>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        cartRepository = CartRepositoryImpl(mockCartDao)
    }

    @Test
    fun `getCartItems should return flow of cart items from DAO`() = runTest {
        val expectedCartItems = listOf(
            CartItemModel(productId = "p1", name = "Product Alpha", price = "100.00", image = "alpha.jpg", quantity = 1),
            CartItemModel(productId = "p2", name = "Product Beta", price = "200.50", image = "beta.jpg", quantity = 3)
        )
        whenever(mockCartDao.getAllCartItems()).thenReturn(flowOf(expectedCartItems))

        val resultFlow = cartRepository.getCartItems()
        val actualCartItems = resultFlow.first()

        verify(mockCartDao).getAllCartItems()
        assertThat(actualCartItems).isEqualTo(expectedCartItems)
    }

    @Test
    fun `addProductToCart should insert new item if product is not in cart`() = runTest {
        val productToAdd = ProductModel(
            id = "prodNew",
            name = "New Gadget",
            description = "Latest model",
            brand = "TechBrand",
            model = "X1000",
            price = "99.99",
            image = "new_gadget.png"
        )
        whenever(mockCartDao.getCartItemByProductId(productToAdd.id)).thenReturn(null)

        cartRepository.addProductToCart(productToAdd)

        verify(mockCartDao).getCartItemByProductId(productToAdd.id)
        verify(mockCartDao).insertOrUpdateItem(cartItemCaptor.capture())

        val capturedCartItem = cartItemCaptor.value
        assertThat(capturedCartItem.productId).isEqualTo(productToAdd.id)
        assertThat(capturedCartItem.name).isEqualTo(productToAdd.name)
        assertThat(capturedCartItem.price).isEqualTo(productToAdd.price)
        assertThat(capturedCartItem.image).isEqualTo(productToAdd.image)
        assertThat(capturedCartItem.quantity).isEqualTo(1)
    }

    @Test
    fun `addProductToCart should update quantity if product already exists in cart`() = runTest {
        val productToAdd = ProductModel(
            id = "prodExisting",
            name = "Old Gadget",
            description = "Classic model",
            brand = "RetroBrand",
            model = "Z500",
            price = "49.50",
            image = "old_gadget.jpg"
        )
        val existingCartItem = CartItemModel(
            productId = productToAdd.id,
            name = productToAdd.name,
            price = productToAdd.price,
            image = productToAdd.image,
            quantity = 2
        )
        whenever(mockCartDao.getCartItemByProductId(productToAdd.id)).thenReturn(existingCartItem)

        cartRepository.addProductToCart(productToAdd)

        verify(mockCartDao).getCartItemByProductId(productToAdd.id)
        verify(mockCartDao).insertOrUpdateItem(cartItemCaptor.capture())

        val capturedCartItem = cartItemCaptor.value
        assertThat(capturedCartItem.productId).isEqualTo(productToAdd.id)
        assertThat(capturedCartItem.name).isEqualTo(productToAdd.name)
        assertThat(capturedCartItem.price).isEqualTo(productToAdd.price)
        assertThat(capturedCartItem.image).isEqualTo(productToAdd.image)
        assertThat(capturedCartItem.quantity).isEqualTo(existingCartItem.quantity + 1)
    }

    @Test
    fun `removeProductFromCart should call DAO to delete the item`() = runTest {
        val productIdToRemove = "prodToRemove"

        cartRepository.removeProductFromCart(productIdToRemove)

        verify(mockCartDao).deleteItemByProductId(productIdToRemove)
    }

    @Test
    fun `updateItemQuantity should call DAO to update quantity if new quantity is positive`() = runTest {
        val productIdToUpdate = "prodToUpdate"
        val newPositiveQuantity = 5

        cartRepository.updateItemQuantity(productIdToUpdate, newPositiveQuantity)

        verify(mockCartDao).updateQuantity(productIdToUpdate, newPositiveQuantity)
        verify(mockCartDao, never()).deleteItemByProductId(any())
    }

    @Test
    fun `updateItemQuantity should call DAO to delete item if new quantity is zero`() = runTest {
        val productIdToUpdate = "prodToBeDeleted"
        val zeroQuantity = 0

        cartRepository.updateItemQuantity(productIdToUpdate, zeroQuantity)

        verify(mockCartDao).deleteItemByProductId(productIdToUpdate)
        verify(mockCartDao, never()).updateQuantity(any(), any())
    }

    @Test
    fun `updateItemQuantity should call DAO to delete item if new quantity is negative`() = runTest {
        val productIdToUpdate = "prodToBeDeletedNegative"
        val negativeQuantity = -2

        cartRepository.updateItemQuantity(productIdToUpdate, negativeQuantity)

        verify(mockCartDao).deleteItemByProductId(productIdToUpdate)
        verify(mockCartDao, never()).updateQuantity(any(), any())
    }

    @Test
    fun `clearCart should call DAO to clear all items`() = runTest {
        cartRepository.clearCart()

        verify(mockCartDao).clearCart()
    }
}
