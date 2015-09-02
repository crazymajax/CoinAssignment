package com.dermu.coinassignment;

import android.content.res.Resources;
import android.util.Log;

/**
 * A utility class for safe operations.
 * Created by Francois on 8/31/2015.
 */
public class Utils {
    private static final String TAG = "CoinUtils";

    /**
     * simply gets an int value from the resourse but makes sure everything is not null in the
     * process.
     * @param resources the resource of the app (context.getResource())
     * @param resId the id of the integer resource you want to get.
     * @param defaultValue the default value to be retourned in case it can't be found.
     * @return the integer value for the provided resource.
     */
    public static int getIntegerResourceSafely(Resources resources, int resId, int defaultValue) {
        int delay = defaultValue;
        try {
            if (resources != null) {
                delay = resources.getInteger(resId);
            } else {
                Log.e(TAG, "Unable to get resources.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Unable to find credit_card_list_refresh_delay resource.");
        }
        return delay;
    }

}
