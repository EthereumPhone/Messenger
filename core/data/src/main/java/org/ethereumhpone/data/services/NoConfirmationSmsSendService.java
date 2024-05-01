package org.ethereumhpone.data.services;

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.RemoteInput;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


/**
 * Respond to a special intent and send an SMS message without the user's intervention, unless
 * the intent extra "showUI" is true.
 */
public class NoConfirmationSmsSendService extends IntentService {
    private static final String TAG = "NoConfirmationSmsSendService";

    private static final String EXTRA_SUBSCRIPTION = "subscription";
    public static final String EXTRA_SELF_ID = "self_id";

    public NoConfirmationSmsSendService() {
        // Class name will be the thread name.
        super(NoConfirmationSmsSendService.class.getName());

        // Intent should be redelivered if the process gets killed before completing the job.
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        System.out.println("NoConfirmationSmsSendService.onHandleIntent");
    }

}
