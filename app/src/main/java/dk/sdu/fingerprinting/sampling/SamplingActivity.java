package dk.sdu.fingerprinting.sampling;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import dk.sdu.fingerprinting.R;

public class SamplingActivity extends AppCompatActivity {

    private EditText txt_location;
    private TextView lbl_status;
    private TextView lbl_orientation;

    private SamplingViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sampling);
        txt_location = findViewById(R.id.txt_location);
        lbl_status = findViewById(R.id.lbl_status);
        lbl_orientation = findViewById(R.id.lbl_orientation);

        viewModel = ViewModelProviders.of(this).get(SamplingViewModel.class);
        viewModel.init(getLifecycle());

        viewModel.getStatus().observe(this, lbl_status::setText);
        viewModel.getOrientation().observe(this, orientation -> lbl_orientation.setText(String.valueOf(orientation)));
    }

    public void scanAction(View sender) {
        startScanning();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        }
    }

    private void startScanning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }

        viewModel.startScanning(txt_location.getText().toString());
    }

    public void trainAction(View sender) {
        viewModel.train();
    }

    public void clearAction(View sender) {
        viewModel.clear();
    }
}
