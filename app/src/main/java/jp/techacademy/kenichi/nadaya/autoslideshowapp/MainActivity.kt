package jp.techacademy.kenichi.nadaya.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Handler
import java.util.*


class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    //private var pathUri: Uri = Uri.EMPTY
    private var pathUri = listOf<Uri>()
    private var countUri:Int = 0
    private var countButton:Int = 0
    private var initcount:Boolean = true
    private var flagCount :Boolean = true
    private var counter:Int = 0

    private var mTimer: Timer? = null
    // タイマー用の時間のための変数
    private var mTimerSec = 0.0
    private var mHandler = Handler()
    private var playCount :Boolean = true//true:play,false:stop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()

                val button_count = getCountbutton()
                val imageUri = getUri(button_count)
                imageView.setImageURI(imageUri)

                start_button.setOnClickListener {
                    if(playCount == true){
                        initcount = false
                        setCountbutton(true)
                        val button_count = getCountbutton()
                        val uri_count = getCounturi()
                        //Log.d("ANDROID","ボタンカウント="+button_count+",uriカウント="+uri_count)
                        if(button_count < uri_count){
                            val imageUri = getUri(button_count)
                            imageView.setImageURI(imageUri)
                        }else{
                            val button_count = getCountbutton()
                            val imageUri = getUri(button_count)
                            imageView.setImageURI(imageUri)
                            initCountbutton(-1)
                        }
                        flagCount = false
                    }
                }

                back_button.setOnClickListener {
                    if(playCount == true){
                        val uri_count = getCounturi()
                        val button_count = getCountbutton()
                        if(button_count == 0 && initcount == true){
                            val imageUri = getUri(uri_count)
                            imageView.setImageURI(imageUri)
                            initCountbutton(-1)
                            initcount = false
                        }

                        if(countButton == -1 && flagCount == false){
                            initcount = false
                            initCountbutton(getCounturi())
                            setCountbutton(false)
                            val button_count = getCountbutton()
                            val imageUri = getUri(button_count)
                            imageView.setImageURI(imageUri)
                        }

                        if(button_count > 0 && flagCount == false){
                            initcount = false
                            setCountbutton(false)
                            val button_count = getCountbutton()
                            val imageUri = getUri(button_count)
                            imageView.setImageURI(imageUri)
                        }
                    }
                }

                play_button.setOnClickListener {
                    if(playCount == true){
                        playCount = false
                        play_button.text = "停止"
                        if (mTimer == null){
                            mTimer = Timer()
                            mTimer!!.schedule(object : TimerTask() {
                                override fun run() {
                                    mTimerSec += 0.1
                                    mHandler.post {
                                        setCounter(true)
                                        val counter = getCounter()
                                        val uri_count = getCounturi()
                                        if(counter < uri_count){
                                            //Log.d("ANDROID","カウント="+counter+",uriカウント="+uri_count)
                                            val imageUri = getUri(counter)
                                            imageView.setImageURI(imageUri)
                                        }else{
                                            val counter = getCounter()
                                            val imageUri = getUri(counter)
                                            imageView.setImageURI(imageUri)
                                            initCounter(-1)
                                        }
                                    }
                                }
                            }, 2000, 2000) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定
                        }
                    }else{
                        playCount = true
                        play_button.text = "再生"
                        if (mTimer != null){
                            mTimer!!.cancel()
                            mTimer = null
                        }
                    }

                }
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }


    private fun getContentsInfo()  {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {

            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                //Log.d("ANDROID", "URI : " + imageUri.toString())
                //Log.d("ANDROID", "URI : " + id.toString())
                setUri(imageUri)

            } while (cursor.moveToNext())
        }
        //Log.d("ANDROID",""+cursor.getCount())
        setCounturi(cursor.getCount()-1)
        cursor.close()
    }

    public fun setUri(imageUri:Uri){
        this.pathUri += imageUri
    }

    public fun getUri(number:Int):Uri{
        var num:Int
        if(number < 0){
            num = 0
        }else if (number > getCounturi()){
            num = getCounturi()
        }else{
            num = number
        }
        return this.pathUri[num]
    }

    public fun setCounturi(count:Int){
        this.countUri = count
    }

    public fun getCounturi(): Int {
        return this.countUri;
    }

    public fun setCountbutton(pushCount:Boolean){
        if(pushCount == true){
            this.countButton++
        }else if(pushCount == false){
            this.countButton--
        }
    }

    public fun getCountbutton():Int{
        return this.countButton
    }

    public fun initCountbutton(number:Int){
        this.countButton = number
    }

    public fun setCounter(pushCount:Boolean){
        if(pushCount == true) {
            this.counter++
        }
    }

    public fun getCounter():Int{
        return this.counter
    }
    public fun initCounter(number:Int){
        this.counter = number
    }
}
