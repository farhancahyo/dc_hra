package com.shiro.formhrddover.ui.formorientationemployee.header

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.shiro.formhrddover.R
import com.shiro.formhrddover.databinding.ActivityImageZoomBinding

class ImageZoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageZoomBinding

    companion object {
        const val IMAGE_PATH = "image_path"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageZoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Form Patroli Dover"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.hide()

        Glide.with(this)
                .load(intent.getStringExtra(IMAGE_PATH))
                .placeholder(R.drawable.ic_baseline_broken_image_24)
                .into(binding.photoView)
    }
}