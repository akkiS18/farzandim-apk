package uz.farzandim.parent.di

import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.farzandim.parent.data.local.ParentDatabase
import uz.farzandim.parent.data.local.SessionManager
import uz.farzandim.parent.data.remote.*
import uz.farzandim.parent.data.repository.AuthRepository
import uz.farzandim.parent.data.repository.ParentRepository
import uz.farzandim.parent.data.sync.SyncManager
import uz.farzandim.parent.ui.viewmodel.AuthViewModel
import java.util.concurrent.TimeUnit

val appModule = module {
    single { GsonBuilder().setLenient().create() }
    single { SessionManager(androidContext()) }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            ParentDatabase::class.java,
            "farzandim_parent_v4.db"
        )
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    single { get<ParentDatabase>().childDao() }
    single { get<ParentDatabase>().gradeDao() }
    single { get<ParentDatabase>().scheduleDao() }
    single { get<ParentDatabase>().holidayDao() }
    single { get<ParentDatabase>().announcementDao() }
    single { get<ParentDatabase>().menuDao() }
    single { get<ParentDatabase>().clubDao() }
    single { get<ParentDatabase>().bookDao() }
    single { get<ParentDatabase>().syncQueueDao() }
}

val networkModule = module {
    single { DynamicBaseUrlInterceptor(get()) }
    single { TenantInterceptor(get()) }
    single { AuthInterceptor(get()) }
    single { NetworkDebugInterceptor() }
    single { TokenAuthenticator(get(), get()) }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<DynamicBaseUrlInterceptor>())
            .addInterceptor(get<TenantInterceptor>())
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(get<NetworkDebugInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .authenticator(get<TokenAuthenticator>())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    single {
        val sessionManager: SessionManager = get()
        val baseUrl = runBlocking { sessionManager.getBaseUrl() }

        Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
            .build()
            .create(ApiService::class.java)
    }
}

val repositoryModule = module {
    single { AuthRepository(get(), get(), get(), get()) }
    single { ParentRepository(get(), get(), get()) }
    single { SyncManager(androidContext(), get(), get()) }
}

val viewModelModule = module {
    factory { AuthViewModel(get(), get()) }
    factory { uz.farzandim.parent.ui.viewmodel.ParentViewModel(get(), get(), get()) }
}

val appModules = listOf(appModule, databaseModule, networkModule, repositoryModule, viewModelModule)
