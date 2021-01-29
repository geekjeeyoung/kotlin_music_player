package `fun`.chezcandy.timberxclone.ui.activities.base

import `fun`.chezcandy.timberxclone.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PermissionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)
    }
}