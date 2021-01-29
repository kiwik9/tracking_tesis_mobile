package dev.kiwik.tracking.domain.db

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.kiwik.tracking.MvpApp
import dev.kiwik.tracking.domain.db.dao.ResourcesDao
import dev.kiwik.tracking.domain.db.dao.TrackingBaseDao
import dev.kiwik.tracking.domain.db.dao.TrackingDao
import dev.kiwik.tracking.domain.entities.Category
import dev.kiwik.tracking.domain.entities.Place
import dev.kiwik.tracking.domain.entities.Tracking
import dev.kiwik.tracking.domain.entities.TrackingBase
import kotlinx.coroutines.runBlocking


@Database(
        entities = [Tracking::class, TrackingBase::class, Category::class, Place::class],
        version = 1
)
abstract class AppDatabase : RoomDatabase() {

    private val mIsDatabaseCreated = MutableLiveData<Boolean>()

    abstract fun trackingDao(): TrackingDao
    abstract fun resourceDao(): ResourcesDao
    abstract fun trackingBaseDao(): TrackingBaseDao

    val databaseCreated: LiveData<Boolean>
        get() = mIsDatabaseCreated

    // abstract fun dispatchActionDao(): DispatchActionDao

    //Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
    private fun updateDatabaseCreated(context: Context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated()
        }
    }

    private fun setDatabaseCreated() {
        mIsDatabaseCreated.postValue(true)
    }

    companion object {

        @VisibleForTesting
        val DATABASE_NAME = "custom_db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(): AppDatabase {
            val context = MvpApp.instance.applicationContext
            return instance ?: synchronized(this) {
                instance ?: buildDatabaseDev(context).also { instance = it }
            }
        }

        fun destroyInstance() {
            instance = null
        }


        /* Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
    */

        //PARA MIGRACIONES FUTURAS
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Since we didn't alter the table, there's nothing else to do here.
                //database.execSQL("ALTER TABLE Repo ADD COLUMN createdAt TEXT");
            }
        }

        private fun buildDatabasePro(appContext: Context): AppDatabase {
            val database = Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2).build()
            database.setDatabaseCreated()
            return database
        }

        //TODO este metodo es solo para desarrollo
        private fun buildDatabaseDev(appContext: Context): AppDatabase {
            val database = Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    // .allowMainThreadQueries() // todo: quitar esto solo es para probar
                    .build()
            database.setDatabaseCreated()
            runBlocking {


            }

            return database
        }
    }

}
