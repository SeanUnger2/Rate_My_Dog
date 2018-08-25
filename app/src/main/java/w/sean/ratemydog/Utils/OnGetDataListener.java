package w.sean.ratemydog.Utils;

import com.google.firebase.database.DataSnapshot;

public interface OnGetDataListener {
    void onSuccess(DataSnapshot dataSnapshot);
}
