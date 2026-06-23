package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“客户端证书请求模块”。
 * WebView 访问需要客户端证书的网站时，会把 ClientCertRequest 交给这里处理。
 * 主要职责：打开 Android 系统证书选择器、读取用户选中的私钥和证书链，然后把结果返回给 WebView。
 * 阅读顺序：先看 handleRequest，再看 handleAliasSelected，最后看 cancelPending。
 */
import android.security.KeyChain
import android.webkit.ClientCertRequest
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.utils.ShortToast
import java.security.PrivateKey
import java.security.cert.X509Certificate

/**
 * WebView 客户端证书请求控制器。
 *
 * MainActivity 只负责把 BrowserClient 的客户端证书回调委托给本类；本类负责 request 生命周期、
 * 系统证书选择器、后台证书读取以及失败提示。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来启动 KeyChain 选择器、切回 UI 线程和显示 Toast。
 */
class ClientCertificateController(
    private val activity: AppCompatActivity
) {
    private var pendingRequest: ClientCertRequest? = null

    /**
     * 函数 `handleRequest`：处理 WebView 发来的客户端证书请求。
     *
     * 初学者阅读提示：新的请求会先取消旧请求，保证同一时间只有一个 ClientCertRequest 等待用户选择。
     *
     * @param request 参数类型为 `ClientCertRequest?`，表示 WebView 等待证书结果的回调；为空时直接忽略。
     */
    fun handleRequest(request: ClientCertRequest?) {
        val certRequest = request ?: return
        cancelPending()
        pendingRequest = certRequest
        KeyChain.choosePrivateKeyAlias(
            activity,
            { alias -> handleAliasSelected(certRequest, alias) },
            certRequest.keyTypes,
            certRequest.principals,
            certRequest.host,
            certRequest.port,
            null
        )
    }

    /**
     * 函数 `cancelPending`：取消当前还没有完成的客户端证书请求。
     *
     * 初学者阅读提示：Activity 销毁或新请求到来时调用，避免 WebView 一直等待证书选择结果。
     */
    fun cancelPending() {
        pendingRequest?.cancel()
        pendingRequest = null
    }

    /**
     * 函数 `handleAliasSelected`：处理 Android 系统证书选择器返回的别名。
     *
     * 初学者阅读提示：KeyChain.getPrivateKey 和 getCertificateChain 可能阻塞，所以放到后台线程执行。
     *
     * @param request 参数类型为 `ClientCertRequest`，表示等待证书结果的 WebView 请求。
     * @param alias 参数类型为 `String?`，表示用户在系统证书选择器里选中的证书别名；为空表示用户取消。
     */
    private fun handleAliasSelected(
        request: ClientCertRequest,
        alias: String?
    ) {
        if (pendingRequest != request) {
            return
        }
        if (alias.isNullOrBlank()) {
            pendingRequest = null
            request.cancel()
            return
        }

        val appContext = activity.applicationContext
        Thread(
            {
                val credential = runCatching {
                    val privateKey = KeyChain.getPrivateKey(appContext, alias)
                        ?: error("Client certificate private key is unavailable.")
                    val certificateChain = KeyChain.getCertificateChain(appContext, alias)
                        ?: emptyArray()
                    ClientCertificateCredential(privateKey, certificateChain)
                }.getOrElse { error ->
                    if (error is InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                    null
                }

                activity.runOnUiThread {
                    if (pendingRequest != request) {
                        return@runOnUiThread
                    }
                    pendingRequest = null
                    if (credential != null && credential.certificateChain.isNotEmpty()) {
                        request.proceed(credential.privateKey, credential.certificateChain)
                    } else {
                        request.cancel()
                        ShortToast.show(activity, R.string.toast_client_certificate_unavailable)
                    }
                }
            },
            "VideoBrowserClientCert"
        ).start()
    }

    /**
     * Android 客户端证书选择器返回的私钥和证书链。
     *
     * @param privateKey 参数类型为 `PrivateKey`，表示 WebView TLS 握手要使用的客户端私钥。
     * @param certificateChain 参数类型为 `Array<X509Certificate>`，表示和私钥匹配的证书链。
     */
    private data class ClientCertificateCredential(
        val privateKey: PrivateKey,
        val certificateChain: Array<X509Certificate>
    )
}
