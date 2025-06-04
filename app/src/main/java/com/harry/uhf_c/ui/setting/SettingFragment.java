package com.harry.uhf_c.ui.setting;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.harry.uhf_c.databinding.FragmentSettingBinding;
import com.harry.uhf_c.utils.SettingsStorage;

public class SettingFragment extends Fragment {

    private FragmentSettingBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SettingsStorage storage = new SettingsStorage(requireContext());

        // Load lại config nếu cần
        for (int i = 1; i <= 8; i++) {
            int switchId = getResources().getIdentifier("switch_ant" + i, "id", requireContext().getPackageName());
            int editId = getResources().getIdentifier("edit_power_ant" + i, "id", requireContext().getPackageName());

            Switch antSwitch = root.findViewById(switchId);
            EditText powerEdit = root.findViewById(editId);

            antSwitch.setChecked(storage.isAntennaEnabled(i));
            powerEdit.setText(String.valueOf(storage.getAntennaPower(i)));
        }

        binding.editApiAddress.setText(storage.getApiAddress());
        binding.editDeviceSn.setText(storage.getDeviceSn());

        binding.btnSaveSettings.setOnClickListener(v -> {
            try {
                for (int i = 1; i <= 8; i++) {
                    int switchId = getResources().getIdentifier("switch_ant" + i, "id", requireContext().getPackageName());
                    int editId = getResources().getIdentifier("edit_power_ant" + i, "id", requireContext().getPackageName());

                    Switch antSwitch = root.findViewById(switchId);
                    EditText powerEdit = root.findViewById(editId);

                    if (antSwitch != null && powerEdit != null) {
                        boolean isEnabled = antSwitch.isChecked();
                        int power = 0;
                        try {
                            power = Integer.parseInt(powerEdit.getText().toString().trim());
                        } catch (Exception ignored) {}

                        storage.saveAntenna(i, isEnabled, power);
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy Switch/EditText cho Ant " + i, Toast.LENGTH_SHORT).show();
                    }
                }


                storage.saveApiAddress(binding.editApiAddress.getText().toString().trim());
                storage.saveDeviceSn(binding.editDeviceSn.getText().toString().trim());

                Toast.makeText(getContext(), "Đã lưu cấu hình", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("ERFID",e.getMessage());
                throw new RuntimeException(e);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}