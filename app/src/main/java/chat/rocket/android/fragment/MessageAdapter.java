package chat.rocket.android.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emojione.Emojione;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;

import chat.rocket.android.Constants;
import chat.rocket.android.DateTime;
import chat.rocket.android.R;
import chat.rocket.android.api.OkHttpHelper;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.User;
import chat.rocket.android.view.CursorRecyclerViewAdapter;
import chat.rocket.android.view.InlineHightlighter;
import chat.rocket.android.view.Linkify;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*package*/ class MessageAdapter extends CursorRecyclerViewAdapter<MessageViewHolder> {
    private static final int DUMMY_HEADER = 100;
    private static final int DUMMY_FOOTER = 101;

    private static final HashSet<String> sInlineViewSupportedMime = new HashSet<String>(){
        {
            add("image/png");
            add("image/jpg");
            add("image/jpeg");
            add("image/webp");
        }
    };


    private final LayoutInflater mInflater;
    private final String mHost;
    private final String mUserId;
    private final String mToken;
    private int mHeaderHeight;
    private boolean mHasMore;

    private Interceptor mInterceptor;
    private Interceptor getInterceptor() {
        if(mInterceptor==null) {
            mInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    // uid/token is required to download attachment files.
                    // see: RocketChat:lib/fileUpload.coffee
                    Request newRequest = chain.request().newBuilder()
                            .header("Cookie", "rc_uid="+mUserId+";rc_token="+mToken)
                            .build();
                    return chain.proceed(newRequest);
                }
            };
        }
        return mInterceptor;
    }


    public MessageAdapter(Context context, Cursor cursor, String host, String userId, String token) {
        super(context, cursor);
        mInflater = LayoutInflater.from(context);
        mHost = host;
        mUserId = userId;
        mToken = token;
    }

    public int getBasicItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public int getItemCount() {
        return getBasicItemCount() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return DUMMY_HEADER;
        }
        else if (position == getItemCount()-1) {
            return DUMMY_FOOTER;
        }
        return super.getItemViewType(position);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==DUMMY_HEADER) {
            final View v = new View(parent.getContext());
            v.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,mHeaderHeight));
            return new MessageViewHolder(v, mHost);
        }
        else if(viewType==DUMMY_FOOTER) {
            return new MessageViewHolder(mInflater.inflate(R.layout.listitem_start_of_conversation, parent, false), mHost);
        }
        return new MessageViewHolder(mInflater.inflate(R.layout.listitem_message, parent, false),mHost);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int position) {
        int type = getItemViewType(position);
        if (type == DUMMY_HEADER) {
            if(viewHolder.itemView.getHeight()!=mHeaderHeight) {
                viewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeaderHeight));
            }
            return;
        }
        else if(type == DUMMY_FOOTER) {
            viewHolder.itemView.setVisibility(mHasMore? View.GONE : View.VISIBLE);
            return;
        }

        super.onBindViewHolder(viewHolder, position-1);
    }

    @Override
    public void bindView(MessageViewHolder viewHolder, Context context, int position, Cursor cursor) {
        final Message m = Message.createFromCursor(cursor);

        Message nextM = null;
        if(position < getBasicItemCount()-1) {
            if(cursor.moveToPosition(position+1)){
                nextM = Message.createFromCursor(cursor);
            }
        }
        setSequentialOrNewDateIfNeeded(viewHolder, context, nextM, m);

        User u = RocketChatDatabaseHelper.read(context, new RocketChatDatabaseHelper.DBCallback<User>() {
            @Override
            public User process(SQLiteDatabase db) throws JSONException {
                return User.getById(db, m.userId);
            }
        });

        if(u!=null) viewHolder.avatar.setForUser(u.name);

        //see Rocket.Chat:packages/rocketchat-lib/client/MessageTypes.coffee
        String username = (u!=null)? u.getDisplayName() : "unknown user";
        boolean systemMsg = true;
        switch (m.type) {
            case USER_JOINED:
                viewHolder.content.setText("(has joined the channel)");
                break;
            case USER_LEFT:
                viewHolder.content.setText("(has left the channel)");
                break;

            case USER_ADDED:
                viewHolder.content.setText(String.format("(User %s added by %s)",m.content,username));
                break;

            case USER_REMOVED:
                viewHolder.content.setText(String.format("(User %s removed by %s)",m.content,username));
                break;

            case ROOM_NAME_CHANGED:
                viewHolder.content.setText(String.format("(Room name changed to: %s by %s)",m.content,username));
                break;

            case MESSAGE_REMOVED:
                viewHolder.content.setText("(message removed)");
                break;

            case WELCOME:
                viewHolder.content.setText(String.format("Welcome %s!",username));
                break;


            case UNSPECIFIED:
            default:
                systemMsg = false;
        }
        if(systemMsg) {
            viewHolder.disableContentContainer();
            viewHolder.content.setEnabled(false);
        }
        else {
            viewHolder.content.setTypeface(null, Typeface.NORMAL);
            if(TextUtils.isEmpty(m.content)) {
                viewHolder.disableContentContainer();
                viewHolder.content.setVisibility(View.GONE);
            }
            else if(m.content.contains("```")) {
                // TODO: not completely implemented.
                // see: RocketChat:packages/rocketchat-highlight/highlight.coffee
                
                viewHolder.enableContentContainer();
                boolean highlight = false;
                int pad = context.getResources().getDimensionPixelSize(R.dimen.line_half);
                for(String token: TextUtils.split(m.content, "```")){
                    TextView txt = new TextView(context);
                    txt.setTextIsSelectable(true);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(viewHolder.content.getLayoutParams());
                    if(highlight) {
                        txt.setText(token.trim());
                        Linkify.markupSync(txt);
                        txt.setTextColor(context.getResources().getColor(R.color.highlight_text_color));
                        txt.setBackgroundResource(R.drawable.highlight_background);
                        txt.setPadding(pad,pad,pad,pad);
                        params.setMargins(0,pad/2,pad,pad/2);
                        txt.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                    }
                    else {
                        if (TextUtils.isEmpty(token.trim())) {
                            highlight = !highlight;
                            continue;
                        }
                        txt.setText(Emojione.shortnameToUnicode(token.trim(), false));
                        txt.setTextColor(viewHolder.content.getCurrentTextColor());
                        Linkify.markupSync(txt);
                        InlineHightlighter.highlight(txt);
                    }
                    txt.setLayoutParams(params);
                    viewHolder.contentContainer.addView(txt);

                    highlight = !highlight;
                }
            }
            else {
                viewHolder.disableContentContainer();
                viewHolder.content.setText(Emojione.shortnameToUnicode(m.content, false));
                Linkify.markupSync(viewHolder.content);
                InlineHightlighter.highlight(viewHolder.content);
                viewHolder.content.setEnabled(true);
            }
        }

        if(u!=null) viewHolder.username.setText(u.getDisplayName());
        viewHolder.timestamp.setText(DateTime.fromEpocMs(m.timestamp, DateTime.Format.TIME));

        viewHolder.inlineContainer.removeAllViews();

        if(!TextUtils.isEmpty(m.urls)) {
            try {
                JSONArray urls = new JSONArray(m.urls);
                for (int i = 0; i < urls.length(); i++) {
                    insertUrl(viewHolder, urls.getJSONObject(i));
                }
            }
            catch (Exception e) {
                Log.e(Constants.LOG_TAG, "error", e);
            }
        }
        if(!TextUtils.isEmpty(m.attachments)) {
            try {
                JSONArray urls = new JSONArray(m.attachments);
                for (int i = 0; i < urls.length(); i++) {
                    insertAttachment(viewHolder, urls.getJSONObject(i));
                }
            }
            catch (Exception e) {
                Log.e(Constants.LOG_TAG, "error", e);
            }
        }

    }

    private void setSequentialOrNewDateIfNeeded(MessageViewHolder viewHolder, Context context, @Nullable Message nextM, Message m) {
        //see Rocket.Chat:packages/rocketchat-livechat/app/client/views/message.coffee
        if(nextM==null || !DateTime.fromEpocMs(nextM.timestamp, DateTime.Format.DATE)
                .equals(DateTime.fromEpocMs(m.timestamp, DateTime.Format.DATE))) {
            setNewDay(viewHolder, DateTime.fromEpocMs(m.timestamp, DateTime.Format.DATE));
            setSequential(viewHolder, false);
        }
        else if(!m.isGroupable() || !nextM.isGroupable() || !nextM.userId.equals(m.userId)) {
            setNewDay(viewHolder, null);
            setSequential(viewHolder, false);
        }
        else{
            setNewDay(viewHolder, null);
            setSequential(viewHolder, true);
        }


    }

    private void setSequential(MessageViewHolder viewHolder, boolean sequential) {
        if(sequential){
            viewHolder.avatar.hide();
            viewHolder.usertimeContainer.setVisibility(View.GONE);
        }
        else{
            viewHolder.avatar.show();
            viewHolder.usertimeContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setNewDay(MessageViewHolder viewHolder, @Nullable String newDayText) {
        if(TextUtils.isEmpty(newDayText)) viewHolder.newDayContainer.setVisibility(View.GONE);
        else {
            viewHolder.newDayText.setText(newDayText);
            viewHolder.newDayContainer.setVisibility(View.VISIBLE);
        }
    }

    private void insertUrl(MessageViewHolder viewHolder, JSONObject urlObj) throws JSONException {
        if (urlObj.isNull("headers")) return;
        JSONObject header = urlObj.getJSONObject("headers");
        final String url = urlObj.getString("url");

        if (header.isNull("contentType")) return;
        String contentType = header.getString("contentType");

        final Context context = viewHolder.itemView.getContext();
        if (contentType.startsWith("image/") && sInlineViewSupportedMime.contains(contentType)) {
            View v = LayoutInflater.from(context).inflate(R.layout.listitem_inline_image,viewHolder.inlineContainer,false);
            ImageView img = (ImageView) v.findViewById(R.id.list_item_inline_image);
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.image_dummy)
                    .error(R.drawable.image_error)
                    .into(img);
            viewHolder.inlineContainer.addView(v);
        }

        // see Rocket.Chat:packages/rocketchat-oembed/client/oembedUrlWidget.coffee
        if (!urlObj.isNull("meta")) {
            JSONObject meta =urlObj.getJSONObject("meta");

            String title = null;
            if(!meta.isNull("ogTitle")) title = meta.getString("ogTitle");
            else if(!meta.isNull("twitterTitle")) title = meta.getString("twitterTitle");
            else if(!meta.isNull("pageTitle")) title = meta.getString("pageTitle");

            String description = null;
            if(!meta.isNull("ogDescription")) description = meta.getString("ogDescription");
            else if(!meta.isNull("twitterDescription")) description = meta.getString("twitterDescription");
            else if(!meta.isNull("description")) description = meta.getString("description");

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) return;

            if (description.startsWith("\"")) description = description.substring(1);
            if (description.endsWith("\"")) description = description.substring(0, description.length()-1);

            String imageURL = null;
            if(!meta.isNull("ogImage")) imageURL = meta.getString("ogImage");
            else if(!meta.isNull("twitterImage")) imageURL = meta.getString("twitterImage");

            String host = urlObj.getJSONObject("parsedUrl").getString("host");

            View v = LayoutInflater.from(context).inflate(R.layout.listitem_inline_embed_url,viewHolder.inlineContainer,false);

            ((TextView) v.findViewById(R.id.inline_embed_url_host)).setText(host);
            ((TextView) v.findViewById(R.id.inline_embed_url_title)).setText(title);
            ((TextView) v.findViewById(R.id.inline_embed_url_description)).setText(description);


            ImageView img = (ImageView) v.findViewById(R.id.inline_embed_url_image);
            if(TextUtils.isEmpty(imageURL)) img.setVisibility(View.GONE);
            else {
                Picasso.with(context)
                        .load(imageURL)
                        .placeholder(R.drawable.image_dummy)
                        .error(R.drawable.image_error)
                        .into(img);
                img.setVisibility(View.VISIBLE);
            }

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });

            viewHolder.inlineContainer.addView(v);
        }
    }

    private String absolutize(String url) {
        if (url.startsWith("/")) {
            return "https://"+mHost+url;
        }
        else return url;
    }

    private void insertAttachment(MessageViewHolder viewHolder, final JSONObject attachmentObj) throws JSONException {
        final Context context = viewHolder.itemView.getContext();
        //see: RocketChat:packages/rocketchat-message-attachments/client/messageAttachment.html

        boolean add = false;
        View v = LayoutInflater.from(context).inflate(R.layout.listitem_inline_attachment,viewHolder.inlineContainer,false);

        TextView titleText = ((TextView) v.findViewById(R.id.inline_attachment_title));
        if (!attachmentObj.isNull("title")) {
            String title = attachmentObj.getString("title");
            titleText.setText(title);
            titleText.setVisibility(View.VISIBLE);

            if (!attachmentObj.isNull("title_link")) {
                final String link = attachmentObj.getString("title_link");
                TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
                v.setBackgroundResource(outValue.resourceId);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(absolutize(link)));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
            }
        }
        else titleText.setVisibility(View.GONE);

        ImageView img = (ImageView) v.findViewById(R.id.inline_attachment_image);
        if (!attachmentObj.isNull("image_url")) {
            String imageURL = attachmentObj.getString("image_url");
            String imageType = attachmentObj.getString("image_type");

            if (imageType.startsWith("image/") && sInlineViewSupportedMime.contains(imageType)) {

                if (TextUtils.isEmpty(imageURL)) img.setVisibility(View.GONE);
                else {
                    OkHttpClient client = OkHttpHelper.getClient();
                    if(!client.interceptors().contains(getInterceptor())) {
                        client.interceptors().add(getInterceptor());
                    }
                    new Picasso.Builder(context)
                            .downloader(new OkHttp3Downloader(client))
                            .build()
                            .load(absolutize(imageURL))
                            .placeholder(R.drawable.image_dummy)
                            .error(R.drawable.image_error)
                            .into(img);
                    img.setVisibility(View.VISIBLE);
                    add = true;
                }
            }
        }
        else img.setVisibility(View.GONE);

        if (add) viewHolder.inlineContainer.addView(v);
    }

    public void setHeaderHeight(int height){
        mHeaderHeight = height;
        notifyItemChanged(0);
    }

    public void setHasMore(boolean hasMore) {
        mHasMore = hasMore;
        //notifyItemChanged(getItemCount()-1);
    }
}

