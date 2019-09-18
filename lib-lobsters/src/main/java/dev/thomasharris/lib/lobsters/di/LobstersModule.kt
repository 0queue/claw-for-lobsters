package dev.thomasharris.lib.lobsters.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.thomasharris.lib.lobsters.LobstersService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

@Module
class LobstersModule {

    @Provides
    @Reusable
    fun lobstersService(): LobstersService {
        val moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()
        return Retrofit.Builder().baseUrl("https://lobste.rs")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LobstersService::class.java)
    }
}
