package group.nhom14.textospeech.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import group.nhom14.textospeech.databinding.ActivityMainBinding
import group.nhom14.textospeech.ui.main.AudioFragment


class MainActivity : AppCompatActivity() {

    companion object {
        private const val BASE_URL = "https://7282-14-231-130-155.ngrok-free.app"
    }

    private lateinit var mBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initFragment()
    }

    private fun initFragment() {
        supportFragmentManager.beginTransaction()
            .replace(mBinding.mainContainer.id, AudioFragment())
            .commit()
    }


}