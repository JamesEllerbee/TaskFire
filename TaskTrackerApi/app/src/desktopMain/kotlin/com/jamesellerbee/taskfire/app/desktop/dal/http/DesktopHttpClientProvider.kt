package com.jamesellerbee.taskfire.app.desktop.dal.http

import com.jamesellerbee.taskfire.app.interfaces.AppPropertiesProvider
import com.jamesellerbee.taskfire.app.interfaces.HttpClientProvider
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class DesktopHttpClientProvider(serviceLocator: ServiceLocator) : HttpClientProvider {
    private val appPropertiesProvider by serviceLocator.resolveLazy<AppPropertiesProvider>(
        ResolutionStrategy.ByType(
            type = AppPropertiesProvider::class
        )
    )

    override val httpClient: HttpClient by lazy {
        HttpClient(Java) {
            install(HttpCookies)
            install(ContentNegotiation) {
                json()
            }

            if (appPropertiesProvider.get(AppPropertiesProvider.ALLOW_SELF_SIGNED_CERT, true)) {
                engine {
                    config {
                        sslContext(getSslContext())
                    }
                }
            }
        }
    }

    private fun getKeyStore(): KeyStore {
        val keyStoreFile = FileInputStream("/home/joey/dev/git/Taskfire/TaskTrackerApi/keystore.jks")
        val keyStorePassword = "secret".toCharArray()
        val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(keyStoreFile, keyStorePassword)
        return keyStore
    }

    private fun getTrustManagerFactory(): TrustManagerFactory? {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(getKeyStore())
        return trustManagerFactory
    }

    private fun getSslContext(): SSLContext? {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, getTrustManagerFactory()?.trustManagers, null)
        return sslContext
    }

    private fun getTrustManager(): X509TrustManager {
        return getTrustManagerFactory()?.trustManagers?.first { it is X509TrustManager } as X509TrustManager
    }
}