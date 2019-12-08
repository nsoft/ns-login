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

package com.needhamsoftware.nslogin.guice;

import com.google.inject.persist.Transactional;
import com.needhamsoftware.nslogin.model.Persisted;
import com.needhamsoftware.nslogin.service.Filter;
import com.needhamsoftware.nslogin.service.impl.ObjectAlreadyHasIdException;
import com.needhamsoftware.nslogin.service.impl.ObjectServiceImpl;

import javax.inject.Inject;
import java.util.List;

/**
 * Wrapper to apply the guice specific transactional annotations.
 */
public class ObjectServiceWrapper implements GuiceObjectService {

  @Inject
  private ObjectServiceImpl service;

  @Override
  public void loadSystemUser() {
    service.loadSystemUser();
  }

  @Override
  public <T extends Persisted> T get(Class<T> clazz, Long identifier) {
    return service.get(clazz, identifier);
  }

  @Override
  public Persisted getFresh(Class<? extends Persisted> clazz, Long identifier, boolean privileged) {
    return service.getFresh(clazz, identifier, privileged);
  }

  @Override
  public <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows) {
    return service.list(clazz, start, rows);
  }

  @Override
  public <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows, boolean privileged) {
    return service.list(clazz, start, rows, privileged);
  }

  @Override
  public <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows, List<Filter> filters, List<String> sorts) {
    return service.list(clazz, start, rows, filters, sorts);
  }

  @Override
  public <T extends Persisted> List<T> list(Class<T> clazz, int start, int rows, List<Filter> filters, List<String> sorts, boolean privileged) {
    return service.list(clazz, start, rows, filters, sorts, privileged);
  }

  @Override
  public Long count(Class<? extends Persisted> clazz, List<Filter> filters) {
    return service.count(clazz, filters);
  }

  @Override
  public Long count(Class<? extends Persisted> clazz, List<Filter> filters, boolean privileged) {
    return service.count(clazz, filters, privileged);
  }

  @Override
  @Transactional
  public Persisted insert(Persisted persisted) throws ObjectAlreadyHasIdException {
    return service.insert(persisted);
  }

  @Override
  @Transactional
  public Persisted update(Persisted persisted) {
    return service.update(persisted);
  }

  @Override
  @Transactional
  public void delete(Class clazz, Long identifer) {
    service.delete(clazz, identifer);
  }


}
