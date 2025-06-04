package com.harry.uhf_c.ui.log;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.harry.uhf_c.adapter.LogFileAdapter;
import com.harry.uhf_c.databinding.FragmentLogBinding;
import com.harry.uhf_c.utils.SettingsStorage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LogFragment extends Fragment {

    private FragmentLogBinding binding;
    private LogFileAdapter adapter;
    private List<File> todayFiles = new ArrayList<>();
    private List<File> originalFileList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // RecyclerView setup
        binding.recyclerViewLogs.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load and display logs
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
                    originalFileList.remove(file);
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

        binding.recyclerViewLogs.setAdapter(adapter);

        // Search bar filter
        binding.editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLogs(s.toString());
            }
        });

        return root;
    }

    private void filterLogs(String query) {
        todayFiles.clear();
        if (query.isEmpty()) {
            todayFiles.addAll(originalFileList);
        } else {
            for (File file : originalFileList) {
                if (file.getName().toLowerCase().contains(query.toLowerCase())) {
                    todayFiles.add(file);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void shareFile(File file) {
        new Thread(() -> {
            try {
                byte[] input;
                try (FileInputStream fis = new FileInputStream(file);
                     ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
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
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Lỗi khi gửi dữ liệu: " + ex.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void loadRecentLogFiles() {
        File logDir = new File(requireContext().getExternalFilesDir(null), "logs");
        if (!logDir.exists()) return;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        String todayPrefix = dateFormat.format(calendar.getTime());

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
            originalFileList.clear();
            originalFileList.addAll(todayFiles);
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
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toString("UTF-8");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
