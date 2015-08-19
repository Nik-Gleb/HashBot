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
import android.widget.ListView;

import java.util.Locale;

import ru.touchin.hashbot.HashBotApplication;
import ru.touchin.hashbot.R;

/**
 * Blogs Fragment.
 * For displaying blogs list.
 * Created by Gleb on 18.08.2015.
 */
public class BlogsFrgament extends Fragment implements BlogsAdapter.IBlogsAdapter {

    /** The Blogs Fragment LOG TAG. */
    private static final String LOG_TAG = "BlogsFragment";

    /** The Blogs Fragment ARG TAG. */
    private static final String ARG_TAG = "hash_tag";

    /** The Flag of UI-Available. */
    private boolean isUiAvailable = false;

    /** ListView Widget. */
    private ListView mListView = null;

    /** The Activity Class Name. */
    private String mActivityName = null;

    /** All Blogs by   */
    private CharSequence[] mBlogs = new CharSequence[] {
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

    };

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

        mListView = (ListView) view.findViewById(R.id.list_view);
        mListView.setAdapter(new BlogsAdapter(this));

        isUiAvailable = true;

        invalidateListViewAndBlogs();
    }


    /** Release UI-Widgets references. */
    private final void doneUi() {
        // Done Widgets

        mListView.setAdapter(null);
        mListView = null;

        isUiAvailable = false;

    }

    /** Bind ListView and data    */
    private final void invalidateListViewAndBlogs() {
        if (isUiAvailable)
            ((BaseAdapter)mListView.getAdapter()).notifyDataSetChanged();
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

    /** @return hash tag for search. */
    public final CharSequence getHashTag() {
        return getArguments() != null ? getArguments().getCharSequence(ARG_TAG) : null;
    };
}
