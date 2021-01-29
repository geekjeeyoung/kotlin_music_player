package `fun`.chezcandy.timberxclone.permissions

import `fun`.chezcandy.timberxclone.extensions.asString
import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.Single.just
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber


data class GrantResult(
    val permission: String,
    val granted: Boolean
)

interface PermissionsManager {
    fun onGrantResult(): Observable<GrantResult>
    fun attach(activity: Activity)
    fun hasStoragePermission(): Boolean
    fun requestStoragePermission(waitForGranted: Boolean = false): Single<GrantResult>
    fun processResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    fun detach(activity: Activity)
}

class RealPermissionsManager(
    private val context: Application,
    private val mainScheduler: Scheduler
) : PermissionsManager {
    companion object {
        @VisibleForTesting(otherwise = PRIVATE)
        const val REQUEST_CODE_STORAGE = 69
    }

    @VisibleForTesting(otherwise = PRIVATE)
    var activity: Activity? = null
    private val relay = PublishSubject.create<GrantResult>()

    override fun onGrantResult(): Observable<GrantResult> = relay.share().observeOn(mainScheduler)

    override fun attach(activity: Activity) {
        Timber.d("attach(): $activity")
        this.activity = activity
    }

    override fun hasStoragePermission() = hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
    }

    override fun requestStoragePermission(waitForGranted: Boolean) =
        requestPermission(
            REQUEST_CODE_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            waitForGranted
        )


    private fun requestPermission(
        code: Int,
        permission: String,
        waitForGranted: Boolean
    ): Single<GrantResult> {
        Timber.d("Requesting permission: %s", permission)
        if (hasPermission(permission)) {
            Timber.d("Already have this permission!")
            return just(GrantResult(permission, true).also {
                relay.onNext(it)
            })
        }

        val attachedTo = activity ?: throw IllegalStateException("Not attached")
        ActivityCompat.requestPermissions(attachedTo, arrayOf(permission), code)

        return onGrantResult()
            .filter { it.permission == permission }
            .filter {
                if (waitForGranted) {
                    it.granted
                } else {
                    true
                }
            }
            .take(1)
            .singleOrError()
    }

    override fun processResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.d(
            "ProcessResult(): requestCode= %d, permissions: %s, grantResults: %s",
            requestCode,
            permissions.asString(),
            grantResults.asString()
        )
        for ((index, permission) in permissions.withIndex()) {
            val granted = grantResults[index] == PERMISSION_GRANTED
            val result = GrantResult(permission, granted)
            Timber.d("Permission grant result: %s", result)
            relay.onNext(result)
        }

    }

    override fun detach(activity: Activity) {
        if (this.activity === activity) {
            Timber.d("detach(): $activity")
            this.activity = null
        }
    }
}