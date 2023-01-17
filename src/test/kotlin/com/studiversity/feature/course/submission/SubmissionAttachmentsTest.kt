package com.studiversity.feature.course.submission

import com.studiversity.SupabaseConstants
import com.studiversity.database.DatabaseFactory
import com.studiversity.di.coroutineModule
import com.studiversity.di.supabaseClientModule
import com.studiversity.feature.course.work.submission.courseSubmissionModule
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import org.koin.test.KoinTest
import java.io.File

class SubmissionAttachmentsTest : KoinTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun start(): Unit = runBlocking {
            DatabaseFactory.database
            GlobalContext.startKoin {
                modules(
                    coroutineModule,
                    supabaseClientModule,
                    courseSubmissionModule
                )
            }
        }
    }

    private val storage: Storage by inject()
    private val goTrue by inject<GoTrue>()

    init {
        runBlocking {
            goTrue.importAuthToken(SupabaseConstants.key)
        }
    }

    @Test
    fun testAttachments(): Unit = runBlocking {
        storage["main"].list("Folders").onEach {
            println(it.name)
        }
    }

    @Test
    fun testAddFile(): Unit = runBlocking {
        storage["main"].upload(
            "Folder/data.txt",
            File("data.txt").readBytes()
        )
    }
}