package com.github.yqyzxd.sharedtransition

import android.app.Activity
import android.app.SharedElementCallback
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Transition
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw

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
    private val mHandler:Handler= Handler(Looper.getMainLooper())
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
        mReturning=false
        mReturnSharedElementProvider=provider
        activity.setEnterSharedElementCallback(mEnterCallback)
    }

    /**
     *  for calling activity  (exit and reenter animation)
     */
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
                        val newSharedElements:List<androidx.core.util.Pair<View,String>>? =sharedElements(mReenterBundle!!)
                        newSharedElements?.forEach {

                            names.clear()
                            names.add(it.second)
                            sharedElements.clear()
                            val transitionName=ViewCompat.getTransitionName(it.first)
                            transitionName?.apply {
                                sharedElements[transitionName]=it.first
                            }

                        }


                    }

                }
            }
            mReenterBundle=null

        }
    }

    /**
     *  for called activity (enter and return animation)
     */
    private val mEnterCallback: SharedElementCallback =
        object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                if (mReturning) {

                    mReturnSharedElementProvider?.apply {
                        val newSharedElements:List<androidx.core.util.Pair<View,String>>? = sharedElements()

                        if (newSharedElements.isNullOrEmpty()) {
                            names.clear()
                            sharedElements.clear()
                        } else if (startPosition() != currentPosition()) {
                            names.clear()
                            sharedElements.clear()
                            newSharedElements.forEach {
                                names.add(ViewCompat.getTransitionName(it.first)!!)
                                sharedElements[ViewCompat.getTransitionName(it.first)!!] =it.first
                            }
                        }
                    }
                    mReturnSharedElementProvider=null

                }

            }
        }




    private fun makeSceneTransitionAnimation(activity: Activity,provider: IExitSharedElementProvider): ActivityOptionsCompat {
        return ActivityOptionsCompat.makeSceneTransitionAnimation(activity,*provider.shareElements().toTypedArray())
    }

    fun startActivity(context: Context,intent: Intent,startPosition: Int=0,ext:String="",provider: IExitSharedElementProvider?){
        intent.putExtra(EXTRA_KEY_START_POSITION,startPosition)
        intent.putExtra(EXTRA_KEY_EXT,ext)

        var bundle:Bundle?=null
        provider?.apply {
            if (context is Activity){
                bundle=makeSceneTransitionAnimation(context,provider).toBundle()
            }

        }

        ActivityCompat.startActivity(context, intent,bundle)
    }

    fun isReturning():Boolean{
        return mReturning
    }

    /**
     *  called activity call this
     */
    fun finishAfterTransition(activity: Activity) {
        mReturning = true
        val data = Intent()

        mReturnSharedElementProvider?.apply {
            data.putExtra(
                EXTRA_KEY_START_POSITION,
                startPosition()
            )
            data.putExtra(
                EXTRA_KEY_CURRENT_POSITION,
                currentPosition()
            )
            data.putExtra(
                EXTRA_KEY_EXT,
                ext()
            )
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
        try {
            mReenterBundle= Bundle(data.extras)
            handler?.onReenter(mReenterBundle!!)
        }catch (e:Exception){
            e.printStackTrace()
        }


    }

    fun addSharedElementTransitionListener(transition: Transition,listener: Transition.TransitionListener){
        transition.addListener(listener)
    }

    fun postponeEnterTransition(activity: Activity){
        ActivityCompat.postponeEnterTransition(activity)
        Log.d(TAG,"postponeEnterTransition")
        mHandler.postDelayed({
            if (activity.isFinishing.not())
                startPostponedEnterTransition(activity)
        },500)
    }

    @JvmOverloads
    fun startPostponedEnterTransition(activity: Activity,target:View=activity.window.decorView){
        mHandler.removeCallbacksAndMessages(null)
        Log.d(TAG,"startPostponedEnterTransition")
        target.doOnPreDraw {
            ActivityCompat.startPostponedEnterTransition(activity)
        }
    }

    fun dispose(){
        mReturnSharedElementProvider=null
        mReenterSharedElementProvider=null
    }
    companion object {
        const val TAG = "SharedTransition"
        /**
         * set by user to determine the start position
         */
        const val EXTRA_KEY_START_POSITION = "extra_key_position"

        /**
         * set in bundle for user to handle position
         */
        const val EXTRA_KEY_CURRENT_POSITION = "extra_key_current_position"

        /**
         * extra data for user also defined by user
         */
        const val EXTRA_KEY_EXT="extra_key_ext"
    }


}