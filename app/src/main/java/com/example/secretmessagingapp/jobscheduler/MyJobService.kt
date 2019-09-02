package com.example.secretmessagingapp.jobscheduler

import android.app.job.JobParameters
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

internal class MyJobService : android.app.job.JobService() {
    private var jobCancelled = false


    private fun doBackgroundWork(params: JobParameters) {
        if (jobCancelled)
            return

        Thread(Runnable {
            FirebaseDatabase.getInstance().getReference("/latest-messages").removeValue()
            FirebaseDatabase.getInstance().getReference("/user-messages").removeValue()
        }).start()

        FirebaseAuth.getInstance().signOut()

        Log.d(TAG, "Finished wiping evidence!!!")

        // Tell the system that job has been finished
        jobFinished(params, false)

    }


    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG, "Job Started")
                doBackgroundWork(params)
        // job is over once the function is done
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.d(TAG, "Job cancelled before completion")
        jobCancelled = true
        return true
    }

    companion object {
        private val TAG = "JobService"
    }
}