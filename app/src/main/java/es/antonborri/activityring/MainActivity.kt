package es.antonborri.activityring

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import es.antonborri.activityring.ring.ActivityRing
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val pos = when (seekBar) {
            seekBar_out -> 0
            seekBar_middle -> 1
            seekBar_in -> 2
            else -> -1
        }

        ring_containter.get(pos)?.progress = progress / 100f
        cont_small_1.get(pos)?.progress = progress / 100f
        cont_small_2.get(pos)?.progress = progress / 100f
        cont_small_3.get(pos)?.progress = progress / 100f
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (i in 0 until 12) {
            val ring = ActivityRing(this)
            ring.apply {
                when (i % 3) {
                    0 -> {
                        color = ContextCompat.getColor(context, R.color.out)
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_forward)
                        progress = 0f
                    }
                    1 -> {
                        color = ContextCompat.getColor(context, R.color.middle)
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_tab)
                        progress = 0.25f
                    }
                    2 -> {
                        color = ContextCompat.getColor(context, R.color.inner)
                        drawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_up)
                        progress = 1f
                    }
                }
            }
            when (i / 3) {
                0 -> ring_containter
                1 -> cont_small_1
                2 -> cont_small_2
                3 -> cont_small_3
                else -> null
            }?.addRing(ring)
        }

        seekBar_out.setOnSeekBarChangeListener(this)

        seekBar_middle.setOnSeekBarChangeListener(this)

        seekBar_in.setOnSeekBarChangeListener(this)
    }


}
