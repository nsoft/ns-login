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

/*
 * Modifications: made specific to fields only, non-static methods
 */

package com.needhamsoftware.nslogin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import static com.copyright.easiertest.AnnotationUtil.listSupers;

/**
 * Class for handling common operations with annotations, works from type not instance, parameter order
 *
 * @author gheck
 */
public class FieldUtil {

  public void doForEachAnnotatedField(Class clazz, Class<? extends Annotation> annotation, FieldAction action, Object instance) {
    List<Class<?>> classes = listSupers(clazz);

    // for simplicity sake we just run through both rather than checking the @Target of the
    // annotation we've been given. Optimize later.
    for (Class<?> c : classes) {
      for (Field f : c.getDeclaredFields()) {
        if (annotation != null) {
          for (Annotation a : f.getDeclaredAnnotations()) {
            if (annotation == a.annotationType()) {
              action.doTo(f, instance);
            }
          }
        } else {
          action.doTo(f, instance);
        }
      }
    }
  }

  public void doForEachField(FieldAction action, Object instance) {
    doForEachAnnotatedField(instance.getClass(), null ,action,instance);
  }
}
