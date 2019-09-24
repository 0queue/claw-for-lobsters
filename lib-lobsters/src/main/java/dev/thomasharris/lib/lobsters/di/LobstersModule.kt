package dev.thomasharris.lib.lobsters.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.thomasharris.lib.lobsters.Database
import dev.thomasharris.lib.lobsters.LobstersService
import dev.thomasharris.lib.lobsters.StoryDatabaseEntity
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

// TODO Scoping of some sort
@Module
class LobstersModule(private val context: Context) {

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

    @Provides
    fun lobstersDatabase(): Database {

        val dateAdapter = object : ColumnAdapter<Date, Long> {
            override fun encode(value: Date) = value.time

            override fun decode(databaseValue: Long) = Date().apply {
                time = databaseValue
            }
        }

        return Database(
            driver = AndroidSqliteDriver(Database.Schema, context, "claw.db"),
            StoryDatabaseEntityAdapter = StoryDatabaseEntity.Adapter(
                tagsAdapter = object : ColumnAdapter<List<String>, String> {
                    override fun decode(databaseValue: String) =
                        databaseValue.split(",")

                    override fun encode(value: List<String>) =
                        value.joinToString(",")
                },
                createdAtAdapter = dateAdapter,
                insertedAtAdapter = dateAdapter
            )
        )
    }
}
