package gt0.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DB {
    Context mContext;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public DB(Context context) {
        mContext = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = sharedPreferences.edit();
    }

    public boolean isPremium() {
        return sharedPreferences.getBoolean("premium", false);
    }

    public void setPremium(boolean premium) {
        editor.putBoolean("premium", premium);
        editor.apply();
    }

}
