package com.example.cuoiky;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimerDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Sử dụng thời gian hiện tại làm giá trị mặc định cho TimePicker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Tạo một đối tượng TimePickerDialog và trả về nó
        return new TimePickerDialog(getActivity(), this, hour, minute, false);
    }

    // Trong lớp MusicPlayerActivity
    private void showTimerDialog() {
        TimerDialog timerDialog = new TimerDialog();
        timerDialog.show(getChildFragmentManager(), "timer_dialog");
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Xử lý sự kiện khi người dùng chọn thời gian
        // Ở đây, bạn có thể lưu thời gian và thực hiện hành động tắt nhạc khi đến thời gian đó
        String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
        Toast.makeText(getActivity(), "Selected time: " + selectedTime, Toast.LENGTH_SHORT).show();

        // Gọi hàm để thực hiện hành động tắt nhạc ở đây
        // Ví dụ: tắt nhạc khi đến thời gian được chọn
        stopMusicAtSelectedTime(hourOfDay, minute);
    }

    private void stopMusicAtSelectedTime(int hourOfDay, int minute) {
        // Thực hiện hành động tắt nhạc ở đây dựa trên thời gian được chọn
        // Ví dụ: dùng AlarmManager để đặt hẹn giờ
        // Lưu ý: Đối với các hành động phức tạp, có thể cần sử dụng IntentService hoặc JobIntentService
        // để thực hiện trong nền.
    }
}

