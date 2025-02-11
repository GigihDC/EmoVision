package com.verve.emovision.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

object AppModules {
    private val networkModule =
        module {
//            single<ApiServices> { ApiServices.invoke() }
        }
//
    private val localModule =
        module {
//            single<AppDatabase> { AppDatabase.createInstance(androidContext()) }
//            single<CartDao> { get<AppDatabase>().cartDao() }
//            single<ChatDao> { get<AppDatabase>().chatDao() }
//            single<MessageDao> { get<AppDatabase>().messageDao() }
//            single<UserPreference> { UserPreferenceImpl(get()) }
        }
//
    private val datasource =
        module {
//            single<PrefDataSource> { PrefDataSourceImpl(get()) }
//            single<AuthDataSource> { AuthDataSourceImpl(get()) }
//            single<CartDataSource> { CartDataSourceImpl(get()) }
//            single<CategoryDataSource> { CategoryDataSourceImpl(get()) }
//            single<ChatDataSource> { ChatDataSourceImpl(get()) }
//            single<FoodDataSource> { FoodDataSourceImpl(get()) }
//            single<OrderDataSource> { OrderDataSourceImpl(get()) }
//            single<UserDataSource> { UserDataSourceImpl(get(), get()) }
        }
//
    private val repository =
        module {
//            single<PrefRepository> { PrefRepositoryImpl(get()) }
//            single<AuthRepository> { AuthRepositoryImpl(get()) }
//            single<CartRepository> { CartRepositoryImpl(get()) }
//            single<CategoryRepository> { CategoryRepositoryImpl(get()) }
//            single<ChatRepository> { ChatRepositoryImpl(get()) }
//            single<MenuRepository> { MenuRepositoryImpl(get()) }
//            single<OrderRepository> { OrderRepositoryImpl(get()) }
//            single<UserRepository> { UserRepositoryImpl(get()) }
        }
//
    private val viewModelModule =
        module {
//            viewModelOf(::CartViewModel)
//            viewModelOf(::ChatViewModel)
//            viewModelOf(::CheckoutViewModel)
//            viewModelOf(::ConsultationOfficerViewModel)
//            viewModelOf(::ConsultationViewModel)
//            viewModelOf(::EditProfileViewModel)
//            viewModelOf(::HistoryViewModel)
//            viewModelOf(::HomeOfficerViewModel)
//            viewModelOf(::HomeViewModel)
//            viewModelOf(::LoginViewModel)
//            viewModelOf(::MainViewModel)
//            viewModelOf(::OrderFragmentViewModel)
//            viewModelOf(::OrderViewModel)
//            viewModelOf(::ProfileViewModel)
//            viewModelOf(::RegisterViewModel)
//            viewModelOf(::SettingsViewModel)
//            viewModelOf(::VerificationViewModel)
//            viewModel { params ->
//                DetailViewModel(
//                    extras = params.get(),
//                    cartRepository = get(),
//                )
//            }
        }
//
    val modules =
        listOf(
            networkModule,
            localModule,
            datasource,
            repository,
            viewModelModule,
        )
}
