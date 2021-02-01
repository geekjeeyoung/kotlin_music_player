@file:Suppress("MemberVisibilityCanBePrivate")

package `fun`.chezcandy.timberxclone.ui.activities.base

import `fun`.chezcandy.timberxclone.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

abstract class PermissionsActivity : AppCompatActivity() {

    protected val permissionsManager by inject
}