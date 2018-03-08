package de.mario222k.mangarx.recentlist;

import javax.inject.Singleton;

import dagger.Component;
import de.mario222k.mangarx.module.AppModule;

@Singleton
@Component(modules = {AppModule.class, RecentModule.class})
public interface RecentComponent {
    void inject (RecentActivity activity);
}
