package w.sean.ratemydog.Utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {

    private static FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference();
}
