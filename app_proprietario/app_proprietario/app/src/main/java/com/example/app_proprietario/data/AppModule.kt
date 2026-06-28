package com.example.app_proprietario.data

import com.example.app_proprietario.data.remote.UbidotsNetworkModule
import com.example.app_proprietario.data.repository.RoomRepositoryImpl
import com.example.app_proprietario.domain.RoomRepository
import com.example.app_proprietario.domain.usecase.GetAllPropertiesUseCase
import com.example.app_proprietario.domain.usecase.GetIntrusionHistoryUseCase
import com.example.app_proprietario.domain.usecase.GetPropertyDetailsUseCase
import com.example.app_proprietario.domain.usecase.GetRoomDetailsUseCase
import com.example.app_proprietario.ui.screens.viewmodel.IntrusionHistoryViewModel
import com.example.app_proprietario.ui.viewmodel.PropertyDetailsViewModel
import com.example.app_proprietario.ui.viewmodel.PropertyListViewModel
import com.example.app_proprietario.ui.viewmodel.RoomDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { UbidotsNetworkModule.provideOkHttpClient() }
    single { UbidotsNetworkModule.provideRetrofit(get()) }
    single { UbidotsNetworkModule.provideUbidotsApi(get()) }

    single<RoomRepository> { RoomRepositoryImpl(api = get()) }

    factory { GetAllPropertiesUseCase(get()) }
    factory { GetPropertyDetailsUseCase(get()) }
    factory { GetRoomDetailsUseCase(get()) }
    factory { GetIntrusionHistoryUseCase(get()) }

    viewModel { PropertyListViewModel(getAllProperties = get()) }

    viewModel { (propertyId: String) ->
        PropertyDetailsViewModel(getPropertyDetails = get(), propertyId = propertyId)
    }

    viewModel { (propertyId: String, propertyName: String, roomId: String) ->
        RoomDetailsViewModel(
            getRoomDetails = get(),
            propertyId = propertyId,
            propertyName = propertyName,
            roomId = roomId
        )
    }

    viewModel { (propertyId: String) ->
        IntrusionHistoryViewModel(getIntrusionHistory = get(), propertyId = propertyId)
    }
}
