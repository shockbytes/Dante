package at.shockbytes.dante.dagger;

import javax.inject.Singleton;

import at.shockbytes.dante.core.BackupActivity;
import at.shockbytes.dante.core.DetailActivity;
import at.shockbytes.dante.core.DownloadActivity;
import at.shockbytes.dante.core.MainActivity;
import at.shockbytes.dante.fragments.DownloadBookFragment;
import at.shockbytes.dante.fragments.MainBookFragment;
import at.shockbytes.dante.fragments.dialogs.StatsDialogFragment;
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
