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

package com.needhamsoftware.nslogin.service;

import com.needhamsoftware.nslogin.model.Persisted;
import com.needhamsoftware.nslogin.service.impl.ObjectAlreadyHasIdException;

import java.util.List;

@SuppressWarnings("SameParameterValue")
public interface ObjectService {

    void loadSystemUser();

    <T extends Persisted> T get(Class<T> clazz, Long identifier);

    Persisted getFresh(Class<? extends Persisted> clazz, Long identifier, boolean privileged);

    <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows);

    <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows, boolean privileged);

    <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows, List<Filter> filters, List<String> sorts);

    <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows, List<Filter> filters, List<String> sorts, boolean privileged);

    Long count(Class<? extends Persisted> clazz, List<Filter> filters);

    Long count(Class<? extends Persisted> clazz, List<Filter> filters, boolean privileged);

    Persisted insert(Persisted persisted) throws ObjectAlreadyHasIdException;

    Persisted update(Persisted persisted);


    void delete(Class clazz, Long identifer);

}
