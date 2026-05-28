package com.ravia.app.data.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthTokenInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runCatching {
            firebaseAuth?.currentUser?.getIdToken(false)?.let {
                Tasks.await(it, 8, TimeUnit.SECONDS).token
            }
        }.getOrNull()

        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}

