package ru.touchin.hashbot.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import ru.touchin.hashbot.HashBotApplication;
import ru.touchin.hashbot.R;
import ru.touchin.hashbot.utils.Utils;

/**
 * Blogs Fragment.
 * For displaying blogs list.
 * Created by Gleb on 18.08.2015.
 */
public class BlogsFrgament extends Fragment implements BlogsAdapter.IBlogsAdapter, View.OnClickListener {

    /** The Blogs Fragment LOG TAG. */
    private static final String LOG_TAG = "BlogsFragment";

    /** The Blogs Fragment ARG TAG. */
    private static final String ARG_TAG = "hash_tag";

    /** State if content successful loaded. */
    private static final int STATE_CONTENT = 0;
    /** State if content loading was failed. */
    private static final int STATE_FAILED = 1;
    /** State if content loading now. */
    private static final int STATE_LOADING = 2;

    /** Current content state. */
    private int mState = STATE_LOADING;

    /** The Flag of UI-Available. */
    private boolean isUiAvailable = false;

    /** ListView Widget. */
    private ListView mListView = null;
    /** Empty Content Widget. */
    private TextView mTextView = null;
    /** Error Message Widget. */
    private LinearLayout mLinearLayout = null;
    /** Retry button. */
    private Button mButton = null;
    /** Progress Bar. */
    private ProgressBar mProgressBar = null;

    /** The Activity Class Name. */
    private String mActivityName = null;

    /** All Blogs by   */
    private CharSequence[] mBlogs = null;/*new CharSequence[] {
            "Item 00",
            "Item 01",
            "Item 02",
            "Item 03",
            "Item 04",
            "Item 05",
            "Item 06",
            "Item 07",
            "Item 08",
            "Item 09",
            "Item 10",
            "Item 11",
            "Item 12",
            "Item 13",
            "Item 14",
            "Item 15",
            "Item 16",
            "Item 17",
            "Item 18",
            "Item 19",
            "Item 20",
            "Item 21"

    };*/

    /** Empty default constructor. */
    public BlogsFrgament(){};

    /* (non-Javadoc)
     * @see android.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivityName  = activity.getClass().getSimpleName();
        log(Log.DEBUG, "onAttach");
        loadBlogs();
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onDetach()
     */
    @Override
    public void onDetach() {
        log(Log.DEBUG, "onDetach");
        mActivityName = null;
        super.onDetach();
    }

