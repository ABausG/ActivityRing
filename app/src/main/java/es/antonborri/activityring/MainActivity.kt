package es.antonborri.activityring

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
         val ring = when(seekBar){
            seekBar_out -> ring_out
            seekBar_middle -> ring_middle
            seekBar_in -> ring_in
            seekBar_innest -> ring_innest
            else -> null
        }

        val tinyRing = when(seekBar){
            seekBar_out -> ring_out2
            seekBar_middle -> ring_middle2
            seekBar_in -> ring_in2
            seekBar_innest -> ring_innest2
            else -> null
        }

        ring?.progress = progress/100f
        tinyRing?.progress = progress/100f
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

        seekBar_out.setOnSeekBarChangeListener(this)

        seekBar_middle.setOnSeekBarChangeListener(this)

        seekBar_in.setOnSeekBarChangeListener(this)

        seekBar_innest.setOnSeekBarChangeListener(this)


    }
}
