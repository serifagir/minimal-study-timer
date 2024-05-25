package com.example.pomodoro_timer_java;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

public class PomodoroController {
    @FXML private Label sessionNameLabel = new Label();
    @FXML private Label timerLabel;
    @FXML private ProgressBar sessionProgressBar;
    @FXML private Button restartButton;
    @FXML private Button startButton;
    @FXML private Button skipSessionButton = new Button();

    @FXML private Slider studyTimeSlider;
    @FXML private Slider shortBreakTimeSlider;
    @FXML private Slider longBreakTimeSlider;
    @FXML private Slider sessionCountSlider;

    @FXML private Label studyTimeSliderLabel;
    @FXML private Label shortBreakTimeSliderLabel;
    @FXML private Label longBreakTimeSliderLabel;
    @FXML private Label sessionCountSliderLabel;

    @FXML private int studyTime;
    @FXML private int shortBreakTime;
    @FXML private int longBreakTime;
    @FXML private int sessionCount;

    @FXML private RadioButton autoStartRadioButton;

    private Timer timer ;
    private TimerTask timerTask;
    private int seconds;
    private boolean isTimerRunning = false;
    private boolean isStudyTimeRunning = true;
    int currentSessionCount;

    FontIcon pauseIcon = new FontIcon("bi-pause");
    FontIcon startIcon = new FontIcon("bi-play");

    Preferences preferences = Preferences.userRoot();

    public void initialize() {
        studyTimeSlider.setValue(preferences.getInt("studyTimePrefValue", 25));
        studyTimeSliderLabel.setText(Integer.toString((int)studyTimeSlider.getValue()));
        studyTime = (int) studyTimeSlider.getValue();

        shortBreakTimeSlider.setValue(preferences.getInt("shortBreakTimePrefValue", 5));
        shortBreakTimeSliderLabel.setText(Integer.toString((int)shortBreakTimeSlider.getValue()));
        shortBreakTime = (int) shortBreakTimeSlider.getValue();

        longBreakTimeSlider.setValue(preferences.getInt("longBreakTimePrefValue", 15));
        longBreakTimeSliderLabel.setText(Integer.toString((int)longBreakTimeSlider.getValue()));
        longBreakTime = (int) longBreakTimeSlider.getValue();

        sessionCountSlider.setValue(preferences.getInt("sessionCountPrefValue", 4));
        sessionCountSliderLabel.setText(Integer.toString((int)sessionCountSlider.getValue()));
        sessionCount = (int) sessionCountSlider.getValue();


        isStudyTimeRunning = false;
        isTimerRunning = false;
        sessionHandler();

        pauseIcon.setIconSize(64);
        startIcon.setIconSize(64);
    }

