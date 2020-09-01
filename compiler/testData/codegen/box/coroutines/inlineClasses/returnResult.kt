// WITH_RUNTIME
// WITH_COROUTINES
// IGNORE_BACKEND: JVM_IR

import helpers.*
import kotlin.coroutines.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

var continuation: Continuation<Unit>? = null

suspend fun suspendMe() = suspendCoroutine<Unit> { continuation = it }

@Suppress("RESULT_CLASS_IN_RETURN_TYPE")
suspend fun signInFlowStepFirst(): Result<Unit> = try {
    Result.success(suspendMe())
} catch (e: Exception) {
    Result.failure(e)
}

fun box(): String {
    builder {
        val res: Result<Unit> = signInFlowStepFirst()
        if (res.exceptionOrNull()!!.message != "BOOYA") error("FAIL")
    }
    continuation!!.resumeWithException(Exception("BOOYA"))
    return "OK"
}
