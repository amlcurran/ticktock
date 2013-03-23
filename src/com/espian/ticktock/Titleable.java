/*
 * Copyright (C) 2013 Alex Curran.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.espian.ticktock;

/**
 * Interface allowing a Fragment to easily show a title for a {@link android.support.v4.view.PagerTitleStrip}.
 */
public interface Titleable {

	public String getTitle();

}
