/*
 * Copyright 2013 Google Inc.
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

package com.savare.sincronizacao;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/** Service to handle sync requests.
 *
 * <p>This service is invoked in response to Intents with action android.content.SavareSyncAdapter, and
 * returns a Binder connection to SavareSyncAdapter.
 *
 * <p>For performance, only one sync adapter will be initialized within this application's context.
 *
 * <p>Note: The SavareSyncService itself is not notified when a new sync occurs. It's role is to
 * manage the lifecycle of our {@link SavareSyncAdapter} and provide a handle to said SavareSyncAdapter to the
 * OS on request.
 */
public class SavareSyncService extends Service {
    private static final String TAG = "SAVARE";

    private static final Object sSyncAdapterLock = new Object();
    private static SavareSyncAdapter sSavareSyncAdapter = null;

    /**
     * Thread-safe constructor, creates static {@link SavareSyncAdapter} instance.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Service created - SuyncService");

        synchronized (sSyncAdapterLock) {
            if (sSavareSyncAdapter == null) {
                sSavareSyncAdapter = new SavareSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed - SuyncService");
    }

    /**
     * Return Binder handle for IPC communication with {@link SavareSyncAdapter}.
     *
     * <p>New sync requests will be sent directly to the SavareSyncAdapter using this channel.
     *
     * @param intent Calling intent
     * @return Binder handle for {@link SavareSyncAdapter}
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind - SuyncService");

        return sSavareSyncAdapter.getSyncAdapterBinder();
    }
}
