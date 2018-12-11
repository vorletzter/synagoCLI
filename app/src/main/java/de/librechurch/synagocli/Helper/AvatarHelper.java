package de.librechurch.synagocli.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomPreviewData;
import org.matrix.androidsdk.db.MXMediasCache;
import org.matrix.androidsdk.rest.model.RoomMember;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.rest.model.group.Group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.librechurch.synagocli.R;

public class AvatarHelper {

    // the background thread
    private static HandlerThread mImagesThread = null;
    private static android.os.Handler mImagesThreadHandler = null;
    private static Handler mUIHandler = null;


    //==============================================================================================================
    // Avatars generation
    // (taken from the Original Riot-Vector)
    //==============================================================================================================

    // avatars cache
    static final private LruCache<String, Bitmap> mAvatarImageByKeyDict = new LruCache<>(20 * 1024 * 1024);
    // the avatars background color
    static final private List<Integer> mColorList = new ArrayList<>(Arrays.asList(0xff76cfa6, 0xff50e2c2, 0xfff4c371));

    /**
     * Provides the avatar background color from a text.
     *
     * @param text the text.
     * @return the color.
     */
    public static int getAvatarColor(String text) {
        long colorIndex = 0;

        if (!TextUtils.isEmpty(text)) {
            long sum = 0;

            for (int i = 0; i < text.length(); i++) {
                sum += text.charAt(i);
            }

            colorIndex = sum % mColorList.size();
        }

        return mColorList.get((int) colorIndex);
    }

    /**
     * Create a thumbnail avatar.
     *
     * @param context         the context
     * @param backgroundColor the background color
     * @param text            the text to display.
     * @return the generated bitmap
     */
    private static Bitmap createAvatarThumbnail(Context context, int backgroundColor, String text) {
        float densityScale = context.getResources().getDisplayMetrics().density;
        // the avatar size is 42dp, convert it in pixels.
        return createAvatar(backgroundColor, text, (int) (42 * densityScale));
    }

    /**
     * Create an avatar bitmap from a text.
     *
     * @param backgroundColor the background color.
     * @param text            the text to display.
     * @param pixelsSide      the avatar side in pixels
     * @return the generated bitmap
     */
    private static Bitmap createAvatar(int backgroundColor, String text, int pixelsSide) {
        android.graphics.Bitmap.Config bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;

        Bitmap bitmap = Bitmap.createBitmap(pixelsSide, pixelsSide, bitmapConfig);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(backgroundColor);

        // prepare the text drawing
        Paint textPaint = new Paint();
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(Color.WHITE);
        // the text size is proportional to the avatar size.
        // by default, the avatar size is 42dp, the text size is 28 dp (not sp because it has to be fixed).
        textPaint.setTextSize(pixelsSide * 2 / 3);

        // get its size
        Rect textBounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textBounds);

        // draw the text in center
        canvas.drawText(text,
                (canvas.getWidth() - textBounds.width() - textBounds.left) / 2,
                (canvas.getHeight() + textBounds.height() - textBounds.bottom) / 2,
                textPaint);

