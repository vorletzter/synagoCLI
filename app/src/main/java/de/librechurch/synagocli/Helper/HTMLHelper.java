package de.librechurch.synagocli.Helper;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import org.matrix.androidsdk.rest.callback.ApiCallback;
import org.matrix.androidsdk.rest.model.MatrixError;
import org.matrix.androidsdk.rest.model.URLPreview;
import org.matrix.androidsdk.rest.model.message.Message;
import org.matrix.androidsdk.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLHelper {

    private static final String LOG_TAG = HTMLHelper.class.getSimpleName();


    //================================================================================
    // HTML management
    //================================================================================

    private final Map<String, String> mHtmlMap = new HashMap<>();

    /**
     * Retrieves the sanitised html.
     * !!!!!! WARNING !!!!!!
     * IT IS NOT REMOTELY A COMPREHENSIVE SANITIZER AND SHOULD NOT BE TRUSTED FOR SECURITY PURPOSES.
     * WE ARE EFFECTIVELY RELYING ON THE LIMITED CAPABILITIES OF THE HTML RENDERER UI TO AVOID SECURITY ISSUES LEAKING UP.
     *
     * @param html the html to sanitize
     * @return the sanitised HTML
     */
    @Nullable
    String getSanitisedHtml(final String html) {
        // sanity checks
        if (TextUtils.isEmpty(html)) {
            return null;
        }

        String res = mHtmlMap.get(html);

        if (null == res) {
            res = sanitiseHTML(html);
            mHtmlMap.put(html, res);
        }

        return res;
    }

    private static final Set<String> mAllowedHTMLTags = new HashSet<>(Arrays.asList(
            "font", // custom to matrix for IRC-style font coloring
            "del", // for markdown
            "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "p", "a", "ul", "ol", "sup", "sub",
            "nl", "li", "b", "i", "u", "strong", "em", "strike", "code", "hr", "br", "div",
            "table", "thead", "caption", "tbody", "tr", "th", "td", "pre", "span", "img"));

    private static final Pattern mHtmlPatter = Pattern.compile("<(\\w+)[^>]*>", Pattern.CASE_INSENSITIVE);

    /**
     * Sanitise the HTML.
     * The matrix format does not allow the use some HTML tags.
     *
     * @param htmlString the html string
     * @return the sanitised string.
     */
    private static String sanitiseHTML(final String htmlString) {
        String html = htmlString;
        Matcher matcher = mHtmlPatter.matcher(htmlString);

        Set<String> tagsToRemove = new HashSet<>();

        while (matcher.find()) {

            try {
                String tag = htmlString.substring(matcher.start(1), matcher.end(1));

                // test if the tag is not allowed
                if (!mAllowedHTMLTags.contains(tag)) {
                    tagsToRemove.add(tag);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "sanitiseHTML failed " + e.getLocalizedMessage(), e);
            }
        }

        // some tags to remove ?
        if (!tagsToRemove.isEmpty()) {
            // append the tags to remove
            String tagsToRemoveString = "";

            for (String tag : tagsToRemove) {
                if (!tagsToRemoveString.isEmpty()) {
                    tagsToRemoveString += "|";
                }

                tagsToRemoveString += tag;
            }

            html = html.replaceAll("<\\/?(" + tagsToRemoveString + ")[^>]*>", "");
        }

        return html;
    }

    /*
     * *********************************************************************************************
     *  Url preview managements
     * *********************************************************************************************
     */
    private final Map<String, List<String>> mExtractedUrls = new HashMap<>();
    private final Map<String, URLPreview> mUrlsPreviews = new HashMap<>();
    private final Set<String> mPendingUrls = new HashSet<>();

    /**
     * Retrieves the webUrl extracted from a text
     *
     * @param text the text
     * @return the web urls list
     */
    private List<String> extractWebUrl(String text) {
        List<String> list = mExtractedUrls.get(text);

        if (null == list) {
            list = new ArrayList<>();

            Matcher matcher = android.util.Patterns.WEB_URL.matcher(text);
            while (matcher.find()) {
                try {
                    String value = text.substring(matcher.start(0), matcher.end(0));

                    if (!list.contains(value)) {
                        list.add(value);
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "## extractWebUrl() " + e.getMessage(), e);
                }
            }

            mExtractedUrls.put(text, list);
        }

        return list;
    }

    /*
    void manageURLPreviews(final Message message, final View convertView, final String id) {
        LinearLayout urlsPreviewLayout = convertView.findViewById(R.id.messagesAdapter_urls_preview_list);

        // sanity checks
        if (null == urlsPreviewLayout) {
            return;
        }

        //
        if (TextUtils.isEmpty(message.body)) {
            urlsPreviewLayout.setVisibility(View.GONE);
            return;
        }

        List<String> urls = extractWebUrl(message.body);

        if (urls.isEmpty()) {
            urlsPreviewLayout.setVisibility(View.GONE);
            return;
        }

        // avoid removing items if they are displayed
        if (TextUtils.equals((String) urlsPreviewLayout.getTag(), id)) {
            // all the urls have been displayed
            if (urlsPreviewLayout.getChildCount() == urls.size()) {
                return;
            }
        }

        urlsPreviewLayout.setTag(id);

        // remove url previews
        while (urlsPreviewLayout.getChildCount() > 0) {
            urlsPreviewLayout.removeViewAt(0);
        }

        urlsPreviewLayout.setVisibility(View.VISIBLE);

        for (final String url : urls) {
            final String downloadKey = url.hashCode() + "---";
            String displayKey = url + "<----->" + id;

            if (!mSession.isURLPreviewEnabled()) {
                if (!mUrlsPreviews.containsKey(downloadKey)) {
                    mUrlsPreviews.put(downloadKey, null);
                    mAdapter.notifyDataSetChanged();
                }
            } else if (UrlPreviewView.Companion.didUrlPreviewDismiss(displayKey)) {
                Log.d(LOG_TAG, "## manageURLPreviews() : " + displayKey + " has been dismissed");
            } else if (mPendingUrls.contains(url)) {
                // please wait
            } else if (!mUrlsPreviews.containsKey(downloadKey)) {
                mPendingUrls.add(url);
                mSession.getEventsApiClient().getURLPreview(url, System.currentTimeMillis(), new ApiCallback<URLPreview>() {
                    @Override
                    public void onSuccess(URLPreview urlPreview) {
                        mPendingUrls.remove(url);

                        if (!mUrlsPreviews.containsKey(downloadKey)) {
                            mUrlsPreviews.put(downloadKey, urlPreview);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onNetworkError(Exception e) {
                        onSuccess(null);
                    }

                    @Override
                    public void onMatrixError(MatrixError e) {
                        onSuccess(null);
                    }

                    @Override
                    public void onUnexpectedError(Exception e) {
                        onSuccess(null);
                    }
                });
            } else {
                UrlPreviewView previewView = new UrlPreviewView(mContext);
                previewView.setUrlPreview(mContext, mSession, mUrlsPreviews.get(downloadKey), displayKey);
                urlsPreviewLayout.addView(previewView);
            }
        }
    }
    */
}
