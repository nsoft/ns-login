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

package com.needhamsoftware.nslogin.shiro;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;

import java.util.List;
import java.util.Set;

/**
 * A slightly more permissive permission implementation wherein a wild card on either side
 * of the implies equation grans permission to all at that level This allows things like
 * a permission for User 15 to have all access to themselves with something like User:*:15
 * In the base class that will fail if checked against User:read:* In this class it will succeed.
 */
public class DoubleWildcardPermission extends WildcardPermission {
  public DoubleWildcardPermission() {
  }

  public DoubleWildcardPermission(String wildcardString) {
    super(wildcardString);
  }

  public DoubleWildcardPermission(String wildcardString, boolean caseSensitive) {
    super(wildcardString, caseSensitive);
  }

  @Override
  public boolean implies(Permission p) {
    // By default only supports comparisons with other WildcardPermissions
    if (!(p instanceof WildcardPermission)) {
      return false;
    }

    DoubleWildcardPermission wp = (DoubleWildcardPermission) p;

    List<Set<String>> otherParts = wp.getParts();

    int i = 0;
    for (Set<String> otherPart : otherParts) {
      // If this permission has less parts than the other permission, everything after the number of parts contained
      // in this permission is automatically implied, so return true
      if (getParts().size() - 1 < i) {
        return true;
      } else {
        Set<String> part = getParts().get(i);
        if (!part.contains(WILDCARD_TOKEN) &&
            !otherPart.contains(WILDCARD_TOKEN) && // this is the only difference from the base class.
            !part.containsAll(otherPart)) {
          return false;
        }
        i++;
      }
    }

    // If this permission has more parts than the other parts, only imply it if all of the other parts are wildcards
    for (; i < getParts().size(); i++) {
      Set<String> part = getParts().get(i);
      if (!part.contains(WILDCARD_TOKEN)) {
        return false;
      }
    }

    return true;
  }
}
