package chat.rocket.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class NavigationLinearLayout extends LinearLayout {
    private int mMaxWidth;

    public NavigationLinearLayout(Context context) {
        super(context);
        setMaxWidth(context);
    }

    public NavigationLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMaxWidth(context);
    }

    public NavigationLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setMaxWidth(context);
    }

    private void setMaxWidth(Context context) {
        mMaxWidth = context.getResources().getDimensionPixelSize(android.support.design.R.dimen.design_navigation_max_width);
    }


    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        switch (MeasureSpec.getMode(widthSpec)) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                widthSpec = MeasureSpec.makeMeasureSpec(
                        Math.min(MeasureSpec.getSize(widthSpec), mMaxWidth), MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.UNSPECIFIED:
                widthSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.EXACTLY);
                break;
        }
        // Let super sort out the height
        super.onMeasure(widthSpec, heightSpec);
    }
}
