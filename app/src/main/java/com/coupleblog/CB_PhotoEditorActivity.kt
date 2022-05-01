package com.coupleblog

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.coupleblog.a100photo.*
import com.coupleblog.a100photo.filters.FilterListener
import com.coupleblog.a100photo.filters.FilterViewAdapter
import com.coupleblog.a100photo.tools.EditingToolsAdapter
import com.coupleblog.a100photo.tools.ToolType
import com.coupleblog.base.CB_BaseActivity
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.coupleblog.a200photoeditor.*
import com.coupleblog.a200photoeditor.shape.ShapeBuilder
import com.coupleblog.a200photoeditor.shape.ShapeType
import com.google.android.gms.ads.AdSize
import kotlin.Exception

class CB_PhotoEditorActivity: CB_BaseActivity(CB_SingleSystemMgr.ACTIVITY_TYPE.PHOTO_EDTIOR),
     OnPhotoEditorListener,
     PropertiesBSFragment.Properties,
     ShapeBSFragment.Properties,
     EmojiBSFragment.EmojiListener,
     StickerBSFragment.StickerListener, EditingToolsAdapter.OnItemSelected, FilterListener
{
    companion object
    {
        var cameraListener: CameraListener? = null
        var mHasBSSheet = false
    }

    // PhotoEditor Begin
    private var _photoEditor: PhotoEditor? = null
    private val mPhotoEditor get() = _photoEditor!!

    private var _shapeBuilder: ShapeBuilder? = null
    private val mShapeBuilder get() = _shapeBuilder!!

    private var mPropertiesBSFragment: PropertiesBSFragment? = null
    private var mShapeBSFragment: ShapeBSFragment? = null
    private var mEmojiBSFragment: EmojiBSFragment? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private val mConstraintSet = ConstraintSet()
    private var mIsFilterVisible = false

    // PhotoEditor End
    private lateinit var binding: PhotoEditorBinding

    interface CameraListener
    {
        fun onProcess()
        fun onCancel()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding   = DataBindingUtil.setContentView(this, R.layout.activity_photo_editor)
        binding.apply {
            activity = this@CB_PhotoEditorActivity

            llmTools = LinearLayoutManager(this@CB_PhotoEditorActivity, LinearLayoutManager.HORIZONTAL, false)
            toolsAdapter = EditingToolsAdapter(this@CB_PhotoEditorActivity)

            llmFilters = LinearLayoutManager(this@CB_PhotoEditorActivity, LinearLayoutManager.HORIZONTAL, false)
            filterViewAdapter = FilterViewAdapter(this@CB_PhotoEditorActivity)

            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }

        window.statusBarColor = Color.TRANSPARENT

        // bottom text view
        mPropertiesBSFragment = PropertiesBSFragment()
        mEmojiBSFragment = EmojiBSFragment()
        mStickerBSFragment = StickerBSFragment()
        mShapeBSFragment = ShapeBSFragment()

        mStickerBSFragment!!.setStickerListener(this)
        mEmojiBSFragment!!.setEmojiListener(this)
        mPropertiesBSFragment!!.setPropertiesChangeListener(this)
        mShapeBSFragment!!.setPropertiesChangeListener(this)

        mHasBSSheet = false

        // use custom fonts
        val loraRegularTf = ResourcesCompat.getFont(applicationContext, R.font.lora_regular)

        // load fonts from asset
        val emogiTf = Typeface.createFromAsset(applicationContext.assets, "emojione-android.ttf")

        _photoEditor = PhotoEditor.Builder(applicationContext, binding.photoEditorView)
            .setPinchTextScalable(true)
            .setClipSourceImage(false)
            .setDefaultTextTypeface(loraRegularTf)
            .setDefaultEmojiTypeface(emogiTf)
            .build()

        mPhotoEditor.setOnPhotoEditorListener(this@CB_PhotoEditorActivity)
    }

    private fun getResizedBitmap(bm: Bitmap, newWidth: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val fWidthRatio = newWidth.toFloat() / width
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(fWidthRatio, fWidthRatio - 0.25f)
        // RECREATE THE NEW BITMAP
        return Bitmap.createBitmap(
            bm, 0, 0, width, height,
            matrix, false
        )
    }

    override fun onStart() {
        super.onStart()

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels - CB_AppFunc.convertDpToPixel(250)

        // set bitmap to imageView
        if(CB_ViewModel.editorBitmap != null)
        {
            CB_ViewModel.editorBitmap = getResizedBitmap(CB_ViewModel.editorBitmap!!, width)
            binding.photoEditorView.source.setImageBitmap(CB_ViewModel.editorBitmap)
        }
        else
        {
            val imageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            imageBitmap.eraseColor(Color.WHITE)

            if(imageBitmap == null)
            {
                Log.e(strTag, "invalid bitmap data")
                finish()
                return
            }

            CB_ViewModel.editorBitmap = imageBitmap
            binding.photoEditorView.source.setImageBitmap(imageBitmap)
        }

        mPhotoEditor.setFilterEffect(PhotoFilter.NONE)
    }

    override fun onPause()
    {
        binding.adView.pause()
        super.onPause()
    }

    override fun onResume()
    {
        binding.adView.resume()
        super.onResume()
    }

    override fun onDestroy()
    {
        binding.adView.destroy()
        super.onDestroy()
    }

    fun undo() { mPhotoEditor.undo() }
    fun redo() { mPhotoEditor.redo() }
    fun clearAllChanges()
    {
        if (CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG))
            return

        // if you click discard button, it'll be discarded all changes
        CB_AppFunc.confirmDialog(this@CB_PhotoEditorActivity, R.string.str_warning,
            R.string.str_clear_all_views_msg, R.drawable.warning_icon, true, R.string.str_discard,
        yesListener = {  _, _ -> mPhotoEditor.clearAllViews() }, R.string.str_cancel, null)
    }

    fun uploadImage()
    {
        val saveSettings = SaveSettings.Builder()
            .setClearViewsEnabled(true)
            .setTransparencyEnabled(true)
            .build()

        mPhotoEditor.saveAsBitmap(saveSettings, object : OnSaveBitmap
        {
            override fun onBitmapReady(saveBitmap: Bitmap?)
            {
                infoLog("mPhotoEditor: uploadImage")
                CB_ViewModel.editorBitmap = saveBitmap
                cameraListener?.onProcess()
                finish()
            }

            override fun onFailure(e: Exception?)
            {
                e?.printStackTrace()
                CB_AppFunc.okDialog(this@CB_PhotoEditorActivity, R.string.str_error,
                    R.string.str_failed_to_upload_image, R.drawable.error_icon, true)
            }
        })
    }

    override fun onBackPressed()
    {
        if(mIsFilterVisible)
        {
            // if filter is visible, turn off the filter
            showFilter(false)
            binding.txtCurrentTool.text = getString(R.string.app_name)
        }
        else if(!mPhotoEditor.isCacheEmpty)
        {
            // if it has changes
            CB_AppFunc.confirmDialog(this@CB_PhotoEditorActivity, R.string.str_warning,
                R.string.str_discard_msg, R.drawable.warning_icon, true,
                R.string.str_discard,
                yesListener = { _, _ ->
                    super.onBackPressed()
                    cameraListener?.onCancel()
                }, R.string.str_cancel, null)
        }
        else
        {
            // if it has no changes
            cameraListener?.onCancel()
            super.onBackPressed()
        }
    }

    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int)
    {
        if(rootView == null || text == null)
            return

        val textEditorDialogFragment = TextEditorDialogFragment.show(this, text, colorCode)
        textEditorDialogFragment.setOnTextEditorListener { inputText: String?, newColorCode: Int ->
            val styleBuilder = TextStyleBuilder()
            styleBuilder.withTextColor(newColorCode)
            mPhotoEditor.editText(rootView, inputText, styleBuilder)
            binding.txtCurrentTool.text = getString(R.string.label_text)
        }
    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int)
    {
        debugLog("onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]")
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int)
    {
        debugLog("onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]")
    }

    override fun onStartViewChangeListener(viewType: ViewType?)
    {
        debugLog("onStartViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onStopViewChangeListener(viewType: ViewType?)
    {
        debugLog("onStopViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onTouchSourceImage(event: MotionEvent?)
    {
        debugLog("onTouchView() called with: event = [$event]")
    }

    override fun onColorChanged(colorCode: Int)
    {
        mPhotoEditor.setShape(mShapeBuilder.withShapeColor(colorCode))
    }

    override fun onOpacityChanged(opacity: Int)
    {
        mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity))
    }

    override fun onShapeSizeChanged(shapeSize: Int)
    {
        mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize.toFloat()))
    }

    override fun onShapePicked(shapeType: ShapeType?)
    {
        mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType))
    }

    override fun onEmojiClick(emojiUnicode: String?)
    {
        mPhotoEditor.addEmoji(emojiUnicode)
    }

    override fun onStickerClick(bitmap: Bitmap?)
    {
        mPhotoEditor.addImage(bitmap)
    }

    override fun onFilterSelected(photoFilter: PhotoFilter?)
    {
        mPhotoEditor.setFilterEffect(photoFilter)
    }

    override fun onToolSelected(toolType: ToolType)
    {
        when(toolType)
        {
            ToolType.SHAPE ->
            {
                if(mHasBSSheet)
                    return

                _shapeBuilder = ShapeBuilder()
                mPhotoEditor.apply {
                    setBrushDrawingMode(true)
                    setShape(mShapeBuilder)
                }

                showBottomSheetDialogFragment(mShapeBSFragment)
                binding.txtCurrentTool.text = getString(R.string.label_shape)
                mHasBSSheet = true
            }

            ToolType.TEXT ->
            {
                if(mHasBSSheet)
                    return

                // show fragment -> select color and text -> process this function
                val textEditorFragment = TextEditorDialogFragment.show(this@CB_PhotoEditorActivity)
                textEditorFragment.setOnTextEditorListener { inputText, colorCode ->
                    val styleBuilder = TextStyleBuilder()
                    styleBuilder.withTextColor(colorCode)

                    mPhotoEditor.addText(inputText, styleBuilder)
                    binding.txtCurrentTool.text = getString(R.string.label_text)
                }

                mHasBSSheet = true
            }

            ToolType.ERASER ->
            {
                mPhotoEditor.brushEraser()
                binding.txtCurrentTool.text = getString(R.string.label_eraser_mode)
            }

            ToolType.FILTER ->
            {
                showFilter(true)
                binding.txtCurrentTool.text = getString(R.string.label_filter)
            }

            ToolType.EMOJI ->
            {
                if(mHasBSSheet)
                    return

                showBottomSheetDialogFragment(mEmojiBSFragment)
                binding.txtCurrentTool.text = getString(R.string.label_emoji)

                mHasBSSheet = true
            }
            ToolType.STICKER ->
            {
                if(mHasBSSheet)
                    return

                showBottomSheetDialogFragment(mStickerBSFragment)
                binding.txtCurrentTool.text = getString(R.string.label_sticker)

                mHasBSSheet = true
            }
        }
    }

    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?)
    {
        if(fragment == null || fragment.isAdded)
            return

        fragment.show(supportFragmentManager, fragment.tag)
    }

    private fun showFilter(isVisible: Boolean)
    {
        mIsFilterVisible = isVisible
        mConstraintSet.clone(binding.rootView)

        // manage constraintSet with its visibility
        if(isVisible)
        {
            // |A|
            mConstraintSet.apply {
                clear(binding.rvFilterView.id, ConstraintSet.START)
                connect(binding.rvFilterView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                connect(binding.rvFilterView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            }
        }
        else
        {
            // |OO|A <=
            mConstraintSet.apply {
                clear(binding.rvFilterView.id, ConstraintSet.END)
                connect(binding.rvFilterView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END)
            }
        }

        val changeBounds = ChangeBounds().apply {
            duration = 350
            interpolator = AnticipateOvershootInterpolator(1.0f)
        }

        TransitionManager.beginDelayedTransition(binding.rootView, changeBounds)
        mConstraintSet.applyTo(binding.rootView)
    }
}