package dk.sdu.fingerprinting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import dk.sdu.fingerprinting.nearest_neighbor.ClassificationActivity;
import dk.sdu.fingerprinting.sampling.SamplingActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void samplingAction(View sender) {
        Intent samplingIntent = new Intent(this, SamplingActivity.class);
        startActivity(samplingIntent);
    }

    public void classificationAction(View sender) {
        Intent classificationIntent = new Intent(this, ClassificationActivity.class);
        startActivity(classificationIntent);
    }
}
