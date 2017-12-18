package at.shockbytes.dante.dagger;

import javax.inject.Singleton;

import at.shockbytes.dante.ui.activity.BackupActivity;
import at.shockbytes.dante.ui.activity.DetailActivity;
import at.shockbytes.dante.ui.activity.DownloadActivity;
import at.shockbytes.dante.ui.activity.MainActivity;
import at.shockbytes.dante.ui.fragment.DownloadBookFragment;
import at.shockbytes.dante.ui.fragment.MainBookFragment;
import at.shockbytes.dante.ui.fragment.dialogs.StatsDialogFragment;
import at.shockbytes.dante.util.barcode.QueryCaptureActivity;
import dagger.Component;

/**
 * @author Martin Macheiner
 *         Date: 19.01.2017.
 */

@Singleton
@Component(modules = {NetworkModule.class, AppModule.class})
public interface AppComponent {

    void inject(MainActivity activity);

    void inject(DetailActivity activity);

    void inject(BackupActivity activity);

    void inject(QueryCaptureActivity activity);

    void inject(DownloadActivity activity);

    void inject(MainBookFragment fragment);

    void inject(StatsDialogFragment fragment);

    void inject(DownloadBookFragment fragment);

}
