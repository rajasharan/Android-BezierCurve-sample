package com.rajasharan.curvepaths;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static Fragment newInstance(String title) {
        Fragment f = new MainActivityFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        f.setArguments(args);
        return f;
    }

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments().getString("title").equalsIgnoreCase(MainActivity.CUBIC)) {
            return inflater.inflate(R.layout.fragment_cubic, container, false);
        }
        else {
            return inflater.inflate(R.layout.fragment_quad, container, false);
        }
    }
}
