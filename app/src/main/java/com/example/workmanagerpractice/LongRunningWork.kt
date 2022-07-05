package com.example.workmanagerpractice

import android.content.ContentValues.TAG
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.HandlerCompat
import androidx.work.*
import com.example.workmanagerpractice.databinding.ActivityLongRunningWorkBinding
import com.example.workmanagerpractice.databinding.ActivityOneTimeWorkBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.util.concurrent.TimeUnit

class LongRunningWork : AppCompatActivity() {
    private lateinit var binding: ActivityLongRunningWorkBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLongRunningWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true) // true - should be charging, false - either charge or not
            //.setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            //.setRequiresDeviceIdle(true)
            .build()


        val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicWorker>(15, TimeUnit.MINUTES,
        1, TimeUnit.MINUTES)
            .setConstraints(constraints)
//            .setBackoffCriteria(  // retry, back off policy
//                BackoffPolicy.LINEAR,
//                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
//                TimeUnit.MILLISECONDS)
            .addTag("Periodic work tag")
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<OneTimeWorker>()
            .setConstraints(constraints)  // constraint
            .setInitialDelay(2, TimeUnit.SECONDS)  // delay
            .addTag("One time work tag")  // tag
//            .setInputData(workDataOf( // input data
//                "oneTimeData" to "Welcome to one time work",
//
//            ))
            .setInputData(
                createInputData()
            )
            .setBackoffCriteria(  // retry, back off policy
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS)
            .build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(oneTimeWorkRequest)

        //val workInfos = workManager.getWorkInfosByTagLiveData("One time work tag")

//        workInfos.observe(this){ infos ->
//            val workInfo = infos[1]
//
//            Log.d(TAG, "onCreate: work state ${workInfo.state}")
//            when(workInfo.state){
//                WorkInfo.State.SUCCEEDED ->{
//                    Log.d(TAG, "onCreate: work state succeeded ${workInfo.tags}")
//                }
//                WorkInfo.State.RUNNING ->{
//                    Log.d(TAG, "onCreate: work state running")
//                }
//                else -> {
//                    Log.d(TAG, "onCreate: work state others")
//                }
//            }
//
//        }

        val workInfo = workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)

        workInfo.observe(this){ info ->


            Log.d(TAG, "onCreate: work progress ${info.progress.getInt("progress", 0)}")
            when(info.state){
                WorkInfo.State.SUCCEEDED ->{
                    Toast.makeText(this, "${info.outputData.getString("oneTimeOutputData")}",
                        Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onCreate: work state succeeded ${info.tags}")
                }
                WorkInfo.State.RUNNING ->{
                    Log.d(TAG, "onCreate: work state running")
                }
                WorkInfo.State.ENQUEUED -> {
                    Log.d(TAG, "onCreate: work state enqueued")
                }
                WorkInfo.State.CANCELLED -> {
                    Log.d(TAG, "onCreate: work state cancelled")
                }
                WorkInfo.State.FAILED -> {
                    Toast.makeText(this, "${info.outputData.getString("oneTimeOutputData")}",
                        Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onCreate: work state failed")
                }
                else -> {
                    Log.d(TAG, "onCreate: ${info.state}")
                }


            }

        }

    }

    private fun createInputData(): Data {
        return Data.Builder()
            .putString("oneTimeInputData", "Welcome to one time work")
            .build()
    }


    class PeriodicWorker(context: Context, workerParameters: WorkerParameters):
            Worker(context, workerParameters){

        override fun doWork(): Result {

            Log.d(TAG, "doWork: periodic work called")

            val handler = HandlerCompat.createAsync(Looper.getMainLooper())
            handler.post {
                Toast.makeText(applicationContext, "Periodic work called",
                    Toast.LENGTH_SHORT).show()
            }

            //playMusic()

            return Result.success()
        }

        private fun playMusic(){
            MediaPlayer.create(applicationContext, R.raw.sampleaudio).apply {
                start()
            }
        }
    }

    class OneTimeWorker(context: Context, workerParameters: WorkerParameters):
        Worker(context, workerParameters){

        private val handler = HandlerCompat.createAsync(Looper.getMainLooper())

        override fun doWork(): Result {

            try{
                val data = inputData.getString("oneTimeInputData")

                Log.d(TAG, "doWork: one time work started")

                handler.post {
                    Toast.makeText(applicationContext, "$data started",
                        Toast.LENGTH_SHORT).show()
                }

                setProgressAsync(workDataOf("progress" to 0))

                runBlocking {
                    Log.d(TAG, "doWork: inside run blocking")
                    delay(5000)


                    handler.post {
                        Toast.makeText(applicationContext, "One time work finished", Toast.LENGTH_SHORT).show()
                    }
                    Log.d(TAG, "doWork: one time work finished")

            }

                setProgressAsync(workDataOf("progress" to 100))  // not updating
                //playMusic()


                return Result.success(createOutputData("oneTimeOutputData", "Success data returned"))
            }
            catch (e: Exception){
                return Result.failure(createOutputData("oneTimeOutputData", "Failure data returned"))
            }

        }

        private fun createOutputData(key: String, value: String): Data {
            return Data.Builder()
                .putString(key, value)
                .build()
        }

        override fun onStopped() {
            super.onStopped()

            Log.d(TAG, "doWork: onStopped: called")
            handler.post {
                Toast.makeText(applicationContext, "Background work constraint failed", Toast.LENGTH_SHORT).show()
            }

        }

        
        private fun playMusic(){
            MediaPlayer.create(applicationContext, R.raw.sampleaudio).apply {
                start()
            }
        }

    }


    // Backoff delay specifies the minimum amount of time to wait before retrying your work after the first attempt.
// This value can be no less than 10 seconds (or MIN_BACKOFF_MILLIS).

    //Backoff policy defines how the backoff delay should increase over time for subsequent retry attempts.
// WorkManager supports 2 backoff policies, LINEAR and EXPONENTIAL.


}