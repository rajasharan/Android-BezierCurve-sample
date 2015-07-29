package com.rajasharan.curvepaths;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {
    public static final String CUBIC = "Cubic Bezier Example";
    public static final String QUAD = "Quad Bezier Example";

    private Fragment mCubicBezier;
    private Fragment mQuadBezier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(CUBIC);
        mCubicBezier = MainActivityFragment.newInstance(CUBIC);
        mQuadBezier = MainActivityFragment.newInstance(QUAD);
        getSupportFragmentManager().beginTransaction().replace(R.id.root, mCubicBezier).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.cubic) {
            setTitle(CUBIC);
            getSupportFragmentManager().beginTransaction().replace(R.id.root, mCubicBezier).commit();
            return true;
        } else if (id == R.id.quad) {
            setTitle(QUAD);
            getSupportFragmentManager().beginTransaction().replace(R.id.root, mQuadBezier).commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
