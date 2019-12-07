/*
 *    Copyright (c) 2019, Needham Software LLC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.needhamsoftware.nslogin.model;

/**
 * The different types of notifications. Not all notifications types are likely to be displayed in a single
 * location. For example a table that allows in-cell edits will likely have a region for displaying
 * error, warning and success messages, but not info or recommendations. Pages may have a butter-bar for
 * info and recommendation messages, but not error/warning/success. A game with a HUD however might consume
 * all types in one location.
 */
public enum NotificationType {
  ERROR,              // things that went wrong and caused request/operation failure
  WARNING,            // things that were unexpected or indicate a condition the user needs to know about urgently
  SUCCESS,            // affirmative success message
  INFO,               // non-urgent informational message (system maintenance announcement, etc)
  RECOMMENDATION,     // helpful suggestions to improve user experience/utilization of the system
}
