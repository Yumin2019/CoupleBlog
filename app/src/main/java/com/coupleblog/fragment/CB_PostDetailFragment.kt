package com.coupleblog.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coupleblog.R
import com.coupleblog.model.CB_Post
import com.coupleblog.parent.CB_BaseFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.lang.IllegalArgumentException

class CB_PostDetailFragment: CB_BaseFragment("PostDetail")
{
    companion object
    {
        const val ARGU_POST_KEY = "strPostKey"
    }

    private lateinit var postKey: String

    private lateinit var postRef: DatabaseReference
    private var postListener: ValueEventListener? = null

    private var _binding            : PostDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = PostDetailBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_PostDetailFragment
            layoutManager   = LinearLayoutManager(context)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // get postKey from arguments
        postKey = requireArguments().getString(ARGU_POST_KEY)
            ?: throw IllegalArgumentException("must pass postKey")
    }

    override fun onStart()
    {
        super.onStart()

        // add value event listener to the post
        // need to modify this !!!! with databinding
        val postListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val post = snapshot.getValue<CB_Post>()
                post?.let {

                }
            }

            override fun onCancelled(error: DatabaseError)
            {

            }
        }
    }

    override fun onStop()
    {
        super.onStop()
    }

    override fun backPressed()
    {
        findNavController().popBackStack()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    fun menuButton()
    {

    }

}