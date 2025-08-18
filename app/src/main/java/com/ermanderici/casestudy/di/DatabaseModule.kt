package com.ermanderici.casestudy.di

import android.content.Context
import androidx.room.Room
import com.ermanderici.casestudy.data.AppDatabase // Your AppDatabase class
import com.ermanderici.casestudy.data.ProductDao // Your ProductDao interface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "product_database" // Or your database name
        )
            // Add any other configurations like .fallbackToDestructiveMigration() if needed
            .build()
    }

    @Provides
    // No @Singleton needed here if AppDatabase is @Singleton,
    // as Hilt will always provide the same AppDatabase instance.
    fun provideProductDao(appDatabase: AppDatabase): ProductDao {
        return appDatabase.productDao() // Assumes AppDatabase has this method
    }
}