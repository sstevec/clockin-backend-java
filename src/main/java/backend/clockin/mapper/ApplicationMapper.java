package backend.clockin.mapper;

import backend.clockin.pojo.applyRecord.ApplicationRecord;
import backend.clockin.pojo.applyRecord.ApplicationReviewList;
import backend.clockin.pojo.dayOff.BoundedDayOffRecord;
import backend.clockin.pojo.dayOff.DayOffRecord;
import backend.clockin.pojo.dayOff.MergedDayOffRecord;
import backend.clockin.pojo.extraWork.ExtraWorkRecord;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.Date;

public interface ApplicationMapper {

    void deleteDayOffRecord(@Param("id") Integer id);

    void addApplicationRecord(ApplicationRecord applicationRecord);

    ApplicationRecord getApplicationRecord(@Param("applicationId") String applicationId);

    void deleteApplicationRecord(@Param("applicationId") String applicationId);

    void changeApplicationRecord(@Param("applicationId")String applicationId,
                                 @Param("applicationDetail")String applicationDetail,
                                 @Param("applicationDescription")String applicationDescription);

    void changeApplicationStatus(@Param("applicationId")String applicationId,
                                 @Param("targetStatus") String targetStatus);

    void deleteDayOffRecordByDate(@Param("start") String start,@Param("end") String end,@Param("offType") String offType);

    void addExtraWorkRecord(@Param("extraWorkRecord") ExtraWorkRecord extraWorkRecord);

    void addReviewList(@Param("applicationReviewList") ApplicationReviewList applicationReviewList);

    void deleteReviewList(@Param("applicationId") String applicationId);

    ApplicationReviewList getReviewList(@Param("applicationId") String applicationId);

    ArrayList<ApplicationReviewList> getAllReviewList();

    void updateReviewList(@Param("applicationId") String applicationId,
                          @Param("newReviewResult") String newReviewResult,
                          @Param("status") String status);

    void deleteExtraWorkRecord(@Param("applicationId") String applicationId);

    void addMergedDayOffRecord(@Param("mergedDayOffRecord") MergedDayOffRecord mergedDayOffRecord);

    ArrayList<MergedDayOffRecord> getMergedDayOffRecord(@Param("uid") String uid, @Param("day") String day);

    void addBoundedDayOffRecord(@Param("boundedDayOffRecord") BoundedDayOffRecord boundedDayOffRecord);

    BoundedDayOffRecord getBoundedDayOffRecord(@Param("uid") String uid, @Param("day") String day);

    MergedDayOffRecord getMergedDayOffRecordById(@Param("id") String startId);

    void deleteMergedRecord(@Param("uid") String uid, @Param("day") String day);

    void deleteBoundedRecord(@Param("uid") String uid, @Param("day") String day);

    void updateChangeDayOffType(@Param("type")String type,@Param("changeHour") String changeHour, @Param("uid") String uid);

    ArrayList<DayOffRecord> getDayOffRecordByType(@Param("uid") String uid, @Param("day") String day,@Param("type") String type);

    ExtraWorkRecord getExtraWorkRecord(@Param("uid") String uid,@Param("day") String date);
}
