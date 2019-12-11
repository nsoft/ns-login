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


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.needhamsoftware.nslogin.servlet.Messages;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.needhamsoftware.nslogin.model.Validatable.*;
import static com.needhamsoftware.nslogin.model.Validatable.checkInteger;

/**
 * Class with properties of various types to validate our REST capabilities. Typically
 * this class should be removed in your application
 */
@Entity
@JsonIdentityInfo(generator=JSOGGenerator.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class TestThing extends Persisted implements Validatable{
  private String aString;
  private int anInt;
  private double aDouble;
  private Instant anInstant;

  @ManyToOne
  private AppUser aUser;

  @ManyToOne
  private TestThing aThing;

  @OneToMany
  private List<TestThing> someThings;

  public TestThing() {
  }

  public String getaString() {
    return aString;
  }

  public void setaString(String aString) {
    this.aString = aString;
  }

  public int getAnInt() {
    return anInt;
  }

  public void setAnInt(int anInt) {
    this.anInt = anInt;
  }

  public double getaDouble() {
    return aDouble;
  }

  public void setaDouble(double aDouble) {
    this.aDouble = aDouble;
  }

  public Instant getAnInstant() {
    return anInstant;
  }

  public void setAnInstant(Instant anInstant) {
    this.anInstant = anInstant;
  }

  public AppUser getaUser() {
    return aUser;
  }

  public void setaUser(AppUser aUser) {
    this.aUser = aUser;
  }

  public TestThing getaThing() {
    return aThing;
  }

  public void setaThing(TestThing aThing) {
    this.aThing = aThing;
  }

  public List<TestThing> getSomeThings() {
    return someThings;
  }

  public void setSomeThings(List<TestThing> someThings) {
    this.someThings = someThings;
  }

  @Override
  public boolean validate() {
    int incommingErrors = Messages.DO.errorCount();
    return Messages.DO.errorCount() > incommingErrors;
  }

  @Override
  public boolean validateMap(Map<String, Object> map) {
    int incommingErrors = Messages.DO.errorCount();
    String propName;
    Object id = map.get("id");
    if (id != null) {
      propName = "id";
      checkInteger(id, propName);
    }
    propName = "anInt";
    Object anInt = map.get(propName);
    if(anInt != null) {
      checkInteger(anInt, propName);
    }
    propName = "aDouble";
    Object aDouble = map.get(propName);
    if (aDouble != null) {
      checkDouble(aDouble, propName);
    }
    propName = "anInstant";
    Object anInstant = map.get(propName);
    if (anInstant != null) {
      checkInstant(anInstant, propName);
    }

    return Messages.DO.errorCount() > incommingErrors;
  }

  @Override
  public boolean isValidated() {
    return false;
  }
}
