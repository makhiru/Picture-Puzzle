package com.example.picturepuzzels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener, View.OnDragListener {

    RelativeLayout imgselect;
    ImageView image;
    Button btnstart;

    ImageView[] img = new ImageView[25];
    LinearLayout[] ll = new LinearLayout[25];
    List<Photo> arrimg = new ArrayList();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgselect = findViewById(R.id.imgselect);
        image = findViewById(R.id.image);
        btnstart = findViewById(R.id.btnstart);

        for (int i = 0; i < 25; i++) {
            int id = getResources().getIdentifier("img" + i, "id", getPackageName());
            img[i] = findViewById(id);

            int id2 = getResources().getIdentifier("ll" + i, "id", getPackageName());
            ll[i] = findViewById(id2);
            ll[i].setOnDragListener(this);
        }

        imgselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] option = {"Camera", "Gallery"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.create();
                builder.setIcon(R.drawable.ic_baseline_photo_size_select_actual_24);
                builder.setTitle("Select Item");
                builder.setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        if (i == 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{"android.permission.CAMERA"}, 2001);
                            }
                        } else if (i == 1) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 2002);
                            }
                        }
                    }
                });
                builder.show();
            }
        });

        btnstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image.getDrawable() != null) {
                    splitimage(image, 25);
                }else {
                    Toast.makeText(MainActivity.this, "Please, Image Select.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2001) {
            Intent icamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(icamera, 801);
        } else {
            Intent igallary = new Intent(Intent.ACTION_PICK);
            igallary.setType("image/*");
            startActivityForResult(igallary, 811);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 811) {
                image.setImageURI(data.getData());
            }
            if (requestCode == 801) {
                image.setImageURI(data.getData());
            }
        }
    }

    private void splitimage(ImageView image, int Chunknumber) {
        int rows, cols;
        int chunkheight, chunkwidth;

        ArrayList<Photo> chunkedimages = new ArrayList<>(Chunknumber);

        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable drawable = image.getBackground();
        if (drawable != null) {
            drawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        image.draw(canvas);

        Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        rows = cols = (int) Math.sqrt(Chunknumber);
        chunkheight = bitmap.getHeight() / rows;
        chunkwidth = bitmap.getWidth() / cols;

        int yCoord = 0;
        int i = 0;
        for (int x = 0; x < rows; x++) {
            int xCoord = 0;
            for (int y = 0; y < cols; y++) {
                chunkedimages.add(new Photo(Bitmap.createBitmap(scaleBitmap, xCoord, yCoord, chunkwidth, chunkheight), i));
                i++;
                xCoord += chunkwidth;
            }
            yCoord += chunkheight;
        }
        arrimg.addAll(chunkedimages);
        btnstart.setVisibility(View.GONE);
        imgselect.setVisibility(View.GONE);

        findViewById(R.id.grid).setVisibility(View.VISIBLE);
        Collections.shuffle(arrimg);
        for (int j = 0; j < 25; j++) {
            img[j].setImageBitmap(arrimg.get(j).getImg());
            img[j].setTag(String.valueOf(arrimg.get(j).getTag()));
            img[j].setOnLongClickListener(MainActivity.this);

        }
    }

    @Override
    public boolean onLongClick(View v) {

        ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
        String[] type = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData clipData = new ClipData(v.getTag().toString(), type, item);
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
        v.startDrag(clipData, shadowBuilder, v, 0);
        return true;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {

        int action = event.getAction();

        switch (action) {

            case DragEvent.ACTION_DRAG_STARTED:
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                v.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                v.invalidate();
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                v.getBackground().clearColorFilter();
                v.invalidate();
                return true;

            case DragEvent.ACTION_DROP:
                v.getBackground().clearColorFilter();
                v.invalidate();

                LinearLayout layout = (LinearLayout) v;

                if (layout.getChildCount() == 0) {
                    View view = (View) event.getLocalState();
                    ViewGroup group = (ViewGroup) view.getParent();
                    group.removeView(view);
                    if (group.getBackground() == null) {
                        group.setVisibility(view.GONE);
                    }
                    layout.addView(view);
                    view.setVisibility(view.VISIBLE);
                } else {
                    View view1 = (View) event.getLocalState();
                    ViewGroup group = (ViewGroup) view1.getParent();
                    group.removeView(view1);
                    layout.addView(view1);

                    View view2 = layout.getChildAt(0);
                    layout.removeView(view2);
                    group.addView(view2);

                    view1.setVisibility(view1.VISIBLE);
                    view2.setVisibility(view2.VISIBLE);

                    win();
                }
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                v.getBackground().clearColorFilter();
                v.invalidate();
                return true;

            default:
                Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
                break;

        }
        return false;
    }

    public void win() {
        int count = 0;
        for (int i = 0; i < 25; i++) {

            View view = ll[i].getChildAt(0);
            int tag = Integer.parseInt(view.getTag().toString());
            if (tag != i) {
                count++;
            }
        }
        if (count == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.create();
            builder.setCancelable(false);
            builder.setTitle("Winner");
            builder.setMessage("Are You Winner!!!");
            builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    recreate();
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }
}