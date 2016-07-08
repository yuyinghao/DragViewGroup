package com.xiaoyu.dragviewgroup.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaoyu.dragviewgroup.R;
import com.xiaoyu.dragviewgroup.library.DragViewGroup;

import java.util.Random;

public class MainActivity extends Activity {

    private DragViewGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        group = (DragViewGroup)findViewById(R.id.mydragview);
        group.setAdapter(adapter);
        group.setItemClickListener(new DragViewGroup.ItemClickListener() {
            @Override
            public void onItemClickListener(int pos, View view) {
                Toast.makeText(MainActivity.this, "第"+pos+"被点击", Toast.LENGTH_SHORT).show();
            }
        });
        group.setChildPositionChangeListener(new DragViewGroup.ChildPositionChangeListener() {
            @Override
            public void onChildPositionChange(int pos1, View view1, int pos2, View view2) {
                Toast.makeText(MainActivity.this, "第"+pos1+"个元素和第"+pos2+"个元素交换了位置", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BaseAdapter adapter = new BaseAdapter() {

        private int[] colors = new int[]{Color.BLUE, Color.CYAN, Color.DKGRAY, Color.GRAY, Color.GREEN, Color.LTGRAY, Color.MAGENTA, Color.RED, Color.YELLOW};
        private Random random = new Random();

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView t = new TextView(parent.getContext());
            t.setBackgroundColor(colors[random.nextInt(colors.length)]);
            t.setText(position + "");
            t.setGravity(Gravity.CENTER);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(200, 200);
            params.setMargins(10, 10, 10, 10);
            t.setLayoutParams(params);
            return t;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return 50;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
