package com.pmb.network

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.logging.Logger
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class NetworkManger(
    val context: Context

) {
    val client: HttpClient = HttpClient(Android) {
        defaultRequest {
            host = "172.20.140.242:8443/api/v1"
//            host = "172.20.140.167:8443/api/v1"
            url {
                protocol = URLProtocol.HTTPS
            }
            headers {
                append("Content-Type", "application/json")
                append("appVer", "0.0.1")
                append("Accept-Language", "fa;fa-IR")
                append("osType", "7")
            }
        }

        // temp
        engine {
            sslManager = { httpsURLConnection ->
                httpsURLConnection.hostnameVerifier = HostnameVerifier { _, _ -> true }
                httpsURLConnection.sslSocketFactory = SSLContext.getInstance("TLS")
                    .apply {
                        init(null, arrayOf(AllCertsTrustManager()), SecureRandom())
                    }.socketFactory
            }
        }

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
//            storage = SessionCookieStorage()
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("ktor", message)
                }
            }
            level = LogLevel.ALL
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
//                coerceInputValues = true need it.
                prettyPrint = true

            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 20_000
            connectTimeoutMillis = 20_000
            socketTimeoutMillis = 20_000
        }

//        installErrorHandler()
    }


/*    inline fun <reified Request, reified Response> request(
        endpoint: String,
        data: Request? = null,
        queryParams: Map<String, String> = emptyMap()
    ): Flow<Result<SuccessData<Response>>> {
        val request = MobileApiRequest(
            // this data is temporary
            metaData = RequestMetaData(
                initialVec = "",
                imei = context.getAndroidId(),
                osType = 7,
                osVersion = Build.VERSION.RELEASE.toIntOrNull() ?: 1,
                deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
            ),
            data = data,
        )

        return safeRequestCall {
            client.post {
                url {
                    path(endpoint)

                    queryParams.forEach { (key, value) ->
                        parameters.append(key, value)
                    }
                }
                setBody(request)
            }.body()
        }
    }*/
}

class AllCertsTrustManager : X509TrustManager {

    override fun checkClientTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?
    ) {
    }

    override fun checkServerTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?
    ) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
}