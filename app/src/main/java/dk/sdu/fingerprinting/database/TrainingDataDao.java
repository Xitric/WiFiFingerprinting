package dk.sdu.fingerprinting.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TrainingDataDao {

    @Insert
    void insert(List<TrainingData> trainingData);

    @Query("SELECT * FROM trainingdata")
    LiveData<List<TrainingData>> getAll();

    @Query("DELETE FROM trainingdata")
    void clear();
}
