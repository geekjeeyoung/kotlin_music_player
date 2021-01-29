package `fun`.chezcandy.timberxclone.permissions

import android.app.Activity
import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*

data class GrantResult(
        val permission: String,
        val granted: Boolean
)

interface PermissionsManager {
    fun onGrantResult(): Observable<GrantResult>
    fun attach(activity: Activity)
    fun hasStoragePermission(): Boolean
    fun requestStoragePermission(waitForGranted: Boolean = false): Single<GrantResult>
    fun processResult(requestCode: Int, permissions: Array<out String>, grantResult: IntArray)
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
        
    }
}