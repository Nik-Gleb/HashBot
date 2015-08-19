/*
 *  MainActivity.java
 *
 *  Copyright © 2015, TouchIn.
 *  All rights reserved.
 */
package ru.touchin.hashbot.app;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ru.touchin.hashbot.HashBotApplication;
import ru.touchin.hashbot.R;
import ru.touchin.hashbot.utils.Utils;

/**
 * @author Gleb Nikitenko
 * @version 1.0
 * @since Aug 18, 2015
 */
public class MainActivity extends AppCompatActivity {
    
    /** The activity root view. */
    private View rootView = null;

    /** ViewPager Widget. */
    private ViewPager mViewPager = null;
    /** Tab Layout Widget. */
    private TabLayout mTabLayout = null;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (HashBotApplication.IS_DEBUG_MODE)
            rootView = getWindow().getDecorView()
            .findViewById(android.R.id.content);

        mViewPager = (ViewPager)findViewById(R.id.view_pager);
        mViewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }
    
    /** {@inheritDoc} */
    @Override
    protected final void onDestroy() {

        mTabLayout = null;

        mViewPager.setAdapter(null);
        mViewPager = null;

        if (rootView != null) {
            Utils.unbindReferences(rootView);
            rootView = null;
            System.gc();
            System.runFinalization();
            System.gc();
        }
        super.onDestroy();
    }

    /** Fragments Adapter for managing view pager. */
    private static final class FragmentsAdapter extends FragmentStatePagerAdapter {

        /** All Pages (in this implementation hardcoded). */
        private final BlogsFrgament[] mItems = new BlogsFrgament[] {
                BlogsFrgament.newInstance("Android"),
                BlogsFrgament.newInstance("Twitter"),
                BlogsFrgament.newInstance("Dribble")
        };

        /**
         * Constructs new fragments pager with support fragment manager.
         * @param fragmentManager support fragment manager
         */
        FragmentsAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }

        /**
         * Return the Fragment associated with a specified position.
         *
         * @param position position of fragment.
         */
        @Override
        public Fragment getItem(int position) {return mItems[position];}

        /** Return the number of views available. */
        @Override
        public int getCount() {return mItems.length;}

        /**
         * @param position position
         * @return page title by position
         */
        @Override
        public CharSequence getPageTitle(int position) {return "#" + mItems[position].getHashTag();}
    }
    
}