    /** {@inheritDoc} */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_blogs, container, false);
        initUi(rootView);
        return rootView;
    }

    /** {@inheritDoc} */
    @Override
    public void onDestroyView() {
        doneUi();
        super.onDestroyView();
    }

    /**
     * UI-Widgets Initialization
     *
     * @param view - root view
     */
    private final void initUi(View view) {
        // Init Widgets

        mLinearLayout = (LinearLayout) view.findViewById(R.id.error_layout);

        mButton = (Button) mLinearLayout.findViewById(R.id.button_retry);
        mButton.setOnClickListener(this);

        mTextView = (TextView) view.findViewById(R.id.text_view_empty);

        mListView = (ListView) view.findViewById(R.id.list_view);
        mListView.setAdapter(new BlogsAdapter(this));

        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        isUiAvailable = true;

        invalidateListViewAndBlogs();

        invalidateProgressBarAndState();
        invalidateErrorLayoutAndState();
        invalidateListViewAndState();
    }


    /** Release UI-Widgets references. */
    private final void doneUi() {
        // Done Widgets

        mProgressBar = null;

        mListView.setAdapter(null);
        mListView = null;

        mTextView = null;

        mButton.setOnClickListener(null);
        mButton = null;

        mLinearLayout = null;

        isUiAvailable = false;

    }

    /** Bind ListView and data    */
    private final void invalidateListViewAndBlogs() {
        if (isUiAvailable) {
            ((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
            mTextView.setVisibility(mBlogs == null || mBlogs.length == 0 ?
                    View.VISIBLE : View.INVISIBLE);
        }
    }

    /** Bind ListView and state. */
    private final void invalidateListViewAndState() {
        if (isUiAvailable)
            mListView.setVisibility(mState == STATE_CONTENT ? View.VISIBLE : View.INVISIBLE);
    }

    /** Bind ProgressBar and state. */
    private final void invalidateProgressBarAndState() {
        if (isUiAvailable) {
            final boolean visibility = mState == STATE_LOADING;
            mProgressBar.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
            if (visibility && mTextView.getVisibility() == View.VISIBLE)
                mTextView.setVisibility(View.INVISIBLE);
        }
    }

    /** Bind Error Message and state. */
    private final void invalidateErrorLayoutAndState() {
        if (isUiAvailable) {
            final boolean visibility = mState == STATE_FAILED;
            mLinearLayout.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
            if (visibility && mTextView.getVisibility() == View.VISIBLE)
                mTextView.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * Log message to LogCat - console
     * @param priority	The priority/type of this log message
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by {@code format},
     *            additional arguments are ignored.
     **/
    @SuppressWarnings("unused")
    private final void log(int priority, String format, Object... args) {
        final String tag = !TextUtils.isEmpty(mActivityName) ?
                mActivityName + " / " + LOG_TAG : LOG_TAG;
        if (HashBotApplication.IS_DEBUG_MODE)
            Log.println(priority, tag, String.format(Locale.getDefault(),
                    format, args));
    }

    /**
     * Log message to LogCat - console
     * @param priority	The priority/type of this log message
     * @param message	The message you would like logged.
     **/
    @SuppressWarnings("unused")
    private final void log(int priority, String message) {
        final String tag = !TextUtils.isEmpty(mActivityName) ?
                mActivityName + " / " + LOG_TAG : LOG_TAG;
        if (HashBotApplication.IS_DEBUG_MODE)
            Log.println(priority, tag, message);
    }

    /**
     * Constructs new Blogs Fragment with specified tag for search.
     * @param tag tag for search
     * @return fragment instance
     */
    public static BlogsFrgament newInstance(CharSequence tag) {
        if (tag == null || TextUtils.isEmpty(tag.toString()))
            throw new IllegalArgumentException();
        final BlogsFrgament blogsFrgament = new BlogsFrgament();
        final Bundle arguments = new Bundle();
        arguments.putString(ARG_TAG, tag.toString());
        blogsFrgament.setArguments(arguments);
        return blogsFrgament;
    }

    /** @return @link #mBlogs. */
    @Override
    public final CharSequence[] getBlogs() {return mBlogs;}

    /** @param blogs to set new @link #mBlogs. */
    private final void setBlogs(CharSequence[] blogs) {
        if (compareBlogs(mBlogs, blogs)) return;
        mBlogs = blogs;
        invalidateListViewAndBlogs();
    }

    /** @param newState fragment new state. */
    private final void setState(int newState) {
        if (mState == newState) return;
        mState = newState;
        invalidateProgressBarAndState();
        invalidateErrorLayoutAndState();
        invalidateListViewAndState();
    }

    /**
     * Compare two blog arrays
     * @param oldBlogs old array
     * @param newBlogs new array
     * @return true if they are equals
     */
    private static final boolean compareBlogs(CharSequence[] oldBlogs, CharSequence[] newBlogs) {
        if (oldBlogs == null && newBlogs == null) return true;

        if((oldBlogs == null && newBlogs != null)
                || oldBlogs != null && newBlogs == null
                || oldBlogs.length != newBlogs.length){
            return false;
        }

        return Arrays.equals(oldBlogs, newBlogs);

    }

    /** @return hash tag for search. */
    public final CharSequence getHashTag() {
        return getArguments() != null ? getArguments().getCharSequence(ARG_TAG) : null;
    };

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_retry:
                loadBlogs();
                break;
            default:
                break;
        }

    }

    /** Request for load new Blogs. */
    private final void loadBlogs() {

        setState(STATE_LOADING);

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                boolean isFailed = false;
                CharSequence[] newBlogs = null;
                try {
                    newBlogs = getRandomBlogs();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isFailed = true;
                }

                final boolean isFailedMainThread = isFailed;
                final CharSequence[] newBlogsMainThread = newBlogs;

                if (getActivity() == null) return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFailedMainThread) {
                            setBlogs(newBlogsMainThread);
                            setState(STATE_CONTENT);
                        } else
                            setState(STATE_FAILED);
                    }
                });

            }
        });

        thread.start();

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               thread.interrupt();
            }
        }, 500);*/
    }

    /** @return emulated twitter - blogs. */
    private final CharSequence[] getRandomBlogs() throws InterruptedException {
        Thread.sleep(2000);
        final Random r = new Random();
        final int count = r.nextInt(99) + 1;
        final CharSequence[] result = new CharSequence[count];
        for (int i = 0; i < count; i++)
            result[i] = Utils.randomString();
        return result;

    }
}
