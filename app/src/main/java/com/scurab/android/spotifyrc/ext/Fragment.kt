package com.scurab.android.spotifyrc.ext

import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.scurab.android.spotifyrc.Common
import com.scurab.android.spotifyrc.R

fun Fragment.replaceFragment(fragment: Fragment, backStack: Boolean = true, animations: IntArray = Common.fadeInOutAnimations) {
    requireActivity().supportFragmentManager.commit {
        setCustomAnimations(animations[0], animations[1], animations[2], animations[3])
        replace(R.id.fragment_container, fragment)
        if (backStack) {
            addToBackStack(null)
        }
    }
}

fun Fragment.addFragment(fragment: Fragment, backStack: Boolean = true, animations: IntArray = Common.fadeInOutAnimations) {
    requireActivity().supportFragmentManager.commit {
        setCustomAnimations(animations[0], animations[1], animations[2], animations[3])
        add(R.id.fragment_container, fragment)
        if (backStack) {
            addToBackStack(null)
        }
    }
}