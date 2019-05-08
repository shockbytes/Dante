package at.shockbytes.dante.network

import android.content.Context

import java.io.IOException
import java.io.InputStream
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

import at.shockbytes.dante.R
import okhttp3.OkHttpClient

// TODO Throw this away later, should not be used when moving to Heroku
object SelfSigningClientBuilder {

    fun createClient(context: Context): OkHttpClient? {

        var client: OkHttpClient? = null

        var cf: CertificateFactory? = null
        var cert: InputStream? = null
        var ca: Certificate? = null
        var sslContext: SSLContext? = null
        try {
            cf = CertificateFactory.getInstance("X.509")
            cert = context.resources.openRawResource(R.raw.shockbytes_certificate) // Place your 'my_cert.crt' file in `res/raw`

            ca = cf!!.generateCertificate(cert)
            cert!!.close()

            val keyStoreType = KeyStore.getDefaultType()
            val keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", ca)

            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)

            sslContext = SSLContext.getInstance("TLS")
            sslContext!!.init(null, tmf.trustManagers, null)

            client = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory)
                .hostnameVerifier { hostname, session -> true }
                .build()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }

        return client
    }
}