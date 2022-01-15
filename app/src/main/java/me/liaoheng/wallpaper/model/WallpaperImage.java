/*
 * Copyright (C) 2022 liaoheng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.liaoheng.wallpaper.model;

import java.io.File;

/**
 * @author liaoheng
 * @date 2022-01-15 17:21
 */
public class WallpaperImage {
    private File home;
    private File lock;

    public WallpaperImage(File home, File lock) {
        this.home = home;
        this.lock = lock;
    }

    public File getHome() {
        return home;
    }

    public void setHome(File home) {
        this.home = home;
    }

    public File getLock() {
        return lock;
    }

    public void setLock(File lock) {
        this.lock = lock;
    }
}
