package com.iproov.example

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.json.jsonDeserializer
import com.iproov.androidapiclient.AssuranceType
import com.iproov.androidapiclient.ClaimType
import com.iproov.androidapiclient.kotlinfuel.ApiClientFuel
import com.iproov.example.databinding.ActivityMainBinding
import com.iproov.sdk.api.IProov
import com.iproov.sdk.api.exception.SessionCannotBeStartedTwiceException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var binding: ActivityMainBinding
    private var sessionStateJob: Job? = null
    private var retryCount = 0
    private val maxRetries = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Constants.API_KEY.isEmpty() || Constants.SECRET.isEmpty()) {
            throw IllegalStateException("You must set the API_KEY and SECRET values in the Constants.kt file!")
        }

        // Removed reference to startOnboardingButton since it doesn't exist
        binding.usernameEditText.visibility = View.VISIBLE
        binding.startVerificationButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            if (username.isNotEmpty()) {
                if (retryCount < maxRetries) {
                    launchIProov(ClaimType.ENROL, AssuranceType.GENUINE_PRESENCE, username)
                } else {
                    Toast.makeText(this, "Maximum retry attempts reached.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Please enter a valid user ID.", Toast.LENGTH_LONG).show()
            }
        }

        binding.versionTextView.text = getString(R.string.kotlin_version_format, IProov.sdkVersion)
        IProov.session?.let { session -> observeSessionState(session) }
    }

    private fun observeSessionState(session: IProov.Session, whenReady: (() -> Unit)? = null) {
        sessionStateJob?.cancel()
        sessionStateJob = lifecycleScope.launch(Dispatchers.IO) {
            session.state
                .onSubscription { whenReady?.invoke() }
                .collect { state ->
                    if (sessionStateJob?.isActive == true) {
                        withContext(Dispatchers.Main) {
                            when (state) {
                                is IProov.State.Success -> {
                                    onResult(getString(R.string.success), "")
                                    retryCount = 0
                                }
                                is IProov.State.Failure -> {
                                    retryCount++
                                    onResult(state.failureResult.reason.feedbackCode, getString(state.failureResult.reason.description))
                                }
                                is IProov.State.Error -> {
                                    retryCount++
                                    onResult(getString(R.string.error), state.exception.localizedMessage)
                                }
                                is IProov.State.Canceled -> onResult(getString(R.string.canceled), null)
                                else -> {}
                            }
                        }
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun launchIProov(claimType: ClaimType, assuranceType: AssuranceType, username: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.isIndeterminate = true

        val apiClientFuel = ApiClientFuel(this, Constants.FUEL_URL, Constants.API_KEY, Constants.SECRET)

        uiScope.launch(Dispatchers.IO) {
            try {
                val token = apiClientFuel.getToken(assuranceType, claimType, username)
                if (!job.isActive) return@launch
                startScan(token)
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    ex.printStackTrace()
                    val errorMsg = if (ex is FuelError) {
                        val json = jsonDeserializer().deserialize(ex.response)
                        json.obj().getString("error_description")
                    } else {
                        getString(R.string.failed_to_get_token)
                    }
                    onResult(getString(R.string.error), errorMsg)
                }
            }
        }
    }

    @Throws(SessionCannotBeStartedTwiceException::class)
    private fun startScan(token: String) {
        IProov.createSession(applicationContext, Constants.BASE_URL, token).let { session ->
            observeSessionState(session) { session.start() }
        }
    }

    private fun onResult(title: String?, resultMessage: String?) {
        binding.progressBar.progress = 0
        binding.progressBar.visibility = View.GONE

        AlertDialog.Builder(this@MainActivity)
            .setTitle(title)
            .setMessage(resultMessage)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.cancel() }
            .show()
    }
}
