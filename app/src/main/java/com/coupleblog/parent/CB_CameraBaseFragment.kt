package com.coupleblog.parent

import android.content.Intent
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.ScaleGestureDetector
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.a100photo.EditImageActivity
import com.coupleblog.fragment.CB_PhotoEditorFragment
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
abstract class CB_CameraBaseFragment(strTag: String,
                                     protected val uploadType: UPLOAD_TYPE,
                                     protected val bDeferred: Boolean = false)
    : CB_BaseFragment(strTag), CB_PhotoEditorFragment.CameraListener
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

            // image is not null, go to PhotoEditorFragment
            CB_AppFunc.mainScope.launch {
                CB_ViewModel.editorBitmap = imageBitmap
                CB_PhotoEditorFragment.cameraListener = this@CB_CameraBaseFragment
                startActivity(Intent(requireContext(), EditImageActivity::class.java))
                /*beginActionToEdtior()*/
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
}