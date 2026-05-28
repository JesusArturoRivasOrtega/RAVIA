package com.ravia.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.ravia.app.data.remote.*
import com.ravia.app.data.repository.*
import com.ravia.app.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth?,
        firebaseMessaging: FirebaseMessaging?,
        usersApi: UsersApi,
        dataStore: DataStore<Preferences>
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, firebaseMessaging, usersApi, dataStore)

    @Provides
    @Singleton
    fun provideReportsRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
        reportsApi: ReportsApi
    ): ReportsRepository = ReportsRepositoryImpl(context, firebaseAuth, firestore, reportsApi)

    @Provides
    @Singleton
    fun provideAlertsRepository(
        firebaseAuth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
        alertsApi: AlertsApi
    ): AlertsRepository = AlertsRepositoryImpl(firebaseAuth, firestore, alertsApi)

    @Provides
    @Singleton
    fun provideRiskZonesRepository(
        firestore: FirebaseFirestore?,
        riskZonesApi: RiskZonesApi
    ): RiskZonesRepository = RiskZonesRepositoryImpl(firestore, riskZonesApi)

    @Provides
    @Singleton
    fun provideMissingPersonsRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
        missingPersonsApi: MissingPersonsApi
    ): MissingPersonsRepository = MissingPersonsRepositoryImpl(context, firebaseAuth, firestore, missingPersonsApi)

    @Provides
    @Singleton
    fun provideChatbotRepository(
        firebaseAuth: FirebaseAuth?,
        firestore: FirebaseFirestore?,
        chatbotApi: ChatbotApi
    ): ChatbotRepository = ChatbotRepositoryImpl(firebaseAuth, firestore, chatbotApi)

    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context,
        locationClient: FusedLocationProviderClient
    ): LocationRepository = LocationRepositoryImpl(context, locationClient)
}
