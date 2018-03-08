package de.mario222k.mangarx.chapter;

import javax.inject.Singleton;

import dagger.Component;
import de.mario222k.mangarx.module.AppModule;

@Singleton
@Component(modules = {AppModule.class, ChapterModule.class})
public interface ChapterComponent {
    void inject (ChapterActivity activity);
}
