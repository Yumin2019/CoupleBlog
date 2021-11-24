package com.coupleblog.parent

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.coupleblog.R
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.storage.CB_UploadService
import com.coupleblog.storage.UPLOAD_TYPE
import kotlinx.coroutines.launch
import java.io.File

// all of fragments want to use camera function, extend this fragment
abstract class CB_CameraBaseFragment(strTag: String,
                                     protected val uploadType: UPLOAD_TYPE,
                                     protected val bDeferred: Boolean = false) : CB_BaseFragment(strTag)
{
    // registerForActivityResult
    protected lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    protected lateinit var galleryLauncher: ActivityResultLauncher<String>
    protected var funDeferred: (()->Unit)? = null

    protected var imageUri: Uri? = null
    protected var strFilePath: String? = null
    protected var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        createTempFile()

        // camera launcher
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture())
        { isSaved ->

            CB_AppFunc.networkScope.launch {

                if(!isSaved)
                {
                    Log.e(strTag, "user canceled camera")
                    return@launch
                }

                // image save was successful
                // getBitmap from uri
                imageBitmap = CB_AppFunc.getBitmapFromUri(requireActivity().applicationContext.contentResolver, imageUri!!)
                if(imageBitmap == null)
                {
                    Log.e(strTag, "failed to convert uri to bitmap")
                    uploadFailed()
                    return@launch
                }

                // bitmap was successful
                imageBitmap = CB_AppFunc.changeImageOrientation(imageBitmap!!, strFilePath!!)
                if(imageBitmap == null)
                {
                    Log.e(strTag, "failed to change orientation on image")
                    uploadFailed()
                    return@launch
                }

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
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent())
        { uri ->
            CB_AppFunc.networkScope.launch {
                if(uri == null)
                {
                    Log.e(strTag, "user canceled gallery")
                    return@launch
                }

                val strPath = CB_AppFunc.getPathFromURI(requireActivity(), uri)
                if(strPath == null)
                {
                    Log.e(strTag, "getPathFromUri error uri:$uri")
                    uploadFailed(R.string.str_select_image_from_gallery)
                    return@launch
                }

                imageBitmap = CB_AppFunc.getBitmapFromUri(requireActivity().applicationContext.contentResolver, uri)
                if(imageBitmap == null)
                {
                    Log.e(strTag, "failed to convert uri to bitmap")
                    uploadFailed()
                    return@launch
                }

                // bitmap was successful
                imageBitmap = CB_AppFunc.changeImageOrientation(imageBitmap!!, strPath)
                if(imageBitmap == null)
                {
                    Log.e(strTag, "failed to change orientation on image")
                    uploadFailed()
                    return@launch
                }

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