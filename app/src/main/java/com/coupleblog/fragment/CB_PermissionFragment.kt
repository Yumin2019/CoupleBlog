package com.coupleblog.fragment

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.coupleblog.R
import com.coupleblog.base.CB_BaseFragment
import com.coupleblog.singleton.CB_AppFunc

class CB_PermissionFragment : CB_BaseFragment()
{
    private var _binding            : PermissionBinding? = null
    private val binding get() = _binding!!

    // permissions
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    val permissions = arrayOf(Manifest.permission.CAMERA)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = PermissionBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner  = viewLifecycleOwner
            fragment        = this@CB_PermissionFragment
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // permission launcher
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        {
            // permission denied
            if(!CB_AppFunc.checkPermission(Manifest.permission.CAMERA))
            {
                // Camera
                CB_AppFunc.confirmDialog(requireActivity(), R.string.str_camera,
                    R.string.str_normal_permission_message, R.drawable.camera, false,
                    R.string.str_setting,
                    { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:${requireActivity().packageName}")
                        startActivity(intent)

                    }, R.string.str_cancel, null)
            }
        }
    }

    override fun onResume()
    {
        super.onResume()
        if(CB_AppFunc.checkPermission(permissions))
        {
            // user allowed these permissions
            beginAction(R.id.action_CB_PermissionFragment_to_CB_LoginFragment, R.id.CB_PermissionFragment)
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    fun okButton()
    {
        if(CB_AppFunc.checkPermission(permissions))
        {
            // user allowed these permissions
            beginAction(R.id.action_CB_PermissionFragment_to_CB_LoginFragment, R.id.CB_PermissionFragment)
        }
        else
        {
            // request permissions
            permissionLauncher.launch(permissions)
        }
    }

    override fun backPressed()
    {
        finalBackPressed()
    }
}