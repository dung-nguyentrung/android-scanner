package com.harry.uhf_c.ui.inventory;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.harry.uhf_c.entity.TagInfo;
import com.harry.uhf_c.utils.SettingsStorage; // Assuming SettingsStorage is needed for reader config
import com.realopeniot.readers_framework.core.ApiCallback;
import com.realopeniot.readers_framework.core.RFIDApiManager;
import com.realopeniot.readers_framework.core.ResponseMessage;
import com.realopeniot.readers_framework.core.bean.Ant;
import com.realopeniot.readers_framework.core.bean.Power;
import com.realopeniot.readers_framework.core.bean.RfParam;
import com.realopeniot.readers_framework.core.bean.TagData;
import com.realopeniot.readers_framework.core.protocal.ReaderCoreAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import rx.schedulers.Schedulers;

public class InventoryViewModel extends AndroidViewModel {

    // LiveData để quan sát dữ liệu thẻ và trạng thái
    private final MutableLiveData<List<TagInfo>> _tagInfoList = new MutableLiveData<>();
    public LiveData<List<TagInfo>> tagInfoList = _tagInfoList;

    private final MutableLiveData<Integer> _identifiedTagCount = new MutableLiveData<>();
    public LiveData<Integer> identifiedTagCount = _identifiedTagCount;

    private final MutableLiveData<String> _rfStatusMessage = new MutableLiveData<>();
    public LiveData<String> rfStatusMessage = _rfStatusMessage;

    // Logic xử lý RFID
    private RFIDApiManager readerManager;
    private volatile boolean isReading = false;
    private RfParam rfParam;
    private ConcurrentHashMap<String, TagInfo> tagInfoMap = new ConcurrentHashMap<>();
    private TagInventoryThread inventoryThread;
    private SettingsStorage storage;

