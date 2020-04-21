package dk.sdu.fingerprinting.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SampleDao {

    @Insert
    void insertSample(Sample sample);

    @Query("SELECT * FROM sample")
    LiveData<List<Sample>> getSamples();

    @Query("SELECT * FROM sample WHERE location = :location AND ap_mac = :apMac AND orientation = :orientation")
    List<Sample> getSamples(String location, String apMac, int orientation);

    @Query("SELECT DISTINCT location from sample")
    List<String> getLocations();

    @Query("SELECT DISTINCT ap_mac from sample WHERE location = :location AND orientation = :orientation")
    List<String> getMacAddresses(String location, int orientation);

    @Query("DELETE FROM sample")
    void clear();
}
