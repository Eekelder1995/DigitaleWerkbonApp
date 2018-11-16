package eekelder.digitalewerkbon;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jur-e on 7-10-2017.
 */

interface BooleanChangedListener{
    void onMyBooleanChanged();
}

public class ExecuteStatus {
    private static int status;
    private static BooleanChangedListener listener;// ChangeListener listener;

    public static int getStatus() {
        return status;
    }

    public static void setStatus(int number) {
        status = number;
        listener.onMyBooleanChanged();
    }

    public static void addMyBooleanListener(BooleanChangedListener l) {
    listener = l;
    }
}
