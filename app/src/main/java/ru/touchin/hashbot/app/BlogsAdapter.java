/*
 *	BlogsAdapter.java
 *	HashBot
 *
 *	Copyright © 2015, TouchIn.
 *	All rights reserved.
 */
package ru.touchin.hashbot.app;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ru.touchin.hashbot.R;

/**
 * Blogs Adapter.
 *
 * @author Gleb Nikitenko
 * @version 1.0
 * @since Aug 13, 2015
 */
public final class BlogsAdapter extends BaseAdapter {

    // Container Fragment must implement this interface
    public interface IBlogsAdapter {
        public Context getContext();
        public CharSequence[] getBlogs();
    }

    /** Container Fragment. */
    private final IBlogsAdapter mFragment;

    /**
     * Constructs a new BlogsAdapter with Fragment.
     * @param fragment container fragment.
     */
    public BlogsAdapter(Fragment fragment) {
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mFragment = (IBlogsAdapter) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException("fragment must implement IBlogsAdapter");
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getCount() {
        return (mFragment == null || mFragment.getBlogs() == null) ? 0 : mFragment.getBlogs().length;
    }

    /** {@inheritDoc} */
    @Override
    public CharSequence getItem(int position) {
        return (mFragment == null || mFragment.getBlogs() == null) ? null : mFragment.getBlogs()[position];
    }

    /** {@inheritDoc} */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /** {@inheritDoc} */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = buildView(parent);
        if (convertView != null) bindView(position, convertView);
             
        return convertView;
    }
    
    /**
     * Build new view.
     * @param parent    parent view
     * @return          view
     */
    private final View buildView(ViewGroup parent) {
        final LayoutInflater layoutInflater = LayoutInflater.from(mFragment.getContext());
        final TextView textView = (TextView) layoutInflater.inflate(R.layout.blog_header_item,
                parent, false);
        return textView;
    }

    /**
     * Bind data and widget by position
     * @param position      some position
     * @param convertView   reusable widget
     */
    private final void bindView(int position, View convertView) {
        ((TextView)convertView).setText(getItem(position));
    }

}
