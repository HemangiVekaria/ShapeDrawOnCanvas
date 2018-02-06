package editing.canvas.com.imageeditingoncanvas;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.editing.canvas.library.ShapeDrawingActivity;
import com.editing.canvas.library.util.Helper;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ImageView imagePlaceholder;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int EDITED_IMAGE_CODE = 100;
    private static int CAMERA_CODE = 1;
    TextView txtEditImage;
    Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView txtCaptureImage = findViewById(R.id.txtCaptureImage);
        txtCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkForPermission();
                } else {
                    if (Build.VERSION.SDK_INT > 23) {
                        openCamera2Activity();
                    } else {
                        openCamera1Activity();
                    }
                }
            }
        });

        TextView txtSelectFromGallery = findViewById(R.id.txtSelectImageFromGallery);
        txtSelectFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGalleryForImageVideoSelect();
            }
        });

        imagePlaceholder = findViewById(R.id.imagePlaceholder);
        txtEditImage = findViewById(R.id.txtEditImage);
        txtEditImage.setVisibility(View.GONE);
        txtEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri == null) {
                    Toast.makeText(getApplicationContext(), "Select image from Gallery or Capture from Camera", Toast.LENGTH_SHORT).show();
                } else {
                    startActivityForResult(new Intent(MainActivity.this, ShapeDrawingActivity.class).setData(imageUri), EDITED_IMAGE_CODE);
                }
            }
        });
    }

    private static int GALLERY_IMAGE_SELECT = 1001;

    private void openGalleryForImageVideoSelect() {
        Intent intent;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI);
        }
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*"});
        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, GALLERY_IMAGE_SELECT);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkForPermission() {
        int hasCameraAccessPermission = checkSelfPermission(Manifest.permission.CAMERA);
        int hasExternalStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasCameraAccessPermission != PackageManager.PERMISSION_GRANTED || hasExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        } else {
            openCamera2Activity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera2Activity();
            } else {
                Toast.makeText(getApplicationContext(), "No permission to access camera", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openCamera2Activity() {
        startActivityForResult(new Intent(MainActivity.this, Camera2NewActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), CAMERA_CODE);
    }

    private void openCamera1Activity() {
        startActivityForResult(new Intent(MainActivity.this, Camera1Activity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), CAMERA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_IMAGE_SELECT) {
            String imagePath = Helper.getRealPathFromURI(getApplicationContext(), data.getData());
            String path = Helper.getPath(getApplicationContext(), data.getData());
            if (imagePath != null && imagePath.length() > 0) {
                imageUri = Uri.fromFile(new File(imagePath));
                imagePlaceholder.setImageURI(Uri.fromFile(new File(imagePath)));
            } else if (path != null && path.length() > 0) {
                // getSelectedVideoImageMoveToNextActivity(path);
                imageUri = Uri.fromFile(new File(path));
                imagePlaceholder.setImageURI(Uri.fromFile(new File(path)));
            } else {
                imageUri = data.getData();
                imagePlaceholder.setImageURI(data.getData());
            }
            txtEditImage.setVisibility(View.VISIBLE);
        } else if (requestCode == CAMERA_CODE) {
            Uri imageUri1 = data.getData();
            imageUri = imageUri1;
            imagePlaceholder.setImageURI(imageUri1);
            txtEditImage.setVisibility(View.VISIBLE);
        } else if (requestCode == EDITED_IMAGE_CODE) {
            imagePlaceholder.setImageURI(data.getData());
        }
    }
}
