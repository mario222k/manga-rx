package de.mario222k.mangarx.plugin;

import javax.inject.Singleton;

import dagger.Component;
import de.mario222k.mangarx.application.PluginDialogFragment;
import de.mario222k.mangarx.module.AppModule;

@Singleton
@Component(modules = {AppModule.class})
public interface PluginComponent {
    void inject (PluginDialogFragment fragment);
}
