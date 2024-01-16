package com.example.basiccustomview

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.res.use
import androidx.core.graphics.toColorInt

val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Float.dp: Float get() = (this * Resources.getSystem().displayMetrics.density)


class MyCustomView @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    companion object{
        const val DEFAULT_STEP_SUCCESS_COLOR = "#67D39D"
        const val DEFAULT_STEP_FAILED_COLOR = "#EB6A6E"
        const val DEFAULT_STEP_PENDING_COLOR = "#3E3953"
        const val DEFAULT_TOTAL_STEPS = 12
        const val DEFAULT_SUCCESS_STEPS = 2
        const val DEFAULT_FAILED_STEPS = 2
        const val DEFAULT_GAP_BETWEEN_STEPS = 0.8f
        const val DEFAULT_STEP_THICKNESS = 10f
    }

    private var stepStrokeType = Paint.Cap.BUTT
    private var stepThickness = DEFAULT_STEP_THICKNESS.dp

    private var stepSuccessColor = DEFAULT_STEP_SUCCESS_COLOR.toColorInt()
    private var stepFailureColor = DEFAULT_STEP_FAILED_COLOR.toColorInt()
    private var stepPendingColor = DEFAULT_STEP_PENDING_COLOR.toColorInt()

    private var totalSteps = DEFAULT_TOTAL_STEPS
    private var successfulSteps = DEFAULT_SUCCESS_STEPS
    private var failedSteps = DEFAULT_FAILED_STEPS
    private var pendingSteps = calculatePendingSteps()

    private var gapBetweenEachStep = DEFAULT_GAP_BETWEEN_STEPS.dp
    private var widthOfEachStep = calculateWidthOfEachStep()

    private val rectF = RectF()

    private val stepSuccessfulPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = stepSuccessColor
        strokeWidth = stepThickness
        strokeCap = stepStrokeType
    }

    private val stepFailedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = stepFailureColor
        strokeWidth = stepThickness
        strokeCap = stepStrokeType
    }

    private val stepPendingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = stepPendingColor
        strokeWidth = stepThickness
        strokeCap = stepStrokeType
    }

    init {
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.MyCustomView,
            defStyleAttr,
            0
        ).use {
            extractValues(it)
            setValues()
        }
    }

    private fun extractValues(typedArray: TypedArray) {
        typedArray.apply {
            stepSuccessColor =
                getColor(
                    R.styleable.MyCustomView_success_step_color,
                    DEFAULT_STEP_SUCCESS_COLOR.toColorInt()
                )

            stepFailureColor =
                getColor(
                    R.styleable.MyCustomView_failure_step_color,
                    DEFAULT_STEP_FAILED_COLOR.toColorInt()
                )

            stepPendingColor = getColor(
                R.styleable.MyCustomView_pending_step_color,
                DEFAULT_STEP_PENDING_COLOR.toColorInt()
            )

            gapBetweenEachStep = getDimension(
                R.styleable.MyCustomView_gap_between_steps,
                DEFAULT_GAP_BETWEEN_STEPS.dp
            )

            stepThickness = getDimension(
                R.styleable.MyCustomView_step_thickness,
                DEFAULT_STEP_THICKNESS.dp
            )

        }
    }

    private fun setValues() {
        stepSuccessfulPaint.apply {
            color = stepSuccessColor
            strokeWidth = stepThickness
        }
        stepFailedPaint.apply {
            color = stepFailureColor
            strokeWidth = stepThickness
        }
        stepPendingPaint.apply {
            color = stepPendingColor
            strokeWidth = stepThickness
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = ((width - stepThickness) / 2f) - 10f

        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        var start = 270f

        for (i in 0..<totalSteps) {

            val paint = when (getStatus(successfulSteps, failedSteps, i)) {
                State.SUCCESS -> stepSuccessfulPaint
                State.FAILED -> stepFailedPaint
                State.PENDING -> stepPendingPaint
            }

            canvas.drawArc(rectF, start, widthOfEachStep, false, paint)

            start += widthOfEachStep + gapBetweenEachStep
        }
    }

    var x: ValueAnimator? = null

    fun setData(totalSteps: Int, successfulSteps: Int, failedSteps:Int) {
        this.totalSteps = totalSteps
        this.successfulSteps = successfulSteps
        this.failedSteps = failedSteps
        this.pendingSteps = calculatePendingSteps()
        this.widthOfEachStep = calculateWidthOfEachStep()

        x?.cancel()
        x = ValueAnimator.ofFloat(1f, totalSteps.toFloat()).apply {
            duration = 300
            start()
            this.addUpdateListener {

                val item = (it.animatedValue as Float).toInt()
                this@MyCustomView.totalSteps = item
                invalidate()
            }
        }
        postInvalidate()
    }

    private fun calculatePendingSteps() = totalSteps - successfulSteps - failedSteps
    private fun calculateWidthOfEachStep() = (360f / totalSteps) - gapBetweenEachStep

    /* override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec)

         val desiredWidth = 100
         val desiredHeight = 100

         val widthMode = MeasureSpec.getMode(widthMeasureSpec)
         val heightMode = MeasureSpec.getMode(heightMeasureSpec)

         val widthSize = MeasureSpec.getSize(widthMeasureSpec)
         val heightSize = MeasureSpec.getSize(heightMeasureSpec)

         var width: Int = -1
         var height: Int = -1

         //Measure Width
         width = if (widthMode == MeasureSpec.EXACTLY) {
             //Must be this size
             widthSize
         } else if (widthMode == MeasureSpec.AT_MOST) {
             //Can't be bigger than...
             Math.min(desiredWidth, widthSize)
         } else {
             //Be whatever you want
             desiredWidth
         }


         //Measure Height
         height = if (heightMode == MeasureSpec.EXACTLY) {
             //Must be this size
             heightSize
         } else if (heightMode == MeasureSpec.AT_MOST) {
             //Can't be bigger than...
             Math.min(desiredHeight, heightSize)
         } else {
             //Be whatever you want
             desiredHeight
         }

         //MUST CALL THIS
         setMeasuredDimension(width, height)

     }*/

    enum class State {
        SUCCESS, FAILED, PENDING
    }

    private fun getStatus(success: Int, failed: Int, index: Int): State {
        return if (index < success) {
            State.SUCCESS
        } else if (index < success + failed) {
            State.FAILED
        } else State.PENDING
    }

}

