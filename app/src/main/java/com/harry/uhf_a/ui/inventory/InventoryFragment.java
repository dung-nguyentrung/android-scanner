package com.harry.uhf_a.ui.inventory;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.harry.uhf_a.R;
import com.harry.uhf_a.adapter.TagAdapter;
import com.harry.uhf_a.databinding.FragmentInventoryBinding;
import com.harry.uhf_a.entity.TagInfo;
import com.harry.uhf_a.utils.LogUtils;
import com.realopeniot.readers_framework.core.ApiCallback;
import com.realopeniot.readers_framework.core.RFIDApiManager;
import com.realopeniot.readers_framework.core.ResponseMessage;
import com.realopeniot.readers_framework.core.bean.Ant;
import com.realopeniot.readers_framework.core.bean.Power;
import com.realopeniot.readers_framework.core.bean.RfParam;
import com.realopeniot.readers_framework.core.bean.TagData;
import com.realopeniot.readers_framework.core.protocal.ReaderCoreAdapter;
import com.realopeniot.uar_utils.GpioUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import rx.schedulers.Schedulers;

public class InventoryFragment extends Fragment {

    private FragmentInventoryBinding binding;
    private InventoryViewModel inventoryViewModel;
    private static final int START = 1;
    private static final int STOP = 0;

    private RFIDApiManager readerManager;
    private boolean isReading = false;
    private RfParam rfParam;
    Map<String, TagInfo> tagInfoMap = new HashMap<>();

    private TagAdapter tagAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        binding.recyclerViewTags.setLayoutManager(new LinearLayoutManager(getContext()));
        tagAdapter = new TagAdapter(new ArrayList<>());
        binding.recyclerViewTags.setAdapter(tagAdapter);
        GpioUtils gpioUtils = new GpioUtils(getContext());
        gpioUtils.setRfidEn(GpioUtils.RfidEnStatusEnum.STATUS_ON);

        initReader();
        binding.btnStart.setOnClickListener(v -> {
            boolean starting = binding.btnStart.getText().toString().equals(getString(R.string.start));
            binding.btnStart.setText(starting ? R.string.stop : R.string.start);
            onStartClicked(starting ? START : STOP);
        });

        binding.btnReset.setOnClickListener(v -> onResetClicked());
        binding.btnUpload.setOnClickListener(v -> onUploadClicked());

        return binding.getRoot();
    }

    private void initReader() {
        readerManager = new RFIDApiManager(requireContext(), ReaderCoreAdapter.CoreKey.CORE1204);
        int maxAntennaNum = 8;

        rfParam = new RfParam();
        rfParam.setFastAnt(true);
        rfParam.setBlockSelect("EPC");
        rfParam.setAntList(generateAntList(maxAntennaNum));
        rfParam.setPowerList(generatePowerList(maxAntennaNum));

        isReading = true;
        new TagInventoryThread().start();
        Toast.makeText(getContext(),"Init RFID Reader successfully", Toast.LENGTH_LONG).show();
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
        for (int i = 0; i < count; i++) {
            Power power = new Power();
            power.setAntNumber(i + 1);
            power.setPower(30);
            list.add(power);
        }
        return list;
    }

    public void onStartClicked(int type) {
        isReading = (type == START);
        if (isReading)
        {
            new TagInventoryThread().start();
        }
    }

    public void onResetClicked() {
        tagInfoMap.clear();
        tagAdapter.updateList(new ArrayList<>());
        binding.tvTotalCount.setText("Tổng số: 0" );
    }

    public void onUploadClicked() {
            try {
                // Tạo JSON structure
                JSONObject root = new JSONObject();
                root.put("timestamp", getCurrentFormattedTime());
                root.put("method", "device_report_probe_data");
                root.put("sn", "d56f07f5");

                long currentMillis = System.currentTimeMillis();
                JSONObject data = new JSONObject();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                String fileNameTime =  sdf.format(new Date(System.currentTimeMillis()));
                data.put("timestamp", String.valueOf(currentMillis));
                data.put("id", "d56f07f5");
                data.put("temperature", "0");

                JSONArray tagArray = new JSONArray();
                for (TagInfo tag : tagInfoMap.values()) {
                    JSONObject tagJson = new JSONObject();
                    tagJson.put("direction", "1");
                    tagJson.put("firstTime", currentMillis); // dummy firstTime
                    tagJson.put("lastTime", currentMillis); // dummy lastTime
                    tagJson.put("firstAnt", tag.getAnt());  // giả định same as ant
                    tagJson.put("ant", tag.getAnt());
                    tagJson.put("rssi", "11"); // dummy RSSI
                    tagJson.put("epc", tag.getEpc());
                    tagArray.put(tagJson);
                }

                data.put("tagList", tagArray);
                root.put("data", data);

                // Ghi vào file
                String fileName = "hwl_" + fileNameTime + ".json";
                File dir = new File(requireContext().getExternalFilesDir(null), "logs");
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, fileName);

                FileWriter writer = new FileWriter(file);
                writer.write(root.toString(4)); // Pretty print
                writer.close();

                Toast.makeText(getContext(), "Dữ liệu đã ghi vào file: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi khi ghi file JSON: " + e.getMessage() , Toast.LENGTH_SHORT).show();
            }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String getCurrentFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return sdf.format(new Date());
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    private void scheduleTableUpdate() {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }

        updateRunnable = () -> updateRecyclerView(new ArrayList<>(tagInfoMap.values()));
        handler.postDelayed(updateRunnable, 500);
    }

    private void updateRecyclerView(List<TagInfo> tagInfos) {
        if (getActivity() == null || binding == null) return;

        requireActivity().runOnUiThread(() -> {
            tagAdapter.updateList(tagInfos);
            binding.tvTotalCount.setText("Tổng số: " + tagInfos.size());
        });
    }

    class TagInventoryThread extends Thread {
        @Override
        public void run() {
            tagInfoMap.clear();

            while (isReading) {
                readerManager.startAsyncInventory(new ApiCallback<ResponseMessage>() {
                    @Override
                    public void onSuccess(ResponseMessage responseMessage) {
                        LogUtils.logout("RFID", "Inventory success: " + responseMessage.getMsg());
                    }

                    @Override
                    public void onError(String msg) {
                        LogUtils.logout("RFID", "Inventory error: " + msg);
                    }
                }, rfParam, Schedulers.immediate());

                readerManager.asyncGetTagDataList(new ApiCallback<ArrayList<TagData>>() {
                    @Override
                    public void onSuccess(ArrayList<TagData> tagDataArrayList) {
                        LogUtils.logout("RFID", "GetTagDataList success: " + tagDataArrayList.size());
                        for (TagData tag : tagDataArrayList) {
                            final String epc = tag.getEpc();
                            if (epc == null || epc.isEmpty()) continue;

                            TagInfo tagInfo = tagInfoMap.get(epc);

                            if (tagInfo == null) {
                                tagInfo = new TagInfo();
                                tagInfo.setEpc(epc);
                                tagInfo.setAnt(tag.getAnt());
                                tagInfo.setFirstTime(getCurrentFormattedTime());
                                tagInfo.setCount(1);
                                tagInfoMap.put(epc, tagInfo);
                            } else {
                                int currentCount = tagInfo.getCount();
                                tagInfo.setCount(currentCount + 1);
                            }
                        }
                        scheduleTableUpdate();
                    }

                    @Override
                    public void onError(String msg) {
                        LogUtils.logout("DEBUG-RFID", "asyncGetTagDataList error: " + msg);
                    }
                }, Schedulers.immediate());

            }
        }
    }
}