        // Return the avatar
        return bitmap;
    }

    /**
     * Return the char to display for a name
     *
     * @param name the name
     * @return teh first char
     */
    private static String getInitialLetter(String name) {
        String firstChar = " ";

        if (!TextUtils.isEmpty(name)) {
            int idx = 0;
            char initial = name.charAt(idx);

            if ((initial == '@' || initial == '#' || initial == '+') && (name.length() > 1)) {
                idx++;
            }

            // string.codePointAt(0) would do this, but that isn't supported by
            // some browsers (notably PhantomJS).
            int chars = 1;
            char first = name.charAt(idx);

            // LEFT-TO-RIGHT MARK
            if ((name.length() >= 2) && (0x200e == first)) {
                idx++;
                first = name.charAt(idx);
            }

            // check if it’s the start of a surrogate pair
            if (0xD800 <= first && first <= 0xDBFF && (name.length() > (idx + 1))) {
                char second = name.charAt(idx + 1);
                if (0xDC00 <= second && second <= 0xDFFF) {
                    chars++;
                }
            }

            firstChar = name.substring(idx, idx + chars);
        }

        return firstChar.toUpperCase();
        //return firstChar.toUpperCase(VectorLocale.INSTANCE.getApplicationLocale());
    }

    /**
     * Returns an avatar from a text.
     *
     * @param context the context.
     * @param aText   the text.
     * @param create  create the avatar if it does not exist
     * @return the avatar.
     */
    public static Bitmap getAvatar(Context context, int backgroundColor, String aText, boolean create) {
        String firstChar = getInitialLetter(aText);
        String key = firstChar + "_" + backgroundColor;

        // check if the avatar is already defined
        Bitmap thumbnail = mAvatarImageByKeyDict.get(key);

        if ((null == thumbnail) && create) {
            thumbnail = AvatarHelper.createAvatarThumbnail(context, backgroundColor, firstChar);
            mAvatarImageByKeyDict.put(key, thumbnail);
        }

        return thumbnail;
    }

    /**
     * Set the default vector avatar for a member.
     *
     * @param imageView   the imageView to set.
     * @param userId      the member userId.
     * @param displayName the member display name.
     */
    private static void setDefaultMemberAvatar(final ImageView imageView, final String userId, final String displayName) {
        // sanity checks
        if (null != imageView && !TextUtils.isEmpty(userId)) {
            final Bitmap bitmap = AvatarHelper.getAvatar(imageView.getContext(),
                    AvatarHelper.getAvatarColor(userId), TextUtils.isEmpty(displayName) ? userId : displayName, true);

            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                imageView.setImageBitmap(bitmap);
            } else {
                final String tag = userId + " - " + displayName;
                imageView.setTag(tag);

                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.equals(tag, (String) imageView.getTag())) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        }
    }

    /**
     * Set the room avatar in an imageView.
     *
     * @param context   the context
     * @param session   the session
     * @param imageView the image view
     * @param room      the room
     */
    public static void loadRoomAvatar(Context context, MXSession session, ImageView imageView, Room room) {
        if (null != room) {
            AvatarHelper.loadUserAvatar(context,
                    session, imageView, room.getAvatarUrl(), room.getRoomId(), room.getRoomDisplayName(context));
        }
    }

    /**
     * Set the room avatar in an imageView by consider the room preview data.
     *
     * @param context         the context
     * @param session         the session
     * @param imageView       the image view
     * @param roomPreviewData the room preview
     */
    public static void loadRoomAvatar(Context context, MXSession session, ImageView imageView, RoomPreviewData roomPreviewData) {
        if (null != roomPreviewData) {
            AvatarHelper.loadUserAvatar(context,
                    session, imageView, roomPreviewData.getRoomAvatarUrl(), roomPreviewData.getRoomId(), roomPreviewData.getRoomName());
        }
    }

    /**
     * Set the group avatar in an imageView.
     *
     * @param context   the context
     * @param session   the session
     * @param imageView the image view
     * @param group     the group
     */
    public static void loadGroupAvatar(Context context, MXSession session, ImageView imageView, Group group) {
        if (null != group) {
            AvatarHelper.loadUserAvatar(context,
                    session, imageView, group.getAvatarUrl(), group.getGroupId(), group.getDisplayName());
        }
    }

    /**
     * Set the call avatar in an imageView.
     *
     * @param context   the context
     * @param session   the session
     * @param imageView the image view
     * @param room      the room
     */
    public static void loadCallAvatar(Context context, MXSession session, ImageView imageView, Room room) {
        // sanity check
        if ((null != room) && (null != session) && (null != imageView) && session.isAlive()) {
            // reset the imageView tag
            imageView.setTag(null);

            String callAvatarUrl = room.getCallAvatarUrl();
            String roomId = room.getRoomId();
            String displayName = room.getRoomDisplayName(context);
            int pixelsSide = imageView.getLayoutParams().width;

            // when size < 0, it means that the render graph must compute it
            // so, we search the valid parent view with valid size
            if (pixelsSide < 0) {
                ViewParent parent = imageView.getParent();

                while ((pixelsSide < 0) && (null != parent)) {
                    if (parent instanceof View) {
                        View parentAsView = (View) parent;
                        pixelsSide = parentAsView.getLayoutParams().width;
                    }
                    parent = parent.getParent();
                }
            }

            // if the avatar is already cached, use it
            if (session.getMediasCache().isAvatarThumbnailCached(callAvatarUrl, context.getResources().getDimensionPixelSize(R.dimen.profile_avatar_size))) {
                session.getMediasCache().loadAvatarThumbnail(session.getHomeServerConfig(),
                        imageView, callAvatarUrl, context.getResources().getDimensionPixelSize(R.dimen.profile_avatar_size));
            } else {
                Bitmap bitmap = null;

                if (pixelsSide > 0) {
                    // get the avatar bitmap.
                    bitmap = AvatarHelper.createAvatar(AvatarHelper.getAvatarColor(roomId), getInitialLetter(displayName), pixelsSide);
                }

                // until the dedicated avatar is loaded.
                session.getMediasCache().loadAvatarThumbnail(session.getHomeServerConfig(),
                        imageView, callAvatarUrl, context.getResources().getDimensionPixelSize(R.dimen.profile_avatar_size), bitmap);
            }
        }
    }

    /**
     * Set the room member avatar in an imageView.
     *
     * @param context    the context
     * @param session    the session
     * @param imageView  the image view
     * @param roomMember the room member
     */
    public static void loadRoomMemberAvatar(Context context, MXSession session, ImageView imageView, RoomMember roomMember) {
        if (null != roomMember) {
            AvatarHelper.loadUserAvatar(context, session, imageView, roomMember.getAvatarUrl(), roomMember.getUserId(), roomMember.displayname);
        }
    }

    /**
     * Set the user avatar in an imageView.
     *
     * @param context   the context
     * @param session   the session
     * @param imageView the image view
     * @param user      the user
     */
    public static void loadUserAvatar(Context context, MXSession session, ImageView imageView, User user) {
        if (null != user) {
            AvatarHelper.loadUserAvatar(context, session, imageView, user.getAvatarUrl(), user.user_id, user.displayname);
        }
    }

    /**
     * Set the user avatar in an imageView.
     *
     * @param context     the context
     * @param session     the session
     * @param imageView   the image view
     * @param avatarUrl   the avatar url
     * @param userId      the user id
     * @param displayName the user display name
     */
    public static void loadUserAvatar(final Context context,
                                      final MXSession session,
                                      final ImageView imageView,
                                      final String avatarUrl,
                                      final String userId,
                                      final String displayName) {
        // sanity check
        if ((null == session) || (null == imageView) || !session.isAlive()) {
            return;
        }

        // reset the imageView tag
        imageView.setTag(null);

        if (session.getMediasCache().isAvatarThumbnailCached(avatarUrl, context.getResources().getDimensionPixelSize(R.dimen.profile_avatar_size))) {
            session.getMediasCache().loadAvatarThumbnail(session.getHomeServerConfig(),
                    imageView, avatarUrl, context.getResources().getDimensionPixelSize(R.dimen.profile_avatar_size));
        } else { // if not set
            if (null == mImagesThread) {
                mImagesThread = new HandlerThread("ImagesThread", Thread.MIN_PRIORITY);
                mImagesThread.start();
                mImagesThreadHandler = new android.os.Handler(mImagesThread.getLooper());
                mUIHandler = new Handler(Looper.getMainLooper());
            }

            final Bitmap bitmap = AvatarHelper.getAvatar(imageView.getContext(),
                    AvatarHelper.getAvatarColor(userId), TextUtils.isEmpty(displayName) ? userId : displayName, false);

            // test if the default avatar has been computed
            if (null != bitmap) {
                imageView.setImageBitmap(bitmap);

                if (!TextUtils.isEmpty(avatarUrl)) {
                    final String tag = avatarUrl + userId + displayName;
                    imageView.setTag(tag);

                    if (!MXMediasCache.isMediaUrlUnreachable(avatarUrl)) {
                        mImagesThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (TextUtils.equals(tag, (String) imageView.getTag())) {
                                    session.getMediasCache().loadAvatarThumbnail(session.getHomeServerConfig(),
                                            imageView, avatarUrl, context.getResources().getDimensionPixelSize(R.dimen.profile_avatar_size), bitmap);
                                }
                            }
                        });
                    }
                }
            } else {
                final String tmpTag0 = "00" + avatarUrl + "-" + userId + "--" + displayName;
                imageView.setTag(tmpTag0);

                // create the default avatar in the background thread
                mImagesThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.equals(tmpTag0, (String) imageView.getTag())) {
                            imageView.setTag(null);
                            setDefaultMemberAvatar(imageView, userId, displayName);

                            if (!TextUtils.isEmpty(avatarUrl) && !MXMediasCache.isMediaUrlUnreachable(avatarUrl)) {
                                final String tmpTag1 = "11" + avatarUrl + "-" + userId + "--" + displayName;
                                imageView.setTag(tmpTag1);

                                // wait that it is rendered to load the right one
                                mUIHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // test if the imageView tag has not been updated
                                        if (TextUtils.equals(tmpTag1, (String) imageView.getTag())) {
                                            final String tmptag2 = "22" + avatarUrl + userId + displayName;
                                            imageView.setTag(tmptag2);

                                            mImagesThreadHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // test if the imageView tag has not been updated
                                                    if (TextUtils.equals(tmptag2, (String) imageView.getTag())) {
                                                        final Bitmap bitmap = AvatarHelper.getAvatar(imageView.getContext(),
                                                                AvatarHelper.getAvatarColor(userId),
                                                                TextUtils.isEmpty(displayName) ? userId : displayName,
                                                                false);
                                                        session.getMediasCache().loadAvatarThumbnail(session.getHomeServerConfig(),
                                                                imageView,
                                                                avatarUrl,
                                                                context.getResources().getDimensionPixelSize(R.dimen.profile_avatar_size),
                                                                bitmap);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }
    }

}
