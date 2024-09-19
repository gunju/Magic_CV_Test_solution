package fit.magic.cv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import fit.magic.cv.databinding.ActivityMainBinding
import fit.magic.cv.repcounter.ExerciseRepCounterImpl

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels() // ViewModel initialization
    private lateinit var exerciseRepCounter: ExerciseRepCounterImpl // Rep counter initialization

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        exerciseRepCounter = ExerciseRepCounterImpl()

        observeProgressUpdates()

    }

    private fun observeProgressUpdates() {
        viewModel.progressLiveData.observe(this) { progress ->
            activityMainBinding.progressBar.progress = (progress * 100).toInt()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Finish the activity when back is pressed
    }
}
