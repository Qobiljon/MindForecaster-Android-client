package kr.ac.inha.nsl.mindnavigator;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Tools.RES_OK)
            switch (requestCode) {
                case INTERVENTION_ACTIVITY:
                    event.setIntervention(InterventionsActivity.result);
                    InterventionsActivity.result = null;
                    break;
                case EVALUATION_ACTIVITY:
                    // TODO: Do something
                    break;
                case FEEDBACK_ACTIVITY:
                    // TODO: Do something
                    break;
                default:
                    break;
            }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //region Variables
    private final int EVALUATION_ACTIVITY = 0, INTERVENTION_ACTIVITY = 1, FEEDBACK_ACTIVITY = 2;
    static Event event;

    private EditText eventTitle, stressCause;
    private TextView startDateText, startTimeText, endDateText, endTimeText;
    private SeekBar stressLvl;
    private Switch switchAllDay;
    private ViewGroup inactiveLayout;
    private RadioGroup stressTypeGroup;
    private ViewGroup stressLevelDetails;
    private ViewGroup repeatNotificationDetails;
    private Switch shareSwitch;

    private Calendar startTime, endTime;
    //endregion

    private void init() {
        eventTitle = findViewById(R.id.edit_event_title);
        switchAllDay = findViewById(R.id.all_day_switch);
        startDateText = findViewById(R.id.txt_event_start_date);
        startTimeText = findViewById(R.id.txt_event_start_time);
        endDateText = findViewById(R.id.txt_event_end_date);
        endTimeText = findViewById(R.id.txt_event_end_time);
        stressLvl = findViewById(R.id.stressLvl);
        inactiveLayout = findViewById(R.id.layout_to_be_inactive);
        stressTypeGroup = findViewById(R.id.stress_type_group);
        stressCause = findViewById(R.id.txt_stress_cause);
        stressLevelDetails = findViewById(R.id.stress_level_details);
        repeatNotificationDetails = findViewById(R.id.repeat_notification_details);
        shareSwitch = findViewById(R.id.share_switch);

        Calendar selCal = Calendar.getInstance();
        selCal.setTimeInMillis(getIntent().getLongExtra("selectedDayMillis", 0));

        startDateText.setText(String.format(Locale.US,
                "%s, %02d %s",
                selCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()),
                selCal.get(Calendar.DAY_OF_MONTH),
                selCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        ));
        startDateText.setTag(selCal.getTimeInMillis());

        startTimeText.setText(String.format(Locale.US,
                "%02d:%02d",
                selCal.get(Calendar.HOUR),
                selCal.get(Calendar.MINUTE)));
        startTimeText.setTag(selCal.getTimeInMillis());
        startTime = (Calendar) selCal.clone();

        selCal.add(Calendar.HOUR, 1);
        endDateText.setText(String.format(Locale.US,
                "%s, %02d %s",
                selCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()),
                selCal.get(Calendar.DAY_OF_MONTH),
                selCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        ));
        endDateText.setTag(selCal.getTimeInMillis());

        endTimeText.setText(String.format(Locale.US,
                "%02d:%02d",
                selCal.get(Calendar.HOUR),
                selCal.get(Calendar.MINUTE)));
        endDateText.setTag(selCal.getTimeInMillis());
        endTime = (Calendar) selCal.clone();

        stressLvl.getProgressDrawable().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.slvl0_color, null), PorterDuff.Mode.SRC_IN);
        stressLvl.getThumb().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.slvl0_color, null), PorterDuff.Mode.SRC_IN);
        stressLvl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= 0 && progress < 50) {
                    int slvl0Col = ResourcesCompat.getColor(getResources(), R.color.slvl0_color, null);
                    stressLvl.getProgressDrawable().setColorFilter(slvl0Col, PorterDuff.Mode.SRC_IN);
                    stressLvl.getThumb().setColorFilter(slvl0Col, PorterDuff.Mode.SRC_IN);
                    inactiveLayout.setVisibility(View.GONE);
                } else if (progress > 50 && progress < 80) {
                    int slvl1Col = ResourcesCompat.getColor(getResources(), R.color.slvl1_color, null);
                    stressLvl.getProgressDrawable().setColorFilter(slvl1Col, PorterDuff.Mode.SRC_IN);
                    stressLvl.getThumb().setColorFilter(slvl1Col, PorterDuff.Mode.SRC_IN);
                    inactiveLayout.setVisibility(View.VISIBLE);
                } else {
                    int slvl2Col = ResourcesCompat.getColor(getResources(), R.color.slvl2_color, null);
                    stressLvl.getProgressDrawable().setColorFilter(slvl2Col, PorterDuff.Mode.SRC_IN);
                    stressLvl.getThumb().setColorFilter(slvl2Col, PorterDuff.Mode.SRC_IN);
                    inactiveLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        switchAllDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startTimeText.setVisibility(View.GONE);
                    endTimeText.setVisibility(View.GONE);

                    startTime.set(Calendar.HOUR, 0);
                    startTime.set(Calendar.MINUTE, 0);
                    startTime.set(Calendar.SECOND, 0);
                    startTime.set(Calendar.MILLISECOND, 0);
                } else {
                    startTimeText.setVisibility(View.VISIBLE);
                    endTimeText.setVisibility(View.VISIBLE);
                }
            }
        });

        event = new Event();
    }

    public void moreOptionsClick(View view) {
        findViewById(R.id.text_more_event_options).setVisibility(View.GONE);
        findViewById(R.id.more_options_layout).setVisibility(View.VISIBLE);
    }

    public void expandStressLevelClick(View view) {
        TextView optionView = (TextView) view;

        if (stressLevelDetails.getVisibility() == View.VISIBLE) {
            stressLevelDetails.setVisibility(View.GONE);
            optionView.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.img_expand), null);
        } else {
            stressLevelDetails.setVisibility(View.VISIBLE);
            optionView.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.img_collapse), null);
        }
    }

    public void expandRepeatNotificationClick(View view) {
        TextView optionView = (TextView) view;

        if (repeatNotificationDetails.getVisibility() == View.VISIBLE) {
            repeatNotificationDetails.setVisibility(View.GONE);
            optionView.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.img_expand), null);
        } else {
            repeatNotificationDetails.setVisibility(View.VISIBLE);
            optionView.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.img_collapse), null);
        }
    }

    public void interventionsClick(View view) {
        Intent intent = new Intent(this, InterventionsActivity.class);
        startActivityForResult(intent, INTERVENTION_ACTIVITY);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    public void evaluationClick(View view) {
        Intent intent = new Intent(this, EvaluationActivity.class);
        startActivityForResult(intent, EVALUATION_ACTIVITY);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    public void feedbackClick(View view) {
        Intent intent = new Intent(this, FeedbackActivity.class);
        startActivityForResult(intent, FEEDBACK_ACTIVITY);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    public void cancelClick(View view) {
        finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }

    public void saveClick(View view) {
        event.setTitle(eventTitle.getText().toString());
        // region Collect start and end time
        if (!switchAllDay.isChecked()) {
            String startStr = startTimeText.getText().toString();
            String endStr = endTimeText.getText().toString();

            startTime.set(Calendar.HOUR, Integer.parseInt(startStr.substring(0, startStr.indexOf(':'))));
            startTime.set(Calendar.HOUR, Integer.parseInt(startStr.substring(startStr.indexOf(':') + 1)));
            endTime.set(Calendar.HOUR, Integer.parseInt(endStr.substring(0, endStr.indexOf(':'))));
            endTime.set(Calendar.HOUR, Integer.parseInt(endStr.substring(endStr.indexOf(':') + 1)));
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis((long) startDateText.getTag());
        startTime.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
        startTime.set(Calendar.MONTH, cal.get(Calendar.MONTH));
        startTime.set(Calendar.YEAR, cal.get(Calendar.YEAR));
        cal.setTimeInMillis((long) startTimeText.getTag());
        startTime.set(Calendar.HOUR, cal.get(Calendar.HOUR));
        startTime.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);

        cal.setTimeInMillis((long) endDateText.getTag());
        endTime.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
        endTime.set(Calendar.MONTH, cal.get(Calendar.MONTH));
        endTime.set(Calendar.YEAR, cal.get(Calendar.YEAR));
        cal.setTimeInMillis((long) endTimeText.getTag());
        endTime.set(Calendar.HOUR, cal.get(Calendar.HOUR));
        endTime.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        endTime.set(Calendar.SECOND, 0);
        endTime.set(Calendar.MILLISECOND, 0);
        // endregion
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        switch (stressTypeGroup.getCheckedRadioButtonId()) {
            case R.id.stressor_positive:
                event.setStressType("positive");
                break;
            case R.id.stressor_negative:
                event.setStressType("negative");
                break;
            case R.id.stressor_unknown:
                event.setStressType("unknown");
                break;
            default:
                break;
        }
        event.setStressCause(stressCause.getText().toString());
        event.setSharing(shareSwitch.isChecked());

        finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }
}
