package com.edgehome.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

/**
 * Hosts the real Edge Home dashboard served by the hub's gateway.
 * Nothing is bundled: the page comes from the configured gateway URL.
 */
class MainActivity : ComponentActivity() {

    private lateinit var web: WebView
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var errorView: View
    private lateinit var errorTitle: TextView
    private lateinit var errorBody: TextView
    private lateinit var errorHost: TextView

    private lateinit var gatewayUrl: String
    private lateinit var gatewayHost: String
    private var mainFrameFailed = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val saved = Gateway.saved(this)
        if (saved == null) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }
        gatewayUrl = saved
        gatewayHost = Uri.parse(saved).host.orEmpty()

        setContentView(R.layout.activity_main)
        web = findViewById(R.id.web)
        swipe = findViewById(R.id.swipe)
        errorView = findViewById(R.id.error)
        errorTitle = findViewById(R.id.error_title)
        errorBody = findViewById(R.id.error_body)
        errorHost = findViewById(R.id.error_host)

        findViewById<Button>(R.id.error_retry).setOnClickListener { retry() }
        findViewById<Button>(R.id.error_change).setOnClickListener { openSetup() }

        swipe.setColorSchemeColors(getColor(R.color.accent))
        swipe.setProgressBackgroundColorSchemeColor(getColor(R.color.surface))
        // Only pull to refresh when the page is scrolled to the very top
        swipe.setOnChildScrollUpCallback { _, _ -> web.scrollY > 0 }
        swipe.setOnRefreshListener {
            if (errorView.visibility == View.VISIBLE) retry() else web.reload()
        }

        configureWebView()

        if (savedInstanceState == null || web.restoreState(savedInstanceState) == null) {
            load()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (errorView.visibility != View.VISIBLE && web.canGoBack()) {
                    web.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        web.setBackgroundColor(getColor(R.color.window_bg))

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // No file, content or location access from the page
            allowFileAccess = false
            allowContentAccess = false
            setGeolocationEnabled(false)
            mediaPlaybackRequiresUserGesture = true
            setSupportMultipleWindows(false)
            userAgentString = "$userAgentString EdgeHomeAndroid/${BuildConfig.VERSION_NAME}"
        }

        // Let the dashboard's own dark/light CSS decide; never auto-darken it
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(web.settings, false)
        }

        // Keep the dashboard's sign-in across launches
        CookieManager.getInstance().setAcceptCookie(true)

        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                mainFrameFailed = false
            }

            override fun onPageFinished(view: WebView, url: String?) {
                swipe.isRefreshing = false
                if (!mainFrameFailed) errorView.visibility = View.GONE
                CookieManager.getInstance().flush()
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) showError(Problem.UNREACHABLE)
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse
            ) {
                if (request.isForMainFrame && errorResponse.statusCode >= 500) {
                    showError(Problem.HUB_ERROR)
                }
            }

            // Never accept a bad certificate
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                handler.cancel()
                showError(Problem.CERTIFICATE)
            }

            // The gateway's pages stay in the app; any other site opens in the browser
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url
                if (url.scheme == "https" && url.host.equals(gatewayHost, ignoreCase = true)) return false
                openExternally(url)
                return true
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            // Camera, microphone and other device permissions: always refused
            override fun onPermissionRequest(request: PermissionRequest) {
                request.deny()
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback
            ) {
                callback.invoke(origin, false, false)
            }

            // File and photo pickers: always cancelled
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                filePathCallback.onReceiveValue(null)
                return true
            }
        }
    }

    private fun load() {
        mainFrameFailed = false
        swipe.isRefreshing = true
        web.loadUrl(gatewayUrl)
    }

    private fun retry() {
        errorView.visibility = View.GONE
        val current = web.url
        if (current.isNullOrEmpty() || current == "about:blank") load() else {
            mainFrameFailed = false
            swipe.isRefreshing = true
            web.reload()
        }
    }

    private enum class Problem { UNREACHABLE, HUB_ERROR, CERTIFICATE }

    private fun showError(problem: Problem) {
        mainFrameFailed = true
        swipe.isRefreshing = false
        when (problem) {
            Problem.UNREACHABLE -> {
                errorTitle.setText(R.string.error_unreachable_title)
                errorBody.setText(R.string.error_unreachable_body)
            }
            Problem.HUB_ERROR -> {
                errorTitle.setText(R.string.error_hub_title)
                errorBody.setText(R.string.error_hub_body)
            }
            Problem.CERTIFICATE -> {
                errorTitle.setText(R.string.error_cert_title)
                errorBody.setText(R.string.error_cert_body)
            }
        }
        errorHost.text = gatewayHost
        errorView.visibility = View.VISIBLE
    }

    private fun openSetup() {
        startActivity(Intent(this, SetupActivity::class.java).putExtra(SetupActivity.EXTRA_EDIT, true))
    }

    private fun openExternally(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) {
            // Nothing can open it; stay on the dashboard
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::web.isInitialized) web.saveState(outState)
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }

    override fun onDestroy() {
        if (::web.isInitialized) web.destroy()
        super.onDestroy()
    }
}
