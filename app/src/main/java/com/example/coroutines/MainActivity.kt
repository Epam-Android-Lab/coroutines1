package com.example.coroutines

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutines.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TAG"
    }

    private var formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val exceptionHandler = CoroutineExceptionHandler { ctx, t ->
        log("${t.message}")
    }

    private val mainScope: CoroutineScope = CoroutineScope(SupervisorJob() + exceptionHandler)

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.launchExampleMB.setOnClickListener {
            log("onClick, start")
            launchExample()
            log("onClick, end")
        }

        binding.scopeLaunchExampleMB.setOnClickListener {
            log("onClick, start")
            scopeLaunch()
            log("onClick, end")
        }

        binding.asyncExampleMB.setOnClickListener {
            log("onClick, start")
            asyncExample()
            log("onClick, end")
        }

        binding.joinLaunchExampleMB.setOnClickListener {
            log("onClick, start")
            launchJoin()
            log("onClick, end")
        }

        binding.dispatchersExampleMB.setOnClickListener {
            log("onClick, start")
            dispatchersExample()
            log("onClick, end")
        }

        binding.exceptionsExampleMB.setOnClickListener {
            log("onClick, start")
            exceptionHandlerExample()
            log("onClick, end")
        }

        binding.channelsExampleMB.setOnClickListener {
            log("onClick, start")
            channelsExample()
            log("onClick, end")
        }

        binding.broadcastChannelsExampleMB.setOnClickListener {
            log("onClick, start")
            broadcastExample()
            log("onClick, end")
        }


        binding.actorExampleMB.setOnClickListener {
            log("onClick, start")
            actorExample()
            log("onClick, end")
        }

        binding.produceExampleMB.setOnClickListener {
            log("onClick, start")
            produceExample()
            log("onClick, end")
        }

        binding.flowExampleMB.setOnClickListener {
            log("onClick, start")
            flowExample()
            log("onClick, end")
        }
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }

    private fun launchExample() {
        val job = GlobalScope.launch(
            context = EmptyCoroutineContext,
            start = CoroutineStart.DEFAULT,
            block = {
                log("launchExample start on scope $this")
                download()
                log("launchExample end")
            }
        )
    }

    private fun scopeLaunch() {
        val scope = CoroutineScope(Job())
        scope.launch {
            log("scopeLaunch custom start on scope $this")
            download()
            log("scopeLaunch custom end")
        }
        GlobalScope.launch {
            log("scopeLaunch global start on scope $this")
            download()
            log("scopeLaunch global end")
        }
        scope.cancel()
    }

    private fun launchJoin() {
        GlobalScope.launch {
            log("launch join start")
            val job = launch {
                download()
            }
//            job.join()
            log("launch join end")
        }
    }

    private fun asyncExample() {
        GlobalScope.launch {
            val deferred = async<String> {
                log("async start")
                delay(5000)
                log("async end")
                return@async "async result"
            }

            val result = deferred.await()
            log("received $result")
        }
    }

    private fun dispatchersExample() {
        val scope = CoroutineScope(Job() + Dispatchers.Default)

        repeat(100) {
            scope.launch {
                Thread.sleep(1000)
                log("dispatcher $it")
            }
        }
    }

    private fun exceptionHandlerExample() {
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            log(throwable.message.toString())
        }
        val scope = CoroutineScope(Job())

        scope.launch {
            log("first start")
            delay(1000)
            log("first finish")
        }

        scope.launch {
            log("second start")
            delay(2000)
            log("second finish")
        }
    }

    private fun channelsExample() {
        val channel = Channel<Int>(Channel.Factory.BUFFERED)
        GlobalScope.launch {
            channel.send(1)
            channel.send(2)
            channel.send(3)
            channel.close()
        }

        GlobalScope.launch {
            log(channel.receive().toString())
            log(channel.receive().toString())
            log(channel.receive().toString())

//            for (element in channel) {
//                log(element.toString())
//            }
        }
    }

    private fun broadcastExample() {
        val channel = BroadcastChannel<Int>(1)

        val channelSubscription1 = channel.openSubscription()
        val channelSubscription2 = channel.openSubscription()

        GlobalScope.launch {
            delay(1000)
            channel.send(1)
            channel.send(2)
            channel.send(3)
            channel.send(4)
        }

        GlobalScope.launch {
            for (element in channelSubscription1) {
                log(element.toString())
            }
        }

        GlobalScope.launch {
            for (element in channelSubscription2) {
                log(element.toString())
            }
        }
    }

    private fun produceExample() {
        val channel = GlobalScope.produce<Int> {
            send(1)
            send(2)
            send(3)
            send(4)
            close()
        }

        GlobalScope.launch {
            channel.consumeEach {
                log(it.toString())
            }
        }
    }

    private fun actorExample() {
        val channel = GlobalScope.actor<Int> {
            consumeEach {
                log(it.toString())
            }
        }

        GlobalScope.launch {
            channel.send(1)
            channel.send(2)
            channel.send(3)
            channel.send(4)
            channel.close()
        }
    }

    private fun flowExample() {
        val flow = flow<Int> {
            emit(20)
            emit(30)
            emit(40)
        }

        GlobalScope.launch {
            flow.collect {
                    log(it.toString())
                }
        }
    }

    private fun log(text: String) {
        Log.d(TAG, "${formatter.format(Date())} log: $text [${Thread.currentThread().name}]")
    }

    private suspend fun download(): Int {
        delay(5000)
        return suspendCoroutine {
            log("download done")
            it.resume(30)
        }
    }

    private suspend fun downloadWithBlock(): Int {
        Thread.sleep(5000)
        return suspendCancellableCoroutine {
            log("download done")
            it.resume(30)
        }
    }
}