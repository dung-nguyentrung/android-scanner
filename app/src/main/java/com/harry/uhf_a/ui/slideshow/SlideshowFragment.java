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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

        loadTodayLogFiles();

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
        Uri fileUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Chia sẻ file JSON qua..."));
    }


    private void loadTodayLogFiles() {
        File logDir = new File(requireContext().getExternalFilesDir(null), "logs");
        if (!logDir.exists()) return;

        String todayPrefix = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault())
                .format(new Date());

        File[] files = logDir.listFiles((dir, name) ->
                name.endsWith(".json") && name.startsWith("hwl_" + todayPrefix));

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
