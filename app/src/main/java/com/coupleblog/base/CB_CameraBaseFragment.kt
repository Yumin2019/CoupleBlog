package com.coupleblog.base

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.ScaleGestureDetector
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.coupleblog.CB_PhotoEditorActivity
import com.coupleblog.R
import com.coupleblog.a100photo.EditImageActivity
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.coupleblog.storage.CB_UploadService
import com.coupleblog.storage.UPLOAD_TYPE
import ja.burhanrashid52.photoeditor.PhotoEditor
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.lang.Exception

// all of fragments want to use camera function, extend this fragment
abstract class CB_CameraBaseFragment(protected val uploadType: UPLOAD_TYPE,
                                     protected val bDeferred: Boolean = false)
    : CB_BaseFragment(), CB_PhotoEditorActivity.CameraListener
{
    // registerForActivityResult
    protected lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    protected lateinit var galleryLauncher: ActivityResultLauncher<String>
    protected var funDeferred: (()->Unit)? = null

    protected var imageUri: Uri? = null
    protected var strFilePath: String? = null
    protected var imageBitmap: Bitmap? = null

    // CameraListener
    override fun onProcess()
    {
        debugLog("cameraListener: onProcess")
        imageBitmap = CB_ViewModel.editorBitmap

        if(!bDeferred)
        {
            debugLog("upload start")
            saveBitmapAndUpload()
        }
        else
        {
            debugLog("deferred upload")
            funDeferred?.invoke()
        }
    }

    override fun onCancel()
    {
        debugLog("cameraListener: onCancel")
    }

    abstract fun beginActionToEdtior()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        createTempFile()

        // camera launcher
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture())
        { isSaved ->

            if(!isSaved)
            {
                Log.e(strTag, "user canceled camera")
                return@registerForActivityResult
            }

            imageProcess()
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent())
        { uri ->

            if(uri == null)
            {
                Log.e(strTag, "user canceled gallery")
                return@registerForActivityResult
            }

            imageUri = uri
            imageProcess()
        }
    }

    protected fun imageProcess()
    {
        CB_AppFunc.networkScope.launch {

            imageBitmap = CB_AppFunc.getBitmapFromUri(requireActivity().applicationContext.contentResolver, imageUri!!)
            if(imageBitmap == null)
            {
                Log.e(strTag, "failed to convert uri to bitmap")
                uploadFailed()
                return@launch
            }

            // N 버전 이상만 화면 회전을 시도한다. (외부 저장소 권한 제거)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                try
                {
                    val inputStream = CB_AppFunc.application.contentResolver.openInputStream(imageUri!!)!!
                    val exifInterface = ExifInterface(inputStream)
                    imageBitmap = CB_AppFunc.changeImageOrientation(imageBitmap!!, exifInterface)
                }
                catch (e: IOException)
                {
                    e.printStackTrace()
                    imageBitmap = null
                }

                if(imageBitmap == null)
                {
                    Log.e(strTag, "failed to change orientation on image")
                    uploadFailed()
                    return@launch
                }
            }

            // image is not null, go to EditImageActivity
            CB_AppFunc.mainScope.launch {
                CB_ViewModel.editorBitmap = imageBitmap
                CB_PhotoEditorActivity.cameraListener = this@CB_CameraBaseFragment
                startActivity(Intent(requireActivity(), CB_PhotoEditorActivity::class.java))
            }
        }
    }

    protected fun saveBitmapAndUpload(strDatabaseKey: String? = null)
    {
        if(imageBitmap == null || strFilePath.isNullOrEmpty())
        {
            errorLog("imageBitmap == null || strFilePath.isNullOrEmpty()")
            return
        }

        // save image as jpg format
        CB_AppFunc.saveBitmapToFileCache(imageBitmap!!, strFilePath!!)

        // upload image file
        imageUri = FileProvider.getUriForFile(requireContext(), getString(R.string.file_provider), File(strFilePath!!))
        val intent = Intent(requireContext(), CB_UploadService::class.java)
            .putExtra(CB_UploadService.FILE_URI, imageUri)
            .putExtra(CB_UploadService.UPLOAD_TYPE_KEY, uploadType.ordinal)
            .setAction(CB_UploadService.ACTION_UPLOAD)

        if(!strDatabaseKey.isNullOrEmpty())
            intent.putExtra(CB_UploadService.DATABASE_KEY, strDatabaseKey)

        requireActivity().startService(intent)
    }

    private fun createTempFile()
    {
        val strTime = CB_AppFunc.getDateStringForSave()
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            strTime,      // prefix
            ".jpg",  // suffix
            storageDir    // directory
        ).apply { deleteOnExit() }

        imageBitmap = null
        strFilePath = imageFile.absolutePath
        imageUri =  FileProvider.getUriForFile(requireContext(), getString(R.string.file_provider), imageFile)
    }

    private fun uploadFailed(iRes: Int = R.string.str_failed_to_upload_image)
    {
        CB_AppFunc.mainScope.launch {
            if(CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.OK_DIALOG))
                return@launch

            CB_AppFunc.okDialog(requireActivity(), R.string.str_error,
                iRes, R.drawable.error_icon, true)
        }
    }

    protected fun cameraActivity()
    {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 100);
    }

    protected fun galleryActivity()
    {
        val intent =  Intent();
        intent.type = "image/*";
        intent.action = Intent.ACTION_GET_CONTENT;
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 200);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                100 -> {
                    // 카메라에서 이미지 선택을 하고 돌아온 경우
                    val photo = data!!.extras!!["data"] as Bitmap?
                    CB_ViewModel.editorBitmap = photo
                    CB_PhotoEditorActivity.cameraListener = this@CB_CameraBaseFragment
                    val intent = Intent(requireActivity(), CB_PhotoEditorActivity::class.java)
                    startActivity(intent)
                }
                200 ->                     // 이미지를 선택해서 들어온 경우
                    try {
                        val uri = data!!.data
                        val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                        CB_ViewModel.editorBitmap = bitmap
                        CB_PhotoEditorActivity.cameraListener = this@CB_CameraBaseFragment
                        val intent = Intent(requireActivity(), CB_PhotoEditorActivity::class.java)
                        startActivity(intent)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
            }
        }
    }
}