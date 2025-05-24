package com.harry.uhf_a.ui.slideshow;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.harry.uhf_a.R;
import com.harry.uhf_a.adapter.LogFileAdapter;
import com.harry.uhf_a.databinding.FragmentSlideshowBinding;
import com.harry.uhf_a.utils.SettingsStorage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SlideshowFragment extends Fragment {

    private RecyclerView recyclerView;
    private LogFileAdapter adapter;
    private List<File> todayFiles = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(com.harry.uhf_a.R.layout.fragment_slideshow, container, false);
        recyclerView = root.findViewById(R.id.recyclerViewLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//        loadTodayLogFiles();
        loadRecentLogFiles();
        adapter = new LogFileAdapter(todayFiles, new LogFileAdapter.OnLogFileActionListener() {
            @Override
            public void onView(File file) {
                showFileContentDialog(file);
            }

            @Override
            public void onDelete(File file) {
                boolean deleted = file.delete();
                if (deleted) {
                    todayFiles.remove(file);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Đã xóa " + file.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Không thể xóa file", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onShare(File file) {
                shareFile(file);
            }
        });


        recyclerView.setAdapter(adapter);

        return root;
    }

    private void shareFile(File file) {
        new Thread(() -> {
            try {
                byte[] input;
                try (FileInputStream fis = new FileInputStream(file)) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = fis.read(buffer)) != -1) {
                        bos.write(buffer, 0, read);
                    }
                    input = bos.toByteArray();
                }
                SettingsStorage storage = new SettingsStorage(requireContext());
                URL url = new URL(storage.getApiAddress());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(input.length);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();

                requireActivity().runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        Toast.makeText(getContext(), "Upload thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Upload thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });

                conn.disconnect();
            } catch (Exception ex) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Lỗi khi gửi dữ liệu: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();


    }


//    private void  loadTodayLogFiles() {
//        File logDir = new File(requireContext().getExternalFilesDir(null), "logs");
//        if (!logDir.exists()) return;
//
//        String todayPrefix = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault())
//                .format(new Date());
//
//        File[] files = logDir.listFiles((dir, name) ->
//                name.endsWith(".json") && name.startsWith("hwl_" + todayPrefix));
//
//        if (files != null) {
//            todayFiles.clear();
//            todayFiles.addAll(Arrays.asList(files));
//            Collections.sort(todayFiles, Comparator.comparing(File::getName).reversed());
//        }
//    }

    private void loadRecentLogFiles() {
        File logDir = new File(requireContext().getExternalFilesDir(null), "logs");
        if (!logDir.exists()) return;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());

        // Hôm nay
        Calendar calendar = Calendar.getInstance();
        String todayPrefix = dateFormat.format(calendar.getTime());

        // Hôm qua
        calendar.add(Calendar.DATE, -1);
        String yesterdayPrefix = dateFormat.format(calendar.getTime());

        File[] files = logDir.listFiles((dir, name) ->
                name.endsWith(".json") && (
                        name.startsWith("hwl_" + todayPrefix) ||
                                name.startsWith("hwl_" + yesterdayPrefix)
                )
        );

        if (files != null) {
            todayFiles.clear();
            todayFiles.addAll(Arrays.asList(files));
            Collections.sort(todayFiles, Comparator.comparing(File::getName).reversed());
        }
    }


    private void showFileContentDialog(File file) {
        try {
            String content = readFileContent(file);
            new AlertDialog.Builder(requireContext())
                    .setTitle(file.getName())
                    .setMessage(content)
                    .setPositiveButton("Đóng", null)
                    .show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Lỗi đọc file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String readFileContent(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }

        fis.close();
        return baos.toString("UTF-8");
    }

}
