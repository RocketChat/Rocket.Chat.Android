package chat.rocket.android.fragment;

abstract class ConstrainedActionManager {
    private boolean mConstrainedMet;
    private boolean mShouldAction;

    public void setConstrainedMet(boolean met) {
        mConstrainedMet = met;
        actioinIfNeeded();
    }
    public void setShouldAction(boolean shouldAction) {
        mShouldAction = shouldAction;
        actioinIfNeeded();
    }

    private void actioinIfNeeded() {
        if(mConstrainedMet && mShouldAction) action();
    }

    abstract protected void action();
}
