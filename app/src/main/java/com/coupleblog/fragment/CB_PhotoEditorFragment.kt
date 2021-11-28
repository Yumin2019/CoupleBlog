package com.coupleblog.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.parent.CB_BaseFragment
import com.coupleblog.singleton.CB_ViewModel
import ja.burhanrashid52.photoeditor.PhotoEditor

class CB_PhotoEditorFragment: CB_BaseFragment("PhotoEditorFragment")
{
    private var _binding            : PhotoEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoEditor: PhotoEditor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = PhotoEditorBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // set bitmap to imageView
        if(CB_ViewModel.editorBitmap != null)
            binding.photoEditorView.source.setImageBitmap(CB_ViewModel.editorBitmap)
        else
            binding.photoEditorView.source.setBackgroundResource(R.color.white)

        // use custom fonts
        val loraRegularTf = ResourcesCompat.getFont(requireActivity(), R.font.lora_regular)

        // load fonts from asset
        val emogiTf = Typeface.createFromAsset(requireContext().assets, "emojione-android.ttf")

        photoEditor = PhotoEditor.Builder(requireContext(), binding.photoEditorView)
            .setPinchTextScalable(true)
            .setClipSourceImage(false)
            .setDefaultTextTypeface(loraRegularTf)
            .setDefaultEmojiTypeface(emogiTf)
            .build()

        photoEditor.setBrushDrawingMode(true);
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    override fun backPressed()
    {
        findNavController().popBackStack()
    }
}