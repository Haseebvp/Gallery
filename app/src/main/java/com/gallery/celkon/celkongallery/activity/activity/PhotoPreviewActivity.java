package com.gallery.celkon.celkongallery.activity.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gallery.celkon.celkongallery.R;
import com.gallery.celkon.celkongallery.activity.component.PhoneMediaControl;
import com.gallery.celkon.celkongallery.activity.component.PhotoPreview;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoPreviewActivity extends AppCompatActivity implements OnPageChangeListener {

    private ViewPager mViewPager;
    protected List<PhoneMediaControl.PhotoEntry> photos = new ArrayList<>();
    protected int current, folderPosition, currentposition;

    protected Context context;
    private Toolbar toolbar;

    private ImageView edit, share, delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photopreview);


        context = PhotoPreviewActivity.this;
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle mBundle = getIntent().getExtras();
        folderPosition = mBundle.getInt("Key_FolderID");
        current = mBundle.getInt("Key_ID");

        photos.addAll(GalleryFragment.albumsSorted.get(folderPosition).photos);

        mViewPager = (ViewPager) findViewById(R.id.vp_base_app);
        edit = (ImageView) findViewById(R.id.edit);
        share = (ImageView) findViewById(R.id.share);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = photos.get(currentposition).path.lastIndexOf("/");
                int dotindex = photos.get(currentposition).path.lastIndexOf(".");
                String name = photos.get(currentposition).path.substring(index + 1, dotindex);
                System.out.println(name);
                showEditDialog(name);

            }
        });


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharefile(photos.get(currentposition).path);
            }
        });


        mViewPager.addOnPageChangeListener(this);
        overridePendingTransition(R.anim.activity_alpha_action_in, 0);
        bindData();
    }

    private void deleteImage(String path) {
        File file = new File(path);

        try {
            if (file.isDirectory()) {

                for (File child : file.listFiles()) {

                    deleteImage(child.getAbsolutePath());
                }
            }
            Log.d("FILE DELETE : ", String.valueOf(file.delete()));

//            file.delete();
            System.out.println("FILE SIZE BEFORE : " + photos.size());
            photos.remove(currentposition);
            System.out.println("FILE SIZE AFTER : " + photos.size());
            bindData();
        } catch (Exception e) {
            System.out.println(String.format("Error deleting %s", file.getName()));
        }
    }

    public void callBroadCast() {
        if (Build.VERSION.SDK_INT >= 14) {
            Log.e("-->", " >= 14");
            MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                /*
                 *   (non-Javadoc)
                 * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
                 */
                public void onScanCompleted(String path, Uri uri) {
                    Log.e("ExternalStorage", "Scanned " + path + ":");
                    Log.e("ExternalStorage", "-> uri=" + uri);
                }
            });
        } else {
            Log.e("-->", " < 14");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }

    }

    private void sharefile(String path) {
        try {
            File file = new File(path);
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEditDialog(final String name) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.edit_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);
        edt.setText(name);
        TextView done = (TextView) dialogView.findViewById(R.id.done);
        TextView cancel = (TextView) dialogView.findViewById(R.id.cancel);
        final AlertDialog b = dialogBuilder.create();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = edt.getText().toString();
                rename(newName, name);
                b.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.dismiss();
            }
        });


        b.show();
    }

    private void rename(String newName, String oldName) {
        System.out.println("name : " + newName);
        String out = "";
        int index = photos.get(currentposition).path.lastIndexOf("/");
        String path = photos.get(currentposition).path;
        int dotindex = photos.get(currentposition).path.lastIndexOf(".");
        String temp = path.substring(0, index+1);
        out = out + path.substring(dotindex, path.length());
        System.out.println("jjj : " + temp + " , " + out+" , "+temp+newName+out);
        File from = new File(temp, oldName + out);
        File to = new File(temp, newName + out);
        from.renameTo(to);
        photos.get(currentposition).setPath(temp+newName+out);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void bindData() {
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(current);
        toolbar.setTitle((current + 1) + "/" + photos.size());
    }

    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            if (photos == null) {
                return 0;
            } else {
                return photos.size();
            }
        }

        @Override
        public View instantiateItem(final ViewGroup container, final int position) {
            PhotoPreview photoPreview = new PhotoPreview(context);
            ((ViewPager) container).addView(photoPreview);
            System.out.println("PATH : " + photos.get(position).path);
            System.out.println("PATH : " + photos.get(currentposition).path.lastIndexOf("/"));

            currentposition = position;
            photoPreview.loadImage(photos.get(position));
            return photoPreview;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    };

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        current = arg0;
        updatePercent();
    }

    protected void updatePercent() {
        toolbar.setTitle((current + 1) + "/" + photos.size());
    }
}
