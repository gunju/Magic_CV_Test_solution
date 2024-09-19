// Copyright (c) 2024 Magic Tech Ltd

package fit.magic.cv.repcounter

import fit.magic.cv.PoseLandmarkerHelper

abstract class ExerciseRepCounter {

    private var listener: ExerciseEventListener? = null

    private var repCount = 0

    abstract fun setResults(resultBundle: PoseLandmarkerHelper.ResultBundle)

    fun setListener(listener: ExerciseEventListener?) {
        this.listener = listener
    }

    fun incrementRepCount() {
        repCount++ // Increment the repcount variable
        listener?.repCountUpdated(repCount) //send the updated value to UI
    }

    fun resetRepCount() {
        repCount = 0 //After reset, set repCount value to 0
        listener?.repCountUpdated(repCount)
    }

    fun sendProgressUpdate(progress: Float) {
        listener?.progressUpdated(progress) //progress bar updation
    }

    fun sendFeedbackMessage(message: String) {
        listener?.showFeedback(message) //display message on app
    }
}

interface ExerciseEventListener {
    fun repCountUpdated(count: Int)

    fun progressUpdated(progress: Float)

    fun showFeedback(feedback: String)
}