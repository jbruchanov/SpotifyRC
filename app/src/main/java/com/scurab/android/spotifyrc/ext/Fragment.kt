package com.scurab.android.spotifyrc.ext

import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.scurab.android.spotifyrc.R

fun Fragment.replaceFragment(fragment: Fragment, backStack: Boolean = true) {
    requireActivity().supportFragmentManager.commit {
        setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
        replace(R.id.fragment_container, fragment)
        if (backStack) {
            addToBackStack(null)
        }
    }
}