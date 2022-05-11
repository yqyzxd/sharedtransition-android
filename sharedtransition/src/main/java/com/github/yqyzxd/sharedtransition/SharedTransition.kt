package com.github.yqyzxd.sharedtransition

import android.app.Activity
import android.app.SharedElementCallback
import android.content.Intent
import android.os.Bundle
import android.transition.Transition
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import java.util.concurrent.ThreadPoolExecutor

/**
 * Copyright (C), 2015-2022, 杭州迈优文化创意有限公司
 * FileName: SharedTransition
 * Author: wind
 * Date: 2022/5/10 14:10
 * Description: used for SharedElement Transition
 * Path: 路径
 * History:
 *  <author> <time> <version> <desc>
 *
 */
class SharedTransition {
    private var mReturning = false
    private var mReenterBundle: Bundle? = null

    /**
     * when reenter called activity this field provide the sharedElement
     */
    private var mReenterSharedElementProvider:IReenterSharedElementProvider?=null

    /**
     * when calling activity return this field provide the sharedElement
     */
    private var mReturnSharedElementProvider:IReturnSharedElementProvider?=null

    /**
     *  called activity call this
     */
    fun setExitSharedElementCallback(activity: Activity,provider:IReenterSharedElementProvider){
        mReenterSharedElementProvider=provider
        activity.setExitSharedElementCallback(mExitCallback)
    }


    /**
     *  calling activity call this
     */
    fun setEnterSharedElementCallback(activity: Activity,provider:IReturnSharedElementProvider){
        mReturnSharedElementProvider=provider
        activity.setEnterSharedElementCallback(mEnterCallback)
    }

    //for calling activity  (exit and reenter animation)
    private val mExitCallback: SharedElementCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(
            names: MutableList<String>,
            sharedElements: MutableMap<String, View>
        ) {
            mReenterBundle?.apply {
                val startingPosition: Int =
                    getInt(EXTRA_KEY_START_POSITION)
                val currentPosition: Int =
                    getInt(EXTRA_KEY_CURRENT_POSITION)

                if (startingPosition != currentPosition) {
                    mReenterSharedElementProvider?.apply {
                        val newTransitionName: String? = getString(EXTRA_KEY_CURRENT_TRANSITION_NAME)
                        val newSharedElement: View? =getSharedElement(mReenterBundle!!)
                        if (newSharedElement != null && newTransitionName!=null) {
                            names.clear()
                            names.add(newTransitionName)
                            sharedElements.clear()
                            sharedElements[newTransitionName] = newSharedElement
                        }
                    }

                }
            }
            mReenterBundle=null

        }
    }

    //for called activity (enter and return animation)
    private val mEnterCallback: SharedElementCallback =
        object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                if (mReturning) {
                    mReturnSharedElementProvider?.apply {
                        val sharedElement: View? = getSharedElement()

                        if (sharedElement == null) {
                            names.clear()
                            sharedElements.clear()
                        } else if (startPosition() != currentPosition()) {

                            println(
                                "ViewCompat.getTransitionName(sharedElement)：" + ViewCompat.getTransitionName(
                                    sharedElement
                                )
                            )
                            names.clear()
                            names.add(ViewCompat.getTransitionName(sharedElement)!!)
                            sharedElements.clear()
                            sharedElements[ViewCompat.getTransitionName(sharedElement)!!] =
                                sharedElement
                        }
                    }

                }
            }
        }



    /**
     *  called activity call this
     */
    fun finishAfterTransition(activity: Activity, startPosition: Int = 0, curPosition: Int = 0) {
        mReturning = true
        val data = Intent()
        data.putExtra(
            EXTRA_KEY_START_POSITION,
            startPosition
        )
        data.putExtra(
            EXTRA_KEY_CURRENT_POSITION,
            curPosition
        )

        mReturnSharedElementProvider?.apply {
            getSharedElement()?.let { sharedElement->
                data.putExtra(
                    EXTRA_KEY_CURRENT_TRANSITION_NAME,
                    ViewCompat.getTransitionName(sharedElement)
                )
            }

        }
        activity.setResult(Activity.RESULT_OK, data)
        ActivityCompat.finishAfterTransition(activity)
    }





    /**
     * calling activity call this
     */
    fun onActivityReenter(activity: Activity,requestCode:Int,data:Intent) {
        onActivityReenter(activity,requestCode,data,null)
    }

    /**
     * calling activity call this
     */
    fun onActivityReenter(activity: Activity,requestCode:Int,data:Intent,handler:IRenterHandler?) {
        //activity.onActivityReenter(requestCode,data)
        mReenterBundle= Bundle(data.extras)
        handler?.onReenter(mReenterBundle!!)
    }

    fun addSharedElementTransitionListener(transition: Transition,listener: Transition.TransitionListener){
        transition.addListener(listener)
    }


    companion object {
        const val EXTRA_KEY_START_POSITION = "extra_key_position"
        const val EXTRA_KEY_CURRENT_POSITION = "extra_key_current_position"
        const val EXTRA_KEY_CURRENT_TRANSITION_NAME = "extra_key_current_transition_name"
    }


}