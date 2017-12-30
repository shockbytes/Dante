package at.shockbytes.dante.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import at.shockbytes.dante.dagger.AppComponent;
import at.shockbytes.dante.ui.fragment.SettingsFragment;

public class SettingsActivity extends BackNavigableActivity {
    
    public static Intent newIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, SettingsFragment.newInstance())
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

	@Override
	public void injectToGraph(@NotNull AppComponent appComponent) {
		// Do nothing
	}
}
