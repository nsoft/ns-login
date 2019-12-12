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

public class SimpleObjectFilter implements Filter {
  private String field;
  private String operator;
  private Object value;

  public SimpleObjectFilter(String field, String operator, Object value) {
    this.field = field;
    this.operator = operator;
    this.value = value;
  }

  @Override
  public String getField() {
    return field;
  }

  @Override
  public String getOperator() {
    return operator;
  }

  @Override
  public Object getValue(ObjectService service) {
    return value;
  }
}
