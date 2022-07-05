package com.example.workmanagerpractice

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.os.HandlerCompat
import androidx.work.*
import com.example.workmanagerpractice.databinding.ActivityOneTimeWorkBinding

class OneTimeWork : AppCompatActivity() {
    private lateinit var binding: ActivityOneTimeWorkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOneTimeWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val workRequest = OneTimeWorkRequestBuilder<OneTimeWorker>()
            //.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        //val workRequest = OneTimeWorkRequest.from(OneTimeWorker::class.java)

        val workManager = WorkManager.getInstance(this)
        //workManager.enqueue(workRequest)

        workManager.enqueueUniqueWork(
            "unique one time work",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        // when needed to cancel work  like button click
        //workManager.cancelWorkById(workRequest.id)
    }

    // inner class - inner can access the members of its outer class.
    // Inner classes carry a reference to an object of an outer class:
    // do work not calling when worker defined as inner

    class OneTimeWorker(context: Context, workerParameters: WorkerParameters):
        Worker(context, workerParameters){

        override fun doWork(): Result {
        //Toast.makeText(applicationContext , "One time work request called", Toast.LENGTH_SHORT).show()
            //java.lang.NullPointerException: Can't toast on a thread that has not called Looper.prepare()
            Log.d(TAG, "doWork: called")

            val handler = HandlerCompat.createAsync(Looper.getMainLooper())
            handler.post {
                Toast.makeText(applicationContext, "One time work request called",
                    Toast.LENGTH_SHORT).show()
            }

            // Here I am unable to toast, start another activity..
            return Result.success()
        }


    }

}


