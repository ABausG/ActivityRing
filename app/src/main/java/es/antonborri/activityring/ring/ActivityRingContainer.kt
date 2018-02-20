package es.antonborri.activityring.ring

import android.content.Context
import android.content.res.TypedArray
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.util.AttributeSet
import android.util.Log
import es.antonborri.activityring.R

/**
 * Created by antonborries on 20.02.18.
 */
class ActivityRingContainer(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    var activityRings: MutableList<ActivityRing> = ArrayList<ActivityRing>()
        set(value) {
            field = value
            removeAllViews()
            updateConstraints()
        }

    private var strokeWidth: Float = 20f
        set(value) {
            field = value
            if (value > 0) {
                for (ring in activityRings) {
                    ring.strokeWidth = value
                }
                removeAllViews()
                updateConstraints()
            }
        }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        calculateStrokeWidth()

    }

    var ringMargin: Int = 2
        set(value) {
            field = (value * resources.displayMetrics.density).toInt()
        }
    var showIcons: Boolean = true
        set(value) {
            field = value
            for (ring in activityRings){
                ring.showIcon = value
            }
        }

    init {
        if (isInEditMode) {
            val ring1 = ActivityRing(context)
            ring1.apply {
                progress = 0.7f
            }
            addRing(ring1)
        }

        if (attrs != null) {
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ActivityRingContainer)
            showIcons = a.getBoolean(R.styleable.ActivityRingContainer_showIcon, showIcons)
            ringMargin = a.getDimension(R.styleable.ActivityRingContainer_ringMargin, 2f).toInt()
            a.recycle()
        }
    }

    fun addRings(rings: List<ActivityRing>) {
        addRing(*rings.toTypedArray())
    }

    fun addRing(vararg ring: ActivityRing) {
        activityRings.addAll(ring)
        for (newRing in ring) {
            newRing.showIcon = showIcons
        }
        calculateStrokeWidth()
        updateConstraints()
    }

    private fun calculateStrokeWidth() {
        strokeWidth = (width / Math.max(12, activityRings.size * 4)).toFloat()
    }

    private fun updateConstraints() {
        for (index in childCount until activityRings.size) {
            val view = activityRings[index]
            view.strokeWidth = strokeWidth

            view.setPadding(ringMargin, ringMargin, ringMargin, ringMargin)
            view.id = index + 1311
            addView(view)
            val id = view.id
            val targetId: Int
            var margin: Int
            if (index == 0) {
                targetId = ConstraintSet.PARENT_ID
                margin = ringMargin
            } else {
                targetId = getChildAt(index - 1).id
                margin = ringMargin + strokeWidth.toInt()
            }
            val constraintSet = ConstraintSet()
            constraintSet.apply {
                clone(this@ActivityRingContainer)
                connect(id, ConstraintSet.START, targetId, ConstraintSet.START, margin)
                connect(id, ConstraintSet.TOP, targetId, ConstraintSet.TOP, margin)
                connect(id, ConstraintSet.END, targetId, ConstraintSet.END, margin)
                connect(id, ConstraintSet.BOTTOM, targetId, ConstraintSet.BOTTOM, margin)
                constrainWidth(id, ConstraintSet.MATCH_CONSTRAINT)
                constrainHeight(id, ConstraintSet.MATCH_CONSTRAINT)
                setDimensionRatio(id, "1:1")
                applyTo(this@ActivityRingContainer)
            }
        }
        //invalidate()
    }

    fun get(position: Int): ActivityRing? {
        return if (position >= 0) {
            activityRings[position]
        } else {
            null
        }
    }
}