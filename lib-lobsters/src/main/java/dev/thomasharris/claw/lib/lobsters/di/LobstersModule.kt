package dev.thomasharris.claw.lib.lobsters.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dev.thomasharris.claw.lib.lobsters.Comment
import dev.thomasharris.claw.lib.lobsters.CommentStatus
import dev.thomasharris.claw.lib.lobsters.Database
import dev.thomasharris.claw.lib.lobsters.LobstersService
import dev.thomasharris.claw.lib.lobsters.Story
import dev.thomasharris.claw.lib.lobsters.User
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
class LobstersModule(private val context: Context) {

    @Provides
    @Singleton
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
    @Singleton
    fun lobstersDatabase(): Database {

        val dateAdapter = object : ColumnAdapter<Date, Long> {
            override fun encode(value: Date) = value.time

            override fun decode(databaseValue: Long) = Date().apply {
                time = databaseValue
            }
        }

        val statusAdapter = object : ColumnAdapter<CommentStatus, Long> {
            override fun decode(databaseValue: Long): CommentStatus {
                return CommentStatus.values()[databaseValue.toInt()]
            }

            override fun encode(value: CommentStatus): Long {
                return value.ordinal.toLong()
            }
        }

        return Database(
            driver = AndroidSqliteDriver(Database.Schema, context, "claw.db"),
            storyAdapter = Story.Adapter(
                tagsAdapter = object : ColumnAdapter<List<String>, String> {
                    override fun decode(databaseValue: String) =
                        databaseValue.split(",")

                    override fun encode(value: List<String>) =
                        value.joinToString(",")
                },
                createdAtAdapter = dateAdapter,
                insertedAtAdapter = dateAdapter
            ),
            userAdapter = User.Adapter(
                createdAtAdapter = dateAdapter,
                insertedAtAdapter = dateAdapter
            ),
            commentAdapter = Comment.Adapter(
                createdAtAdapter = dateAdapter,
                updatedAtAdapter = dateAdapter,
                insertedAtAdapter = dateAdapter,
                statusAdapter = statusAdapter
            )
        )
    }

    @Provides
    fun lobstersQueries(database: Database) = database.lobstersQueries

    @Provides
    @Singleton
    fun backgroundExecutor(): Executor = Executors.newSingleThreadExecutor()
}
