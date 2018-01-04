package chat.rocket.android

import chat.rocket.android.extensions.printStackTraceOnDebug
import chat.rocket.android.service.KeepAliveJob
import unquietcode.tools.esm.EnumStateMachine
import unquietcode.tools.esm.TransitionException

object ConnectionStatusManager {
    enum class State {
        OFFLINE, CONNECTING, ONLINE
    }

    private const val DEBUG = false
    private val DEFAULT_CALLBACK = object : TransitionCallback {
        override fun onTransitioned(success: Boolean) {
        }
    }
    private val stateMachine: EnumStateMachine<State>

    init {
        stateMachine = EnumStateMachine(State.OFFLINE)

        stateMachine.addTransitions(State.OFFLINE, State.CONNECTING)
        stateMachine.addTransitions(State.CONNECTING, State.ONLINE, State.OFFLINE)
        stateMachine.addTransitions(State.ONLINE, State.OFFLINE)
    }

    @Synchronized
    fun transitionCount() = stateMachine.transitionCount()

    @Synchronized
    fun currentState() = stateMachine.currentState()

    @Synchronized
    fun setOnline(callback: TransitionCallback = DEFAULT_CALLBACK) {
        KeepAliveJob.cancelPendingJobRequests()
        tryTransitionTo(State.ONLINE, callback)
    }

    @Synchronized
    fun setOnline() {
        KeepAliveJob.cancelPendingJobRequests()
        tryTransitionTo(State.ONLINE, DEFAULT_CALLBACK)
    }

    @Synchronized
    fun setConnecting(callback: TransitionCallback = DEFAULT_CALLBACK) {
        KeepAliveJob.cancelPendingJobRequests()
        tryTransitionTo(State.CONNECTING, callback)
    }

    @Synchronized
    fun setConnecting() {
        KeepAliveJob.cancelPendingJobRequests()
        tryTransitionTo(State.CONNECTING, DEFAULT_CALLBACK)
    }

    @Synchronized
    fun setConnectionError(callback: TransitionCallback = DEFAULT_CALLBACK) {
        KeepAliveJob.schedule()
        tryTransitionTo(State.OFFLINE, callback)
    }

    @Synchronized
    fun setConnectionError() {
        KeepAliveJob.schedule()
        tryTransitionTo(State.OFFLINE, DEFAULT_CALLBACK)
    }

    @Synchronized
    fun setOffline() {
        stateMachine.reset()
    }

    private fun tryTransitionTo(newState: State, callback: TransitionCallback) {
        try {
            stateMachine.transition(newState)
            callback.onTransitioned(true)
        } catch (e: TransitionException) {
            if (DEBUG) {
                e.printStackTraceOnDebug()
            }
            callback.onTransitioned(false)
        }
    }

    interface TransitionCallback {
        fun onTransitioned(success: Boolean)
    }
}