package at.shockbytes.dante.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;

import at.shockbytes.dante.R;
import at.shockbytes.dante.ui.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    
    public static Intent newIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
		}
		setContentView(R.layout.activity_settings);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(false);
		}
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.content_frame, SettingsFragment.newInstance())
				.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		//Finish activity when user presses home button
		if (item.getItemId() == android.R.id.home) {
			supportFinishAfterTransition();
		}
		return super.onOptionsItemSelected(item);
	}

}
