package com.eselman.locationreminderapp.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.eselman.locationreminderapp.R
import com.eselman.locationreminderapp.base.BaseFragment
import com.eselman.locationreminderapp.base.NavigationCommand
import com.eselman.locationreminderapp.databinding.FragmentAuthenticationBinding
import com.firebase.ui.auth.AuthUI
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthenticationFragment:BaseFragment() {
    override val viewModel:AuthenticationViewModel by viewModel()

    private lateinit var binding: FragmentAuthenticationBinding

    companion object {
        const val SIGN_IN_REQUEST_CODE = 1001
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_authentication, container, false
            )
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.loginBtn.setOnClickListener {
            launchSignInFlow()
        }
        observeAuthenticationState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                navigateToReminders()
            } else {
                Toast.makeText(activity, "Sign Failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(viewLifecycleOwner, {authenticationState ->
            when (authenticationState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    navigateToReminders()
                }

                else -> {
                    //launchSignInFlow()
                }
            }
        })
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }

    private fun navigateToReminders() {
        viewModel.navigationCommand.postValue(
            NavigationCommand.To(AuthenticationFragmentDirections.actionAuthenticationFragmentToReminderListFragment())
        )
    }
}
//          TODO: a bonus is to customize the sign in flow to look nice using :
//https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout