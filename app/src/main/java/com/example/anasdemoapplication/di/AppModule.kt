package com.example.anasdemoapplication.di

import com.example.anasdemoapplication.data.StockRepository
import com.example.anasdemoapplication.data.StockRepositoryImpl
import com.example.anasdemoapplication.network.ApiInterface
import com.example.anasdemoapplication.utils.Constants
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindsStockRepository(stockRepositoryImpl: StockRepositoryImpl): StockRepository

    companion object {
        @Provides
        @Singleton
        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create()).build()
        }

        @Provides
        @Singleton
        fun getApiInterface(): ApiInterface {
            return getRetrofitInstance().create(ApiInterface::class.java)
        }
    }
}