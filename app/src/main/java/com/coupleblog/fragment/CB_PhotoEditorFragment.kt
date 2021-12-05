package com.coupleblog.fragment

import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.view.animation.AnticipateOvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.coupleblog.R
import com.coupleblog.a100photo.*
import com.coupleblog.a100photo.filters.FilterListener
import com.coupleblog.a100photo.filters.FilterViewAdapter
import com.coupleblog.a100photo.tools.EditingToolsAdapter
import com.coupleblog.a100photo.tools.ToolType
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc
import com.coupleblog.singleton.CB_SingleSystemMgr
import com.coupleblog.singleton.CB_ViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.*
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType
import java.lang.Exception

class CB_PhotoEditorFragment: CB_BaseFragment("PhotoEditorFragment"),
     OnPhotoEditorListener,
     PropertiesBSFragment.Properties,
     ShapeBSFragment.Properties,
     EmojiBSFragment.EmojiListener,
     StickerBSFragment.StickerListener, EditingToolsAdapter.OnItemSelected, FilterListener
{
    companion object
    {
        var cameraListener: CameraListener? = null
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
    private var _binding            : PhotoEditorBinding? = null
    private val binding get() = _binding!!

    interface CameraListener
    {
        fun onProcess()
        fun onCancel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = PhotoEditorBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment = this@CB_PhotoEditorFragment
            viewModel = CB_ViewModel.Companion

            llmTools = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            toolsAdapter = EditingToolsAdapter(this@CB_PhotoEditorFragment)

            llmFilters = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            filterViewAdapter = FilterViewAdapter(this@CB_PhotoEditorFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // bottom text view
        CB_ViewModel.strCurTool.postValue(getString(R.string.app_name))

        // FULL SCREEN + NO ACTION BAR
        requireActivity().apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        mPropertiesBSFragment = PropertiesBSFragment()
        mEmojiBSFragment = EmojiBSFragment()
        mStickerBSFragment = StickerBSFragment()
        mShapeBSFragment = ShapeBSFragment()

        mStickerBSFragment!!.setStickerListener(this)
        mEmojiBSFragment!!.setEmojiListener(this)
        mPropertiesBSFragment!!.setPropertiesChangeListener(this)
        mShapeBSFragment!!.setPropertiesChangeListener(this)

        // use custom fonts
        val loraRegularTf = ResourcesCompat.getFont(requireActivity(), R.font.lora_regular)

        // load fonts from asset
        val emogiTf = Typeface.createFromAsset(requireContext().assets, "emojione-android.ttf")

        _photoEditor = PhotoEditor.Builder(requireContext(), binding.photoEditorView)
            .setPinchTextScalable(true)
            .setClipSourceImage(false)
            .setDefaultTextTypeface(loraRegularTf)
            .setDefaultEmojiTypeface(emogiTf)
            .build()

        mPhotoEditor.setOnPhotoEditorListener(this@CB_PhotoEditorFragment)

        // set bitmap to imageView
        if(CB_ViewModel.editorBitmap != null)
        {
            binding.photoEditorView.source.setImageBitmap(CB_ViewModel.editorBitmap)
        }
        else
        {
            binding.photoEditorView.source.setBackgroundResource(R.color.white)
        }
    }

    override fun onDetach()
    {
        super.onDetach()

        // DISABLE FULL SCREEN + ACTION BAR
        requireActivity().apply {
            requestWindowFeature(Window.FEATURE_ACTION_BAR)
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    fun undo() { mPhotoEditor.undo() }
    fun redo() { mPhotoEditor.redo() }
    fun clearAllChanges()
    {
        if (CB_SingleSystemMgr.isDialog(CB_SingleSystemMgr.DIALOG_TYPE.CONFIRM_DIALOG))
            return

        // if you click discard button, it'll be discarded all changes
        CB_AppFunc.confirmDialog(requireActivity(), R.string.str_warning,
            R.string.str_clear_all_views_msg, R.drawable.warning_icon, true, R.string.str_discard,
        yesListener = {  _, _ -> mPhotoEditor.clearAllViews() }, R.string.str_cancel, null)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressed()
    {
        if(mIsFilterVisible)
        {
            // if filter is visible, turn off the filter
            showFilter(false)
            CB_ViewModel.strCurTool.postValue(getString(R.string.app_name))
        }
        else if(!mPhotoEditor.isCacheEmpty)
        {
            // if it has changes
            CB_AppFunc.confirmDialog(requireActivity(), R.string.str_warning,
                R.string.str_discard_msg, R.drawable.warning_icon, true,
                R.string.str_discard,
                yesListener = { _, _ ->
                    findNavController().popBackStack()
                    cameraListener?.onCancel()
                }, R.string.str_cancel, null)
        }
        else
        {
            // if it has no changes
            findNavController().popBackStack()
            cameraListener?.onCancel()
        }
    }

    override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int)
    {
        val textEditorDialogFragment = TextEditorDialogFragment.show(requireActivity() as AppCompatActivity, text, colorCode)
        textEditorDialogFragment.setOnTextEditorListener { inputText: String?, newColorCode: Int ->
            val styleBuilder = TextStyleBuilder()
            styleBuilder.withTextColor(newColorCode)
            mPhotoEditor.editText(rootView, inputText, styleBuilder)
            CB_ViewModel.strCurTool.postValue(getString(R.string.label_text))
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
        CB_ViewModel.strCurTool.postValue(getString(R.string.label_brush))
    }

    override fun onOpacityChanged(opacity: Int)
    {
        mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity))
        CB_ViewModel.strCurTool.postValue(getString(R.string.label_brush))
    }

    override fun onShapeSizeChanged(shapeSize: Int)
    {
        mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize.toFloat()))
        CB_ViewModel.strCurTool.postValue(getString(R.string.label_brush))
    }

    override fun onShapePicked(shapeType: ShapeType?)
    {
        mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType))
        CB_ViewModel.strCurTool.postValue(getString(R.string.label_brush))
    }

    override fun onEmojiClick(emojiUnicode: String?)
    {
        mPhotoEditor.addEmoji(emojiUnicode)
        CB_ViewModel.strCurTool.postValue(getString(R.string.label_emoji))
    }

    override fun onStickerClick(bitmap: Bitmap?)
    {
        mPhotoEditor.addImage(bitmap)
        CB_ViewModel.strCurTool.postValue(getString(R.string.label_sticker))
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
                _shapeBuilder = ShapeBuilder()
                mPhotoEditor.apply {
                    setBrushDrawingMode(true)
                    setShape(mShapeBuilder)
                }
                showBottomSheetDialogFragment(mShapeBSFragment)
                CB_ViewModel.strCurTool.postValue(getString(R.string.label_shape))
            }

            ToolType.TEXT ->
            {
                // show fragment -> select color and text -> process this function
                val textEditorFragment = TextEditorDialogFragment.show(requireActivity() as AppCompatActivity)
                textEditorFragment.setOnTextEditorListener { inputText, colorCode ->
                    val styleBuilder = TextStyleBuilder()
                    styleBuilder.withTextColor(colorCode)

                    mPhotoEditor.addText(inputText, styleBuilder)
                    CB_ViewModel.strCurTool.postValue(getString(R.string.label_text))
                }
            }

            ToolType.ERASER ->
            {
                mPhotoEditor.brushEraser()
                CB_ViewModel.strCurTool.postValue(getString(R.string.label_eraser_mode))
            }

            ToolType.FILTER ->
            {
                showFilter(true)
                CB_ViewModel.strCurTool.postValue(getString(R.string.label_filter))
            }

            ToolType.EMOJI -> showBottomSheetDialogFragment(mEmojiBSFragment)
            ToolType.STICKER -> showBottomSheetDialogFragment(mStickerBSFragment)
        }
    }

    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?)
    {
        if(fragment == null || fragment.isAdded)
            return

        fragment.show(requireActivity().supportFragmentManager, fragment.tag)
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

    fun saveImage()
    {
        mPhotoEditor.saveAsBitmap(object: OnSaveBitmap{
            override fun onBitmapReady(saveBitmap: Bitmap)
            {
                CB_ViewModel.editorBitmap = saveBitmap
                findNavController().popBackStack()
                cameraListener?.onProcess()
            }

            override fun onFailure(e: Exception)
            {
                e.printStackTrace()
            }
        })
    }
}