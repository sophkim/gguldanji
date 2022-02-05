package com.example.guru_1

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MissingCompleteActivity : AppCompatActivity() {

    //변수 정의
    lateinit var missingImage: ImageView
    lateinit var missingInfo: TextView
    lateinit var missingDate: TextView
    lateinit var missingPlace: TextView
    lateinit var missingFeat: TextView
    lateinit var missingPhone: TextView
    lateinit var missingSay: TextView
    lateinit var saveButton: Button
    lateinit var imageLayout: ConstraintLayout
    lateinit var shareButton: Button
    lateinit var imageBitmap: Bitmap
    lateinit var back: ImageButton
    lateinit var home: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missing_complete)

        //위젯과 변수 연결
        missingImage = findViewById(R.id.missingCom_img)
        missingInfo = findViewById(R.id.missingCom_info)
        missingDate = findViewById(R.id.missingCom_date)
        missingPlace = findViewById(R.id.missingCom_place)
        missingFeat = findViewById(R.id.missingCom_tx3)
        missingPhone = findViewById(R.id.missingCom_phone)
        missingSay = findViewById(R.id.missingCom_say)
        saveButton = findViewById(R.id.missingCom_save)
        imageLayout = findViewById(R.id.imageLayout)
        shareButton = findViewById(R.id.missingCom_share)
        back = findViewById(R.id.missingCom_back)
        home = findViewById(R.id.missingCom_home)

        //메인버튼 클릭 이벤트
        home.setOnClickListener {
            var intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        //뒤로가기 버튼 클릭 이벤트
        back.setOnClickListener {
            var intent = Intent(this,MissingInfoActivity::class.java)
            startActivity(intent)
        }

        //인텐트 받기
        val byteArray = intent.getByteArrayExtra("image")
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

        val namein = intent.getStringExtra("name").toString()
        val agein = intent.getStringExtra("age").toString()
        val genderin = intent.getStringExtra("gender").toString()
        val featin = intent.getStringExtra("feature").toString()
        val datein = intent.getStringExtra("date").toString()
        val placein = intent.getStringExtra("place").toString()
        val numin = intent.getStringExtra("num").toString()
        val sayin = intent.getStringExtra("say").toString()

        //날짜 데이터 전처리 예: 년, 월, 일
        val datesplit = datein.split("/")
        val year = datesplit[0]
        val month = datesplit[1]
        val day = datesplit[2]

        //받은 값 보여주기
        missingImage.setImageBitmap(bitmap)
        missingInfo.text = namein + "/" + agein + "/" + genderin
        missingDate.text = year + "년 " + month + "월 " + day + "일"
        missingPlace.text = placein
        missingFeat.text = featin
        missingPhone.text = numin
        missingSay.text = sayin

        //이미지 저장 버튼 누를 때
        saveButton.setOnClickListener {
            val imageBitmap = viewToBitmap(imageLayout) //뷰를 이미지로
            saveImage(imageBitmap) //이미지를 저장
            Toast.makeText(baseContext, "이미지 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show()
        }

        //이미지 공유버튼 누를 때
        shareButton.setOnClickListener {
            imageBitmap = viewToBitmap(imageLayout) //뷰를 이미지로
            val uri = getImageUri(this, imageBitmap) //이미지를 URI로
            //이미지 공유
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("image/*")
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            val chooser = Intent.createChooser(intent, "친구에게 공유하기")
            startActivity(chooser)
        }
    }
    //뷰 to 이미지 변환
    fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }
    //이미지 저장
    public fun saveImage(bitmap: Bitmap) {
        val fileName = System.currentTimeMillis().toString() + ".png" // 파일이름 현재시간.png

        /*
        * ContentValues() 객체 생성.
        * ContentValues는 ContentResolver가 처리할 수 있는 값을 저장해둘 목적으로 사용된다.
        * */
        val contentValues = ContentValues()
        contentValues.apply {
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/ImageSave") // 경로 설정
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName) // 파일이름을 put해준다.
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1) // 현재 is_pending 상태임을 만들어준다.
            // 다른 곳에서 이 데이터를 요구하면 무시하라는 의미로, 해당 저장소를 독점할 수 있다.
        }

        // 이미지를 저장할 uri를 미리 설정해놓는다.
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        try {
            if(uri != null) {
                val image = contentResolver.openFileDescriptor(uri, "w", null)
                // write 모드로 file을 open한다.

                if(image != null) {
                    val fos = FileOutputStream(image.fileDescriptor)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    //비트맵을 FileOutputStream를 통해 compress한다.
                    fos.close()

                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0) // 저장소 독점을 해제한다.
                    contentResolver.update(uri, contentValues, null, null)
                }
            }
        } catch(e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //이미지 to URI
    private fun getImageUri(context: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            context.getContentResolver(),
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }


}