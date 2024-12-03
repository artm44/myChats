//package com.artm44.mychats.ui.chat
//
//import android.os.Bundle
//import android.widget.ImageView
//import androidx.appcompat.app.AppCompatActivity
//
//class FullScreenImageActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_full_screen_image)
//
//        val imageUrl = intent.getStringExtra("IMAGE_URL")
//        val imageView: ImageView = findViewById(R.id.fullScreenImageView)
//
//        imageUrl?.let {
//            Glide.with(this)
//                .load(it)
//                .into(imageView)
//        }
//
//        imageView.setOnClickListener {
//            finish() // Закрываем Activity при нажатии
//        }
//    }
//}
