package com.example.basiccustomview

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

    private var stepStrokeType = Paint.Cap.BUTT
    private var stepThickness = 10f.dp

    private var stepSuccessColor = Color.parseColor("#67D39D")
    private var stepFailureColor = Color.parseColor("#EB6A6E")
    private var stepPendingColor = Color.parseColor("#3E3953")

    private var totalSteps = 5
    private var successfullSteps = 2
    private var failedSteps = 1
    private var pendingSteps = calculatePendingSteps()

    private var gapBetweenEachEMI = 1.dp
    private var widthOfEachStep = (360f / totalSteps) - gapBetweenEachEMI

    private val rectF = RectF()

    val stepSuccessfullPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = stepSuccessColor
        strokeWidth = stepThickness
        strokeCap = stepStrokeType
    }

    val stepFailedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = stepFailureColor
        strokeWidth = stepThickness
        strokeCap = stepStrokeType
    }

    val stepPendingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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
                    Color.parseColor("#67D39D")
                )

            stepFailureColor =
                getColor(
                    R.styleable.MyCustomView_failure_step_color,
                    Color.parseColor("#EB6A6E")
                )

            stepPendingColor = getColor(
                R.styleable.MyCustomView_pending_step_color,
                Color.parseColor("#3E3953")
            )

            gapBetweenEachEMI = getDimension(
                R.styleable.MyCustomView_gap_between_steps,
                1f.dp
            ).toInt()

            stepThickness = getDimension(
                R.styleable.MyCustomView_step_thickness,
                10f.dp
            )

        }
    }

    private fun setValues() {
        stepSuccessfullPaint.apply {
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
        Log.e("MyCoustomView","can was drawn $totalSteps")
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width - stepThickness) / 2f - 10f

        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        var start = 270f

        for (i in 0..<totalSteps) {

            val paint = when (getStatus(successfullSteps, failedSteps, i)) {
                State.SUCCESS -> stepSuccessfullPaint
                State.FAILED -> stepFailedPaint
                State.PENDING -> stepPendingPaint
            }

            canvas.drawArc(rectF, start, widthOfEachStep, false, paint)
            start = start + widthOfEachStep + gapBetweenEachEMI
        }
    }

    fun setdata(totalSteps: Int, successfullSteps: Int, failedSteps:Int) {
        this.totalSteps = totalSteps
        this.successfullSteps = successfullSteps
        this.failedSteps = failedSteps
        this.pendingSteps = calculatePendingSteps()
        this.widthOfEachStep = calculateWidthOfEachStep()
        postInvalidate()
    }

    private fun calculatePendingSteps() = totalSteps - successfullSteps - failedSteps
    private fun calculateWidthOfEachStep() = (360f / totalSteps) - gapBetweenEachEMI

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