    // Handler và Runnable để cập nhật LiveData một cách có kiểm soát
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateLiveDataRunnable;
    private static final long LIVE_DATA_UPDATE_INTERVAL_MS = 200; // Cập nhật LiveData thường xuyên hơn một chút so với UI trực tiếp

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        storage = new SettingsStorage(application.getApplicationContext());
        initReader();
        _identifiedTagCount.setValue(0); // Khởi tạo số lượng thẻ
    }

    // Khởi tạo Reader (chỉ gọi một lần trong ViewModel)
    private void initReader() {
        readerManager = new RFIDApiManager(getApplication().getApplicationContext(), ReaderCoreAdapter.CoreKey.CORE1204);
        int maxAntennaNum = 8; // Adjust based on your reader's actual antenna count

        rfParam = new RfParam();
        rfParam.setFastAnt(true);
        rfParam.setBlockSelect("EPC");
        rfParam.setAntList(generateAntList(maxAntennaNum));
        rfParam.setPowerList(generatePowerList(maxAntennaNum));

        _rfStatusMessage.postValue(getCurrentFormattedTime() + ": Reader initialized. Ready.");
    }

    private List<Ant> generateAntList(int count) {
        List<Ant> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new Ant(i, "ANT" + i, true));
        }
        return list;
    }

    private List<Power> generatePowerList(int count) {
        List<Power> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            if (storage.isAntennaEnabled(i)) {
                Power power = new Power();
                power.setAntNumber(i);
                power.setPower(storage.getAntennaPower(i));
                list.add(power);
            }
        }
        return list;
    }

    // Bắt đầu quá trình đọc RFID
    public synchronized void startInventory() {
        if (isReading) {
            Log.d("InventoryViewModel", "Already reading, not starting new inventory.");
            return;
        }

        isReading = true;
        _rfStatusMessage.postValue(getCurrentFormattedTime() + ": Starting inventory...");

        // Đảm bảo dừng luồng cũ nếu nó vẫn đang chạy
        if (inventoryThread != null) {
            inventoryThread.interrupt();
            try {
                inventoryThread.join(500); // Đợi luồng cũ dừng
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e("InventoryViewModel", "Error while waiting for old thread to stop: " + e.getMessage());
            }
        }
        inventoryThread = new TagInventoryThread();
        inventoryThread.start();
        Log.d("InventoryViewModel", "New inventory thread started.");
    }

    // Dừng quá trình đọc RFID
    public synchronized void stopInventory() {
        if (!isReading) {
            Log.d("InventoryViewModel", "Not reading, no need to stop inventory.");
            return;
        }

        isReading = false;
        if (inventoryThread != null) {
            inventoryThread.interrupt(); // Yêu cầu luồng dừng
            try {
                inventoryThread.join(500); // Đợi luồng kết thúc
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e("InventoryViewModel", "Error while waiting for inventory thread to stop: " + e.getMessage());
            }
            inventoryThread = null;
        }
        handler.removeCallbacks(updateLiveDataRunnable); // Dừng cập nhật LiveData
        _rfStatusMessage.postValue(getCurrentFormattedTime() + ": Inventory stopped.");
        Log.d("InventoryViewModel", "RFID inventory stopped.");
    }

    // Đặt lại dữ liệu thẻ
    public void resetTagData() {
        tagInfoMap.clear();
        _tagInfoList.postValue(new ArrayList<>(tagInfoMap.values()));
        _identifiedTagCount.postValue(0);
        Log.d("InventoryViewModel", "Tag data reset.");
    }

    // Lấy thời gian hiện tại được định dạng
    private String getCurrentFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return sdf.format(new Date());
    }

    // Luồng xử lý kiểm kê thẻ
    class TagInventoryThread extends Thread {
        @Override
        public void run() {
            Log.d("TagInventoryThread", "TagInventoryThread started in ViewModel.");
            while (isReading) {
                // Bước 1: Bắt đầu kiểm kê không đồng bộ
                readerManager.startAsyncInventory(new ApiCallback<ResponseMessage>() {
                    @Override
                    public void onSuccess(ResponseMessage responseMessage) {
                        // Cập nhật trạng thái kết nối
                        _rfStatusMessage.postValue(getCurrentFormattedTime() + ": Connected... [OK]");
                    }

                    @Override
                    public void onError(String msg) {
                        _rfStatusMessage.postValue(getCurrentFormattedTime() + ": Error occurred... [" + msg + "]");
                    }
                }, rfParam, Schedulers.immediate());

                // Bước 2: Lấy dữ liệu thẻ không đồng bộ
                readerManager.asyncGetTagDataList(new ApiCallback<ArrayList<TagData>>() {
                    @Override
                    public void onSuccess(ArrayList<TagData> tagDataArrayList) {
                        if (tagDataArrayList != null && !tagDataArrayList.isEmpty()) {
                            for (TagData tag : tagDataArrayList) {
                                final String epc = tag.getEpc();
                                if (epc == null || epc.isEmpty() || !epc.contains("E28")) continue;

                                tagInfoMap.compute(epc, (key, existingTagInfo) -> {
                                    if (existingTagInfo == null) {
                                        TagInfo newTagInfo = new TagInfo();
                                        newTagInfo.setEpc(epc);
                                        newTagInfo.setAnt(tag.getAnt());
                                        newTagInfo.setFirstTime(getCurrentFormattedTime());
                                        newTagInfo.setCount(1);
                                        return newTagInfo;
                                    } else {
                                        existingTagInfo.setCount(existingTagInfo.getCount() + 1);
                                        // Cập nhật lastTime nếu bạn muốn theo dõi thời gian đọc cuối cùng
                                        // existingTagInfo.setLastTime(getCurrentFormattedTime());
                                        return existingTagInfo;
                                    }
                                });
                            }
                            scheduleLiveDataUpdate(); // Lập lịch cập nhật LiveData sau khi xử lý thẻ
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        Log.e("DEBUG-RFID", "asyncGetTagDataList error: " + msg);
                        _rfStatusMessage.postValue(getCurrentFormattedTime() + ": Data read error: [" + msg + "]");
                    }
                }, Schedulers.immediate());

                SystemClock.sleep(50); // Giữ độ trễ để tránh vòng lặp bận
            }
            Log.d("TagInventoryThread", "TagInventoryThread terminated in ViewModel.");
        }
    }

    // Lập lịch cập nhật LiveData để tránh cập nhật quá thường xuyên
    private void scheduleLiveDataUpdate() {
        if (updateLiveDataRunnable != null) {
            handler.removeCallbacks(updateLiveDataRunnable);
        }
        updateLiveDataRunnable = () -> {
            _tagInfoList.postValue(new ArrayList<>(tagInfoMap.values()));
            _identifiedTagCount.postValue(tagInfoMap.size());
        };
        handler.postDelayed(updateLiveDataRunnable, LIVE_DATA_UPDATE_INTERVAL_MS);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopInventory(); // Đảm bảo dừng mọi hoạt động khi ViewModel bị hủy
        Log.d("InventoryViewModel", "ViewModel onCleared: Inventory stopped and resources released.");
        readerManager = null; // Giải phóng tài nguyên reader
    }
}