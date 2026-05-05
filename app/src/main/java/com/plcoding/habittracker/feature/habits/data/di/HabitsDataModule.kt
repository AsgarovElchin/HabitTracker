package com.plcoding.habittracker.feature.habits.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.plcoding.habittracker.feature.habits.data.HabitDatabase
import com.plcoding.habittracker.feature.habits.data.RoomHabitDataSource
import com.plcoding.habittracker.feature.habits.data.SampleDataSeeder
import com.plcoding.habittracker.feature.habits.domain.HabitLocalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.dsl.module

val habitsDataModule = module {
    single {
        val db = Room.databaseBuilder(
            get<Context>(),
            HabitDatabase::class.java,
            "habits.db"
        ).fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = get<HabitDatabase>()
                    SampleDataSeeder.seed(database.habitDao)
                }
            }
        }).build()
        db
    }
    single { get<HabitDatabase>().habitDao }
    single<HabitLocalDataSource> { RoomHabitDataSource(get()) }
}
