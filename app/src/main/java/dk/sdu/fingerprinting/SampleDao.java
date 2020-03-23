package dk.sdu.fingerprinting;

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

    @Query("SELECT * FROM sample WHERE ap_mac = :apMac")
    LiveData<List<Sample>> getSamples(String apMac);

    @Query("DELETE FROM sample")
    void clear();
}
