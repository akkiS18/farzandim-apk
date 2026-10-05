package uz.farzandim.teacher.di

import androidx.room.Room
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.farzandim.teacher.data.local.AppDatabase
import uz.farzandim.teacher.data.local.SessionManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import uz.farzandim.teacher.data.remote.ApiService
import uz.farzandim.teacher.data.remote.AuthInterceptor
import uz.farzandim.teacher.data.remote.DynamicBaseUrlInterceptor
import uz.farzandim.teacher.data.remote.TenantInterceptor
import uz.farzandim.teacher.data.remote.TokenAuthenticator
import uz.farzandim.teacher.data.repository.AuthRepository
import uz.farzandim.teacher.data.repository.JournalRepository
import uz.farzandim.teacher.data.repository.ScheduleRepository
import uz.farzandim.teacher.data.sync.SyncManager
import java.util.concurrent.TimeUnit

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "farzandim_teacher.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().schoolDao() }
    single { get<AppDatabase>().sessionDao() }
    single { get<AppDatabase>().classDao() }
    single { get<AppDatabase>().subjectDao() }
    single { get<AppDatabase>().studentDao() }
    single { get<AppDatabase>().scheduleDao() }
    single { get<AppDatabase>().gradeDao() }
    single { get<AppDatabase>().commentDao() }
    single { get<AppDatabase>().syncQueueDao() }

    single { SessionManager(androidContext()) }
}

val networkModule = module {
    single<Gson> { GsonBuilder().setLenient().create() }
    single { DynamicBaseUrlInterceptor(get()) }
    single { TenantInterceptor(get()) }
    single { AuthInterceptor(get()) }
    single { uz.farzandim.teacher.data.remote.NetworkDebugInterceptor() }
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
            .addInterceptor(get<uz.farzandim.teacher.data.remote.NetworkDebugInterceptor>())
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
    single { AuthRepository(get(), get(), get(), get(), get()) }
    single { ScheduleRepository(get(), get()) }
    single { JournalRepository(get(), get(), get(), get(), get(), get()) }
    single { SyncManager(androidContext(), get(), get(), get()) }
}

val viewModelModule = module {
    factory { uz.farzandim.teacher.ui.viewmodel.AuthViewModel(get(), get()) }
    factory { uz.farzandim.teacher.ui.viewmodel.HomeViewModel(get(), get()) }
    factory { uz.farzandim.teacher.ui.viewmodel.JournalViewModel(get(), get()) }
    factory { uz.farzandim.teacher.ui.viewmodel.CommentViewModel(get(), get(), get()) }
}

val appModules = listOf(databaseModule, networkModule, repositoryModule, viewModelModule)