    @FXML private void toggleTimer() {
        if(!isTimerRunning) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    updateTimer();
                }
            };
            isStudyTimeRunning = true;
            isTimerRunning = true;
            timer.scheduleAtFixedRate(timerTask, 0, 1000);
            startButton.setGraphic(pauseIcon);
            studyTimeSlider.setDisable(true);
            shortBreakTimeSlider.setDisable(true);
            longBreakTimeSlider.setDisable(true);
            sessionCountSlider.setDisable(true);
        } else {
            isTimerRunning = false;
            startButton.setGraphic(startIcon);
            timer.cancel();
            timer.purge();
            timerTask.cancel();
        }
    }

    @FXML protected void sessionHandler() {
        if(currentSessionCount == 2 * sessionCount) {
            currentSessionCount = 0;
            seconds = studyTime * 60;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    timerLabel.setText(formatTimer(seconds));
                    sessionNameLabel.setText("study");
                }
            });
        }  else {
            if (currentSessionCount % 2 == 0) {
                seconds = studyTime * 1;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        timerLabel.setText(formatTimer(seconds));
                        sessionNameLabel.setText("study");
                    }
                });
                skipSessionButton.setVisible(false);
            } else if (currentSessionCount % 2 == 1 && currentSessionCount != 2 * sessionCount - 1 ) {
                seconds = shortBreakTime * 10;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        timerLabel.setText(formatTimer(seconds));
                        sessionNameLabel.setText("break");
                    }
                });
                skipSessionButton.setVisible(true);
            } else if (currentSessionCount == 2 * sessionCount - 1 ) {
                seconds = longBreakTime * 60;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        timerLabel.setText(formatTimer(seconds));
                        sessionNameLabel.setText("long break");
                    }
                });
                skipSessionButton.setVisible(true);
            }
        }
    }

    @FXML protected void updateTimer() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                timerLabel.setText(formatTimer(seconds));
            }
        });
        seconds--;
        if(seconds == 0) {
            jumpNextSession();
        }
    }

    @FXML protected void jumpNextSession() {
        handleSessionCountBar((double)1/sessionCount);
        currentSessionCount++;
        sessionHandler();
        isTimerRunning = false;
        timer.cancel();
        timer.purge();
        timerTask.cancel();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                startButton.setGraphic(startIcon);
            }
        });
        isStudyTimeRunning = false;
        studyTimeSlider.setDisable(false);
        shortBreakTimeSlider.setDisable(false);
        longBreakTimeSlider.setDisable(false);
        sessionCountSlider.setDisable(false);
    }

    protected String formatTimer(int seconds) {
        int tempMinutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", tempMinutes, remainingSeconds);
    }

    protected void handleSessionCountBar(double valueToAdd) {
        if (currentSessionCount % 2 == 0) {
            double prevVal = sessionProgressBar.getProgress();
            sessionProgressBar.setProgress(prevVal + valueToAdd);
        }
//        if (currentSessionCount == 0) {
//            sessionProgressBar.setProgress(0);
//        }
    }

    @FXML protected void skipForward() {
        handleSessionCountBar((double)1/sessionCount);
        currentSessionCount++;
        sessionHandler();
        timer.cancel();
        timer.purge();
        startButton.setGraphic(startIcon);
    }

    @FXML protected void setStudyTime () {
        if (!isStudyTimeRunning)  {
            int studyTimeSliderValue = (int) studyTimeSlider.getValue();
            studyTimeSlider.setDisable(false);
            studyTimeSliderLabel.setText(Integer.toString(studyTimeSliderValue));
            studyTime = (int) studyTimeSlider.getValue();
            preferences.putInt("studyTimePrefValue", (int) studyTimeSlider.getValue());
            sessionHandler();
        } else {
            studyTimeSlider.setDisable(true);
        }
    }

    @FXML protected void setShortBreakTime () {
        if (!isStudyTimeRunning) {
            shortBreakTimeSlider.setDisable(false);
            shortBreakTimeSliderLabel.setText(Integer.toString((int)shortBreakTimeSlider.getValue()));
            shortBreakTime = (int) shortBreakTimeSlider.getValue();
            preferences.putInt("shortBreakTimePrefValue", (int) shortBreakTimeSlider.getValue());
            sessionHandler();
        } else {
            shortBreakTimeSlider.setDisable(true);
        }
    }

    @FXML protected void setLongBreakTime () {
        if (!isStudyTimeRunning) {
            longBreakTimeSlider.setDisable(false);
            longBreakTimeSliderLabel.setText(Integer.toString((int)longBreakTimeSlider.getValue()));
            longBreakTime = (int) longBreakTimeSlider.getValue();
            preferences.putInt("longBreakTimePrefValue", (int) longBreakTimeSlider.getValue());
            sessionHandler();
        } else {
            longBreakTimeSlider.setDisable(true);
        }
    }

    @FXML protected void setSessionCount () {
        if (!isStudyTimeRunning) {
            sessionCountSlider.setDisable(false);
            sessionCountSliderLabel.setText(Integer.toString((int)sessionCountSlider.getValue()));
            sessionCount = (int) sessionCountSlider.getValue();
            preferences.putInt("sessionCountPrefValue", (int) sessionCountSlider.getValue());
            sessionHandler();
        } else {
            sessionCountSlider.setDisable(true);
        }
    }

    @FXML protected void resetSessions () {
        currentSessionCount = 0;
        timer.cancel();
        timer.purge();
        timerTask.cancel();
        startButton.setGraphic(startIcon);
        sessionProgressBar.setProgress(0);
        isStudyTimeRunning = false;
        isTimerRunning = false;
        sessionHandler();
    }


}