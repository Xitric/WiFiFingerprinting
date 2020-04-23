package dk.sdu.fingerprinting.nearest_neighbor;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import dk.sdu.fingerprinting.R;
import dk.sdu.fingerprinting.database.FingerprintingDatabase;

public class ClassificationActivity extends AppCompatActivity {

    private NnManager nnManager;
    private TestDataSampler testDataSampler;
    private TextView locationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classification);
        locationTextView = findViewById(R.id.location);
        testDataSampler = new TestDataSampler(this, getLifecycle());
        nnManager = new NnManager(FingerprintingDatabase.getInstance(this));
    }

    public void findLocationAction(View view) {
        testDataSampler.getTestData().observe(this, this::findLocation);
    }

    private void findLocation(TestData testData) {
        nnManager.getLocation(testData, 1).observe(this, locationTextView::setText);
    }
}
