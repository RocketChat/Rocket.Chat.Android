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
    fun setOnline(callback: TransitionCallback) {
        KeepAliveJob.cancelPendingJobRequests()
        tryTransitionTo(State.ONLINE, callback)
    }

    @Synchronized
    fun setConnecting(callback: TransitionCallback) {
        KeepAliveJob.cancelPendingJobRequests()
        tryTransitionTo(State.CONNECTING, callback)
    }

    @Synchronized
    fun setConnectionError(callback: TransitionCallback) {
        KeepAliveJob.schedule()
        tryTransitionTo(State.OFFLINE, callback)
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