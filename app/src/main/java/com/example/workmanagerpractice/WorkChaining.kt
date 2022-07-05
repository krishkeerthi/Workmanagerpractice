package com.example.workmanagerpractice

import android.content.ContentValues
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.os.HandlerCompat
import androidx.work.*
import com.example.workmanagerpractice.databinding.ActivityWorkChainingBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class WorkChaining : AppCompatActivity() {
    private lateinit var binding: ActivityWorkChainingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkChainingBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val requestOne = OneTimeWorkRequestBuilder<WorkRequestOne>()
            //.setInputMerger(OverwritingInputMerger::class.java) this is default
            .setInputData(workDataOf(
                "requestOneKeyOne" to "input to first worker from work request"
            ))
            .build()

        val requestOneB = OneTimeWorkRequest.from(WorkRequestOneB::class.java)


        val requestTwo = OneTimeWorkRequestBuilder<WorkRequestTwo>()
            .setInputMerger(ArrayCreatingInputMerger::class.java)
            .build()

        val requestThree = OneTimeWorkRequest.from(WorkRequestThree::class.java)


        val workManager = WorkManager.getInstance(this)
        workManager.beginWith(listOf(requestOne, requestOneB))
            .then(requestTwo)
            .then(requestThree)
            .enqueue()

        // Note: I expected three toast to happen separately but all three are toasted simultaneously

    }

    class WorkRequestOne(context: Context, workerParameters: WorkerParameters):
        Worker(context, workerParameters){

        private val handler = HandlerCompat.createAsync(Looper.getMainLooper())

        override fun doWork(): Result {

            val data = inputData.getString("requestOneKeyOne")

            Log.d(ContentValues.TAG, "doWork: one time work request one started \n" +
                    " $data")

            handler.post {
                Toast.makeText(applicationContext, " started1 data: $data",
                    Toast.LENGTH_SHORT).show()
            }
            return Result.success(Data.Builder()
                .putString("requestTwoKeyOne", "input to second worker from first worker A")
                .build())
        }

        }

    class WorkRequestOneB(context: Context, workerParameters: WorkerParameters):
        Worker(context, workerParameters){

        private val handler = HandlerCompat.createAsync(Looper.getMainLooper())

        override fun doWork(): Result {


            Log.d(ContentValues.TAG, "doWork: one time work request one started B")

            handler.post {
                Toast.makeText(applicationContext, " started1B data: ",
                    Toast.LENGTH_SHORT).show()
            }
            return Result.success(Data.Builder()
                .putString("requestTwoKeyOne", "input to second worker from first worker B")
                .build())
        }

    }

    class WorkRequestTwo(context: Context, workerParameters: WorkerParameters):
        Worker(context, workerParameters){

        private val handler = HandlerCompat.createAsync(Looper.getMainLooper())

        override fun doWork(): Result {

            val data = inputData.getStringArray("requestTwoKeyOne")

            Log.d(ContentValues.TAG, "doWork: one time work request two started ${data?.size} " +
                    "data: ${data?.toList()}")

            handler.post {
                Toast.makeText(applicationContext, " started2 data: $data",
                    Toast.LENGTH_SHORT).show()
            }
            return Result.success(
                workDataOf(
                    "requestThreeKeyOne" to "input to third worker from second worker 1",
                    "requestThreeKeyTwo" to "input to third worker from second worker 2"
                // "requestThreeKeyOne" key is used again, then latest value alone is chosen
                )
            )
        }

    }

    class WorkRequestThree(context: Context, workerParameters: WorkerParameters):
        Worker(context, workerParameters){

        private val handler = HandlerCompat.createAsync(Looper.getMainLooper())

        override fun doWork(): Result {

            val data1 = inputData.getString("requestThreeKeyOne")
            val data2 = inputData.getString("requestThreeKeyTwo")
            val data3 = inputData.getString("requestTwoKeyOne")  // only data from previous worker will be available
            // to this worker

            Log.d(ContentValues.TAG, "doWork: one time work request three started \n $data1 \n $data2 \n $data3")

            handler.post {
                Toast.makeText(applicationContext, " started3 data: $data1",
                    Toast.LENGTH_SHORT).show()
            }
            return Result.success()
        }

    }
}