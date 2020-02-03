/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package ru.rian.testanhuawei;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;




// TODO: import classes from Analytics Kit
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;
import static com.huawei.hms.analytics.type.HAEventType.*;
import static com.huawei.hms.analytics.type.HAParamType.*;

public class MainActivity extends AppCompatActivity {

    // TODO: Define a var for Analytics Instance
    HiAnalyticsInstance instance;

    private Button btnSetting;

    private int questions [] = {R.string.q1,R.string.q2,R.string.q3,R.string.q4,R.string.q5};
    private boolean answers [] = {true,true,false,false,true};

    private int curQuestionIdx = 0;

    private TextView txtQuestion;

    private Button btnNext;

    private Button btnTrue;

    private Button btnFalse;

    private Button postScore;

    private int score = 0;


    // TODO: Define a var for Analytics Instance
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: Initiate Analytics Kit

        // Enable Analytics Kit Log
        HiAnalyticsTools.enableLog();
        // Generate the Analytics Instance
        instance = HiAnalytics.getInstance(this);
        // You can also use Context initialization
        //Context context = this.getApplicationContext();
        //instance = HiAnalytics.getInstance(context);

        // Enable the collection capability
        instance.setAnalyticsEnabled(true);
        // Enable the automatic collection capability.
        instance.setAutoCollectionEnabled(true);

        // TODO: Registering the HMS Service
// Register the HMS service and collect automatic events (account events).
// Automatic account event collection requires HMS APK 4.0.0.300 or a later version.
        instance.regHmsSvcEvent();
		
        txtQuestion = (TextView)findViewById(R.id.question_text_view);
        txtQuestion.setText(questions[curQuestionIdx]);

        btnSetting = (Button)findViewById(R.id.setting_button);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivityForResult(i,0);
            }
        });

        btnNext = (Button)findViewById(R.id.next_button);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curQuestionIdx = (curQuestionIdx+1) % questions.length;
                nextQuestion();
            }
        });

        btnTrue = (Button)findViewById(R.id.true_button);
        btnTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(true);
                reportAnswerEvt("true");


            }
        });

/**
 * Obtain a token.
 */

        btnFalse = (Button)findViewById(R.id.false_button);
        btnFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(false);
                reportAnswerEvt("false");
            }
        });

        postScore = (Button)findViewById(R.id.post_score_button);
        postScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postScore();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: UnRegistering the HMS Service
        // TODO: Deregistering the HMS Service
        // After the HMS service is unbound, no automatic event (account event) is reported.
        instance.unRegHmsSvcEvent();
    }

    private void getToken() {
        Log.i(MyPushService.TAG, "get token: begin");

        // get token
        new Thread() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(MainActivity.this).getString("client/app_id");
                    pushtoken = HmsInstanceId.getInstance(MainActivity.this).getToken(appId, "HCM");
                    if(!TextUtils.isEmpty(pushtoken)) {
                        Log.i(MyPushService.TAG, "get token:" + pushtoken);
                        showLog(pushtoken);
                    }
                } catch (Exception e) {
                    Log.i(MyPushService.TAG,"getToken failed, " + e);

                }
            }
        }.start();
    }

    String pushtoken;

    private void showLog(final String log) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (txtQuestion != null) {
                    txtQuestion.setText(log);
                    Toast.makeText(MainActivity.this, pushtoken, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void nextQuestion() {
        txtQuestion.setText(questions[curQuestionIdx]);
    }

    private boolean checkAnswer(boolean answer) {
        String q =txtQuestion.getText().toString().trim();

        if(answer == answers[curQuestionIdx]) {
            score = score + 20;
            Toast.makeText(this,R.string.correct_answer, Toast.LENGTH_SHORT).show();
            // TODO: Report a customized Event

        }
        else {
            Toast.makeText(this,R.string.wrong_answer, Toast.LENGTH_SHORT).show();
            // TODO: Report a customized Event

        }
        return answers[curQuestionIdx];
    }

    private void reportAnswerEvt(String answer) {
        // TODO: Report a customzied Event
        // Event Name: Answer
        // Event Parameters:
        //  -- question: String
        //  -- answer:String
        //  -- answerTime: String

        // Initialize parameters.
        Bundle bundle = new Bundle();
        bundle.putString("question", txtQuestion.getText().toString().trim());
        bundle.putString("answer",answer);
/*
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        bundle.putString("answerTime",sdf.format(new Date()));
*/

        // Report a predefined event.
        instance.onEvent("Answer", bundle);

    }

    private void postScore() {
/*        // TODO: Report score by using SUBMITSCORE Event

        // Initiate Parameters
        Bundle bundle = new Bundle();
        bundle.putLong(SCORE, score);

        // Report a predefined Event
        instance.onEvent(SUBMITSCORE, bundle);*/
        getToken();
    }
}
