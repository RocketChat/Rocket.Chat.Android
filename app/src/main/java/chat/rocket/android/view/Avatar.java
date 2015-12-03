package chat.rocket.android.view;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import chat.rocket.android.Constants;

public class Avatar {
    private String mHost;
    private View mContainer;
    private TextView mTextView;
    private ImageView mImageView;


    private static final int[] COLORS = new int[]{0xFFF44336,0xFFE91E63,0xFF9C27B0,0xFF673AB7,0xFF3F51B5,0xFF2196F3,0xFF03A9F4,0xFF00BCD4,0xFF009688,0xFF4CAF50,0xFF8BC34A,0xFFCDDC39,0xFFFFC107,0xFFFF9800,0xFFFF5722,0xFF795548,0xFF9E9E9E,0xFF607D8B};

    public Avatar(String host, View colorContainer, TextView initialsText, ImageView image) {
        mHost = host;
        mContainer = colorContainer;
        mTextView = initialsText;
        mImageView = image;
    }

    private int getColorForUser(String username){
        return COLORS[username.length()%COLORS.length];
    }

    private String getInitialsForUser(String username){
        String name = username.replaceAll("[^A-Za-z0-9]", ".").replaceAll("\\.+", ".").replaceAll("(^\\.)|(\\.$)", "");
        String[] initials = name.split("\\.");
        if (initials.length>=2) {
            return (firstChar(initials[0]) + firstChar(initials[initials.length - 1])).toUpperCase();
        }
        else{
            String name2 = name.replaceAll("[^A-Za-z0-9]","");
            return (name2.length()<2)? name2 : name2.substring(0,2).toUpperCase();
        }
    }
    private String firstChar(String s) {
        return TextUtils.isEmpty(s)?  "" : s.substring(0,1);
    }

    public void setForUser(String username) {
        String url = null;

        switchToText();

        mContainer.setBackgroundColor(getColorForUser(username));
        mTextView.setText(getInitialsForUser(username));
        mTextView.setTextColor(0xffffffff);

        //from Rocket.Chat:packages/rocketchat-ui/lib/avatar.coffee
        //REMARK! this is often SVG image! (see: Rocket.Chat:server/startup/avatar.coffee)
        try {
            url = "http://"+mHost+"/avatar/"+ URLEncoder.encode(username,"UTF-8")+".jpg";
        } catch (UnsupportedEncodingException e) {
            Log.e(Constants.LOG_TAG,"error",e);
        }

        Picasso.with(mImageView.getContext())
                .load(Uri.parse(url))
                .into(mImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        switchToImage();
                    }

                    @Override
                    public void onError() {
                        switchToText();
                    }
                });
    }

    private void switchToImage(){
        mContainer.setVisibility(View.GONE);
        mImageView.setVisibility(View.VISIBLE);
    }

    private void switchToText(){
        mContainer.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.GONE);
    }

    public void hide(){
        mContainer.setAlpha(0);
        mImageView.setAlpha(0.0f);
    }

    public void show(){
        mContainer.setAlpha(1);
        mImageView.setAlpha(1.0f);
    }
}
