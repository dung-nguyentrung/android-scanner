package com.harry.uhf_c.ui.inventory;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.harry.uhf_c.R;
import com.harry.uhf_c.adapter.TagAdapter;
import com.harry.uhf_c.databinding.FragmentInventoryBinding;
import com.harry.uhf_c.entity.TagInfo;

import com.harry.uhf_c.utils.SettingsStorage;
import com.realopeniot.uar_utils.GpioUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class InventoryFragment extends Fragment {

    private FragmentInventoryBinding binding;
    private InventoryViewModel inventoryViewModel;
    private static final int START = 1;
    private static final int STOP = 0;

    private TagAdapter tagAdapter;
    private SettingsStorage storage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Khởi tạo ViewModel
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        binding.rvTagData.setLayoutManager(new LinearLayoutManager(getContext()));
        tagAdapter = new TagAdapter(new ArrayList<>());
        binding.rvTagData.setAdapter(tagAdapter);

        // Khởi tạo GpioUtils và SettingsStorage
        GpioUtils gpioUtils = new GpioUtils(getContext());
        gpioUtils.setRfidEn(GpioUtils.RfidEnStatusEnum.STATUS_ON);
        storage = new SettingsStorage(requireContext());

        // Hiển thị thông tin IP, API Url, Device SN
        binding.tvIp.setText("IP: " + getDeviceIpAddress() + " | API Url: " + storage.getApiAddress() + " | Device sn: " + storage.getDeviceSn());

        // Quan sát LiveData từ ViewModel
        inventoryViewModel.tagInfoList.observe(getViewLifecycleOwner(), tagInfos -> {
            tagAdapter.updateList(tagInfos);
        });

        inventoryViewModel.identifiedTagCount.observe(getViewLifecycleOwner(), count -> {
            binding.tvIdentifiedCount.setText(String.valueOf(count));
        });

        inventoryViewModel.rfStatusMessage.observe(getViewLifecycleOwner(), message -> {
            binding.tvRfStatusMessage.setText(message);
        });


        // Khởi tạo trạng thái ban đầu của nút và bắt đầu đọc
        // Do ViewModel đã tự động khởi tạo Reader và chúng ta muốn trạng thái nút khớp với ViewModel
        // Chúng ta có thể gọi startInventory() từ ViewModel ngay khi Fragment được tạo, hoặc
        // để ViewModel tự quản lý việc này nếu nó cần chạy liên tục.
        // Trong trường hợp này, chúng ta sẽ cho ViewModel bắt đầu đọc ngay khi nó được tạo
        // và Fragment sẽ cập nhật UI dựa trên trạng thái của ViewModel.
        // Do initReader() trong ViewModel không tự động startInventory(), chúng ta cần gọi nó ở đây
        // Hoặc ViewModel có thể có một cờ để tự động bắt đầu khi được khởi tạo.
        // Để đơn giản, chúng ta sẽ giả định ViewModel được khởi tạo và sẵn sàng.
        // Trạng thái nút ban đầu sẽ phụ thuộc vào việc ViewModel đang đọc hay không.
        // Tuy nhiên, vì ViewModel không tiếp xúc trực tiếp với UI (nút),
        // chúng ta sẽ cần ViewModel cung cấp LiveData cho trạng thái đọc.
        // Tạm thời, chúng ta sẽ bắt đầu đọc ngay và cập nhật UI.
        onStartClicked(START); // Bắt đầu đọc khi Fragment được tạo


        binding.btnInventory.setOnClickListener(v -> {
            boolean starting = binding.btnInventory.getText().toString().equals(getString(R.string.start));
            onStartClicked(starting ? START : STOP);
        });

        binding.btnClearData.setOnClickListener(v -> onResetClicked());
        binding.btnUpload.setOnClickListener(v -> onUploadClicked());

        return binding.getRoot();
    }

    private String getDeviceIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP_ERROR", "Không thể lấy IP", ex);
        }
        return "Không xác định";
    }

    public void onStartClicked(int type) {
        if (type == START) {
            inventoryViewModel.startInventory();
            binding.btnInventory.setText(R.string.stop);
            binding.btnInventory.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.buttonRed));
        } else {
            inventoryViewModel.stopInventory();
            binding.btnInventory.setText(R.string.start);
            binding.btnInventory.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.buttonPrimary));
        }
    }

    public void onResetClicked() {
        inventoryViewModel.resetTagData();
        Toast.makeText(getContext(), "Dữ liệu đã được xóa!", Toast.LENGTH_SHORT).show();
    }

    public void onUploadClicked() {
        // Trước khi upload, dừng việc đọc
        inventoryViewModel.stopInventory(); // Dừng đọc qua ViewModel
        binding.btnInventory.setText(R.string.start); // Đặt lại nút về "Start"
        binding.btnInventory.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.buttonPrimary));


        // Lấy dữ liệu hiện tại từ ViewModel để upload
        List<TagInfo> tagsToUpload = inventoryViewModel.tagInfoList.getValue();
        if (tagsToUpload == null || tagsToUpload.isEmpty()) {
            Toast.makeText(getContext(), "Không có thẻ để tải lên!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SettingsStorage storage = new SettingsStorage(requireContext());
            // Tạo JSON structure
            JSONObject root = new JSONObject();
            root.put("timestamp", getCurrentFormattedTime());
            root.put("method", "device_report_probe_data");
            root.put("sn", storage.getDeviceSn());

            long currentMillis = System.currentTimeMillis();
            JSONObject data = new JSONObject();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            String fileNameTime = sdf.format(new Date(currentMillis));
            data.put("timestamp", String.valueOf(currentMillis));
            data.put("id", storage.getDeviceSn());
            data.put("temperature", "0");

            JSONArray tagArray = new JSONArray();
            for (TagInfo tag : tagsToUpload) { // Sử dụng dữ liệu từ ViewModel
                JSONObject tagJson = new JSONObject();
                tagJson.put("direction", "1");
                tagJson.put("firstTime", currentMillis);
                tagJson.put("lastTime", currentMillis);
                tagJson.put("firstAnt", tag.getAnt());
                tagJson.put("ant", tag.getAnt());
                tagJson.put("rssi", "11");
                tagJson.put("epc", tag.getEpc());
                tagArray.put(tagJson);
            }

            data.put("tagList", tagArray);
            root.put("data", data);

            // Ghi vào file
            String fileName = "hwl_" + fileNameTime + ".json";
            File dir = new File(requireContext().getExternalFilesDir(null), "logs");
            if (!dir.exists() && !dir.mkdirs()) {
                Toast.makeText(getContext(), "Không thể tạo thư mục logs.", Toast.LENGTH_SHORT).show();
                return;
            }
            File file = new File(dir, fileName);

            FileWriter writer = new FileWriter(file);
            writer.write(root.toString(4));
            writer.close();

            // Gửi dữ liệu lên API
            new Thread(() -> {
                try {
                    URL url = new URL(storage.getApiAddress());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    byte[] input = root.toString().getBytes(StandardCharsets.UTF_8);
                    conn.setFixedLengthStreamingMode(input.length);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();

                    requireActivity().runOnUiThread(() -> {
                        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                            Toast.makeText(getContext(), "Upload thành công !", Toast.LENGTH_SHORT).show();
                            // Sau khi upload thành công, xóa dữ liệu qua ViewModel
                            inventoryViewModel.resetTagData();
                        } else {
                            Toast.makeText(getContext(), "Upload thất bại ! Mã lỗi: " + responseCode , Toast.LENGTH_SHORT).show();
                        }
                    });

                    conn.disconnect();
                } catch (Exception ex) {
                    Log.e("Upload", "Lỗi khi gửi dữ liệu: " + ex.getMessage(), ex);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Lỗi khi gửi dữ liệu: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();


        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi tạo/ghi file JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Upload", "Error in onUploadClicked: " + e.getMessage(), e);
        }
    }

    private String getCurrentFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return sdf.format(new Date());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Không cần gọi stopReading() trực tiếp ở đây vì ViewModel sẽ tự động xử lý khi onCleared() được gọi.
        // Điều này đảm bảo ViewModel có thể sống sót qua các thay đổi cấu hình.
        binding = null;
    }
}