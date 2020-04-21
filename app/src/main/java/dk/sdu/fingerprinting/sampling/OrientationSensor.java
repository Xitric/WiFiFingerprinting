package dk.sdu.fingerprinting.sampling;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;

public class OrientationSensor implements SensorEventListener, LifecycleObserver {

    private final SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private final MutableLiveData<Float> orientation;

    private OrientationSensorListener listener;
    private int previousOrientation = -1;

    public OrientationSensor(Context context, Lifecycle lifecycle) {
        this.sensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        this.orientation = new MutableLiveData<>();
        lifecycle.addObserver(this);
    }

    public LiveData<Float> getOrientation() {
        return orientation;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void register() {
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }

        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected void deregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
        }

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        orientation.postValue(orientationAngles[0]);

        int newOrientation = getClosestIndex(new double[]{0, Math.PI / 2, Math.PI, -Math.PI / 2}, orientationAngles[0]);
        if (previousOrientation != newOrientation) {
            previousOrientation = newOrientation;
            listener.onOrientation(newOrientation);
        }
    }

    private int getClosestIndex(double[] values, double reference) {
        double smallestDifference = Math.PI;
        int smallestIndex = 0;

        for (int i = 0; i < values.length; i++) {
            double value = values[i];
            double diff = Math.abs(value - reference);
            if (diff < smallestDifference) {
                smallestDifference = diff;
                smallestIndex = i;
            }
        }

        return smallestIndex;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //No-Op
    }

    public void addListener(OrientationSensorListener listener) {
        this.listener = listener;
    }

    public interface OrientationSensorListener {
        void onOrientation(int orientation);
    }
}
