package fit.magic.cv.repcounter

import fit.magic.cv.PoseLandmarkerHelper
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.PI

class ExerciseRepCounterImpl : ExerciseRepCounter() {

    private var lungeInProgress = false
    private var previousLeg: String? = null // to check if lunges performed are alternating or not
    private val progressThreshold = 0.5f // threshold for check if lunge is correctly performed or not
    private val lungeThreshold = 70.0 // threshold for checking the knee angle, ideally it should be 90
    private val maxAngle = 180.0 // angle for knee and ankle to check if person is standing straight
    private val smoothingFactor = 0.1 // Smoothing factor for angle changes
    private val zThreshold = 0.017 // checking changes for front facing lunges

    override fun setResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        val poseLandmarkerResults = resultBundle.results.firstOrNull() ?: return

        if (poseLandmarkerResults.landmarks().isEmpty()) return

        // Get x,y coordinate of hip, knee and ankle
        val leftHip = getLandmarkCoordinates(poseLandmarkerResults, 23)
        val rightHip = getLandmarkCoordinates(poseLandmarkerResults, 24)
        val leftKnee = getLandmarkCoordinates(poseLandmarkerResults, 25)
        val rightKnee = getLandmarkCoordinates(poseLandmarkerResults, 26)
        val leftAnkle = getLandmarkCoordinates(poseLandmarkerResults, 27)
        val rightAnkle = getLandmarkCoordinates(poseLandmarkerResults, 28)

        // For front lunges, z dimension is required
        val leftKneeZ = poseLandmarkerResults.landmarks().first().get(25).z()
        val rightKneeZ = poseLandmarkerResults.landmarks().first().get(26).z()

        val leftKneeAngle = smoothAngle(calculateAngle(leftHip, leftKnee, leftAnkle))
        val rightKneeAngle = smoothAngle(calculateAngle(rightHip, rightKnee, rightAnkle))

        // to check if which leg is moving forward
        val leftProgress = calculateProgress(leftKneeAngle)
        val rightProgress = calculateProgress(rightKneeAngle)

        // check if both leg coordinates are getting shifted or not
        val rawProgress = (leftProgress + rightProgress) / 2

        // Updation of progress bar based condition of threshold
        val scaledProgress = if (rawProgress > progressThreshold) 1.0f else rawProgress / progressThreshold

        // Update the progress bar
        sendProgressUpdate(scaledProgress)

        // conditions for left lunge or right lunge
        if (rawProgress > progressThreshold && !lungeInProgress) {
            val leadLeg = if (abs(leftKneeZ - rightKneeZ) > zThreshold)
            {
                if (leftKneeZ < rightKneeZ) "left" else "right"
            }
            else {
                previousLeg
            }

            // display which type of lunge is user performing
            sendFeedbackMessage("LeadLeg: $leadLeg, Progress: $rawProgress")

            if (leadLeg == "left" && previousLeg != "left")
            {
                lungeInProgress = true
                previousLeg = "left"
                incrementRepCount()
                sendFeedbackMessage("Lunge counted for left leg.")
            }


            if (leadLeg == "right" && previousLeg != "right")
            {
                lungeInProgress = true
                previousLeg = "right"
                incrementRepCount()
                sendFeedbackMessage("Lunge counted for right leg.")
            }
        }


        if (rawProgress < 0.45f) {
            lungeInProgress = false
            sendFeedbackMessage("Lunge detection reset.")//When user stands the lunge position is reset
        }
    }

    // Using trignometry for calculating angles between hip,knee and ankle
    private fun calculateAngle(a: Pair<Float, Float>, b: Pair<Float, Float>, c: Pair<Float, Float>): Double
    {
        val radians = atan2(c.second - b.second, c.first - b.first) - atan2(a.second - b.second, a.first - b.first)
        var angle = Math.abs(radians * 180.0 / PI)
        if (angle > 180.0) {
            angle = 360.0 - angle
        }
        return angle
    }

    // Function for extracting x,y coordinate
    private fun getLandmarkCoordinates(result: PoseLandmarkerResult, index: Int): Pair<Float, Float>
    {
        val landmark = result.landmarks().first().get(index)
        return Pair(landmark.x(), landmark.y())
    }

    // Function to calculate value for progress bar based on angle and converting it into 0,1
    private fun calculateProgress(kneeAngle: Double): Float
    {
        return ((maxAngle - kneeAngle) / (maxAngle - lungeThreshold)).toFloat().coerceIn(0.0f, 1.0f)
    }

    // Smoothing the angle to prevent sudden changes from causing incorrect counting
    private fun smoothAngle(currentAngle: Double): Double
    {
        return (1 - smoothingFactor) * currentAngle + smoothingFactor * previousAngle(currentAngle)
    }

    // function to store current angle value such that in it can be used for function smoothAngle
    private fun previousAngle(currentAngle: Double): Double
    {
        return currentAngle
    }
}
