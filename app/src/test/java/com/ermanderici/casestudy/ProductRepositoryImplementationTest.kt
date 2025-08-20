package com.ermanderici.casestudy

import com.ermanderici.casestudy.data.ProductDao
import com.ermanderici.casestudy.data.ProductRepositoryImpl
import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.network.ApiService
import com.ermanderici.casestudy.util.Resource
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException

class ProductRepositoryImplTest {

    @Mock
    private lateinit var mockApiService: ApiService

    @Mock
    private lateinit var mockProductDao: ProductDao

    private lateinit var productRepository: ProductRepositoryImpl

    private val testDispatcher = StandardTestDispatcher()

    private val sampleProduct1 = ProductModel("1", "Product 1", "Desc 1", "Brand A", "Model X", "100.00", "img1.jpg", false)
    private val sampleProduct2 = ProductModel("2", "Product 2", "Desc 2", "Brand B", "Model Y", "200.00", "img2.jpg", true)
    private val sampleNetworkList = listOf(sampleProduct1, sampleProduct2.copy(isFavorite = false))
    private val sampleDaoList = listOf(sampleProduct1, sampleProduct2)


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        productRepository = ProductRepositoryImpl(mockApiService, mockProductDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getProducts emits loading with initial cache then success with merged network data`() = runTest(testDispatcher) {
        val initialCache = listOf(sampleProduct1.copy(name = "Initial P1"), sampleProduct2.copy(isFavorite = true))
        val networkProducts = listOf(
            sampleProduct1.copy(name = "Network P1", price = "105.00"),
            sampleProduct2.copy(name = "Network P2", price = "205.00", isFavorite = false)
        )
        val finalMergedAndSavedProducts = listOf(
            sampleProduct1.copy(name = "Network P1", price = "105.00", isFavorite = false),
            sampleProduct2.copy(name = "Network P2", price = "205.00", isFavorite = true)
        )

        whenever(mockProductDao.getAllProducts())
            .thenReturn(flowOf(initialCache))
            .thenReturn(flowOf(finalMergedAndSavedProducts))


        whenever(mockApiService.getProducts()).thenReturn(Response.success(networkProducts))

        val results = productRepository.getProducts().toList()

        assertThat(results.size).isEqualTo(2)
        assertThat(results[0]).isInstanceOf(Resource.Loading::class.java)
        assertThat(results[0].data).isEqualTo(initialCache)

        assertThat(results[1]).isInstanceOf(Resource.Success::class.java)
        assertThat(results[1].data).isEqualTo(finalMergedAndSavedProducts)

        verify(mockApiService).getProducts()
        verify(mockProductDao).insertAll(finalMergedAndSavedProducts)
        verify(mockProductDao, times(3)).getAllProducts()
    }


    @Test
    fun `getProducts emits loading then error on network IO exception uses initial cache for error`() = runTest(testDispatcher) {
        val initialCache = listOf(sampleProduct1)
        whenever(mockProductDao.getAllProducts())
            .thenReturn(flowOf(initialCache))
            .thenReturn(flowOf(initialCache))


        whenever(mockApiService.getProducts()).thenThrow(IOException("Network unavailable"))

        val results = productRepository.getProducts().toList()

        assertThat(results.size).isEqualTo(2)
        assertThat(results[0]).isInstanceOf(Resource.Loading::class.java)
        assertThat(results[0].data).isEqualTo(initialCache)

        assertThat(results[1]).isInstanceOf(Resource.Error::class.java)
        assertThat(results[1].data).isEqualTo(initialCache)
        assertThat(results[1].message).contains("Network Connection Error: Network unavailable")

        verify(mockApiService).getProducts()
        verify(mockProductDao, never()).insertAll(any())
        verify(mockProductDao, times(3)).getAllProducts()
    }

    @Test
    fun `getProducts emits loading then error on API non-successful response uses initial cache for error`() = runTest(testDispatcher) {
        val initialCache = listOf(sampleProduct1)
        whenever(mockProductDao.getAllProducts())
            .thenReturn(flowOf(initialCache))
            .thenReturn(flowOf(initialCache))
        val errorResponse = Response.error<List<ProductModel>>(500, "Server Error".toResponseBody(null))
        whenever(mockApiService.getProducts()).thenReturn(errorResponse)

        val results = productRepository.getProducts().toList()

        assertThat(results.size).isEqualTo(2)
        assertThat(results[0]).isInstanceOf(Resource.Loading::class.java)
        assertThat(results[0].data).isEqualTo(initialCache)

        assertThat(results[1]).isInstanceOf(Resource.Error::class.java)
        assertThat(results[1].data).isEqualTo(initialCache)
        assertThat(results[1].message).contains("Network Error: 500 Server Error")

        verify(mockApiService).getProducts()
        verify(mockProductDao, never()).insertAll(any())
        verify(mockProductDao, times(3)).getAllProducts()
    }

    @Test
    fun `getProducts emits loading then success with empty list if network body is null and db is empty`() = runTest(testDispatcher) {
        val initialCache = emptyList<ProductModel>()
        whenever(mockProductDao.getAllProducts())
            .thenReturn(flowOf(initialCache))
            .thenReturn(flowOf(emptyList()))

        whenever(mockApiService.getProducts()).thenReturn(Response.success(null))

        val results = productRepository.getProducts().toList()

        assertThat(results.size).isEqualTo(2)
        assertThat(results[0]).isInstanceOf(Resource.Loading::class.java)
        assertThat(results[0].data).isEqualTo(initialCache)

        assertThat(results[1]).isInstanceOf(Resource.Success::class.java)
        assertThat(results[1].data).isEmpty()

        verify(mockApiService).getProducts()
        verify(mockProductDao, never()).insertAll(any())
        verify(mockProductDao, times(3)).getAllProducts()
    }


    @Test
    fun `updateFavoriteStatus success`() = runTest(testDispatcher) {
        val productId = "1"
        val isFavorite = true
        val updatedProduct = sampleProduct1.copy(isFavorite = true)
        whenever(mockProductDao.getProductById(productId)).thenReturn(updatedProduct)

        val result = productRepository.updateFavoriteStatus(productId, isFavorite)

        verify(mockProductDao).updateFavoriteStatus(productId, isFavorite)
        verify(mockProductDao).getProductById(productId)
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat(result.data).isEqualTo(updatedProduct)
    }

    @Test
    fun `updateFavoriteStatus product not found after update`() = runTest(testDispatcher) {
        val productId = "1"
        val isFavorite = true
        whenever(mockProductDao.getProductById(productId)).thenReturn(null)

        val result = productRepository.updateFavoriteStatus(productId, isFavorite)

        verify(mockProductDao).updateFavoriteStatus(productId, isFavorite)
        verify(mockProductDao).getProductById(productId)
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Product not found after update.")
    }

    @Test
    fun `updateFavoriteStatus DAO exception`() = runTest(testDispatcher) {
        val productId = "1"
        val isFavorite = true
        val exception = RuntimeException("DAO failed")
        whenever(mockProductDao.updateFavoriteStatus(productId, isFavorite)).thenThrow(exception)

        val result = productRepository.updateFavoriteStatus(productId, isFavorite)

        verify(mockProductDao).updateFavoriteStatus(productId, isFavorite)
        verify(mockProductDao, never()).getProductById(any())
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Failed to update favorite status in DB: DAO failed")
        assertThat(result.exception).isEqualTo(exception)
    }

    @Test
    fun `getProductById success with product`() = runTest(testDispatcher) {
        val productId = "1"
        whenever(mockProductDao.getProductById(productId)).thenReturn(sampleProduct1)

        val result = productRepository.getProductById(productId)

        verify(mockProductDao).getProductById(productId)
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat(result.data).isEqualTo(sampleProduct1)
    }

    @Test
    fun `getProductById success with null if not found`() = runTest(testDispatcher) {
        val productId = "unknown"
        whenever(mockProductDao.getProductById(productId)).thenReturn(null)

        val result = productRepository.getProductById(productId)

        verify(mockProductDao).getProductById(productId)
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat(result.data).isNull()
    }

    @Test
    fun `getProductById DAO exception`() = runTest(testDispatcher) {
        val productId = "1"
        val exception = RuntimeException("DAO failed")
        whenever(mockProductDao.getProductById(productId)).thenThrow(exception)

        val result = productRepository.getProductById(productId)

        verify(mockProductDao).getProductById(productId)
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Failed to get product by ID from DB: DAO failed")
        assertThat(result.data).isNull()
        assertThat(result.exception).isEqualTo(exception)
    }

    @Test
    fun `getFavoriteProducts success`() = runTest(testDispatcher) {
        val favoriteProducts = listOf(sampleProduct2)
        whenever(mockProductDao.getFavoriteProducts()).thenReturn(flowOf(favoriteProducts))

        val result = productRepository.getFavoriteProducts().first()

        verify(mockProductDao).getFavoriteProducts()
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat(result.data).isEqualTo(favoriteProducts)
    }

    @Test
    fun `getFavoriteProducts success with empty list`() = runTest(testDispatcher) {
        whenever(mockProductDao.getFavoriteProducts()).thenReturn(flowOf(emptyList()))

        val result = productRepository.getFavoriteProducts().first()

        verify(mockProductDao).getFavoriteProducts()
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat(result.data).isEmpty()
    }

    @Test
    fun `getFavoriteProducts DAO exception in flow`() = runTest(testDispatcher) {
        val exception = IOException("DB error")
        whenever(mockProductDao.getFavoriteProducts()).thenReturn(flow { throw exception })


        val result = productRepository.getFavoriteProducts().first()


        verify(mockProductDao).getFavoriteProducts()
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Failed to load favorites from database: DB error")
        assertThat(result.data).isEmpty()
    }
}

